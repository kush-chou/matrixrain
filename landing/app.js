/**
 * Matrix Rain High-Legibility Canvas Engine & Interactive Simulator
 * 
 * Key Architectural Fixes for Razor-Sharp Legibility:
 * 1. Full Canvas Clear every frame (#000000): Eliminates fractional subpixel
 *    smudging and motion blur that previously turned characters into green smears.
 * 2. Proper Cell Grid Geometry: Column width (1.5x) and Row height (1.45x)
 *    prevent characters from vertically colliding or horizontally crowding.
 * 3. Centered Baseline Alignment: `textAlign = 'center'` & `textBaseline = 'middle'`
 *    places each Katakana glyph squarely within its cell.
 * 4. Calculated Phosphor Alpha Decay: Crisp per-character opacity calculation
 *    with white phosphor lead head and smooth decay into true OLED black.
 */

// Authentic Japanese Half-width Katakana + Clean Digits + Matrix Runes
const KATAKANA_GLYPHS = [
  'ｦ','ｧ','ｨ','ｩ','ｪ','ｫ','ｬ','ｭ','ｮ','ｯ','ｰ','ｱ','ｲ','ｳ','ｴ','ｵ','ｶ','ｷ','ｸ','ｹ','ｺ',
  'ｻ','ｼ','ｽ','ｾ','ｿ','ﾀ','ﾁ','ﾂ','ﾃ','ﾄ','ﾅ','ﾆ','ﾇ','ﾈ','ﾉ','ﾊ','ﾋ','ﾌ','ﾍ','ﾎ','ﾏ',
  'ﾐ','ﾑ','ﾒ','ﾓ','ﾔ','ﾕ','ﾖ','ﾗ','ﾘ','ﾙ','ﾚ','ﾛ','ﾜ','ﾝ',
  '0','1','2','3','4','5','7','8','9',
  '+','-','*','=','<','>',':','•'
];

const BINARY_GLYPHS = ['0', '1'];
const HEX_GLYPHS = '0123456789ABCDEF'.split('');
const ASCII_GLYPHS = 'ABCDEFGHJKLMNPQRSTUVWXYZ0123456789$#%&*+-/<>='.split('');
const CYBER_GLYPHS = ['$', '%', '#', '@', '<', '>', '*', '+', '=', '~', '!', '?', '0', '1', 'X', 'Z'];

// Theme Palette Definitions with RGB components for exact opacity rendering
const THEMES = {
  classic: {
    name: 'Classic 1999 Green',
    leadColor: '#FFFFFF',
    glowColor: 'rgba(0, 255, 102, 0.95)',
    headRgb: [120, 255, 170],
    bodyRgb: [0, 255, 102],
    tailRgb: [0, 150, 60]
  },
  cyan: {
    name: 'Trinity Cyan',
    leadColor: '#FFFFFF',
    glowColor: 'rgba(0, 229, 255, 0.95)',
    headRgb: [160, 245, 255],
    bodyRgb: [0, 229, 255],
    tailRgb: [0, 140, 180]
  },
  crimson: {
    name: 'Agent Smith Red',
    leadColor: '#FFFFFF',
    glowColor: 'rgba(255, 51, 68, 0.95)',
    headRgb: [255, 160, 170],
    bodyRgb: [255, 51, 68],
    tailRgb: [170, 30, 42]
  },
  amber: {
    name: 'Zion Amber',
    leadColor: '#FFFFFF',
    glowColor: 'rgba(255, 183, 0, 0.95)',
    headRgb: [255, 230, 160],
    bodyRgb: [255, 183, 0],
    tailRgb: [180, 120, 0]
  },
  violet: {
    name: 'Cyberpunk Violet',
    leadColor: '#FFFFFF',
    glowColor: 'rgba(213, 0, 249, 0.95)',
    headRgb: [245, 160, 255],
    bodyRgb: [213, 0, 249],
    tailRgb: [140, 0, 165]
  },
  monochrome: {
    name: 'OLED True White',
    leadColor: '#FFFFFF',
    glowColor: 'rgba(255, 255, 255, 0.85)',
    headRgb: [240, 240, 240],
    bodyRgb: [180, 180, 180],
    tailRgb: [100, 100, 100]
  }
};

