package app.matrixrain;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

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

        private final Handler handler = new Handler(Looper.getMainLooper());
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
        private boolean visible = true;
        private SharedPreferences sharedPreferences;

        public MatrixRainEngine() {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MatrixRainWallpaperService.this);
            sharedPreferences.registerOnSharedPreferenceChangeListener(this);
            prefs.load(sharedPreferences);
            renderer.updateFontSize(prefs.getFontSize());
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
            boolean fontChanged = prefs.load(sp);
            if (fontChanged && width > 0 && height > 0) {
                resizeMatrix();
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
            if (visible) {
                handler.removeCallbacks(drawRunner);
                handler.post(drawRunner);
            } else {
                handler.removeCallbacks(drawRunner);
            }
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            if (!prefs.isTouchEnabled()) {
                super.onTouchEvent(event);
                return;
            }

            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                int x = (int) event.getX();
                int fontSize = prefs.getFontSize();
                if (fontSize > 0) {
                    int col = x / fontSize;
                    simulation.deflect(col, prefs.isGoldEnabled());
                }
            }
            super.onTouchEvent(event);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            this.visible = false;
            handler.removeCallbacks(drawRunner);
            renderer.release();
            if (sharedPreferences != null) {
                sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            this.visible = false;
            handler.removeCallbacks(drawRunner);
            renderer.release();
            if (sharedPreferences != null) {
                sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            this.width = width;
            this.height = height;
            resizeMatrix();
        }

        private void resizeMatrix() {
            if (width > 0 && height > 0) {
                int fontSize = prefs.getFontSize();
                renderer.updateFontSize(fontSize);
                renderer.resize(width, height);

                int columns = width / fontSize;
                int rows = height / fontSize;
                simulation.resize(columns, rows, prefs);

                draw();
            }
        }

        private void draw() {
            handler.removeCallbacks(drawRunner);
            if (!visible) {
                return;
            }

            simulation.step(prefs);
            renderer.render(getSurfaceHolder(), simulation, prefs);

            if (visible) {
                handler.postDelayed(drawRunner, prefs.getDrawDelay());
            }
        }
    }
}
