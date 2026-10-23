package app.matrixrain;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.util.Random;

public class MatrixRainWallpaperService extends WallpaperService {
    @Override
    public Engine onCreateEngine() {
        return new MatrixRainEngine();
    }

    private class MatrixRainEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener {
        private final Handler handler = new Handler(Looper.getMainLooper());
        private final Runnable drawRunner = new Runnable() {
            @Override
            public void run() {
                draw();
            }
        };
        
        // Paints
        private Paint textPaint;       
        private Paint backgroundPaint; 
        private Paint headPaint;       
        private Paint virusPaint;      
        private Paint goldPaint;       
        private Paint fadePaint;
        
        private int width, height;
        private boolean visible = true;
        
        // State
        private int[] dropY;         
        private int[] speeds;        
        private int[] frameCounts;   
        private char[] previousChars;
        private int[] columnType; // 0=Normal, 1=Background, 2=Virus, 3=Gold
        
        private String[] hiddenWords = {"MATRIX", "SYSTEM", "HACK", "WAKE UP", "NEO"};
        private int[] wordActiveIndex; 
        private String[] assignedWord; 
        
        private int fontSize = 16; 
        private int columnSize;
        private final Random random = new Random();
        private int drawDelay = 30;
        
        private Bitmap bufferBitmap;
        private Canvas bufferCanvas;
        private SharedPreferences prefs;

        // Preference Flags
        private boolean enableVirus = true;
        private boolean enableGold = true;
        private boolean enableMessages = true;
        private boolean enableDepth = true;
        private boolean enableGlitch = true;
        private boolean enableTouch = true;

        public MatrixRainEngine() {
            prefs = PreferenceManager.getDefaultSharedPreferences(MatrixRainWallpaperService.this);
            prefs.registerOnSharedPreferenceChangeListener(this);
            loadPreferences();

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

        private void loadPreferences() {
            // Speed
            int speedProgress = prefs.getInt("pref_speed_progress", 70);
            drawDelay = 110 - speedProgress; // 10ms to 110ms
            
            // Density (requires re-initialization if changed)
            int densityProgress = prefs.getInt("pref_density_progress", 80);
            int newFontSize = 10 + (100 - densityProgress) / 2; // Range: 10 to 60
            
            if (newFontSize != fontSize) {
                fontSize = newFontSize;
                initPaints(); // Update text size in paints
                // Trigger surface change logic to resize columns if surface exists
                if (width > 0 && height > 0) {
                    resizeMatrix();
                }
            }

            enableVirus = prefs.getBoolean("pref_virus", true);
            enableGold = prefs.getBoolean("pref_gold", true);
            enableMessages = prefs.getBoolean("pref_messages", true);
            enableDepth = prefs.getBoolean("pref_depth", true);
            enableGlitch = prefs.getBoolean("pref_glitch", true);
            enableTouch = prefs.getBoolean("pref_touch", true);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            loadPreferences();
        }

        private char getRandomMatrixChar() {
            int type = random.nextInt(100);
            if (type < 60) { 
                return (char) (0xFF66 + random.nextInt(0xFF9D - 0xFF66));
            } else if (type < 85) {
                return (char) ('0' + random.nextInt(10));
            } else {
                return (char) (0x21 + random.nextInt(90));
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
            if (visible) {
                handler.post(drawRunner);
            } else {
                handler.removeCallbacks(drawRunner);
            }
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            if (!enableTouch) return;

            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                int x = (int) event.getX();
                int col = x / fontSize;
                if (col >= 0 && col < columnSize) {
                    dropY[col] = 0;
                    speeds[col] = 1; 
                    frameCounts[col] = 0; 
                    columnType[col] = 0; 
                    
                    if (enableGold && random.nextInt(20) == 0) columnType[col] = 3;
                }
            }
            super.onTouchEvent(event);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            this.visible = false;
            handler.removeCallbacks(drawRunner);
            if (bufferBitmap != null) {
                bufferBitmap.recycle();
                bufferBitmap = null;
            }
            prefs.unregisterOnSharedPreferenceChangeListener(this);
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
                columnSize = width / fontSize;
                
                dropY = new int[columnSize];
                speeds = new int[columnSize];
                frameCounts = new int[columnSize];
                previousChars = new char[columnSize];
                columnType = new int[columnSize];
                wordActiveIndex = new int[columnSize];
                assignedWord = new String[columnSize];
                
                for (int i = 0; i < columnSize; i++) {
                    initColumn(i, true);
                    wordActiveIndex[i] = -1;
                }
                
                if (bufferBitmap != null) bufferBitmap.recycle();
                bufferBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                bufferCanvas = new Canvas(bufferBitmap);
                bufferCanvas.drawColor(Color.BLACK);
                
                draw();
            }
        }

