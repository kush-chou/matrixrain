package app.matrixrain;

import android.content.SharedPreferences;

/**
 * Encapsulates preferences decoding, conversion formulas, default values, and theme palettes.
 */
public class RainPreferences {

    public static final String KEY_SPEED = "pref_speed_progress";
    public static final String KEY_DENSITY = "pref_density_progress";
    public static final String KEY_THEME = "pref_theme";
    public static final String KEY_VIRUS = "pref_virus";
    public static final String KEY_GOLD = "pref_gold";
    public static final String KEY_MESSAGES = "pref_messages";
    public static final String KEY_DEPTH = "pref_depth";
    public static final String KEY_GLITCH = "pref_glitch";
    public static final String KEY_TOUCH = "pref_touch";

    public static final String THEME_CLASSIC = "classic";
    public static final String THEME_CYAN = "cyan";
    public static final String THEME_CRIMSON = "crimson";
    public static final String THEME_AMBER = "amber";
    public static final String THEME_VIOLET = "violet";
    public static final String THEME_MONOCHROME = "monochrome";

    public static final int DEFAULT_SPEED_PROGRESS = 70;
    public static final int DEFAULT_DENSITY_PROGRESS = 50;
    public static final String DEFAULT_THEME = THEME_CLASSIC;

    /**
     * Color palette configuration for Matrix Rain themes.
     */
    public static class Palette {
        public final String name;
        public final int textColor;
        public final int bgColor;
        public final int headColor;
        public final int glowColor;
        public final int virusColor;
        public final int goldColor;

        public Palette(String name, int text, int bg, int head, int glow, int virus, int gold) {
            this.name = name;
            this.textColor = text;
            this.bgColor = bg;
            this.headColor = head;
            this.glowColor = glow;
            this.virusColor = virus;
            this.goldColor = gold;
        }
    }

    public static Palette getPalette(String theme) {
        if (theme == null) {
            theme = THEME_CLASSIC;
        }
        switch (theme) {
            case THEME_CYAN:
                return new Palette(THEME_CYAN, 0xFF00E5FF, 0xFF003344, 0xFFEEFFFF, 0xFF00E5FF, 0xFFFF0055, 0xFFFFD700);
            case THEME_CRIMSON:
                return new Palette(THEME_CRIMSON, 0xFFFF3344, 0xFF440008, 0xFFFFEEF0, 0xFFFF3344, 0xFF00FF66, 0xFFFFD700);
            case THEME_AMBER:
                return new Palette(THEME_AMBER, 0xFFFFB700, 0xFF442A00, 0xFFFFFBEA, 0xFFFFB700, 0xFFFF0055, 0xFF00FF66);
            case THEME_VIOLET:
                return new Palette(THEME_VIOLET, 0xFFD500F9, 0xFF35003E, 0xFFFAE6FF, 0xFFD500F9, 0xFF00E5FF, 0xFFFFD700);
            case THEME_MONOCHROME:
                return new Palette(THEME_MONOCHROME, 0xFFE0E0E0, 0xFF333333, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFF3344, 0xFFFFD700);
            case THEME_CLASSIC:
            default:
                return new Palette(THEME_CLASSIC, 0xFF00FF00, 0xFF004400, 0xFFEEFFEE, 0xFF00FF00, 0xFFFF0000, 0xFFFFD700);
        }
    }

    public static int calculateDrawDelay(int speedProgress) {
        int clamped = Math.max(0, Math.min(100, speedProgress));
        return 36 - (clamped * 20) / 100; // 36ms (~28fps) at 0%, 22ms (~45fps) at 70%, 16ms (~60fps) at 100%
    }

    public static int calculateFontSize(int glyphSizeProgress) {
        int clamped = Math.max(0, Math.min(100, glyphSizeProgress));
        return 28 + (clamped * 28) / 100; // 28 at 0% (~39px), 42 at 50% (~59px), 56 at 100% (~78px)
    }

    private int drawDelay = calculateDrawDelay(DEFAULT_SPEED_PROGRESS); // Default: 22ms (70%)
    private int fontSize = calculateFontSize(DEFAULT_DENSITY_PROGRESS); // Default: 42
    private String currentTheme = DEFAULT_THEME;
    private boolean themeChanged = false;

    private boolean virusEnabled = true;
    private boolean goldEnabled = true;
    private boolean messagesEnabled = true;
    private boolean depthEnabled = true;
    private boolean glitchEnabled = true;
    private boolean touchEnabled = true;

    public RainPreferences() {
    }

    /**
     * Loads preferences from SharedPreferences.
     *
     * @param prefs SharedPreferences to read from.
     * @return true if fontSize changed, requiring a matrix re-layout.
     */
    public boolean load(SharedPreferences prefs) {
        if (prefs == null) {
            return false;
        }

        int speedProgress = prefs.getInt(KEY_SPEED, DEFAULT_SPEED_PROGRESS);
        drawDelay = calculateDrawDelay(speedProgress);

        int densityProgress = prefs.getInt(KEY_DENSITY, DEFAULT_DENSITY_PROGRESS);
        int newFontSize = calculateFontSize(densityProgress);
        boolean fontChanged = (newFontSize != fontSize);
        fontSize = newFontSize;

        String newTheme = prefs.getString(KEY_THEME, DEFAULT_THEME);
        themeChanged = (newTheme != null && !newTheme.equals(currentTheme));
        if (newTheme != null) {
            currentTheme = newTheme;
        }

        virusEnabled = prefs.getBoolean(KEY_VIRUS, true);
        goldEnabled = prefs.getBoolean(KEY_GOLD, true);
        messagesEnabled = prefs.getBoolean(KEY_MESSAGES, true);
        depthEnabled = prefs.getBoolean(KEY_DEPTH, true);
        glitchEnabled = prefs.getBoolean(KEY_GLITCH, true);
        touchEnabled = prefs.getBoolean(KEY_TOUCH, true);

        return fontChanged;
    }

    public String getCurrentTheme() {
        return currentTheme;
    }

    public void setCurrentTheme(String theme) {
        if (theme != null && !theme.equals(this.currentTheme)) {
            this.currentTheme = theme;
            this.themeChanged = true;
        }
    }

    public Palette getPalette() {
        return getPalette(currentTheme);
    }

    public boolean isThemeChanged() {
        return themeChanged;
    }

    public int getDrawDelay() {
        return drawDelay;
    }

    public void setDrawDelay(int drawDelay) {
        this.drawDelay = drawDelay;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public boolean isVirusEnabled() {
        return virusEnabled;
    }

    public void setVirusEnabled(boolean virusEnabled) {
        this.virusEnabled = virusEnabled;
    }

    public boolean isGoldEnabled() {
        return goldEnabled;
    }

    public void setGoldEnabled(boolean goldEnabled) {
        this.goldEnabled = goldEnabled;
    }

    public boolean isMessagesEnabled() {
        return messagesEnabled;
    }

    public void setMessagesEnabled(boolean messagesEnabled) {
        this.messagesEnabled = messagesEnabled;
    }

    public boolean isDepthEnabled() {
        return depthEnabled;
    }

    public void setDepthEnabled(boolean depthEnabled) {
        this.depthEnabled = depthEnabled;
    }

    public boolean isGlitchEnabled() {
        return glitchEnabled;
    }

    public void setGlitchEnabled(boolean glitchEnabled) {
        this.glitchEnabled = glitchEnabled;
    }

    public boolean isTouchEnabled() {
        return touchEnabled;
    }

    public void setTouchEnabled(boolean touchEnabled) {
        this.touchEnabled = touchEnabled;
    }
}
