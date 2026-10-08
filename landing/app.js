/**
 * Matrix Rain Canvas Engine & Interactive Simulator
 * Optimized for 60/120Hz displays with touch ripple dynamics.
 */

// Themes configuration
const THEMES = {
  classic: {
    leadColor: '#ffffff',
    trailColor: '#00ff66',
    dimColor: '#003816',
    glowColor: 'rgba(0, 255, 102, 0.65)'
  },
  cyan: {
    leadColor: '#e0ffff',
    trailColor: '#00e5ff',
    dimColor: '#003344',
    glowColor: 'rgba(0, 229, 255, 0.65)'
  },
  crimson: {
    leadColor: '#ffebee',
    trailColor: '#ff3344',
    dimColor: '#440a11',
    glowColor: 'rgba(255, 51, 68, 0.65)'
  },
  amber: {
    leadColor: '#fff8e1',
    trailColor: '#ffb700',
    dimColor: '#4a3500',
    glowColor: 'rgba(255, 183, 0, 0.65)'
  },
  violet: {
    leadColor: '#f3e5f5',
    trailColor: '#d500f9',
    dimColor: '#3a0044',
    glowColor: 'rgba(213, 0, 249, 0.65)'
  },
  monochrome: {
    leadColor: '#ffffff',
    trailColor: '#bbbbbb',
    dimColor: '#333333',
    glowColor: 'rgba(255, 255, 255, 0.5)'
  }
};

// Character sets
const CHARSETS = {
  matrix: () => {
    // Katakana Unicode (0x30A0 - 0x30FF) + Digits + Matrix chars
    if (Math.random() < 0.65) {
      return String.fromCharCode(0x30A0 + Math.floor(Math.random() * 96));
    }
    return String.fromCharCode(0x0030 + Math.floor(Math.random() * 10)); // 0-9
  },
  binary: () => (Math.random() > 0.5 ? '1' : '0'),
  hex: () => '0123456789ABCDEF'[Math.floor(Math.random() * 16)],
  ascii: () => String.fromCharCode(33 + Math.floor(Math.random() * 94)),
  cyber: () => '$%#@<>[]{}*+=~^!?'[Math.floor(Math.random() * 17)]
};

// Engine state
const state = {
  theme: 'classic',
  charset: 'matrix',
  speedMultiplier: 1.0,
  fontSize: 22,
  columns: 0,
  drops: [],
  customInjections: []
};

// Main background canvas
const canvas = document.getElementById('matrix-canvas');
const ctx = canvas.getContext('2d');

function initCanvas() {
  const dpr = window.devicePixelRatio || 1;
  canvas.width = window.innerWidth * dpr;
  canvas.height = window.innerHeight * dpr;
  ctx.scale(dpr, dpr);

  state.columns = Math.floor(window.innerWidth / state.fontSize);
  state.drops = [];

  for (let i = 0; i < state.columns; i++) {
    state.drops[i] = {
      y: Math.random() * -100, // Staggered entry
      speed: 0.5 + Math.random() * 0.9,
      length: 12 + Math.floor(Math.random() * 20),
      chars: []
    };
  }
}

window.addEventListener('resize', initCanvas);
initCanvas();

// Animation loop
let lastTime = 0;
function renderMatrix(timestamp) {
  const dt = timestamp - lastTime;
  lastTime = timestamp;

  // Fade out effect (OLED true black trail persistence)
  ctx.fillStyle = 'rgba(0, 0, 0, 0.08)';
  ctx.fillRect(0, 0, window.innerWidth, window.innerHeight);

  ctx.font = `${state.fontSize}px monospace`;
  const currentTheme = THEMES[state.theme];
  const charGen = CHARSETS[state.charset];

  for (let i = 0; i < state.drops.length; i++) {
    const drop = state.drops[i];
    const x = i * state.fontSize;
    const y = drop.y * state.fontSize;

    // Determine current character
    let char = '';
    if (state.customInjections.length > 0 && Math.random() < 0.25) {
      char = state.customInjections[Math.floor(Math.random() * state.customInjections.length)];
    } else {
      char = charGen();
    }

    // Lead character (Bright glowing head)
    ctx.fillStyle = currentTheme.leadColor;
    ctx.shadowBlur = 10;
    ctx.shadowColor = currentTheme.glowColor;
    ctx.fillText(char, x, y);

    // Trail characters (colored decay)
    ctx.shadowBlur = 0;
    ctx.fillStyle = currentTheme.trailColor;
    ctx.fillText(char, x, y - state.fontSize);

    // Advance drop
    drop.y += drop.speed * state.speedMultiplier;

    // Reset when off bottom
    if (y > window.innerHeight && Math.random() > 0.975) {
      drop.y = 0;
      drop.speed = 0.5 + Math.random() * 0.9;
    }
  }

  requestAnimationFrame(renderMatrix);
}
requestAnimationFrame(renderMatrix);

// Touch / Mouse Ripple Shockwave
window.addEventListener('pointerdown', (e) => {
  const touchCol = Math.floor(e.clientX / state.fontSize);
  for (let offset = -4; offset <= 4; offset++) {
    const colIdx = touchCol + offset;
    if (state.drops[colIdx]) {
      state.drops[colIdx].y -= (5 - Math.abs(offset)) * 2; // Shockwave rebound
    }
  }
});

// UI Controls Binding
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
  if (state.fontSize <= 18) densityVal.textContent = 'High';
  else if (state.fontSize <= 28) densityVal.textContent = 'Medium';
  else densityVal.textContent = 'Low';
  initCanvas();
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

// Wear OS Watch Simulator Mini Canvas
const watchCanvas = document.getElementById('watch-canvas');
const watchCtx = watchCanvas.getContext('2d');
const watchFontSize = 14;
const watchCols = Math.floor(watchCanvas.width / watchFontSize);
const watchDrops = Array.from({ length: watchCols }, () => Math.random() * -20);

function renderWatch() {
  watchCtx.fillStyle = 'rgba(0, 0, 0, 0.12)';
  watchCtx.fillRect(0, 0, watchCanvas.width, watchCanvas.height);

  watchCtx.font = `${watchFontSize}px monospace`;
  const currentTheme = THEMES[state.theme];

  for (let i = 0; i < watchCols; i++) {
    const x = i * watchFontSize;
    const y = watchDrops[i] * watchFontSize;
    const char = CHARSETS.matrix();

    watchCtx.fillStyle = currentTheme.leadColor;
    watchCtx.fillText(char, x, y);

    watchCtx.fillStyle = currentTheme.trailColor;
    watchCtx.fillText(char, x, y - watchFontSize);

    watchDrops[i] += 0.8;
    if (y > watchCanvas.height && Math.random() > 0.95) {
      watchDrops[i] = 0;
    }
  }

  requestAnimationFrame(renderWatch);
}
requestAnimationFrame(renderWatch);

// Live Clock for Watch Mockup
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
