// Optional real-browser acceptance check. No dependency is shipped with the frontend.
import { pathToFileURL } from 'node:url';
import { resolve } from 'node:path';
import { mkdirSync, writeFileSync } from 'node:fs';
import { execFileSync } from 'node:child_process';
import assert from 'node:assert/strict';

const { chromium } = await import(pathToFileURL(resolve('.tools/browser/node_modules/playwright-core/index.mjs')));
const browser = await chromium.launch({
  executablePath: process.env.BROWSER_EXECUTABLE ?? 'C:/Program Files/Google/Chrome/Application/chrome.exe',
  headless: true
});
const evidence = {};
const outputs = new Map();
async function openGame() {
  const page = await browser.newPage({ viewport: { width: 1280, height: 880 } });
  outputs.set(page, '');
  page.on('websocket', socket => socket.on('framereceived', event => {
    const text = typeof event.payload === 'string' ? event.payload : event.payload.toString('utf8');
    outputs.set(page, outputs.get(page) + text);
  }));
  await page.goto(process.env.FRONTEND_URL ?? 'http://localhost:8000');
  await page.frameLocator('#terminal').locator('.xterm-helper-textarea').waitFor({ state: 'attached' });
  await waitFor(() => outputs.get(page).includes('(0-8)'));
  return page;
}
async function waitFor(predicate) {
  for (let attempt = 0; attempt < 100; attempt++) {
    if (await predicate()) return;
    await new Promise(resolve => setTimeout(resolve, 100));
  }
  throw new Error('Browser check timed out');
}
async function input(page, value) {
  await page.frameLocator('#terminal').locator('.xterm-helper-textarea').focus();
  await page.keyboard.type(value);
  await page.keyboard.press('Enter');
}
try {
  const first = await openGame();
  const second = await openGame();
  evidence.frontendVersion = await first.locator('#frontend-version').innerText();
  evidence.backendVersion = await first.locator('#backend-version').innerText();
  assert.match(evidence.frontendVersion, /^\d+\.\d+\.\d+$/);
  assert.match(evidence.backendVersion, /^\d+\.\d+\.\d+$/);
  if (process.env.EXPECTED_VERSION) {
    assert.equal(evidence.frontendVersion, process.env.EXPECTED_VERSION);
    assert.equal(evidence.backendVersion, process.env.EXPECTED_VERSION);
  }
  const frontendHost = new URL(process.env.FRONTEND_URL ?? 'http://localhost:8000').hostname;
  if (process.platform === 'win32' && ['localhost', '127.0.0.1'].includes(frontendHost)) {
    const processes = execFileSync('wsl', ['-d', 'Ubuntu', '--', 'ps', '-C', 'java', '-o', 'pid=,rss=,args='], { encoding: 'utf8' });
    evidence.jvmProcesses = processes.trim().split('\n').filter(line => line.includes('tictactest-'));
    assert.equal(evidence.jvmProcesses.length, 2, 'Two tabs must have two independent Java processes');
  }
  const third = await openGame();
  const fourth = await openGame();
  evidence.simultaneousGames = 4;
  assert.ok([first, second, third, fourth].every(page => outputs.get(page).includes('(0-8)')));
  await third.close();
  await fourth.close();
  for (const move of ['4', '8', '2', '6']) {
    await input(first, move);
    await new Promise(resolve => setTimeout(resolve, 180));
  }
  await waitFor(() => outputs.get(first).includes('winner is: CROSS'));
  evidence.completeGame = 'CROSS wins';
  await input(second, 'abc');
  await waitFor(() => outputs.get(second).includes('NumberFormatException'));
  evidence.invalidInput = 'NumberFormatException ends the game; no shell follows';
  outputs.set(second, '');
  await second.locator('#restart').click();
  await waitFor(() => outputs.get(second).includes('(0-8)'));
  evidence.restart = 'New game started through iframe reload';
  assert.equal(await first.locator('#error').isVisible(), false);
  mkdirSync('build/web-evidence', { recursive: true });
  await second.screenshot({ path: 'build/web-evidence/browser.png', fullPage: true });
  writeFileSync('build/web-evidence/browser.json', JSON.stringify(evidence, null, 2) + '\n');
  console.log(JSON.stringify(evidence, null, 2));
} finally {
  await browser.close();
}
