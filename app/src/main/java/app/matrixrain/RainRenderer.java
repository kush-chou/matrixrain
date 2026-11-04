package app.matrixrain;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.SurfaceHolder;
import androidx.core.content.res.ResourcesCompat;

/**
 * SurfaceHolder-based bitmap double-buffer renderer for Matrix Rain.
 * Renders phosphor decay fade, glyph trailing, radiant dual-pass bloom heads, and glitches.
 * Uses a half-resolution RGB_565 offscreen buffer bitmap scaled up via GPU blit to minimize CPU cost and memory bandwidth.
 */
public class RainRenderer {

    private static final float RENDER_SCALE = 0.5f;

    private Bitmap bufferBitmap;
    private Canvas bufferCanvas;
    private final Rect dstRect = new Rect();
    private final Paint blitPaint = new Paint(Paint.FILTER_BITMAP_FLAG);

    private Paint textPaint;
    private Paint backgroundPaint;
    private Paint headGlowPaint;
    private Paint headCorePaint;
    private Paint virusPaint;
    private Paint goldPaint;
    private Paint fadePaint;

    private int width;
    private int height;
    private int fontSize = 16;
    private Typeface typeface = Typeface.MONOSPACE;
    private String currentThemeName;

    private final Object renderLock = new Object();
    private final char[] glyphBuf = new char[1];

    private static volatile Typeface cachedTypeface;

    public static Typeface getMatrixTypeface(Context context) {
        if (cachedTypeface != null) {
            return cachedTypeface;
        }
        if (context == null) {
            return Typeface.MONOSPACE;
        }
        Context appContext = context.getApplicationContext() != null ? context.getApplicationContext() : context;
        try {
            Typeface tf = ResourcesCompat.getFont(appContext, R.font.share_tech_mono);
            if (tf != null) {
                cachedTypeface = tf;
                return tf;
            }
        } catch (Exception ignored) {
        }
        try {
            Typeface tf = Typeface.createFromAsset(appContext.getAssets(), "fonts/share_tech_mono.ttf");
            if (tf != null) {
                cachedTypeface = tf;
                return tf;
            }
        } catch (Exception ignored) {
        }
        return Typeface.MONOSPACE;
    }

    public RainRenderer() {
        this(16, (Typeface) null);
    }

    public RainRenderer(int initialFontSize) {
        this(initialFontSize, (Typeface) null);
    }

    public RainRenderer(Context context) {
        this(context, 16);
    }

    public RainRenderer(Context context, int initialFontSize) {
        this(initialFontSize, getMatrixTypeface(context));
    }

    public RainRenderer(int initialFontSize, Typeface typeface) {
        this.fontSize = initialFontSize;
        this.typeface = (typeface != null) ? typeface : Typeface.MONOSPACE;
        initPaints();
    }

    public void setTypeface(Typeface typeface) {
        this.typeface = (typeface != null) ? typeface : Typeface.MONOSPACE;
        if (textPaint != null) textPaint.setTypeface(this.typeface);
        if (backgroundPaint != null) backgroundPaint.setTypeface(this.typeface);
        if (headGlowPaint != null) headGlowPaint.setTypeface(this.typeface);
        if (headCorePaint != null) headCorePaint.setTypeface(this.typeface);
        if (virusPaint != null) virusPaint.setTypeface(this.typeface);
        if (goldPaint != null) goldPaint.setTypeface(this.typeface);
    }

    private void initPaints() {
        Typeface matrixFont = (this.typeface != null) ? this.typeface : Typeface.MONOSPACE;

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(fontSize);
        textPaint.setTypeface(matrixFont);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setTextSize(fontSize);
        backgroundPaint.setTypeface(matrixFont);

        headGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headGlowPaint.setTextSize(fontSize);
        headGlowPaint.setTypeface(matrixFont);

        headCorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headCorePaint.setTextSize(fontSize);
        headCorePaint.setTypeface(matrixFont);

        virusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        virusPaint.setTextSize(fontSize);
        virusPaint.setTypeface(matrixFont);

        goldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        goldPaint.setTextSize(fontSize);
        goldPaint.setTypeface(matrixFont);

        fadePaint = new Paint();
        fadePaint.setColor(Color.BLACK);
        fadePaint.setAlpha(10);

        applyTheme(RainPreferences.getPalette(RainPreferences.THEME_CLASSIC));
    }