const CHARSETS = {
  matrix: () => KATAKANA_GLYPHS[Math.floor(Math.random() * KATAKANA_GLYPHS.length)],
  binary: () => BINARY_GLYPHS[Math.floor(Math.random() * BINARY_GLYPHS.length)],
  hex: () => HEX_GLYPHS[Math.floor(Math.random() * HEX_GLYPHS.length)],
  ascii: () => ASCII_GLYPHS[Math.floor(Math.random() * ASCII_GLYPHS.length)],
  cyber: () => CYBER_GLYPHS[Math.floor(Math.random() * CYBER_GLYPHS.length)]
};

// Simulation State
const state = {
  theme: 'classic',
  charset: 'matrix',
  speedMultiplier: 1.0,
  fontSize: 20,
  cellWidth: 30,
  cellHeight: 28,
  columns: [],
  bgColumns: [],
  customInjections: [],
  ripples: []
};

const canvas = document.getElementById('matrix-canvas');
const ctx = canvas.getContext('2d');

function initColumns() {
  const width = window.innerWidth;
  const height = window.innerHeight;
  const dpr = Math.min(window.devicePixelRatio || 1, 2);

  canvas.width = width * dpr;
  canvas.height = height * dpr;
  canvas.style.width = `${width}px`;
  canvas.style.height = `${height}px`;
  ctx.scale(dpr, dpr);

  // Compute clean cell spacing so characters have breathing room
  state.cellWidth = Math.round(state.fontSize * 1.5);
  state.cellHeight = Math.round(state.fontSize * 1.45);

  const numCols = Math.floor(width / state.cellWidth);
  const totalRows = Math.ceil(height / state.cellHeight);
  state.columns = [];

  for (let i = 0; i < numCols; i++) {
    const streamLen = 12 + Math.floor(Math.random() * 20);
    const charGen = CHARSETS[state.charset];

    state.columns.push({
      colIndex: i,
      x: i * state.cellWidth,
      headRow: (Math.random() * -totalRows) - 2,
      speed: 0.22 + Math.random() * 0.28, // controlled, legible fall speed
      length: streamLen,
      chars: Array.from({ length: streamLen }, () => charGen()),
      leadChar: charGen()
    });
  }

  // Background Depth Columns (slight horizontal offset, fainter)
  const bgCellWidth = Math.round(state.cellWidth * 1.4);
  const numBgCols = Math.floor(width / bgCellWidth);
  state.bgColumns = [];

  for (let i = 0; i < numBgCols; i++) {
    const streamLen = 8 + Math.floor(Math.random() * 14);
    const charGen = CHARSETS[state.charset];

    state.bgColumns.push({
      x: (i * bgCellWidth) + (state.cellWidth * 0.5),
      headRow: (Math.random() * -totalRows) - 2,
      speed: 0.12 + Math.random() * 0.15,
      length: streamLen,
      chars: Array.from({ length: streamLen }, () => charGen())
    });
  }
}

window.addEventListener('resize', initColumns);
initColumns();

// Animation Loop
let lastTime = 0;

