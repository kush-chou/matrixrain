/**
 * matrixrain — Pure Minimalist Living Rain Engine
 * 
 * Discrete Grid Tiling & Razor-Sharp Glyphs:
 * - Full canvas wipe (#000000) every frame prevents ghost trails and blur.
 * - Monospace half-width Katakana + digits are locked to integer grid cells.
 * - Zero vertical overlap, zero horizontal crowding.
 * - Auto-hiding HUD for an uninterrupted zen screensaver experience.
 */

// Authentic Half-Width Katakana, Clean Digits & Matrix Runes
const KATAKANA = 'ｧｱｨｲｩｳｪｴｫｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾉﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜｦﾝ';
const DIGITS = '0123456789';
const SYMBOLS = ':・=*+-<>¦|~';
const GLYPHS = (KATAKANA + DIGITS + SYMBOLS).split('');

// Themes Palette
const THEMES = {
  classic: {
    name: 'Classic Green',
    headColor: '#FFFFFF',
    glowColor: 'rgba(0, 255, 102, 0.85)',
    leadRgb: [255, 255, 255],
    neckRgb: [120, 255, 170],
    bodyRgb: [0, 255, 102],
    tailRgb: [0, 140, 50]
  },
  cyan: {
    name: 'Trinity Cyan',
    headColor: '#FFFFFF',
    glowColor: 'rgba(0, 229, 255, 0.85)',
    leadRgb: [255, 255, 255],
    neckRgb: [160, 245, 255],
    bodyRgb: [0, 229, 255],
    tailRgb: [0, 130, 170]
  },
  crimson: {
    name: 'Matrix Red',
    headColor: '#FFFFFF',
    glowColor: 'rgba(255, 51, 68, 0.85)',
    leadRgb: [255, 255, 255],
    neckRgb: [255, 160, 170],
    bodyRgb: [255, 51, 68],
    tailRgb: [160, 20, 35]
  },
  amber: {
    name: 'Zion Amber',
    headColor: '#FFFFFF',
    glowColor: 'rgba(255, 183, 0, 0.85)',
    leadRgb: [255, 255, 255],
    neckRgb: [255, 230, 160],
    bodyRgb: [255, 183, 0],
    tailRgb: [170, 110, 0]
  },
  violet: {
    name: 'Cyber Violet',
    headColor: '#FFFFFF',
    glowColor: 'rgba(213, 0, 249, 0.85)',
    leadRgb: [255, 255, 255],
    neckRgb: [245, 160, 255],
    bodyRgb: [213, 0, 249],
    tailRgb: [130, 0, 160]
  },
  monochrome: {
    name: 'Monochrome White',
    headColor: '#FFFFFF',
    glowColor: 'rgba(255, 255, 255, 0.75)',
    leadRgb: [255, 255, 255],
    neckRgb: [230, 230, 230],
    bodyRgb: [180, 180, 180],
    tailRgb: [90, 90, 90]
  }
};

const THEME_KEYS = Object.keys(THEMES);

// Simulation State
const state = {
  theme: 'classic',
  fontSize: 18,
  cellWidth: 22,
  cellHeight: 24,
  columns: [],
  ripples: [],
  paused: false
};

const canvas = document.getElementById('matrix-canvas');
const ctx = canvas.getContext('2d');

function randomGlyph() {
  return GLYPHS[Math.floor(Math.random() * GLYPHS.length)];
}

function initGrid() {
  const width = window.innerWidth;
  const height = window.innerHeight;
  const dpr = Math.min(window.devicePixelRatio || 1, 2);

  canvas.width = Math.floor(width * dpr);
  canvas.height = Math.floor(height * dpr);
  canvas.style.width = `${width}px`;
  canvas.style.height = `${height}px`;

  ctx.setTransform(dpr, 0, 0, dpr, 0, 0);

  // Dynamic cell geometry for legibility
  state.fontSize = width < 768 ? 16 : 18;
  state.cellWidth = width < 768 ? 18 : 22;
  state.cellHeight = width < 768 ? 20 : 24;

  const numCols = Math.floor(width / state.cellWidth);
  const totalRows = Math.ceil(height / state.cellHeight);
  state.columns = [];

  for (let i = 0; i < numCols; i++) {
    const streamLen = 14 + Math.floor(Math.random() * 22);
    state.columns.push({
      x: i * state.cellWidth + (state.cellWidth / 2),
      colIndex: i,
      headRow: Math.floor(Math.random() * -totalRows),
      rowProgress: 0,
      // Fall speed in rows per second (discrete grid steps)
      rowsPerSec: 10 + Math.random() * 16,
      length: streamLen,
      chars: Array.from({ length: streamLen }, () => randomGlyph()),
      leadChar: randomGlyph()
    });
  }
}