    public void applyTheme(RainPreferences prefs) {
        if (prefs == null) {
            return;
        }
        applyTheme(prefs.getPalette());
    }

    public void applyTheme(RainPreferences.Palette palette) {
        if (palette == null) {
            return;
        }
        this.currentThemeName = palette.name;

        textPaint.setColor(palette.textColor);
        backgroundPaint.setColor(palette.bgColor);

        // Radiant bloom glow paint (aura pass)
        headGlowPaint.setColor(palette.glowColor);
        headGlowPaint.setShadowLayer(16f, 0, 0, palette.glowColor);

        // Brilliant core paint (sharp foreground pass)
        headCorePaint.setColor(palette.headColor);

        // Virus and Gold glowing paints
        virusPaint.setColor(palette.virusColor);
        virusPaint.setShadowLayer(12f, 0, 0, palette.virusColor);

        goldPaint.setColor(palette.goldColor);
        goldPaint.setShadowLayer(12f, 0, 0, palette.goldColor);
    }

    public void updateFontSize(int fontSize) {
        this.fontSize = fontSize;
        if (textPaint != null) textPaint.setTextSize(fontSize);
        if (backgroundPaint != null) backgroundPaint.setTextSize(fontSize);
        if (headGlowPaint != null) headGlowPaint.setTextSize(fontSize);
        if (headCorePaint != null) headCorePaint.setTextSize(fontSize);
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

            int bitmapWidth = Math.max(1, Math.round(width * RENDER_SCALE));
            int bitmapHeight = Math.max(1, Math.round(height * RENDER_SCALE));

            Bitmap oldBitmap = bufferBitmap;
            bufferBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.RGB_565);
            bufferCanvas = new Canvas(bufferBitmap);
            float scaleX = bitmapWidth / (float) width;
            float scaleY = bitmapHeight / (float) height;
            bufferCanvas.scale(scaleX, scaleY);
            bufferCanvas.drawColor(Color.BLACK);

            dstRect.set(0, 0, width, height);

            if (oldBitmap != null && !oldBitmap.isRecycled()) {
                oldBitmap.recycle();
            }
        }
    }

    public void render(SurfaceHolder holder, RainSimulation sim, RainPreferences prefs) {
        if (holder == null || sim == null) {
            return;
        }

        if (prefs != null && (currentThemeName == null || !currentThemeName.equals(prefs.getCurrentTheme()))) {
            applyTheme(prefs);
        }

        Canvas canvas = null;
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                canvas = holder.lockHardwareCanvas();
            } else {
                canvas = holder.lockCanvas();
            }
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
                        } else if (sim.getColumnType(i) == RainSimulation.TYPE_GOLD) {
                            p = goldPaint;
                        }
                        glyphBuf[0] = glitchChar;
                        bufferCanvas.drawText(glyphBuf, 0, 1, x, glitchY * fontSize, p);
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
                            glyphBuf[0] = sim.getTrailChar(i);
                            bufferCanvas.drawText(glyphBuf, 0, 1, x, lastY * fontSize, bodyPaint);
                        }

                        int curY = sim.getDropY(i);
                        if (curY >= 0 && curY <= heightRows) {
                            char ch = sim.getPreviousChar(i);
                            glyphBuf[0] = ch;
                            int colType = sim.getColumnType(i);

                            if (colType == RainSimulation.TYPE_VIRUS) {
                                bufferCanvas.drawText(glyphBuf, 0, 1, x, curY * fontSize, virusPaint);
                            } else if (colType == RainSimulation.TYPE_GOLD) {
                                bufferCanvas.drawText(glyphBuf, 0, 1, x, curY * fontSize, goldPaint);
                            } else if (colType == RainSimulation.TYPE_BACKGROUND) {
                                bufferCanvas.drawText(glyphBuf, 0, 1, x, curY * fontSize, backgroundPaint);
                            } else {
                                // Dual-pass head: radiant bloom glow aura first, then crisp brilliant core
                                bufferCanvas.drawText(glyphBuf, 0, 1, x, curY * fontSize, headGlowPaint);
                                bufferCanvas.drawText(glyphBuf, 0, 1, x, curY * fontSize, headCorePaint);
                            }
                        }
                    }
                }

                // Blit offscreen buffer to surface canvas
                canvas.drawBitmap(bufferBitmap, null, dstRect, blitPaint);
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