function renderMatrix(timestamp) {
  if (!lastTime) lastTime = timestamp;
  const dt = (timestamp - lastTime) / 1000;
  lastTime = timestamp;

  const currentTheme = THEMES[state.theme];
  const charGen = CHARSETS[state.charset];
  const totalRows = Math.ceil(window.innerHeight / state.cellHeight);

  // 1. FULL CANVAS WIPE (PURE OLED BLACK #000000)
  // This completely eliminates ghosting, smudging, and subpixel dragging!
  ctx.fillStyle = '#000000';
  ctx.fillRect(0, 0, window.innerWidth, window.innerHeight);

  // 2. Process Ripples
  for (let r = state.ripples.length - 1; r >= 0; r--) {
    const rip = state.ripples[r];
    rip.radius += 280 * dt;
    rip.alpha -= 1.1 * dt;

    if (rip.alpha <= 0) {
      state.ripples.splice(r, 1);
      continue;
    }

    ctx.save();
    ctx.beginPath();
    ctx.arc(rip.x, rip.y, rip.radius, 0, Math.PI * 2);
    ctx.strokeStyle = `rgba(0, 255, 102, ${rip.alpha * 0.45})`;
    ctx.lineWidth = 1.5;
    ctx.stroke();
    ctx.restore();
  }

  // Shared font configuration for crystal-clear glyph rendering
  ctx.font = `700 ${state.fontSize}px "SF Mono", "Courier New", Consolas, Menlo, monospace`;
  ctx.textAlign = 'center';
  ctx.textBaseline = 'middle';

  // --- Layer 1: Ambient Background Depth Columns ---
  ctx.save();
  const bgFontSize = Math.max(12, Math.floor(state.fontSize * 0.75));
  ctx.font = `600 ${bgFontSize}px "SF Mono", "Courier New", Consolas, Menlo, monospace`;

  for (let i = 0; i < state.bgColumns.length; i++) {
    const col = state.bgColumns[i];
    col.headRow += col.speed * state.speedMultiplier;

    if (col.headRow - col.length > totalRows + 2) {
      col.headRow = (Math.random() * -15) - 2;
    }

    for (let j = 0; j < col.length; j++) {
      const row = Math.floor(col.headRow - j);
      if (row < 0 || row > totalRows) continue;

      const charY = (row * state.cellHeight) + (state.cellHeight / 2);
      const charX = col.x + (state.cellWidth / 2);
      const alpha = Math.max(0.04, (1.0 - (j / col.length)) * 0.28);

      ctx.fillStyle = `rgba(${currentTheme.bodyRgb[0]}, ${currentTheme.bodyRgb[1]}, ${currentTheme.bodyRgb[2]}, ${alpha.toFixed(2)})`;
      ctx.fillText(col.chars[j], charX, charY);
    }
  }
  ctx.restore();

  // --- Layer 2: Main Foreground Rain Columns ---
  ctx.font = `700 ${state.fontSize}px "SF Mono", "Courier New", Consolas, Menlo, monospace`;

  for (let i = 0; i < state.columns.length; i++) {
    const col = state.columns[i];

    // Speed advance
    col.headRow += col.speed * state.speedMultiplier;

    // In-place Character Mutation (Living stream shimmer)
    if (Math.random() < 0.22) {
      const mutateIdx = Math.floor(Math.random() * col.length);
      col.chars[mutateIdx] = (state.customInjections.length > 0 && Math.random() < 0.2)
        ? state.customInjections[Math.floor(Math.random() * state.customInjections.length)]
        : charGen();
    }

    if (Math.random() < 0.3) {
      col.leadChar = charGen();
    }

    // Ripple deflection interaction
    for (let r = 0; r < state.ripples.length; r++) {
      const rip = state.ripples[r];
      const dist = Math.abs(col.x - rip.x);
      if (Math.abs(dist - rip.radius) < 30) {
        col.headRow -= 0.4;
        col.chars[0] = '⚡';
      }
    }

    // Reset when stream fully exits screen
    if (col.headRow - col.length > totalRows + 2) {
      col.headRow = (Math.random() * -20) - 2;
      col.speed = 0.22 + Math.random() * 0.28;
      col.length = 12 + Math.floor(Math.random() * 20);
    }

    const charX = col.x + (state.cellWidth / 2);

    // Draw each glyph crisply inside its grid cell
    for (let j = 0; j < col.length; j++) {
      const row = Math.floor(col.headRow - j);
      if (row < 0 || row > totalRows) continue;

      const charY = (row * state.cellHeight) + (state.cellHeight / 2);
      const char = (j === 0) ? col.leadChar : col.chars[j];
      const normalizedPos = j / col.length; // 0 (head) to 1 (tail)

      if (j === 0) {
        // Leading Head Spark (Razor-sharp white with phosphor bloom)
        ctx.save();
        ctx.fillStyle = currentTheme.leadColor;
        ctx.shadowBlur = 12;
        ctx.shadowColor = currentTheme.glowColor;
        ctx.fillText(char, charX, charY);
        ctx.restore();
      } else if (j <= 2) {
        // Upper Neck (High brightness electric green)
        const alpha = 0.95 - (j * 0.08);
        ctx.fillStyle = `rgba(${currentTheme.headRgb[0]}, ${currentTheme.headRgb[1]}, ${currentTheme.headRgb[2]}, ${alpha.toFixed(2)})`;
        ctx.fillText(char, charX, charY);
      } else if (normalizedPos < 0.6) {
        // Mid Stream Body (Solid readable theme color)
        const alpha = 0.82 - ((normalizedPos - 0.15) * 0.7);
        ctx.fillStyle = `rgba(${currentTheme.bodyRgb[0]}, ${currentTheme.bodyRgb[1]}, ${currentTheme.bodyRgb[2]}, ${alpha.toFixed(2)})`;
        ctx.fillText(char, charX, charY);
      } else {
        // Lower Tail (Deep decaying phosphor fading cleanly into pure black)
        const alpha = Math.max(0.06, (1.0 - normalizedPos) * 0.75);
        ctx.fillStyle = `rgba(${currentTheme.tailRgb[0]}, ${currentTheme.tailRgb[1]}, ${currentTheme.tailRgb[2]}, ${alpha.toFixed(2)})`;
        ctx.fillText(char, charX, charY);
      }
    }
  }

  requestAnimationFrame(renderMatrix);
}
requestAnimationFrame(renderMatrix);

