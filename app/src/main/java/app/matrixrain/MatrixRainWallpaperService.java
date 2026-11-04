package app.matrixrain;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.os.Process;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import androidx.core.content.ContextCompat;

/**
 * Live Wallpaper Service for Matrix Rain.
 * Coordinates preferences, simulation, and rendering.
 * Guarantees 0% standby CPU drain when not visible.
 */
public class MatrixRainWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new MatrixRainEngine();
    }

    private class MatrixRainEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener {

        private final HandlerThread renderThread;
        private final Handler renderHandler;
        private final RainPreferences prefs = new RainPreferences();
        private final RainSimulation simulation = new RainSimulation();
        private final RainRenderer renderer = new RainRenderer();

        private final Runnable drawRunner = new Runnable() {
            @Override
            public void run() {
                draw();
            }
        };

        private int width;
        private int height;
        private volatile boolean visible = true;
        private volatile boolean isPowerSaveMode = false;
        private BroadcastReceiver powerSaveReceiver;
        private SharedPreferences sharedPreferences;

        public MatrixRainEngine() {
            renderThread = new HandlerThread("MatrixRainRenderer", Process.THREAD_PRIORITY_DISPLAY);
            renderThread.start();
            renderHandler = new Handler(renderThread.getLooper());

            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                isPowerSaveMode = pm.isPowerSaveMode();
            }

            powerSaveReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    boolean powerSave = powerManager != null && powerManager.isPowerSaveMode();
                    if (isPowerSaveMode != powerSave) {
                        isPowerSaveMode = powerSave;
                        // If turning on power save, adjust pacing
                        renderHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (visible) {
                                    renderHandler.removeCallbacks(drawRunner);
                                    renderHandler.post(drawRunner);
                                }
                            }
                        });
                    }
                }
            };
            IntentFilter filter = new IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
            ContextCompat.registerReceiver(MatrixRainWallpaperService.this, powerSaveReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);

            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MatrixRainWallpaperService.this);
            sharedPreferences.registerOnSharedPreferenceChangeListener(this);

            renderHandler.post(new Runnable() {
                @Override
                public void run() {
                    prefs.load(sharedPreferences);
                    renderer.setTypeface(RainRenderer.getMatrixTypeface(MatrixRainWallpaperService.this));
                    renderer.updateFontSize(getScaledFontSize());
                    renderer.applyTheme(prefs);
                }
            });
        }

        private int getScaledFontSize() {
            float density = getResources().getDisplayMetrics().density;
            int scaled = Math.round(prefs.getFontSize() * (density >= 3.0f ? 1.4f : 1.1f));
            return Math.max(16, scaled);
        }

        @Override
        public void onSharedPreferenceChanged(final SharedPreferences sp, String key) {
            renderHandler.post(new Runnable() {
                @Override
                public void run() {
                    boolean fontChanged = prefs.load(sp);
                    renderer.applyTheme(prefs);
                    if (fontChanged && width > 0 && height > 0) {
                        resizeMatrix();
                    }
                }
            });
        }

        @Override
        public void onVisibilityChanged(final boolean visible) {
            this.visible = visible;
            renderHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (visible) {
                        renderHandler.removeCallbacks(drawRunner);
                        renderHandler.post(drawRunner);
                    } else {
                        renderHandler.removeCallbacks(drawRunner);
                    }
                }
            });
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                final int x = (int) event.getX();
                renderHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!prefs.isTouchEnabled()) {
                            return;
                        }
                        int fontSize = getScaledFontSize();
                        if (fontSize > 0) {
                            int col = x / fontSize;
                            simulation.deflect(col, prefs.isGoldEnabled());
                        }
                    }
                });
            }
            super.onTouchEvent(event);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            this.visible = false;
            renderHandler.removeCallbacks(drawRunner);
            renderHandler.post(new Runnable() {
                @Override
                public void run() {
                    renderer.release();
                }
            });
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            this.visible = false;
            if (sharedPreferences != null) {
                sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
            }
            renderHandler.removeCallbacks(drawRunner);
            renderHandler.post(new Runnable() {
                @Override
                public void run() {
                    renderer.release();
                }
            });
            renderThread.quitSafely();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, final int width, final int height) {
            super.onSurfaceChanged(holder, format, width, height);
            renderHandler.post(new Runnable() {
                @Override
                public void run() {
                    MatrixRainEngine.this.width = width;
                    MatrixRainEngine.this.height = height;
                    resizeMatrix();
                }
            });
        }

        private void resizeMatrix() {
            if (width > 0 && height > 0) {
                int fontSize = getScaledFontSize();
                renderer.updateFontSize(fontSize);
                renderer.resize(width, height);

                int columns = width / fontSize;
                int rows = height / fontSize;
                simulation.resize(columns, rows, prefs);

                draw();
            }
        }

        private void draw() {
            renderHandler.removeCallbacks(drawRunner);
            if (!visible) {
                return;
            }

            simulation.step(prefs);
            renderer.render(getSurfaceHolder(), simulation, prefs);

            if (visible) {
                renderHandler.postDelayed(drawRunner, prefs.getDrawDelay());
            }
        }
    }
}
