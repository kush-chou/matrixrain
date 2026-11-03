package app.matrixrain;

import java.util.Random;

/**
 * Pure Java simulation engine for Matrix Rain.
 * ZERO Android dependencies (runs entirely on pure JVM).
 */
public class RainSimulation {

    public static final String[] HIDDEN_WORDS = {"MATRIX", "SYSTEM", "HACK", "WAKE UP", "NEO"};

    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_BACKGROUND = 1;
    public static final int TYPE_VIRUS = 2;
    public static final int TYPE_GOLD = 3;

    private int columnSize;
    private int heightRows;

    private int[] dropY;
    private int[] lastDropY;
    private int[] speeds;
    private int[] frameCounts;
    private char[] previousChars;
    private char[] trailChars;
    private int[] columnType; // 0=Normal, 1=Background, 2=Virus, 3=Gold

    private int[] wordActiveIndex;
    private String[] assignedWord;

    private boolean[] columnStepped;
    private boolean[] isGlitching;
    private int[] glitchY;
    private char[] glitchChars;
    private boolean anyGlitching;

    private final Random random;

    public RainSimulation() {
        this(new Random());
    }

    public RainSimulation(Random random) {
        this.random = (random != null) ? random : new Random();
        this.columnSize = 0;
        this.heightRows = 0;
        allocateArrays(0);
    }

    private void allocateArrays(int size) {
        dropY = new int[size];
        lastDropY = new int[size];
        speeds = new int[size];
        frameCounts = new int[size];
        previousChars = new char[size];
        trailChars = new char[size];
        columnType = new int[size];
        wordActiveIndex = new int[size];
        assignedWord = new String[size];
        columnStepped = new boolean[size];
        isGlitching = new boolean[size];
        glitchY = new int[size];
        glitchChars = new char[size];
    }

    public void resize(int columns, int rows, RainPreferences prefs) {
        if (columns <= 0 || rows <= 0) {
            this.columnSize = 0;
            this.heightRows = 0;
            allocateArrays(0);
            return;
        }

        this.columnSize = columns;
        this.heightRows = rows;
        allocateArrays(columns);

        for (int i = 0; i < columnSize; i++) {
            lastDropY[i] = -1;
            glitchY[i] = -1;
            initColumn(i, true, prefs);
            wordActiveIndex[i] = -1;
        }
    }

    public void initColumn(int i, boolean randomY, RainPreferences prefs) {
        if (i < 0 || i >= columnSize) {
            return;
        }

        dropY[i] = randomY ? (heightRows > 0 ? random.nextInt(heightRows) : 0) : 0;
        lastDropY[i] = -1;

        boolean enableVirus = prefs == null || prefs.isVirusEnabled();
        boolean enableGold = prefs == null || prefs.isGoldEnabled();
        boolean enableDepth = prefs == null || prefs.isDepthEnabled();
        boolean enableMessages = prefs == null || prefs.isMessagesEnabled();

        // Determine Column Type based on settings
        float roll = random.nextFloat();

        if (enableVirus && roll > 0.98f) {
            columnType[i] = TYPE_VIRUS;
            speeds[i] = 1;
        } else if (enableGold && roll > 0.96f) {
            columnType[i] = TYPE_GOLD;
            speeds[i] = random.nextInt(2) + 1;
        } else if (enableDepth && roll > 0.60f) {
            columnType[i] = TYPE_BACKGROUND;
            speeds[i] = random.nextInt(2) + 2;
        } else {
            columnType[i] = TYPE_NORMAL;
            speeds[i] = random.nextInt(2) + 1;
        }

        frameCounts[i] = speeds[i];
        previousChars[i] = getRandomMatrixChar();
        trailChars[i] = previousChars[i];

        wordActiveIndex[i] = -1;
        if (enableMessages && !randomY && columnType[i] == TYPE_NORMAL && random.nextInt(50) == 0) {
            assignedWord[i] = HIDDEN_WORDS[random.nextInt(HIDDEN_WORDS.length)];
            wordActiveIndex[i] = 0;
        }
    }

