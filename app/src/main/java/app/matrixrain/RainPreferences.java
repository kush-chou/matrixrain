package app.matrixrain;

import android.content.SharedPreferences;

/**
 * Encapsulates preferences decoding, conversion formulas, and default values.
 */
public class RainPreferences {

    public static final String KEY_SPEED = "pref_speed_progress";
    public static final String KEY_DENSITY = "pref_density_progress";
    public static final String KEY_VIRUS = "pref_virus";
    public static final String KEY_GOLD = "pref_gold";
    public static final String KEY_MESSAGES = "pref_messages";
    public static final String KEY_DEPTH = "pref_depth";
    public static final String KEY_GLITCH = "pref_glitch";
    public static final String KEY_TOUCH = "pref_touch";

    public static final int DEFAULT_SPEED_PROGRESS = 70;
    public static final int DEFAULT_DENSITY_PROGRESS = 80;

    private int drawDelay = 110 - DEFAULT_SPEED_PROGRESS; // Default: 40ms
    private int fontSize = 10 + (100 - DEFAULT_DENSITY_PROGRESS) / 2; // Default: 20

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
        drawDelay = 110 - speedProgress;

        int densityProgress = prefs.getInt(KEY_DENSITY, DEFAULT_DENSITY_PROGRESS);
        int newFontSize = 10 + (100 - densityProgress) / 2;
        boolean fontChanged = (newFontSize != fontSize);
        fontSize = newFontSize;

        virusEnabled = prefs.getBoolean(KEY_VIRUS, true);
        goldEnabled = prefs.getBoolean(KEY_GOLD, true);
        messagesEnabled = prefs.getBoolean(KEY_MESSAGES, true);
        depthEnabled = prefs.getBoolean(KEY_DEPTH, true);
        glitchEnabled = prefs.getBoolean(KEY_GLITCH, true);
        touchEnabled = prefs.getBoolean(KEY_TOUCH, true);

        return fontChanged;
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
