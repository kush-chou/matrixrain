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
    private Paint headPaint;
    private Paint virusPaint;
    private Paint goldPaint;
    private Paint fadePaint;

    private int fontSize = 24;
    private boolean isRunning = false;

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
                    }
                    bufferCanvas.drawText(String.valueOf(glitchChar), x, glitchY * fontSize, p);
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
                        bufferCanvas.drawText(String.valueOf(simulation.getTrailChar(i)), x, lastY * fontSize, bodyPaint);
                    }

                    int curY = simulation.getDropY(i);
                    if (curY >= 0 && curY <= rows) {
                        Paint head = headPaint;
                        if (simulation.getColumnType(i) == RainSimulation.TYPE_VIRUS) {
                            head = virusPaint;
                        } else if (simulation.getColumnType(i) == RainSimulation.TYPE_GOLD) {
                            head = goldPaint;
                        } else if (simulation.getColumnType(i) == RainSimulation.TYPE_BACKGROUND) {
                            head = backgroundPaint;
                        }
                        bufferCanvas.drawText(String.valueOf(simulation.getPreviousChar(i)), x, curY * fontSize, head);
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

    private void initPaints() {
        Typeface matrixFont = Typeface.MONOSPACE;

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFF00FF66);
        textPaint.setTextSize(fontSize);
        textPaint.setTypeface(matrixFont);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(0xFF005522);
        backgroundPaint.setTextSize(fontSize);
        backgroundPaint.setTypeface(matrixFont);

        headPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headPaint.setColor(0xFFEEFFEE);
        headPaint.setTextSize(fontSize);
        headPaint.setTypeface(matrixFont);
        headPaint.setShadowLayer(8f, 0, 0, 0xFF00FF66);

        virusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        virusPaint.setColor(0xFFFF3333);
        virusPaint.setTextSize(fontSize);
        virusPaint.setTypeface(matrixFont);
        virusPaint.setShadowLayer(6f, 0, 0, 0xFFFF0033);

        goldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        goldPaint.setColor(0xFFFFD700);
        goldPaint.setTextSize(fontSize);
        goldPaint.setTypeface(matrixFont);
        goldPaint.setShadowLayer(6f, 0, 0, 0xFFFFD700);

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
