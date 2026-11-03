/**
 * Exact Faithful Implementation of MatrixRainWallpaperService.java
 * Directly matches the Android live wallpaper engine.
 */
const canvas = document.getElementById('matrix-canvas');
const ctx = canvas.getContext('2d');

// Offscreen persistent buffer canvas for authentic phosphorescent decay
let bufferCanvas = document.createElement('canvas');
let bufferCtx = bufferCanvas.getContext('2d');

const hiddenWords = ["MATRIX", "SYSTEM", "HACK", "WAKE UP", "NEO"];

// Palette configurations based on Android paints
const THEMES = {
  classic: {
    text: '#00FF00',
    bg: '#004400',
    head: '#EEFFEE',
    glow: '#00FF00',
    virus: '#FF0000',
    gold: '#FFD700'
  },
  cyan: {
    text: '#00E5FF',
    bg: '#003344',
    head: '#EEFFFF',
    glow: '#00E5FF',
    virus: '#FF0055',
    gold: '#FFD700'
  },
  crimson: {
    text: '#FF3344',
    bg: '#440008',
    head: '#FFEEF0',
    glow: '#FF3344',
    virus: '#00FF66',
    gold: '#FFD700'
  },
  amber: {
    text: '#FFB700',
    bg: '#443000',
    head: '#FFFFEE',
    glow: '#FFB700',
    virus: '#FF2200',
    gold: '#00FF66'
  },
  violet: {
    text: '#D500F9',
    bg: '#350040',
    head: '#FAEEFF',
    glow: '#D500F9',
    virus: '#00E5FF',
    gold: '#FFD700'
  },
  monochrome: {
    text: '#BBBBBB',
    bg: '#333333',
    head: '#FFFFFF',
    glow: '#FFFFFF',
    virus: '#FF3344',
    gold: '#00FF66'
  }
};
const THEME_KEYS = Object.keys(THEMES);

const state = {
  theme: 'classic',
  fontSize: 16,
  columnSize: 0,
  width: 0,
  height: 0,
  dropY: [],
  speeds: [],
  frameCounts: [],
  previousChars: [],
  columnType: [], // 0=Normal, 1=Background depth, 2=Virus, 3=Gold
  wordActiveIndex: [],
  assignedWord: [],
  ripples: [],
  paused: false
};

/**
 * Exact character generator from MatrixRainWallpaperService.java:
 * 60% Halfwidth Katakana (0xFF66 to 0xFF9D)
 * 25% Digits ('0' to '9')
 * 15% ASCII symbols (0x21 to 0x7E)
 */
function getRandomMatrixChar() {
  const type = Math.random() * 100;
  if (type < 60) {
    return String.fromCharCode(0xFF66 + Math.floor(Math.random() * (0xFF9D - 0xFF66)));
  } else if (type < 85) {
    return String.fromCharCode(48 + Math.floor(Math.random() * 10));
  } else {
    return String.fromCharCode(0x21 + Math.floor(Math.random() * 90));
  }
}

function initColumn(i, randomY) {
  const maxRows = Math.floor(state.height / state.fontSize);
  state.dropY[i] = randomY ? Math.floor(Math.random() * maxRows) : 0;

  const roll = Math.random();
  if (roll > 0.98) {
    state.columnType[i] = 2; // Virus (Red)
    state.speeds[i] = Math.floor(Math.random() * 2) + 1;
  } else if (roll > 0.96) {
    state.columnType[i] = 3; // Gold
    state.speeds[i] = Math.floor(Math.random() * 3) + 2;
  } else if (roll > 0.60) {
    state.columnType[i] = 1; // Background depth (Dim dark green)
    state.speeds[i] = Math.floor(Math.random() * 2) + 3;
  } else {
    state.columnType[i] = 0; // Standard foreground (Bright green)
    state.speeds[i] = Math.floor(Math.random() * 2) + 1;
  }

  state.frameCounts[i] = state.speeds[i];
  state.previousChars[i] = getRandomMatrixChar();

  state.wordActiveIndex[i] = -1;
  if (!randomY && state.columnType[i] === 0 && Math.random() < 0.04) {
    state.assignedWord[i] = hiddenWords[Math.floor(Math.random() * hiddenWords.length)];
    state.wordActiveIndex[i] = 0;
  }
}