window.addEventListener('resize', initGrid);
initGrid();

// Render Loop
let lastTime = 0;

function render(timestamp) {
  if (!lastTime) lastTime = timestamp;
  const dt = Math.min((timestamp - lastTime) / 1000, 0.1);
  lastTime = timestamp;

  if (!state.paused) {
    const width = window.innerWidth;
    const height = window.innerHeight;
    const totalRows = Math.ceil(height / state.cellHeight);
    const theme = THEMES[state.theme];

    // 1. Full Canvas Clear (Pure OLED Black)
    ctx.fillStyle = '#000000';
    ctx.fillRect(0, 0, width, height);

    // 2. Process Ripples
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
      ctx.strokeStyle = `rgba(${theme.bodyRgb[0]}, ${theme.bodyRgb[1]}, ${theme.bodyRgb[2]}, ${(rip.alpha * 0.5).toFixed(2)})`;
      ctx.lineWidth = 1.5;
      ctx.stroke();
      ctx.restore();
    }

    // 3. Render Columns in Discrete Cells
    ctx.font = `700 ${state.fontSize}px "SF Mono", "Consolas", "Menlo", "Courier New", monospace`;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';

    for (let i = 0; i < state.columns.length; i++) {
      const col = state.columns[i];

      // Discrete step advance
      col.rowProgress += col.rowsPerSec * dt;
      if (col.rowProgress >= 1) {
        const step = Math.floor(col.rowProgress);
        col.rowProgress -= step;
        col.headRow += step;

        // Shift characters and introduce new lead character
        for (let s = 0; s < step; s++) {
          col.chars.pop();
          col.chars.unshift(col.leadChar);
          col.leadChar = randomGlyph();
        }
      }

      // In-place mutation (living code shimmer)
      if (Math.random() < 0.12) {
        const mutateIndex = Math.floor(Math.random() * col.chars.length);
        col.chars[mutateIndex] = randomGlyph();
      }

      // Check collision with ripples
      for (let r = 0; r < state.ripples.length; r++) {
        const rip = state.ripples[r];
        const dist = Math.abs(col.x - rip.x);
        if (Math.abs(dist - rip.radius) < 25) {
          col.headRow -= 0.5;
          col.chars[0] = '⚡';
        }
      }

      // Reset when fully below screen
      if (col.headRow - col.length > totalRows + 2) {
        col.headRow = Math.floor(Math.random() * -15) - 2;
        col.rowsPerSec = 10 + Math.random() * 16;
        col.length = 14 + Math.floor(Math.random() * 22);
        col.chars = Array.from({ length: col.length }, () => randomGlyph());
      }

      // Render glyphs at integer row positions
      for (let j = 0; j < col.length; j++) {
        const row = col.headRow - j;
        if (row < 0 || row > totalRows) continue;

        const charY = (row * state.cellHeight) + (state.cellHeight / 2);
        const char = (j === 0) ? col.leadChar : col.chars[j];
        const normalizedPos = j / col.length;

        if (j === 0) {
          // Leading Head Character (Pure White with crisp phosphor bloom)
          ctx.save();
          ctx.fillStyle = theme.headColor;
          ctx.shadowBlur = 8;
          ctx.shadowColor = theme.glowColor;
          ctx.fillText(char, col.x, charY);
          ctx.restore();
        } else if (j <= 2) {
          // Upper Neck (High Brightness Accent)
          const alpha = 0.95 - (j * 0.08);
          ctx.fillStyle = `rgba(${theme.neckRgb[0]}, ${theme.neckRgb[1]}, ${theme.neckRgb[2]}, ${alpha.toFixed(2)})`;
          ctx.fillText(char, col.x, charY);
        } else if (normalizedPos < 0.6) {
          // Mid Body (Readable Theme Color)
          const alpha = 0.80 - ((normalizedPos - 0.15) * 0.65);
          ctx.fillStyle = `rgba(${theme.bodyRgb[0]}, ${theme.bodyRgb[1]}, ${theme.bodyRgb[2]}, ${alpha.toFixed(2)})`;
          ctx.fillText(char, col.x, charY);
        } else {
          // Lower Tail (Decaying phosphor fading cleanly into pure black)
          const alpha = Math.max(0.06, (1.0 - normalizedPos) * 0.70);
          ctx.fillStyle = `rgba(${theme.tailRgb[0]}, ${theme.tailRgb[1]}, ${theme.tailRgb[2]}, ${alpha.toFixed(2)})`;
          ctx.fillText(char, col.x, charY);
        }
      }
    }
  }

  requestAnimationFrame(render);
}

