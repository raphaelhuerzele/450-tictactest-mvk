param(
    [Parameter(Mandatory)][ValidatePattern('^v\d+\.\d+\.\d+$')][string]$ReleaseA,
    [Parameter(Mandatory)][ValidatePattern('^v\d+\.\d+\.\d+$')][string]$ReleaseB,
    [string]$AwsProfile = 'lab',
    [string]$Tofu = 'tofu',
    [switch]$StartStoppedInstance
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
if ($ReleaseA -eq $ReleaseB) { throw 'Choose two different published releases.' }
$repositoryRoot = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$environmentNames = @('AWS_PROFILE', 'AWS_REGION', 'INSTANCE_ID', 'BACKEND_VERSION_URL', 'FRONTEND_URL', 'EXPECTED_VERSION')
$originalEnvironment = @{}
foreach ($name in $environmentNames) { $originalEnvironment[$name] = [Environment]::GetEnvironmentVariable($name, 'Process') }
function Invoke-Checked {
    param([string]$Program, [string[]]$Arguments)
    & $Program @Arguments
    if ($LASTEXITCODE -ne 0) { throw "$Program failed (exit $LASTEXITCODE)." }
}
Push-Location $repositoryRoot
try {
    if (-not (Test-Path -LiteralPath '.tools/browser/node_modules/playwright-core/index.mjs')) {
        throw 'Install the browser test dependency first: npm install --prefix .tools/browser --no-save --package-lock=false playwright-core@1.64.1'
    }
    $chrome = if ($env:BROWSER_EXECUTABLE) { $env:BROWSER_EXECUTABLE } else { 'C:/Program Files/Google/Chrome/Application/chrome.exe' }
    if (-not (Test-Path -LiteralPath $chrome)) { throw 'Set BROWSER_EXECUTABLE to the installed Chrome executable.' }
    $env:AWS_PROFILE = $AwsProfile
    Invoke-Checked 'gh' @('auth', 'status')
    $null = Invoke-Checked 'aws' @('sts', 'get-caller-identity', '--output', 'json')
    $outputs = (Invoke-Checked $Tofu @('-chdir=infra', 'output', '-json')) | ConvertFrom-Json
    $env:AWS_REGION = $outputs.aws_region.value
    $env:INSTANCE_ID = $outputs.instance_id.value
    $env:BACKEND_VERSION_URL = $outputs.backend_version_url.value
    $env:FRONTEND_URL = $outputs.frontend_url.value
    $state = Invoke-Checked 'aws' @('ec2', 'describe-instances', '--instance-ids', $env:INSTANCE_ID, '--query', 'Reservations[0].Instances[0].State.Name', '--output', 'text')
    if ($state -eq 'stopped' -and $StartStoppedInstance) {
        $null = Invoke-Checked 'aws' @('ec2', 'start-instances', '--instance-ids', $env:INSTANCE_ID)
        Invoke-Checked 'aws' @('ec2', 'wait', 'instance-running', '--instance-ids', $env:INSTANCE_ID)
    } elseif ($state -ne 'running') {
        throw "Instance is $state. Start the existing instance or use -StartStoppedInstance; no infrastructure will be created."
    }
    $online = $false
    for ($attempt = 0; $attempt -lt 60; $attempt++) {
        $status = Invoke-Checked 'aws' @('ssm', 'describe-instance-information', '--filters', "Key=InstanceIds,Values=$($env:INSTANCE_ID)", '--query', 'InstanceInformationList[0].PingStatus', '--output', 'text')
        if ($status -eq 'Online') { $online = $true; break }
        Start-Sleep -Seconds 2
    }
    if (-not $online) { throw 'SSM did not become Online within two minutes.' }
    # Check both artifact contracts before changing the running application.
    foreach ($tag in @($ReleaseA, $ReleaseB)) {
        $release = (Invoke-Checked 'gh' @('release', 'view', $tag, '--repo', 'raphaelhuerzele/450-tictactest-mvk', '--json', 'assets,isDraft,isPrerelease')) | ConvertFrom-Json
        if ($release.isDraft -or $release.isPrerelease) { throw "$tag must be a stable published release." }
        foreach ($asset in @("tictactest-$($tag.Substring(1)).jar", "tictactest-frontend-$($tag.Substring(1)).zip", 'SHA256SUMS')) {
            if ($asset -notin $release.assets.name) { throw "$tag is missing $asset." }
        }
    }
    $evidenceDirectory = Join-Path $repositoryRoot ('build/manual-acceptance/' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
    $null = New-Item -ItemType Directory -Path $evidenceDirectory -Force
    foreach ($tag in @($ReleaseA, $ReleaseB)) {
        Invoke-Checked 'node' @('scripts/web/deploy.mjs', $tag, '--force')
        $env:EXPECTED_VERSION = $tag.Substring(1)
        Invoke-Checked 'node' @('scripts/web/browser-check.mjs')
        $currentId = Invoke-Checked $Tofu @('-chdir=infra', 'output', '-raw', 'instance_id')
        if ($currentId -ne $env:INSTANCE_ID) { throw 'Instance ID changed during application deployment.' }
        $plan = & $Tofu -chdir=infra plan -detailed-exitcode -no-color -input=false 2>&1
        $planExit = $LASTEXITCODE
        $plan | Set-Content -Encoding utf8 (Join-Path $evidenceDirectory "$tag-plan.txt")
        if ($planExit -ne 0) { throw "OpenTofu plan returned $planExit; inspect the saved plan. No apply was performed." }
        Copy-Item -LiteralPath 'build/deployment/result.json' -Destination (Join-Path $evidenceDirectory "$tag-deployment.json")
        Copy-Item -LiteralPath 'build/web-evidence/browser.json' -Destination (Join-Path $evidenceDirectory "$tag-browser.json")
        Copy-Item -LiteralPath 'build/web-evidence/browser.png' -Destination (Join-Path $evidenceDirectory "$tag-browser.png")
    }
    [ordered]@{ verifiedAt = (Get-Date).ToUniversalTime().ToString('o'); releaseA = $ReleaseA; releaseB = $ReleaseB; instanceId = $env:INSTANCE_ID; frontendUrl = $env:FRONTEND_URL; unchangedInfrastructure = $true } |
        ConvertTo-Json | Set-Content -Encoding utf8 (Join-Path $evidenceDirectory 'summary.json')
    Write-Output "Both local manual deployments passed. Evidence: $evidenceDirectory"
} finally {
    foreach ($name in $environmentNames) { [Environment]::SetEnvironmentVariable($name, $originalEnvironment[$name], 'Process') }
    Pop-Location
}
