/**
 * Matrix Rain Cinematic Canvas Engine & Interactive Simulator
 * Features:
 * - Authentic Japanese Half-width Katakana (U+FF66 - U+FF9D) + Numerals + Cyber runes
 * - In-place glyph mutations (living code cascade shimmer)
 * - Multi-depth 3D layering (background mist + foreground streams)
 * - Pure OLED black `#000000` with phosphor bloom heads
 * - Dynamic pointer shockwave ripple physics
 */

// Authentic Japanese Half-width Katakana + Numerals + Cyber Runes
const KATAKANA_GLYPHS = [
  'ｦ','ｧ','ｨ','ｩ','ｪ','ｫ','ｬ','ｭ','ｮ','ｯ','ｰ','ｱ','ｲ','ｳ','ｴ','ｵ','ｶ','ｷ','ｸ','ｹ','ｺ',
  'ｻ','ｼ','ｽ','ｾ','ｿ','ﾀ','ﾁ','ﾂ','ﾃ','ﾄ','ﾅ','ﾆ','ﾇ','ﾈ','ﾉ','ﾊ','ﾋ','ﾌ','ﾍ','ﾎ','ﾏ',
  'ﾐ','ﾑ','ﾒ','ﾓ','ﾔ','ﾕ','ﾖ','ﾗ','ﾘ','ﾙ','ﾚ','ﾛ','ﾜ','ﾝ',
  '0','1','2','3','4','5','7','8','9',
  ':','•','=','*','+','-','<','>','_','~','"'
];

const BINARY_GLYPHS = ['0', '1'];
const HEX_GLYPHS = '0123456789ABCDEF'.split('');
const ASCII_GLYPHS = '!#$%&*+-/0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[]^_`'.split('');
const CYBER_GLYPHS = ['$', '%', '#', '@', '<', '>', '[', ']', '{', '}', '*', '+', '=', '~', '^', '!', '?', '0', '1', 'X', 'Z'];

// Themes Configuration
const THEMES = {
  classic: {
    name: 'Classic 1999 Green',
    leadColor: '#FFFFFF',
    leadGlow: 'rgba(255, 255, 255, 0.95)',
    headColor: '#80FFAA',
    midColor: '#00FF66',
    tailColor: '#008833',
    dimColor: '#003816',
    glowColor: 'rgba(0, 255, 102, 0.85)'
  },
  cyan: {
    name: 'Trinity Cyan',
    leadColor: '#FFFFFF',
    leadGlow: 'rgba(255, 255, 255, 0.95)',
    headColor: '#A0F8FF',
    midColor: '#00E5FF',
    tailColor: '#0088A8',
    dimColor: '#003344',
    glowColor: 'rgba(0, 229, 255, 0.85)'
  },
  crimson: {
    name: 'Agent Smith Red',
    leadColor: '#FFFFFF',
    leadGlow: 'rgba(255, 255, 255, 0.95)',
    headColor: '#FFA0AA',
    midColor: '#FF3344',
    tailColor: '#AA1E2A',
    dimColor: '#440A11',
    glowColor: 'rgba(255, 51, 68, 0.85)'
  },
  amber: {
    name: 'Zion Amber',
    leadColor: '#FFFFFF',
    leadGlow: 'rgba(255, 255, 255, 0.95)',
    headColor: '#FFEAA0',
    midColor: '#FFB700',
    tailColor: '#B88200',
    dimColor: '#4A3500',
    glowColor: 'rgba(255, 183, 0, 0.85)'
  },
  violet: {
    name: 'Cyberpunk Violet',
    leadColor: '#FFFFFF',
    leadGlow: 'rgba(255, 255, 255, 0.95)',
    headColor: '#F5A8FF',
    midColor: '#D500F9',
    tailColor: '#8F00A6',
    dimColor: '#3A0044',
    glowColor: 'rgba(213, 0, 249, 0.85)'
  },
  monochrome: {
    name: 'OLED True White',
    leadColor: '#FFFFFF',
    leadGlow: 'rgba(255, 255, 255, 0.95)',
    headColor: '#E0E0E0',
    midColor: '#B0B0B0',
    tailColor: '#666666',
    dimColor: '#242424',
    glowColor: 'rgba(255, 255, 255, 0.7)'
  }
};

const CHARSETS = {
  matrix: () => KATAKANA_GLYPHS[Math.floor(Math.random() * KATAKANA_GLYPHS.length)],
  binary: () => BINARY_GLYPHS[Math.floor(Math.random() * BINARY_GLYPHS.length)],
  hex: () => HEX_GLYPHS[Math.floor(Math.random() * HEX_GLYPHS.length)],
  ascii: () => ASCII_GLYPHS[Math.floor(Math.random() * ASCII_GLYPHS.length)],
  cyber: () => CYBER_GLYPHS[Math.floor(Math.random() * CYBER_GLYPHS.length)]
};

