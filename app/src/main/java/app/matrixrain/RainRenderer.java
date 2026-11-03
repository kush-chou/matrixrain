package app.matrixrain;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.SurfaceHolder;

/**
 * SurfaceHolder-based bitmap double-buffer renderer for Matrix Rain.
 * Renders phosphor decay fade, glyph trailing, glowing heads, and glitches.
 */
public class RainRenderer {

    private Bitmap bufferBitmap;
    private Canvas bufferCanvas;

    private Paint textPaint;
    private Paint backgroundPaint;
    private Paint headPaint;
    private Paint virusPaint;
    private Paint goldPaint;
    private Paint fadePaint;

    private int width;
    private int height;
    private int fontSize = 16;

    private final Object renderLock = new Object();

    public RainRenderer() {
        this(16);
    }

    public RainRenderer(int initialFontSize) {
        this.fontSize = initialFontSize;
        initPaints();
    }

    private void initPaints() {
        Typeface matrixFont = Typeface.MONOSPACE;

        textPaint = new Paint();
        textPaint.setColor(0xFF00FF00);
        textPaint.setTextSize(fontSize);
        textPaint.setTypeface(matrixFont);
        textPaint.setAntiAlias(true);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(0xFF004400);
        backgroundPaint.setTextSize(fontSize);
        backgroundPaint.setTypeface(matrixFont);
        backgroundPaint.setAntiAlias(true);

        headPaint = new Paint();
        headPaint.setColor(0xFFEEFFEE);
        headPaint.setTextSize(fontSize);
        headPaint.setTypeface(matrixFont);
        headPaint.setShadowLayer(8f, 0, 0, 0xFF00FF00);
        headPaint.setAntiAlias(true);

        virusPaint = new Paint();
        virusPaint.setColor(0xFFFF0000);
        virusPaint.setTextSize(fontSize);
        virusPaint.setTypeface(matrixFont);
        virusPaint.setShadowLayer(5f, 0, 0, 0xFFFF0000);
        virusPaint.setAntiAlias(true);

        goldPaint = new Paint();
        goldPaint.setColor(0xFFFFD700);
        goldPaint.setTextSize(fontSize);
        goldPaint.setTypeface(matrixFont);
        goldPaint.setShadowLayer(5f, 0, 0, 0xFFFFD700);
        goldPaint.setAntiAlias(true);

        fadePaint = new Paint();
        fadePaint.setColor(Color.BLACK);
        fadePaint.setAlpha(10);
    }

    public void updateFontSize(int fontSize) {
        this.fontSize = fontSize;
        if (textPaint != null) textPaint.setTextSize(fontSize);
        if (backgroundPaint != null) backgroundPaint.setTextSize(fontSize);
        if (headPaint != null) headPaint.setTextSize(fontSize);
        if (virusPaint != null) virusPaint.setTextSize(fontSize);
        if (goldPaint != null) goldPaint.setTextSize(fontSize);
    }

    public void resize(int width, int height) {
        synchronized (renderLock) {
            this.width = width;
            this.height = height;
            if (width <= 0 || height <= 0) {
                return;
            }

            Bitmap oldBitmap = bufferBitmap;
            bufferBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bufferCanvas = new Canvas(bufferBitmap);
            bufferCanvas.drawColor(Color.BLACK);

            if (oldBitmap != null && !oldBitmap.isRecycled()) {
                oldBitmap.recycle();
            }
        }
    }

    public void render(SurfaceHolder holder, RainSimulation sim, RainPreferences prefs) {
        if (holder == null || sim == null) {
            return;
        }

        Canvas canvas = null;
        try {
            canvas = holder.lockCanvas();
            if (canvas == null) {
                return;
            }

            synchronized (renderLock) {
                if (bufferBitmap == null || bufferBitmap.isRecycled() || bufferCanvas == null) {
                    return;
                }

                // Phosphor decay fade overlay
                bufferCanvas.drawRect(0, 0, width, height, fadePaint);

                int columnSize = sim.getColumnSize();
                int heightRows = sim.getHeightRows();

                for (int i = 0; i < columnSize; i++) {
                    float x = i * fontSize;

                    // Glitch rendering
                    if (sim.isGlitching(i)) {
                        int glitchY = sim.getGlitchY(i);
                        char glitchChar = sim.getGlitchChar(i);
                        Paint p = (sim.getColumnType(i) == RainSimulation.TYPE_BACKGROUND) ? backgroundPaint : textPaint;
                        if (sim.getColumnType(i) == RainSimulation.TYPE_VIRUS) {
                            p = virusPaint;
                        }
                        bufferCanvas.drawText(String.valueOf(glitchChar), x, glitchY * fontSize, p);
                    }

                    // Stepped columns: trailing glyph + glowing head
                    if (sim.hasStepped(i)) {
                        int lastY = sim.getLastDropY(i);
                        if (lastY >= 0 && lastY <= heightRows) {
                            Paint bodyPaint;
                            switch (sim.getColumnType(i)) {
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
                            bufferCanvas.drawText(String.valueOf(sim.getTrailChar(i)), x, lastY * fontSize, bodyPaint);
                        }

                        int curY = sim.getDropY(i);
                        if (curY >= 0 && curY <= heightRows) {
                            Paint head = headPaint;
                            if (sim.getColumnType(i) == RainSimulation.TYPE_VIRUS) {
                                head = virusPaint;
                            } else if (sim.getColumnType(i) == RainSimulation.TYPE_GOLD) {
                                head = goldPaint;
                            } else if (sim.getColumnType(i) == RainSimulation.TYPE_BACKGROUND) {
                                head = backgroundPaint;
                            }
                            bufferCanvas.drawText(String.valueOf(sim.getPreviousChar(i)), x, curY * fontSize, head);
                        }
                    }
                }

                // Blit offscreen buffer to surface canvas
                canvas.drawBitmap(bufferBitmap, 0, 0, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null) {
                try {
                    holder.unlockCanvasAndPost(canvas);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void release() {
        synchronized (renderLock) {
            if (bufferBitmap != null) {
                if (!bufferBitmap.isRecycled()) {
                    bufferBitmap.recycle();
                }
                bufferBitmap = null;
                bufferCanvas = null;
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getFontSize() {
        return fontSize;
    }
}