// Pointer Radial Shockwave
window.addEventListener('pointerdown', (e) => {
  state.ripples.push({
    x: e.clientX,
    y: e.clientY,
    radius: 12,
    alpha: 1.0
  });

  const clickCol = Math.floor(e.clientX / state.cellWidth);
  for (let offset = -3; offset <= 3; offset++) {
    const col = state.columns[clickCol + offset];
    if (col) {
      col.headRow -= (4 - Math.abs(offset)) * 0.8;
      for (let k = 0; k < 3; k++) {
        col.chars[k] = CHARSETS[state.charset]();
      }
    }
  }
});

// UI Controls
document.querySelectorAll('.theme-btn').forEach((btn) => {
  btn.addEventListener('click', () => {
    document.querySelectorAll('.theme-btn').forEach((b) => b.classList.remove('active'));
    btn.classList.add('active');
    state.theme = btn.dataset.theme;
  });
});

const charsetSelect = document.getElementById('charset-select');
charsetSelect.addEventListener('change', (e) => {
  state.charset = e.target.value;
  initColumns();
});

const speedSlider = document.getElementById('speed-slider');
const speedVal = document.getElementById('speed-val');
speedSlider.addEventListener('input', (e) => {
  state.speedMultiplier = parseFloat(e.target.value);
  speedVal.textContent = `${state.speedMultiplier.toFixed(1)}x`;
});

const densitySlider = document.getElementById('density-slider');
const densityVal = document.getElementById('density-val');
densitySlider.addEventListener('input', (e) => {
  state.fontSize = parseInt(e.target.value, 10);
  if (state.fontSize <= 16) densityVal.textContent = 'High';
  else if (state.fontSize <= 22) densityVal.textContent = 'Medium';
  else densityVal.textContent = 'Low';
  initColumns();
});

const customTextInput = document.getElementById('custom-text');
customTextInput.addEventListener('keydown', (e) => {
  if (e.key === 'Enter' && customTextInput.value.trim().length > 0) {
    state.customInjections = customTextInput.value.trim().split('');
    customTextInput.style.borderColor = '#ffffff';
    setTimeout(() => {
      customTextInput.style.borderColor = 'rgba(0, 255, 102, 0.3)';
    }, 400);
  }
});

