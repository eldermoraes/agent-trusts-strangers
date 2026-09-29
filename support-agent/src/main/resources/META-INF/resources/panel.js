// The support panel. Markdown in, sanitized HTML out (DOMPurify). What the
// sanitizer keeps (images, links) is what the output guardrail and the CSP handle.
const log = document.getElementById('log');

function render(markdown) {
  return DOMPurify.sanitize(marked.parse(markdown || ''));
}

async function stage() {
  const s = await (await fetch('/api/stage')).json();
  const el = document.getElementById('stage');
  el.textContent = s.label;
  el.className = s.stage >= 5 ? 'safe' : '';
}

function add(who, html, cls) {
  const d = document.createElement('div');
  d.className = 'msg ' + cls;
  d.innerHTML = `<div class="who">${who}</div>${html}`;
  log.prepend(d);
}

document.getElementById('f').addEventListener('submit', async e => {
  e.preventDefault();
  const q = document.getElementById('q').value.trim();
  if (!q) return;
  const btn = document.getElementById('send');
  btn.disabled = true;
  add('Operator', DOMPurify.sanitize(marked.parseInline(q)), 'user');
  try {
    const r = await (await fetch('/api/chat', {
      method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ message: q })
    })).json();
    add('Assistant', render(r.reply), r.blocked ? 'blocked' : '');
  } catch (err) {
    add('Assistant', '<em>request failed: ' + err + '</em>', 'blocked');
  } finally {
    btn.disabled = false;
    stage();
  }
});
stage();
setInterval(stage, 3000);