function resizeMatrix() {
  const dpr = Math.min(window.devicePixelRatio || 1, 2);
  state.width = window.innerWidth;
  state.height = window.innerHeight;

  canvas.width = Math.floor(state.width * dpr);
  canvas.height = Math.floor(state.height * dpr);
  canvas.style.width = `${state.width}px`;
  canvas.style.height = `${state.height}px`;

  ctx.setTransform(dpr, 0, 0, dpr, 0, 0);

  bufferCanvas.width = state.width;
  bufferCanvas.height = state.height;
  bufferCtx.fillStyle = '#000000';
  bufferCtx.fillRect(0, 0, state.width, state.height);

  state.fontSize = state.width < 768 ? 14 : 16;
  state.columnSize = Math.floor(state.width / state.fontSize);

  state.dropY = new Array(state.columnSize);
  state.speeds = new Array(state.columnSize);
  state.frameCounts = new Array(state.columnSize);
  state.previousChars = new Array(state.columnSize);
  state.columnType = new Array(state.columnSize);
  state.wordActiveIndex = new Array(state.columnSize);
  state.assignedWord = new Array(state.columnSize);

  for (let i = 0; i < state.columnSize; i++) {
    initColumn(i, true);
  }
}

window.addEventListener('resize', resizeMatrix);
resizeMatrix();

// Consistent Frame Loop matching Android Handler.postDelayed(drawRunner, 33)
function drawFrame() {
  if (state.paused) return;

  const currentTheme = THEMES[state.theme];

  // 1. Phosphor Decay Wipe on buffer matching Android setAlpha(10) (10/255 ~= 0.039)
  bufferCtx.fillStyle = 'rgba(0, 0, 0, 0.038)';
  bufferCtx.fillRect(0, 0, state.width, state.height);

  bufferCtx.font = `${state.fontSize}px "Consolas", "Monaco", "Menlo", monospace`;
  bufferCtx.textBaseline = 'top';

  // 2. Process Columns
  for (let i = 0; i < state.columnSize; i++) {
    state.frameCounts[i]--;

    // Occasional In-Place Glitch
    if (Math.random() < 0.015) {
      const glitchY = Math.floor(Math.random() * (state.height / state.fontSize));
      let glitchColor = state.columnType[i] === 1 ? currentTheme.bg : currentTheme.text;
      if (state.columnType[i] === 2) glitchColor = currentTheme.virus;
      bufferCtx.fillStyle = glitchColor;
      bufferCtx.fillText(getRandomMatrixChar(), i * state.fontSize, glitchY * state.fontSize);
    }

    if (state.frameCounts[i] <= 0) {
      state.frameCounts[i] = state.speeds[i];

      const x = i * state.fontSize;
      const y = state.dropY[i] * state.fontSize;

      // Paint previous character in body color
      let bodyColor = currentTheme.text;
      if (state.columnType[i] === 1) bodyColor = currentTheme.bg;
      else if (state.columnType[i] === 2) bodyColor = currentTheme.virus;
      else if (state.columnType[i] === 3) bodyColor = currentTheme.gold;

      bufferCtx.fillStyle = bodyColor;
      bufferCtx.fillText(state.previousChars[i], x, y);

      state.dropY[i]++;

      if (state.dropY[i] * state.fontSize > state.height) {
        if (Math.random() > 0.95) {
          initColumn(i, false);
        }
      } else {
        let newChar;
        if (state.wordActiveIndex[i] >= 0) {
          newChar = state.assignedWord[i].charAt(state.wordActiveIndex[i]);
          state.wordActiveIndex[i]++;
          if (state.wordActiveIndex[i] >= state.assignedWord[i].length) {
            state.wordActiveIndex[i] = -1;
          }
        } else {
          newChar = getRandomMatrixChar();
        }

        // Draw new head with glow
        let headColor = currentTheme.head;
        let headGlow = currentTheme.glow;
        if (state.columnType[i] === 2) { headColor = currentTheme.virus; headGlow = currentTheme.virus; }
        else if (state.columnType[i] === 3) { headColor = currentTheme.gold; headGlow = currentTheme.gold; }
        else if (state.columnType[i] === 1) { headColor = currentTheme.bg; headGlow = 'transparent'; }

        bufferCtx.save();
        bufferCtx.fillStyle = headColor;
        if (state.columnType[i] !== 1) {
          bufferCtx.shadowColor = headGlow;
          bufferCtx.shadowBlur = 8;
        }
        bufferCtx.fillText(newChar, x, state.dropY[i] * state.fontSize);
        bufferCtx.restore();

        state.previousChars[i] = newChar;
      }
    }
  }

  // 3. Render buffer onto main screen
  ctx.clearRect(0, 0, state.width, state.height);
  ctx.drawImage(bufferCanvas, 0, 0);

  // 4. Draw Ripples on top
  for (let r = state.ripples.length - 1; r >= 0; r--) {
    const rip = state.ripples[r];
    rip.radius += 8;
    rip.alpha -= 0.04;
    if (rip.alpha <= 0) {
      state.ripples.splice(r, 1);
      continue;
    }
    ctx.beginPath();
    ctx.arc(rip.x, rip.y, rip.radius, 0, Math.PI * 2);
    ctx.strokeStyle = `rgba(0, 255, 102, ${rip.alpha * 0.6})`;
    ctx.lineWidth = 1.5;
    ctx.stroke();
  }
}