        private void initColumn(int i, boolean randomY) {
            dropY[i] = randomY ? random.nextInt(height / fontSize) : 0;
            
            // Determine Column Type based on settings
            float roll = random.nextFloat();
            
            if (enableVirus && roll > 0.98f) {
                columnType[i] = 2; // Virus
                speeds[i] = random.nextInt(2) + 1; 
            } else if (enableGold && roll > 0.96f) {
                columnType[i] = 3; // Gold
                speeds[i] = random.nextInt(3) + 2; 
            } else if (enableDepth && roll > 0.60f) {
                columnType[i] = 1; // Background
                speeds[i] = random.nextInt(2) + 3; 
            } else {
                columnType[i] = 0; // Standard
                speeds[i] = random.nextInt(2) + 1; 
            }
            
            frameCounts[i] = speeds[i];
            previousChars[i] = getRandomMatrixChar();
            
            wordActiveIndex[i] = -1;
            if (enableMessages && !randomY && columnType[i] == 0 && random.nextInt(50) == 0) {
                assignedWord[i] = hiddenWords[random.nextInt(hiddenWords.length)];
                wordActiveIndex[i] = 0;
            }
        }

        private void draw() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas != null && bufferBitmap != null) {
                    bufferCanvas.drawRect(0, 0, width, height, fadePaint);
                    
                    for (int i = 0; i < columnSize; i++) {
                        frameCounts[i]--;
                        
                        // Glitch
                        if (enableGlitch && random.nextInt(1000) < 15) { 
                            int glitchY = random.nextInt(height / fontSize);
                            Paint p = (columnType[i] == 1) ? backgroundPaint : textPaint;
                            if (columnType[i] == 2) p = virusPaint;
                            bufferCanvas.drawText(String.valueOf(getRandomMatrixChar()), i * fontSize, glitchY * fontSize, p);
                        }

                        if (frameCounts[i] <= 0) {
                            frameCounts[i] = speeds[i];
                            
                            float x = i * fontSize;
                            float y = dropY[i] * fontSize;

                            Paint bodyPaint;
                            switch (columnType[i]) {
                                case 1: bodyPaint = backgroundPaint; break;
                                case 2: bodyPaint = virusPaint; break;
                                case 3: bodyPaint = goldPaint; break;
                                default: bodyPaint = textPaint; break;
                            }
                            bufferCanvas.drawText(String.valueOf(previousChars[i]), x, y, bodyPaint);

                            dropY[i]++;
                            
                            if (dropY[i] * fontSize > height) {
                                if (Math.random() > 0.95) {
                                    initColumn(i, false);
                                }
                            } else {
                                char newChar;
                                if (wordActiveIndex[i] >= 0) {
                                    newChar = assignedWord[i].charAt(wordActiveIndex[i]);
                                    wordActiveIndex[i]++;
                                    if (wordActiveIndex[i] >= assignedWord[i].length()) {
                                        wordActiveIndex[i] = -1;
                                    }
                                } else {
                                    newChar = getRandomMatrixChar();
                                }
                                
                                Paint head = headPaint;
                                if (columnType[i] == 2) head = virusPaint; 
                                if (columnType[i] == 3) head = goldPaint;
                                if (columnType[i] == 1) head = backgroundPaint; 

                                bufferCanvas.drawText(String.valueOf(newChar), x, dropY[i] * fontSize, head);
                                previousChars[i] = newChar;
                            }
                        }
                    }
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
            
            handler.removeCallbacks(drawRunner);
            if (visible) {
                handler.postDelayed(drawRunner, drawDelay);
            }
        }
    }
}