// Fullscreen
const fullscreenBtn = document.getElementById('fullscreen-btn');
fullscreenBtn.addEventListener('click', () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen().catch(() => {});
    fullscreenBtn.textContent = '✕ Exit';
  } else {
    document.exitFullscreen().catch(() => {});
    fullscreenBtn.textContent = '⛶ Fullscreen';
  }
});

// Pixel Watch Face Simulator Mini Canvas (Crisp Grid)
const watchCanvas = document.getElementById('watch-canvas');
const watchCtx = watchCanvas.getContext('2d');
const watchFontSize = 14;
const watchCellWidth = 20;
const watchCellHeight = 19;
const watchCols = Math.floor(watchCanvas.width / watchCellWidth);
const watchTotalRows = Math.ceil(watchCanvas.height / watchCellHeight);

const watchDrops = Array.from({ length: watchCols }, (_, i) => ({
  x: i * watchCellWidth,
  headRow: Math.random() * -10,
  speed: 0.2 + Math.random() * 0.2,
  chars: Array.from({ length: 10 }, () => CHARSETS.matrix()),
  leadChar: CHARSETS.matrix()
}));

function renderWatch() {
  // Clear watch face completely to avoid smear
  watchCtx.fillStyle = '#000000';
  watchCtx.fillRect(0, 0, watchCanvas.width, watchCanvas.height);

  watchCtx.font = `700 ${watchFontSize}px "SF Mono", "Courier New", Consolas, Menlo, monospace`;
  watchCtx.textAlign = 'center';
  watchCtx.textBaseline = 'middle';
  const currentTheme = THEMES[state.theme];

  for (let i = 0; i < watchCols; i++) {
    const drop = watchDrops[i];
    drop.headRow += drop.speed;

    if (Math.random() < 0.2) {
      drop.leadChar = CHARSETS[state.charset]();
    }

    if (drop.headRow - 10 > watchTotalRows) {
      drop.headRow = Math.random() * -5;
    }

    const charX = drop.x + (watchCellWidth / 2);

    for (let j = 0; j < 10; j++) {
      const row = Math.floor(drop.headRow - j);
      if (row < 0 || row > watchTotalRows) continue;

      const charY = (row * watchCellHeight) + (watchCellHeight / 2);
      const char = (j === 0) ? drop.leadChar : drop.chars[j];

      if (j === 0) {
        watchCtx.fillStyle = currentTheme.leadColor;
        watchCtx.fillText(char, charX, charY);
      } else if (j <= 2) {
        watchCtx.fillStyle = `rgb(${currentTheme.headRgb.join(',')})`;
        watchCtx.fillText(char, charX, charY);
      } else if (j < 6) {
        watchCtx.fillStyle = `rgb(${currentTheme.bodyRgb.join(',')})`;
        watchCtx.fillText(char, charX, charY);
      } else {
        watchCtx.fillStyle = `rgba(${currentTheme.tailRgb.join(',')}, 0.4)`;
        watchCtx.fillText(char, charX, charY);
      }
    }
  }

  requestAnimationFrame(renderWatch);
}
requestAnimationFrame(renderWatch);

// Clock update for watch simulator
function updateWatchClock() {
  const now = new Date();
  const hours = String(now.getHours()).padStart(2, '0');
  const minutes = String(now.getMinutes()).padStart(2, '0');
  const timeElem = document.getElementById('watch-time');
  if (timeElem) timeElem.textContent = `${hours}:${minutes}`;

  const options = { weekday: 'short', month: 'short', day: 'numeric' };
  const dateElem = document.getElementById('watch-date');
  if (dateElem) dateElem.textContent = now.toLocaleDateString('en-US', options).toUpperCase();
}
setInterval(updateWatchClock, 1000);
updateWatchClock();