let lastTime = 0;
const frameInterval = 30; // 30ms matching Android WallpaperService drawDelay

function loop(timestamp) {
  if (!lastTime) lastTime = timestamp;
  const elapsed = timestamp - lastTime;

  if (elapsed >= frameInterval) {
    lastTime = timestamp - (elapsed % frameInterval);
    drawFrame();
  }

  requestAnimationFrame(loop);
}
requestAnimationFrame(loop);

// Glide / Drag Column Trigger directly from Android WallpaperService.onTouchEvent
let isPointerDown = false;
let lastPointerX = null;

function triggerAtX(currentX) {
  const currentCol = Math.floor(currentX / state.fontSize);
  let startCol = currentCol;
  let endCol = currentCol;

  if (lastPointerX !== null) {
    const prevCol = Math.floor(lastPointerX / state.fontSize);
    startCol = Math.min(prevCol, currentCol);
    endCol = Math.max(prevCol, currentCol);
  }

  for (let c = startCol; c <= endCol; c++) {
    if (c >= 0 && c < state.columnSize) {
      state.dropY[c] = 0;
      state.speeds[c] = 1;
      state.frameCounts[c] = 0;
      state.previousChars[c] = getRandomMatrixChar();
      // 5% chance Gold stream roll directly matching Android onTouchEvent
      state.columnType[c] = (Math.random() < 0.05) ? 3 : 0;
    }
  }

  lastPointerX = currentX;
}

window.addEventListener('pointerdown', (e) => {
  if (e.target.closest('.hud-top') || e.target.closest('.hud-bottom')) return;
  isPointerDown = true;
  triggerAtX(e.clientX);
});

window.addEventListener('pointermove', (e) => {
  if (!isPointerDown) return;
  if (e.target.closest('.hud-top') || e.target.closest('.hud-bottom')) return;
  triggerAtX(e.clientX);
});

window.addEventListener('pointerup', () => {
  isPointerDown = false;
  lastPointerX = null;
});

window.addEventListener('pointercancel', () => {
  isPointerDown = false;
  lastPointerX = null;
});

// Auto-Hide HUD for Pure Screensaver Experience
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

// Theme Picker
function setTheme(themeKey) {
  if (!THEMES[themeKey]) return;
  state.theme = themeKey;
  document.querySelectorAll('.theme-dot').forEach((dot) => {
    dot.classList.toggle('active', dot.dataset.theme === themeKey);
  });
}

document.querySelectorAll('.theme-dot').forEach((dot) => {
  dot.addEventListener('click', () => setTheme(dot.dataset.theme));
});

// Keyboard Shortcuts
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
if (fullscreenBtn) fullscreenBtn.addEventListener('click', toggleFullscreen);
