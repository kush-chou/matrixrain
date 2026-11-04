package app.matrixrain;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Lightweight Matrix Rain View that renders the cascading digital rain behind UI.
 * Renders radiant dual-pass bloom glow and dynamically applies color themes.
 * Automatically halts animation when not visible to consume 0% battery.
 */
public class MatrixRainView extends View {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final RainPreferences prefs = new RainPreferences();
    private final RainSimulation simulation = new RainSimulation();

    private Bitmap bufferBitmap;
    private Canvas bufferCanvas;

    private Paint textPaint;
    private Paint backgroundPaint;
    private Paint headGlowPaint;
    private Paint headCorePaint;
    private Paint virusPaint;
    private Paint goldPaint;
    private Paint fadePaint;

    private int fontSize = 24;
    private Typeface matrixFont;
    private boolean isRunning = false;
    private final char[] glyphBuf = new char[1];

    private final Runnable animRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isRunning) {
                return;
            }

            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0 || bufferCanvas == null || bufferBitmap == null || bufferBitmap.isRecycled()) {
                if (isRunning) {
                    handler.postDelayed(this, prefs.getDrawDelay());
                }
                return;
            }

            // Phosphor decay overlay
            bufferCanvas.drawRect(0, 0, w, h, fadePaint);

            simulation.step(prefs);

            int cols = simulation.getColumnSize();
            int rows = simulation.getHeightRows();

            for (int i = 0; i < cols; i++) {
                float x = i * fontSize;

                // Glitches
                if (simulation.isGlitching(i)) {
                    int glitchY = simulation.getGlitchY(i);
                    char glitchChar = simulation.getGlitchChar(i);
                    Paint p = (simulation.getColumnType(i) == RainSimulation.TYPE_BACKGROUND) ? backgroundPaint : textPaint;
                    if (simulation.getColumnType(i) == RainSimulation.TYPE_VIRUS) {
                        p = virusPaint;
                    } else if (simulation.getColumnType(i) == RainSimulation.TYPE_GOLD) {
                        p = goldPaint;
                    }
                    glyphBuf[0] = glitchChar;
                    bufferCanvas.drawText(glyphBuf, 0, 1, x, glitchY * fontSize, p);
                }

                // Stepped columns
                if (simulation.hasStepped(i)) {
                    int lastY = simulation.getLastDropY(i);
                    if (lastY >= 0 && lastY <= rows) {
                        Paint bodyPaint;
                        switch (simulation.getColumnType(i)) {
                            case RainSimulation.TYPE_BACKGROUND:
                                bodyPaint = backgroundPaint;
                                break;
                            case RainSimulation.TYPE_VIRUS:
                                bodyPaint = virusPaint;
                                break;
                            case RainSimulation.TYPE_GOLD:
                                bodyPaint = goldPaint;
                                break;
                            default:
                                bodyPaint = textPaint;
                                break;
                        }
                        glyphBuf[0] = simulation.getTrailChar(i);
                        bufferCanvas.drawText(glyphBuf, 0, 1, x, lastY * fontSize, bodyPaint);
                    }

                    int curY = simulation.getDropY(i);
                    if (curY >= 0 && curY <= rows) {
                        char ch = simulation.getPreviousChar(i);
                        glyphBuf[0] = ch;
                        int type = simulation.getColumnType(i);

                        if (type == RainSimulation.TYPE_VIRUS) {
                            bufferCanvas.drawText(glyphBuf, 0, 1, x, curY * fontSize, virusPaint);
                        } else if (type == RainSimulation.TYPE_GOLD) {
                            bufferCanvas.drawText(glyphBuf, 0, 1, x, curY * fontSize, goldPaint);
                        } else if (type == RainSimulation.TYPE_BACKGROUND) {
                            bufferCanvas.drawText(glyphBuf, 0, 1, x, curY * fontSize, backgroundPaint);
                        } else {
                            // Dual-pass head: vibrant blurred bloom aura + sharp bright core
                            bufferCanvas.drawText(glyphBuf, 0, 1, x, curY * fontSize, headGlowPaint);
                            bufferCanvas.drawText(glyphBuf, 0, 1, x, curY * fontSize, headCorePaint);
                        }
                    }
                }
            }

            invalidate();

            if (isRunning) {
                handler.postDelayed(this, prefs.getDrawDelay());
            }
        }
    };

    public MatrixRainView(Context context) {
        super(context);
        init();
    }

    public MatrixRainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MatrixRainView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        matrixFont = RainRenderer.getMatrixTypeface(getContext());
        reloadPreferences();
    }

    public void reloadPreferences() {
        try {
            prefs.load(PreferenceManager.getDefaultSharedPreferences(getContext()));
        } catch (Exception ignored) {
        }

        float density = getResources().getDisplayMetrics().density;
        fontSize = Math.round(prefs.getFontSize() * (density >= 3.0f ? 1.4f : 1.1f));
        if (fontSize < 16) {
            fontSize = 16;
        }

        initPaints();

        if (getWidth() > 0 && getHeight() > 0) {
            rebuildSimulation(getWidth(), getHeight());
        }
    }

    public void applyTheme(RainPreferences prefs) {
        if (prefs == null) return;
        RainPreferences.Palette palette = prefs.getPalette();
        if (textPaint != null) textPaint.setColor(palette.textColor);
        if (backgroundPaint != null) backgroundPaint.setColor(palette.bgColor);
        if (headGlowPaint != null) {
            headGlowPaint.setColor(palette.glowColor);
            headGlowPaint.setShadowLayer(16f, 0, 0, palette.glowColor);
        }
        if (headCorePaint != null) headCorePaint.setColor(palette.headColor);
        if (virusPaint != null) {
            virusPaint.setColor(palette.virusColor);
            virusPaint.setShadowLayer(12f, 0, 0, palette.virusColor);
        }
        if (goldPaint != null) {
            goldPaint.setColor(palette.goldColor);
            goldPaint.setShadowLayer(12f, 0, 0, palette.goldColor);
        }
    }

    private void initPaints() {
        if (matrixFont == null) {
            matrixFont = RainRenderer.getMatrixTypeface(getContext());
        }
        RainPreferences.Palette palette = prefs.getPalette();

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(palette.textColor);
        textPaint.setTextSize(fontSize);
        textPaint.setTypeface(matrixFont);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(palette.bgColor);
        backgroundPaint.setTextSize(fontSize);
        backgroundPaint.setTypeface(matrixFont);

        // Radiant bloom glow paint
        headGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headGlowPaint.setColor(palette.glowColor);
        headGlowPaint.setTextSize(fontSize);
        headGlowPaint.setTypeface(matrixFont);
        headGlowPaint.setShadowLayer(16f, 0, 0, palette.glowColor);

        // Brilliant core paint
        headCorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headCorePaint.setColor(palette.headColor);
        headCorePaint.setTextSize(fontSize);
        headCorePaint.setTypeface(matrixFont);

        // Virus paint
        virusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        virusPaint.setColor(palette.virusColor);
        virusPaint.setTextSize(fontSize);
        virusPaint.setTypeface(matrixFont);
        virusPaint.setShadowLayer(12f, 0, 0, palette.virusColor);

        // Gold paint
        goldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        goldPaint.setColor(palette.goldColor);
        goldPaint.setTextSize(fontSize);
        goldPaint.setTypeface(matrixFont);
        goldPaint.setShadowLayer(12f, 0, 0, palette.goldColor);

        fadePaint = new Paint();
        fadePaint.setColor(Color.BLACK);
        fadePaint.setAlpha(12);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            rebuildSimulation(w, h);
        }
    }

    private void rebuildSimulation(int w, int h) {
        if (bufferBitmap != null && !bufferBitmap.isRecycled()) {
            bufferBitmap.recycle();
        }

        bufferBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bufferCanvas = new Canvas(bufferBitmap);
        bufferCanvas.drawColor(Color.BLACK);

        int cols = w / fontSize;
        int rows = h / fontSize;
        simulation.resize(cols, rows, prefs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bufferBitmap != null && !bufferBitmap.isRecycled()) {
            canvas.drawBitmap(bufferBitmap, 0, 0, null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            if (prefs != null && !prefs.isTouchEnabled()) {
                return super.onTouchEvent(event);
            }
            int x = (int) event.getX();
            if (fontSize > 0) {
                int col = x / fontSize;
                boolean gold = prefs != null && prefs.isGoldEnabled();
                simulation.deflect(col, gold);
                simulation.deflect(col - 1, gold);
                simulation.deflect(col + 1, gold);
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void startAnimation() {
        if (isRunning) return;
        isRunning = true;
        handler.removeCallbacks(animRunnable);
        handler.post(animRunnable);
    }

    public void stopAnimation() {
        isRunning = false;
        handler.removeCallbacks(animRunnable);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == View.VISIBLE) {
            startAnimation();
        } else {
            stopAnimation();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (getVisibility() == View.VISIBLE && getWindowVisibility() == View.VISIBLE) {
            startAnimation();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
        if (bufferBitmap != null && !bufferBitmap.isRecycled()) {
            bufferBitmap.recycle();
            bufferBitmap = null;
            bufferCanvas = null;
        }
    }
}
