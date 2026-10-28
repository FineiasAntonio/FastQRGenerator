(() => {
  const L = window.FQ;
  const $ = s => document.querySelector(s);
  const SVG = new Set(['svg', 'rect', 'path', 'text']);
  const kebab = k => k.replace(/[A-Z]/g, m => '-' + m.toLowerCase());
  const h = (tag, attrs, ...kids) => {
    const el = SVG.has(tag) ? document.createElementNS('http://www.w3.org/2000/svg', tag) : document.createElement(tag);
    for (const [k, v] of Object.entries(attrs || {})) {
      if (k === 'key') continue;
      if (k === 'style') for (const [sk, sv] of Object.entries(v)) el.style.setProperty(kebab(sk), sv);
      else el.setAttribute(k === 'viewBox' ? k : kebab(k), v);
    }
    const add = kid => { if (Array.isArray(kid)) kid.forEach(add); else if (kid != null && kid !== false) el.append(kid); };
    kids.forEach(add);
    return el;
  };
  const reduced = matchMedia('(prefers-reduced-motion: reduce)').matches;

  const esc = s => s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  const highlight = (src, lang) => {
    const rules = lang === 'xml'
      ? [[/^<\/?[\w.-]+>?|^\/?>/, 's']]
      : [[/^\/\/[^\n]*/, 'c'], [/^"(?:[^"\\]|\\.)*"|^'(?:[^'\\]|\\.)*'/, 's'],
         [/^\b(?:new|try|return|byte|String|implementation|null|true|false)\b/, 'k'],
         [/^\b[A-Z][A-Za-z0-9_]*\b/, 't'], [/^\b\d+(?:\.\d+)?\b/, 'd'], [/^\b[a-z]\w*(?=\()/, 'f']];
    let out = '', i = 0;
    while (i < src.length) {
      const rest = src.slice(i);
      let hit = null;
      for (const [re, cls] of rules) { const m = rest.match(re); if (m && m[0]) { hit = [m[0], cls]; break; } }
      if (hit) { out += '<span class="' + hit[1] + '">' + esc(hit[0]) + '</span>'; i += hit[0].length; }
      else { out += esc(src[i]); i++; }
    }
    return out;
  };

  const copyBtn = (btn, text) => {
    let t;
    btn.addEventListener('click', () => {
      navigator.clipboard?.writeText(typeof text === 'function' ? text() : text).catch(() => {});
      btn.textContent = L.copied;
      clearTimeout(t);
      t = setTimeout(() => { btn.textContent = L.copy; }, 1600);
    });
  };

  const CODE = {
    maven: '<dependency>\n  <groupId>io.github.fineiasantonio</groupId>\n  <artifactId>fastqrgenerator</artifactId>\n  <version>1.0.0-beta</version>\n</dependency>',
    gradle: "implementation 'io.github.fineiasantonio:fastqrgenerator:1.0.0-beta'"
  };
  let tool = 'maven';
  const installPre = $('#install-code'), installFile = $('#install-file'), installCopy = $('#install-copy');
  const renderInstall = () => {
    installPre.innerHTML = highlight(CODE[tool], tool === 'maven' ? 'xml' : 'java');
    installFile.textContent = tool === 'maven' ? 'pom.xml' : 'build.gradle';
    installCopy.textContent = L.copy;
  };
  document.querySelectorAll('input[name=tool]').forEach(r => r.addEventListener('change', () => { tool = r.value; renderInstall(); }));
  copyBtn(installCopy, () => CODE[tool]);
  renderInstall();
  const quick = $('#quick-code');
  quick.innerHTML = highlight(quick.textContent, 'java');

  const P = { qrText: 'https://github.com/FineiasAntonio/FastQRGenerator', moduleColor: '#1A1A2E', bgColor: '#FFFFFF', radius: 0.4, border: 4, ecc: 'M' };

  const roundedPath = (n, isDark, border, r, scale) => {
    let d = '';
    for (let y = 0; y < n; y++) for (let x = 0; x < n; x++) if (isDark(y, x)) {
      const u = isDark(y - 1, x), dn = isDark(y + 1, x), l = isDark(y, x - 1), rt = isDark(y, x + 1);
      const tl = !u && !l ? r : 0, tr = !u && !rt ? r : 0, br = !dn && !rt ? r : 0, bl = !dn && !l ? r : 0;
      const X = x + border, Y = y + border, A = (k, ex, ey) => k ? 'A' + k + ' ' + k + ' 0 0 1 ' + ex + ' ' + ey : 'L' + ex + ' ' + ey;
      d += 'M' + (X + tl) + ' ' + Y + 'H' + (X + 1 - tr) + A(tr, X + 1, Y + tr) + 'V' + (Y + 1 - br) + A(br, X + 1 - br, Y + 1) + 'H' + (X + bl) + A(bl, X, Y + 1 - bl) + 'V' + (Y + tl) + A(tl, X + tl, Y) + 'Z';
    }
    return d;
  };

  const qrImg = $('#qr-img'), qrMeta = $('#qr-meta'), styleCode = $('#style-code');
  const styleSrc = () => {
    const { qrText, moduleColor, bgColor, radius, border, ecc } = P;
    const txt = qrText.replace(/\\/g, '\\\\').replace(/"/g, '\\"');
    return 'QRCodeGenerator generator = new QRCodeGeneratorBuilder()\n        .eccLevel(ECCLevel.' + ecc + ')\n        .build();\n\nQRCode qr = generator.generate("' + txt + '");\n\nQRCodeStyleDefinitions style = QRCodeStyleDefinitions.builder()\n        .moduleColor("' + moduleColor + '")\n        .backgroundColor("' + bgColor + '")\n        .borderThickness(' + border + ')\n        .moduleShape(ModuleShape.rounded(' + radius + '))\n        .build();\n\nbyte[] png = qr.toImageBytes(ImageExtensions.PNG, 10, style);';
  };
  const renderQR = () => {
    const { qrText, moduleColor, bgColor, radius, border, ecc } = P;
    try {
      if (qrcode.stringToBytesFuncs && qrcode.stringToBytesFuncs['UTF-8']) qrcode.stringToBytes = qrcode.stringToBytesFuncs['UTF-8'];
      const q = qrcode(0, ecc); q.addData(qrText || ' ', 'Byte'); q.make();
      const n = q.getModuleCount(), t = n + border * 2;
      const d = roundedPath(n, (y, x) => y >= 0 && x >= 0 && y < n && x < n && q.isDark(y, x), border, radius);
      const svg = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ' + t + ' ' + t + '" shape-rendering="' + (radius ? 'geometricPrecision' : 'crispEdges') + '"><rect width="' + t + '" height="' + t + '" fill="' + bgColor + '"/><path fill="' + moduleColor + '" stroke="' + moduleColor + '" stroke-width="0.03" d="' + d + '"/></svg>';
      qrImg.src = 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg);
      qrMeta.textContent = L.meta.replace('{v}', (n - 17) / 4).replaceAll('{n}', n).replace('{e}', ecc);
    } catch (err) {
      qrMeta.textContent = L.tooLong.replace('{e}', ecc);
    }
    styleCode.innerHTML = highlight(styleSrc(), 'java');
    document.querySelectorAll('.sw button').forEach(b => b.setAttribute('aria-pressed', String(P[b.dataset.k] === b.dataset.hex)));
    $('#module-hex').textContent = moduleColor;
    $('#bg-hex').textContent = bgColor;
    $('#module-picker').value = moduleColor.toLowerCase();
    $('#bg-picker').value = bgColor.toLowerCase();
    $('#radius-out').textContent = radius.toFixed(2);
    $('#border-out').textContent = border + ' ' + L.modules;
  };
  const bind = (sel, ev, fn) => $(sel).addEventListener(ev, e => { fn(e.target); renderQR(); });
  bind('#qr-text', 'input', el => { P.qrText = el.value; });
  bind('#module-picker', 'input', el => { P.moduleColor = el.value.toUpperCase(); });
  bind('#bg-picker', 'input', el => { P.bgColor = el.value.toUpperCase(); });
  bind('#radius', 'input', el => { P.radius = Math.round(parseFloat(el.value) * 100) / 100; });
  bind('#border', 'input', el => { P.border = parseInt(el.value, 10); });
  document.querySelectorAll('.sw button').forEach(b => b.addEventListener('click', () => { P[b.dataset.k] = b.dataset.hex; renderQR(); }));
  document.querySelectorAll('input[name=ecc]').forEach(r => r.addEventListener('change', () => { P.ecc = r.value; renderQR(); }));
  copyBtn($('#style-copy'), styleSrc);
  renderQR();

  let demo;
  const getDemo = () => {
    if (demo) return demo;
    const TEXT = 'FastQRGenerator';
    const q = qrcode(2, 'M'); q.addData(TEXT, 'Byte'); q.make();
    const n = q.getModuleCount();
    const M = []; for (let r = 0; r < n; r++) { M.push([]); for (let c = 0; c < n; c++) M[r].push(q.isDark(r, c)); }
    const type = (r, c) => {
      if ((r < 7 && c < 7) || (r < 7 && c >= n - 7) || (r >= n - 7 && c < 7)) return 'finder';
      if ((r < 8 && c < 8) || (r < 8 && c >= n - 8) || (r >= n - 8 && c < 8)) return 'sep';
      if (r === 6 || c === 6) return 'timing';
      if (r === n - 8 && c === 8) return 'dark';
      if ((r === 8 && (c < 9 || c >= n - 8)) || (c === 8 && (r < 9 || r >= n - 8))) return 'format';
      if (Math.abs(r - 18) <= 2 && Math.abs(c - 18) <= 2) return 'align';
      return null;
    };
    const T = M.map((row, r) => row.map((_, c) => type(r, c)));
    const order = []; let up = true;
    for (let col = n - 1; col > 0; col -= 2) {
      if (col === 6) col--;
      for (let i = 0; i < n; i++) { const r = up ? n - 1 - i : i; for (const c of [col, col - 1]) if (!T[r][c]) order.push([r, c]); }
      up = !up;
    }
    const fmt = [];
    for (let c = 0; c <= 8; c++) if (c !== 6) fmt.push([8, c]);
    for (let r = 7; r >= 0; r--) if (r !== 6) fmt.push([r, 8]);
    for (let r = n - 1; r >= n - 7; r--) fmt.push([r, 8]);
    for (let c = n - 8; c < n; c++) fmt.push([8, c]);
    let bits = '0100' + TEXT.length.toString(2).padStart(8, '0');
    for (const ch of TEXT) bits += ch.charCodeAt(0).toString(2).padStart(8, '0');
    bits += '0000'; while (bits.length % 8) bits += '0';
    const data = []; for (let i = 0; i < bits.length; i += 8) data.push(parseInt(bits.slice(i, i + 8), 2));
    for (let k = 0; data.length < 28; k++) data.push(k % 2 ? 0x11 : 0xEC);
    const EXP = [], LOG = []; let x = 1;
    for (let i = 0; i < 255; i++) { EXP[i] = x; LOG[x] = i; x <<= 1; if (x & 256) x ^= 0x11d; }
    const mul = (a, b) => a && b ? EXP[(LOG[a] + LOG[b]) % 255] : 0;
    let g = [1];
    for (let i = 0; i < 16; i++) { const ng = new Array(g.length + 1).fill(0); g.forEach((v, j) => { ng[j] ^= v; ng[j + 1] ^= mul(v, EXP[i]); }); g = ng; }
    const res = data.concat(new Array(16).fill(0));
    for (let i = 0; i < data.length; i++) { const co = res[i]; if (co) for (let j = 0; j < g.length; j++) res[i + j] ^= mul(g[j], co); }
    const ec = res.slice(28);
    const maskFn = [(r, c) => (r + c) % 2 === 0, r => r % 2 === 0, (r, c) => c % 3 === 0, (r, c) => (r + c) % 3 === 0,
      (r, c) => (Math.floor(r / 2) + Math.floor(c / 3)) % 2 === 0, (r, c) => (r * c) % 2 + (r * c) % 3 === 0,
      (r, c) => ((r * c) % 2 + (r * c) % 3) % 2 === 0, (r, c) => ((r + c) % 2 + (r * c) % 3) % 2 === 0];
    const variant = k => M.map((row, r) => row.map((v, c) => T[r][c] ? v : (v !== maskFn[k](r, c))));
    const penalty = A => {
      let p = 0;
      const runs = get => { for (let i = 0; i < n; i++) { let len = 1; for (let j = 1; j <= n; j++) { if (j < n && get(i, j) === get(i, j - 1)) len++; else { if (len >= 5) p += 3 + len - 5; len = 1; } } } };
      runs((i, j) => A[i][j]); runs((i, j) => A[j][i]);
      for (let r = 0; r < n - 1; r++) for (let c = 0; c < n - 1; c++) { const v = A[r][c]; if (v === A[r][c + 1] && v === A[r + 1][c] && v === A[r + 1][c + 1]) p += 3; }
      const pat = [1, 0, 1, 1, 1, 0, 1, 0, 0, 0, 0], pat2 = pat.slice().reverse();
      for (let r = 0; r < n; r++) for (let c = 0; c <= n - 11; c++) {
        let h1 = true, h2 = true, v1 = true, v2 = true;
        for (let k = 0; k < 11; k++) { const a = +A[r][c + k], b = +A[c + k][r]; if (a !== pat[k]) h1 = false; if (a !== pat2[k]) h2 = false; if (b !== pat[k]) v1 = false; if (b !== pat2[k]) v2 = false; }
        p += 40 * (h1 + h2 + v1 + v2);
      }
      let dark = 0; A.forEach(row => row.forEach(v => { if (v) dark++; }));
      p += 10 * Math.floor(Math.abs(dark * 100 / (n * n) - 50) / 5);
      return p;
    };
    const variants = [], pens = [];
    for (let k = 0; k < 8; k++) { const v = variant(k); variants.push(v); pens.push(penalty(v)); }
    const best = pens.indexOf(Math.min(...pens));
    return (demo = { TEXT, n, M, T, order, fmt, data, ec, variants, pens, best });
  };

  const fmtCap = (s, ...a) => a.reduce((acc, v, i) => acc.replace('{' + i + '}', v), s);
  const viz = (step, p) => {
    const D = getDemo();
    const ease = t => t < 0 ? 0 : t > 1 ? 1 : 1 - Math.pow(1 - t, 3);
    const seg = (a, b) => ease((p - a) / (b - a));
    const cap = txt => h('div', { class: 'cap' }, txt);
    const wrap = (...kids) => h('div', { class: 'viz-in' }, ...kids);
    const INK = '#201e1d', PAPER = '#ffffff';
    const matrix = fill => {
      const n = D.n, groups = {};
      for (let r = 0; r < n; r++) for (let c = 0; c < n; c++) {
        const f = fill(r, c); if (!f) continue;
        const k = f.color + '|' + (f.op ?? 1);
        (groups[k] = groups[k] || []).push('M' + (c + 1) + ' ' + (r + 1) + 'h1v1h-1z');
      }
      return h('svg', { viewBox: '0 0 ' + (n + 2) + ' ' + (n + 2), width: 300, height: 300, shapeRendering: 'crispEdges', style: { display: 'block', borderRadius: '2px', boxShadow: 'var(--shadow-md)' } },
        h('rect', { width: n + 2, height: n + 2, fill: PAPER }),
        Object.entries(groups).map(([k, d]) => { const [color, op] = k.split('|'); return h('path', { d: d.join(''), fill: color, fillOpacity: +op }); }));
    };
    const mono = 'var(--mono)';
    if (step === 0) {
      const chars = D.TEXT.slice(0, 6).split('');
      const rows = [[L.v.mode, '', '0100'], [L.v.count, '15', '00001111']].concat(chars.map(ch => [ch, '0x' + ch.charCodeAt(0).toString(16).toUpperCase(), ch.charCodeAt(0).toString(2).padStart(8, '0')]));
      return wrap(h('div', { style: { fontFamily: mono, fontSize: '15px', lineHeight: '30px', display: 'grid', gridTemplateColumns: '64px 56px auto', columnGap: '16px', width: '300px' } },
        rows.flatMap((row, i) => {
          const t = seg(0.05 + i * 0.09, 0.2 + i * 0.09), shown = Math.round(t * row[2].length);
          const st = { opacity: t > 0 ? 1 : 0.15 };
          return [
            h('span', { style: { ...st, color: i < 2 ? 'var(--n700)' : 'var(--color-text)', fontWeight: i < 2 ? 400 : 600 } }, row[0]),
            h('span', { style: { ...st, color: 'var(--b700)' } }, row[1]),
            h('span', { style: { ...st, color: 'var(--a700)', letterSpacing: '0.08em' } }, row[2].slice(0, shown) + (t > 0 && t < 1 ? '▌' : ''))
          ];
        })), cap(p > 0.85 ? L.v.s0b : L.v.s0a));
    }
    if (step === 1) {
      const sizes = [21, 25, 29, 33, 37], fitAt = p > 0.45;
      return wrap(h('svg', { viewBox: '0 0 300 300', width: 300, height: 300, style: { display: 'block' } },
        sizes.map((s, i) => {
          const px = s * 7.6, a = seg(i * 0.06, i * 0.06 + 0.25), hl = (i === 0 && !fitAt) ? 'var(--color-accent-2)' : (i === 1 && fitAt) ? 'var(--color-accent)' : null;
          return h('rect', { x: 6, y: 300 - 6 - px * a, width: px * a, height: px * a, fill: hl && i === 1 ? 'color-mix(in srgb, var(--color-accent) 18%, transparent)' : 'none', stroke: hl || 'var(--n400)', strokeWidth: hl ? 2.5 : 1, rx: 2 });
        }),
        sizes.map((s, i) => h('text', { x: 6 + s * 7.6 - 6, y: 300 - 6 - s * 7.6 + 16, textAnchor: 'end', fontSize: 11, fontFamily: 'ui-monospace,Menlo,monospace', fill: 'var(--n700)', opacity: seg(0.2 + i * 0.06, 0.4 + i * 0.06) }, 'V' + (i + 1)))
      ), cap(fitAt ? L.v.s1b : L.v.s1a));
    }
    if (step === 2) {
      const cw = D.data.concat(D.ec);
      return wrap(h('div', { style: { display: 'grid', gridTemplateColumns: 'repeat(8, 34px)', gap: '4px', fontFamily: mono, fontSize: '11px' } },
        cw.map((v, i) => {
          const isEc = i >= 28, t = isEc ? seg(0.5 + (i - 28) * 0.025, 0.58 + (i - 28) * 0.025) : seg(i * 0.012, 0.08 + i * 0.012);
          return h('span', { style: { height: '26px', display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: '2px', opacity: t, transform: 'scale(' + (0.6 + 0.4 * t) + ')', background: isEc ? 'color-mix(in srgb, var(--color-accent-2) 22%, transparent)' : 'var(--n200)', color: isEc ? 'var(--b800)' : 'var(--color-text)' } }, v.toString(16).toUpperCase().padStart(2, '0'));
        })), cap(p < 0.45 ? L.v.s2a : p < 0.5 ? L.v.s2b : L.v.s2c));
    }
    if (step === 3) {
      const cols = ['var(--color-accent)', 'var(--color-accent-2)', 'var(--n500)'];
      const t = seg(0.25, 0.75);
      return wrap(h('div', { style: { position: 'relative', width: '300px', height: '220px' } },
        [0, 1, 2].flatMap(b => [0, 1, 2, 3, 4, 5].map(i => {
          const x0 = i * 40, y0 = b * 44, k = i * 3 + b, x1 = (k % 9) * 33, y1 = 150 + Math.floor(k / 9) * 34;
          const tt = ease((t * 1.4) - k * 0.02);
          return h('span', { style: { position: 'absolute', left: 0, top: 0, width: '28px', height: '28px', borderRadius: '2px', background: cols[b], transform: 'translate(' + (x0 + (x1 - x0) * tt) + 'px,' + (y0 + (y1 - y0) * tt) + 'px)', fontFamily: mono, fontSize: '10px', color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center' } }, String.fromCharCode(65 + b) + (i + 1));
        }))), cap(t < 1 ? L.v.s3a : L.v.s3b));
    }
    if (step === 4) {
      const placed = Math.floor(seg(0.32, 0.95) * D.order.length);
      const idx = new Map(); D.order.forEach(([r, c], i) => idx.set(r * 100 + c, i));
      const grp = { finder: [0, 0.1], sep: [0, 0.1], timing: [0.12, 0.2], align: [0.2, 0.28], dark: [0.2, 0.28], format: [0.2, 0.28] };
      return wrap(matrix((r, c) => {
        const ty = D.T[r][c];
        if (ty) { if (ty === 'format') return { color: '#cfcccb', op: seg(...grp.format) }; return D.M[r][c] ? { color: INK, op: seg(...grp[ty]) } : null; }
        const i = idx.get(r * 100 + c);
        if (i === placed && placed < D.order.length) return { color: 'var(--color-accent-2)' };
        if (i < placed) return D.M[r][c] ? { color: INK } : null;
        return { color: '#e6e4e3' };
      }), cap(p < 0.3 ? L.v.s4a : fmtCap(L.v.s4b, Math.min(placed, D.order.length), D.order.length)));
    }
    if (step === 5) {
      const k = p < 0.82 ? Math.min(7, Math.floor(p / 0.82 * 8)) : D.best, done = p >= 0.82, V = D.variants[k];
      return wrap(matrix((r, c) => D.T[r][c] === 'format' ? { color: '#cfcccb' } : V[r][c] ? { color: D.T[r][c] ? INK : (done ? INK : '#0088b0') } : null),
        cap(fmtCap(done ? L.v.s5b : L.v.s5a, k, D.pens[k])));
    }
    if (step === 6) {
      const shown = Math.floor(seg(0.1, 0.9) * D.fmt.length), on = new Map();
      D.fmt.forEach(([r, c], i) => on.set(r * 100 + c, i));
      return wrap(matrix((r, c) => {
        const i = on.get(r * 100 + c);
        if (i !== undefined) return i < shown ? { color: D.M[r][c] ? 'var(--color-accent-2)' : 'color-mix(in srgb, var(--color-accent-2) 25%, white)' } : { color: '#e6e4e3' };
        return D.M[r][c] ? { color: INK, op: 0.28 } : null;
      }), cap(L.v.s6));
    }
    const n = D.n, rad = ease(p / 0.6) * (P.radius || 0.5);
    const dk = (y, x) => y >= 0 && x >= 0 && y < n && x < n && D.M[y][x];
    const d = roundedPath(n, dk, 1, rad);
    const fmts = ['PNG', 'SVG', 'ANSI', 'isDark(row, col)'];
    return wrap(h('svg', { viewBox: '0 0 ' + (n + 2) + ' ' + (n + 2), width: 300, height: 300, style: { display: 'block', borderRadius: '2px', boxShadow: 'var(--shadow-md)' } },
      h('rect', { width: n + 2, height: n + 2, fill: P.bgColor }),
      h('path', { d, fill: P.moduleColor, stroke: P.moduleColor, strokeWidth: 0.03 })), cap(fmts[Math.min(3, Math.floor(p * 4))] + L.v.s7));
  };

  const DUR = 4500;
  const S = { step: 0, prog: 0, enter: reduced ? 1 : 0, vt: reduced ? 1 : 0, playing: !reduced, hover: false, last: 0 };
  const vizEl = $('#viz'), slide = $('#slide-text'), bars = $('#bars'), count = $('#count'), playBtn = $('#play');
  const pad = i => String(i + 1).padStart(2, '0');
  L.steps.forEach(([title], i) => {
    const b = h('button', { type: 'button', 'aria-label': title }, h('span', { class: 'track' }, h('span', { class: 'fill' })));
    b.addEventListener('click', () => go(i));
    bars.append(b);
  });
  const fills = [...bars.querySelectorAll('.fill')];
  const go = i => {
    S.step = i; S.prog = 0; S.vt = reduced ? 1 : 0; S.enter = reduced ? 1 : 0;
    const n = pad(i);
    document.querySelectorAll('#slide-text .num').forEach(el => { el.textContent = n; });
    $('#step-title').textContent = L.steps[i][0];
    $('#step-body').textContent = L.steps[i][1];
    count.textContent = n + ' / 08';
    paint();
  };
  const paint = () => {
    vizEl.replaceChildren(viz(S.step, S.vt));
    fills.forEach((f, i) => { f.style.width = (i < S.step ? 100 : i === S.step ? S.prog * 100 : 0) + '%'; });
    const f = Math.min(1, S.enter * 2.2);
    slide.style.opacity = f;
    slide.style.transform = 'translateY(' + ((1 - f) * 14) + 'px)';
  };
  const setPlay = on => { S.playing = on; playBtn.textContent = on ? L.pause : L.play; };
  $('#prev').addEventListener('click', () => go((S.step + 7) % 8));
  $('#next').addEventListener('click', () => go((S.step + 1) % 8));
  playBtn.addEventListener('click', () => setPlay(!S.playing));
  $('#pipeline-slide').addEventListener('mouseenter', () => { S.hover = true; });
  $('#pipeline-slide').addEventListener('mouseleave', () => { S.hover = false; });

  const tick = now => {
    const dt = S.last ? now - S.last : 0; S.last = now;
    if (S.playing && !S.hover) {
      S.prog += dt / DUR;
      if (S.prog >= 1) { go((S.step + 1) % 8); }
    }
    if (S.playing || S.enter < 1 || S.vt < 1) {
      S.enter = Math.min(1, S.enter + dt / 700);
      S.vt = Math.min(1, S.vt + dt / (DUR * 0.9));
      paint();
    }
    requestAnimationFrame(tick);
  };
  setPlay(S.playing);
  go(0);
  requestAnimationFrame(tick);
})();
