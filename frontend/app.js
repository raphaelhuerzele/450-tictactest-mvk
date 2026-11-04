'use strict';

(async () => {
  const error = document.getElementById('error');
  const status = document.getElementById('status');
  async function version(url) {
    const response = await fetch(url, { cache: 'no-store', signal: AbortSignal.timeout(8000) });
    if (!response.ok) throw new Error(`Versionsabfrage: HTTP ${response.status}`);
    return (await response.text()).trim();
  }
  try {
    const config = window.TICTACTEST_CONFIG;
    if (!config) throw new Error('config.js fehlt. Die Umgebung muss Terminal- und Versions-URL bereitstellen.');
    const terminalUrl = new URL(config.terminalUrl, location.href);
    const versionUrl = new URL(config.backendVersionUrl, location.href);
    if (![terminalUrl, versionUrl].every(url => ['http:', 'https:'].includes(url.protocol))) {
      throw new Error('Die Konfiguration enthält eine ungültige URL.');
    }
    const terminal = document.getElementById('terminal');
    terminal.src = terminalUrl.href;
    const restart = document.getElementById('restart');
    restart.disabled = false;
    restart.addEventListener('click', () => { terminal.src = terminalUrl.href; });
    status.textContent = 'Terminal bereit';
    const results = await Promise.allSettled([
      version('frontend-version.txt'), version(versionUrl.href)
    ]);
    for (const [index, id] of ['frontend-version', 'backend-version'].entries()) {
      document.getElementById(id).textContent = results[index].status === 'fulfilled'
        ? results[index].value : 'nicht erreichbar';
    }
    if (results.some(result => result.status === 'rejected')) {
      throw new Error('Eine Versionsabfrage ist fehlgeschlagen. Prüfe Backend, config.js und die CORS-Antwort im Browser.');
    }
  } catch (cause) {
    error.textContent = cause.message;
    error.hidden = false;
    status.textContent = 'Verbindung prüfen';
  }
})();