// Global Simulation State
const state = {
  theme: 'classic',
  charset: 'matrix',
  speedMultiplier: 1.0,
  fontSize: 18,
  columns: [],
  bgColumns: [],
  customInjections: [],
  ripples: []
};

// Main Canvas Initialization
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

  // 1. Foreground Main Columns
  const colWidth = state.fontSize * 0.9;
  const numCols = Math.floor(width / colWidth);
  state.columns = [];

  for (let i = 0; i < numCols; i++) {
    const streamLength = 14 + Math.floor(Math.random() * 22);
    const charGen = CHARSETS[state.charset];
    const chars = Array.from({ length: streamLength }, () => charGen());

    state.columns.push({
      x: i * colWidth,
      y: (Math.random() * -100) - 10,
      speed: 0.6 + Math.random() * 0.8,
      length: streamLength,
      chars: chars,
      glitchCooldown: 0,
      leadChar: charGen()
    });
  }

  // 2. Background Depth Plane (Slower, smaller, ambient depth)
  const bgFontSize = Math.max(11, Math.floor(state.fontSize * 0.65));
  const bgColWidth = bgFontSize * 0.95;
  const bgNumCols = Math.floor(width / bgColWidth);
  state.bgColumns = [];

  for (let i = 0; i < bgNumCols; i++) {
    const streamLength = 10 + Math.floor(Math.random() * 16);
    const charGen = CHARSETS[state.charset];
    state.bgColumns.push({
      x: i * bgColWidth,
      y: (Math.random() * -80) - 10,
      speed: 0.3 + Math.random() * 0.4,
      length: streamLength,
      chars: Array.from({ length: streamLength }, () => charGen()),
      fontSize: bgFontSize
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

  // OLED True Black Trail Dissolve
  ctx.fillStyle = 'rgba(0, 0, 0, 0.16)';
  ctx.fillRect(0, 0, window.innerWidth, window.innerHeight);

  // Process & Draw Ripples
  for (let r = state.ripples.length - 1; r >= 0; r--) {
    const rip = state.ripples[r];
    rip.radius += 320 * dt;
    rip.alpha -= 1.2 * dt;

    if (rip.alpha <= 0) {
      state.ripples.splice(r, 1);
      continue;
    }

    ctx.save();
    ctx.beginPath();
    ctx.arc(rip.x, rip.y, rip.radius, 0, Math.PI * 2);
    ctx.strokeStyle = `rgba(0, 255, 102, ${rip.alpha * 0.4})`;
    ctx.lineWidth = 2;
    ctx.stroke();
    ctx.restore();
  }

  // --- Layer 1: Background Depth Columns (Ambient Layer) ---
  ctx.save();
  ctx.globalAlpha = 0.28;
  const bgFont = Math.max(11, Math.floor(state.fontSize * 0.65));
  ctx.font = `${bgFont}px monospace`;
  ctx.fillStyle = currentTheme.dimColor;

  for (let i = 0; i < state.bgColumns.length; i++) {
    const col = state.bgColumns[i];
    col.y += col.speed * state.speedMultiplier * 0.7;

    const headY = col.y * bgFont;
    if (headY - (col.length * bgFont) > window.innerHeight) {
      col.y = -col.length;
      col.speed = 0.3 + Math.random() * 0.4;
    }

    for (let j = 0; j < col.length; j++) {
      const charY = headY - (j * bgFont);
      if (charY < -bgFont || charY > window.innerHeight + bgFont) continue;
      ctx.fillText(col.chars[j], col.x, charY);
    }
  }
  ctx.restore();

  // --- Layer 2: Main Foreground Rain Columns ---
  ctx.font = `600 ${state.fontSize}px monospace`;

  for (let i = 0; i < state.columns.length; i++) {
    const col = state.columns[i];

    // Stream Speed Advance
    col.y += col.speed * state.speedMultiplier;

    // Stream In-Place Character Mutations (Shimmer Glitch Effect)
    if (Math.random() < 0.35) {
      const mutateIdx = Math.floor(Math.random() * col.length);
      col.chars[mutateIdx] = (state.customInjections.length > 0 && Math.random() < 0.25)
        ? state.customInjections[Math.floor(Math.random() * state.customInjections.length)]
        : charGen();
    }

    // Lead character mutates continuously
    if (Math.random() < 0.45) {
      col.leadChar = charGen();
    }

    // Check interaction with active ripples
    for (let r = 0; r < state.ripples.length; r++) {
      const rip = state.ripples[r];
      const dist = Math.abs(col.x - rip.x);
      if (Math.abs(dist - rip.radius) < 25) {
        col.y -= 0.6; // Deflection bounce
        col.chars[0] = '⚡';
      }
    }

    const headY = col.y * state.fontSize;

    // Reset when off bottom
    if (headY - (col.length * state.fontSize) > window.innerHeight) {
      col.y = (Math.random() * -20) - 2;
      col.speed = 0.6 + Math.random() * 0.8;
      col.length = 14 + Math.floor(Math.random() * 22);
    }

    // Draw Stream Characters from Head down to Tail
    for (let j = 0; j < col.length; j++) {
      const charY = headY - (j * state.fontSize);
      if (charY < -state.fontSize || charY > window.innerHeight + state.fontSize) continue;

      const char = (j === 0) ? col.leadChar : col.chars[j];

      if (j === 0) {
        // Leading Head Spark (Blinding White with Phosphor Glow)
        ctx.save();
        ctx.fillStyle = currentTheme.leadColor;
        ctx.shadowBlur = 14;
        ctx.shadowColor = currentTheme.glowColor;
        ctx.fillText(char, col.x, charY);
        ctx.restore();
      } else if (j <= 2) {
        // Upper Neck (Vibrant Neon Electric Accent)
        ctx.fillStyle = currentTheme.headColor;
        ctx.fillText(char, col.x, charY);
      } else if (j < col.length * 0.5) {
        // Mid Body (Pure Saturated Theme Color)
        ctx.fillStyle = currentTheme.midColor;
        ctx.fillText(char, col.x, charY);
      } else if (j < col.length * 0.8) {
        // Lower Tail (Decaying Deep Moss Color)
        ctx.fillStyle = currentTheme.tailColor;
        ctx.fillText(char, col.x, charY);
      } else {
        // Fading Tip into OLED Black
        ctx.fillStyle = currentTheme.dimColor;
        ctx.fillText(char, col.x, charY);
      }
    }
  }

  requestAnimationFrame(renderMatrix);
}
requestAnimationFrame(renderMatrix);

// Pointer Interaction: Dynamic Radial Shockwave
window.addEventListener('pointerdown', (e) => {
  state.ripples.push({
    x: e.clientX,
    y: e.clientY,
    radius: 10,
    alpha: 1.0
  });

  // Local glitch scramble near click
  const clickCol = Math.floor(e.clientX / (state.fontSize * 0.9));
  for (let offset = -4; offset <= 4; offset++) {
    const col = state.columns[clickCol + offset];
    if (col) {
      col.y -= (5 - Math.abs(offset)) * 1.5;
      for (let k = 0; k < 4; k++) {
        col.chars[k] = CHARSETS[state.charset]();
      }
    }
  }
});

// Controls Binding
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
  if (state.fontSize <= 15) densityVal.textContent = 'High';
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

// Fullscreen Toggle
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

// Wear OS Watch Face Canvas Simulation
const watchCanvas = document.getElementById('watch-canvas');
const watchCtx = watchCanvas.getContext('2d');
const watchFontSize = 13;
const watchCols = Math.floor(watchCanvas.width / watchFontSize);
const watchDrops = Array.from({ length: watchCols }, () => ({
  y: Math.random() * -15,
  speed: 0.5 + Math.random() * 0.7,
  chars: Array.from({ length: 12 }, () => CHARSETS.matrix()),
  leadChar: CHARSETS.matrix()
}));

function renderWatch() {
  watchCtx.fillStyle = 'rgba(0, 0, 0, 0.18)';
  watchCtx.fillRect(0, 0, watchCanvas.width, watchCanvas.height);

  watchCtx.font = `600 ${watchFontSize}px monospace`;
  const currentTheme = THEMES[state.theme];

  for (let i = 0; i < watchCols; i++) {
    const drop = watchDrops[i];
    drop.y += drop.speed;

    if (Math.random() < 0.25) {
      drop.leadChar = CHARSETS[state.charset]();
    }

    const x = i * watchFontSize;
    const headY = drop.y * watchFontSize;

    if (headY - (12 * watchFontSize) > watchCanvas.height) {
      drop.y = Math.random() * -5;
    }

    // Lead char
    watchCtx.fillStyle = currentTheme.leadColor;
    watchCtx.fillText(drop.leadChar, x, headY);

    // Trail
    watchCtx.fillStyle = currentTheme.midColor;
    watchCtx.fillText(drop.chars[1], x, headY - watchFontSize);

    watchCtx.fillStyle = currentTheme.tailColor;
    watchCtx.fillText(drop.chars[3], x, headY - (watchFontSize * 2));

    watchCtx.fillStyle = currentTheme.dimColor;
    watchCtx.fillText(drop.chars[5], x, headY - (watchFontSize * 3));
  }

  requestAnimationFrame(renderWatch);
}
requestAnimationFrame(renderWatch);

// Live Digital Watch Clock & Date
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