requestAnimationFrame(render);

// Interactive Radial Ripple
function triggerRipple(x, y) {
  state.ripples.push({
    x,
    y,
    radius: 12,
    alpha: 1.0
  });

  const clickCol = Math.floor(x / state.cellWidth);
  for (let offset = -3; offset <= 3; offset++) {
    const col = state.columns[clickCol + offset];
    if (col) {
      col.headRow -= (4 - Math.abs(offset)) * 0.8;
      for (let k = 0; k < 3; k++) {
        col.chars[k] = randomGlyph();
      }
    }
  }
}

window.addEventListener('pointerdown', (e) => {
  // Avoid rippling when clicking HUD buttons
  if (e.target.closest('.hud-top') || e.target.closest('.hud-bottom')) return;
  triggerRipple(e.clientX, e.clientY);
});

// Zen Screensaver Auto-Hide (3.5s inactivity)
let hideTimer = null;

function resetInactivityTimer() {
  document.body.classList.remove('hud-hidden');
  clearTimeout(hideTimer);
  hideTimer = setTimeout(() => {
    document.body.classList.add('hud-hidden');
  }, 3500);
}

['mousemove', 'mousedown', 'keydown', 'touchstart'].forEach((evt) => {
  window.addEventListener(evt, resetInactivityTimer, { passive: true });
});
resetInactivityTimer();

// Theme Selection
function setTheme(themeKey) {
  if (!THEMES[themeKey]) return;
  state.theme = themeKey;
  document.querySelectorAll('.theme-dot').forEach((dot) => {
    dot.classList.toggle('active', dot.dataset.theme === themeKey);
  });
}

document.querySelectorAll('.theme-dot').forEach((dot) => {
  dot.addEventListener('click', () => {
    setTheme(dot.dataset.theme);
  });
});

// Keyboard Shortcuts: [Space] Pause, [T] Cycle Theme, [F] Fullscreen
window.addEventListener('keydown', (e) => {
  if (e.key === ' ' || e.code === 'Space') {
    e.preventDefault();
    state.paused = !state.paused;
  } else if (e.key === 't' || e.key === 'T') {
    const currentIdx = THEME_KEYS.indexOf(state.theme);
    const nextIdx = (currentIdx + 1) % THEME_KEYS.length;
    setTheme(THEME_KEYS[nextIdx]);
  } else if (e.key === 'f' || e.key === 'F') {
    toggleFullscreen();
  }
});

// Fullscreen Toggle
const fullscreenBtn = document.getElementById('fullscreen-btn');
function toggleFullscreen() {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen().catch(() => {});
    if (fullscreenBtn) fullscreenBtn.textContent = '✕';
  } else {
    document.exitFullscreen().catch(() => {});
    if (fullscreenBtn) fullscreenBtn.textContent = '⛶';
  }
}
if (fullscreenBtn) {
  fullscreenBtn.addEventListener('click', toggleFullscreen);
}
