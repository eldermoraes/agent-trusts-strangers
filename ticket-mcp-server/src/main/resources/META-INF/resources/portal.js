// The customer portal: the public form anyone uses to open a ticket, then the
// ticket and its replies, refreshed every 1.5 s. Same origin as POST /tickets.
// Everything a customer or the agent wrote is rendered with textContent, never as HTML.
const $ = id => document.getElementById(id);
let current = null;   // ticket id being followed
let seen = 0;         // replies already on screen

function show(view) {
  $('new').hidden = view !== 'new';
  $('view').hidden = view !== 'view';
}

function render(t) {
  $('t-id').textContent = '#' + t.id;
  $('t-status').textContent = t.status;
  $('t-status').className = 'status' + (t.status === 'ANSWERED' ? ' answered' : '');
  $('t-subject').textContent = t.subject;
  $('t-from').textContent = t.customerEmail;
  $('t-body').textContent = t.body;
  const box = $('t-replies');
  if (!t.replies.length) {
    box.innerHTML = '';
    const p = document.createElement('p');
    p.className = 'waiting';
    p.textContent = 'No reply yet. This page updates by itself.';
    box.append(p);
    seen = 0;
    return;
  }
  if (t.replies.length === seen) return;
  box.innerHTML = '';
  t.replies.forEach((r, i) => {
    const d = document.createElement('div');
    d.className = 'reply' + (i >= seen ? ' new' : '');
    const who = document.createElement('div');
    who.className = 'who';
    who.textContent = 'Support';
    const text = document.createElement('div');
    text.textContent = r;
    d.append(who, text);
    box.append(d);
  });
  const first = box.children[seen] || box.lastElementChild;   // start of the newest reply
  seen = t.replies.length;
  box.scrollTo({ top: first.offsetTop - box.offsetTop, behavior: 'smooth' });
}

async function poll() {
  if (current == null) return;
  try {
    const r = await fetch('/tickets/' + current, { cache: 'no-store' });
    if (r.ok) render(await r.json());
  } catch (e) { /* server restarting; next tick */ }
}

function follow(id) {
  current = id;
  seen = 0;
  history.replaceState(null, '', '#ticket=' + id);
  show('view');
  poll();
}

$('f').addEventListener('submit', async e => {
  e.preventDefault();
  $('error').textContent = '';
  $('submit').disabled = true;
  try {
    const r = await fetch('/tickets', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ customerEmail: $('email').value.trim(), subject: $('subject').value.trim(), body: $('message').value })
    });
    if (!r.ok) throw new Error('HTTP ' + r.status);
    follow((await r.json()).id);
  } catch (err) {
    $('error').textContent = 'Could not open the ticket (' + err.message + ').';
  } finally {
    $('submit').disabled = false;
  }
});

const m = location.hash.match(/^#ticket=(\d+)$/);
if (m) follow(Number(m[1])); else show('new');
setInterval(poll, 1500);