    public char getRandomMatrixChar() {
        int type = random.nextInt(100);
        if (type < 60) {
            return (char) (0xFF66 + random.nextInt(0xFF9D - 0xFF66));
        } else if (type < 85) {
            return (char) ('0' + random.nextInt(10));
        } else {
            return (char) (0x21 + random.nextInt(90));
        }
    }

    public void step(RainPreferences prefs) {
        boolean enableGlitch = prefs == null || prefs.isGlitchEnabled();
        anyGlitching = false;

        for (int i = 0; i < columnSize; i++) {
            columnStepped[i] = false;
            isGlitching[i] = false;

            // Glitch
            if (enableGlitch && random.nextInt(1000) < 15) {
                isGlitching[i] = true;
                anyGlitching = true;
                glitchY[i] = heightRows > 0 ? random.nextInt(heightRows) : 0;
                glitchChars[i] = getRandomMatrixChar();
            }

            frameCounts[i]--;

            if (frameCounts[i] <= 0) {
                frameCounts[i] = speeds[i];
                columnStepped[i] = true;

                lastDropY[i] = dropY[i];
                trailChars[i] = previousChars[i];

                dropY[i]++;

                if (dropY[i] > heightRows) {
                    if (random.nextFloat() > 0.95f) {
                        initColumn(i, false, prefs);
                    }
                } else {
                    char newChar;
                    if (wordActiveIndex[i] >= 0 && assignedWord[i] != null) {
                        newChar = assignedWord[i].charAt(wordActiveIndex[i]);
                        wordActiveIndex[i]++;
                        if (wordActiveIndex[i] >= assignedWord[i].length()) {
                            wordActiveIndex[i] = -1;
                        }
                    } else {
                        newChar = getRandomMatrixChar();
                    }
                    previousChars[i] = newChar;
                }
            }
        }
    }

    public void deflect(int col, boolean enableGold) {
        if (col >= 0 && col < columnSize) {
            dropY[col] = 0;
            lastDropY[col] = -1;
            speeds[col] = 1;
            frameCounts[col] = 0;
            columnType[col] = (enableGold && random.nextInt(20) == 0) ? TYPE_GOLD : TYPE_NORMAL;
        }
    }

    public int getColumnSize() {
        return columnSize;
    }

    public int getHeightRows() {
        return heightRows;
    }

    public int getDropY(int col) {
        return (col >= 0 && col < columnSize) ? dropY[col] : 0;
    }

    public int getLastDropY(int col) {
        return (col >= 0 && col < columnSize) ? lastDropY[col] : -1;
    }

    public char getPreviousChar(int col) {
        return (col >= 0 && col < columnSize) ? previousChars[col] : ' ';
    }

    public char getTrailChar(int col) {
        return (col >= 0 && col < columnSize) ? trailChars[col] : ' ';
    }

    public int getColumnType(int col) {
        return (col >= 0 && col < columnSize) ? columnType[col] : TYPE_NORMAL;
    }

    public int getSpeed(int col) {
        return (col >= 0 && col < columnSize) ? speeds[col] : 1;
    }

    public int getFrameCount(int col) {
        return (col >= 0 && col < columnSize) ? frameCounts[col] : 0;
    }

    public boolean hasStepped(int col) {
        return (col >= 0 && col < columnSize) && columnStepped[col];
    }

    public boolean isGlitching() {
        return anyGlitching;
    }

    public boolean isGlitching(int col) {
        return (col >= 0 && col < columnSize) && isGlitching[col];
    }

    public int getGlitchY(int col) {
        return (col >= 0 && col < columnSize) ? glitchY[col] : 0;
    }

    public char getGlitchChar(int col) {
        return (col >= 0 && col < columnSize) ? glitchChars[col] : ' ';
    }

    public int getWordActiveIndex(int col) {
        return (col >= 0 && col < columnSize) ? wordActiveIndex[col] : -1;
    }

    public String getAssignedWord(int col) {
        return (col >= 0 && col < columnSize) ? assignedWord[col] : null;
    }
}
