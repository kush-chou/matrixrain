package app.matrixrain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for RainPreferences conversion formulas, default values, and theme palettes.
 */
public class RainPreferencesTest {

    @Test
    public void testCalculateDrawDelay() {
        // Lower bounds and negative inputs clamped to 0 -> 75ms
        assertEquals("Draw delay for -50% should be clamped to 75ms", 75, RainPreferences.calculateDrawDelay(-50));
        assertEquals("Draw delay for -1% should be clamped to 75ms", 75, RainPreferences.calculateDrawDelay(-1));

        // Progress milestones: 0, 50, 70, 100
        assertEquals("Draw delay at 0% should be 75ms (slow trickle)", 75, RainPreferences.calculateDrawDelay(0));
        assertEquals("Draw delay at 50% should be 45ms", 45, RainPreferences.calculateDrawDelay(50));
        assertEquals("Draw delay at 70% should be 33ms (default cascade)", 33, RainPreferences.calculateDrawDelay(70));
        assertEquals("Draw delay at 100% should be 15ms (blazing fast cascade)", 15, RainPreferences.calculateDrawDelay(100));

        // Upper bounds > 100 clamped to 100 -> 15ms
        assertEquals("Draw delay for 101% should be clamped to 15ms", 15, RainPreferences.calculateDrawDelay(101));
        assertEquals("Draw delay for 200% should be clamped to 15ms", 15, RainPreferences.calculateDrawDelay(200));
    }

    @Test
    public void testCalculateFontSize() {
        // Lower bounds and negative inputs clamped to 0 -> 28
        assertEquals("Font size for -50% should be clamped to 28", 28, RainPreferences.calculateFontSize(-50));
        assertEquals("Font size for -1% should be clamped to 28", 28, RainPreferences.calculateFontSize(-1));

        // Progress milestones: 0, 50, 70, 100
        assertEquals("Font size at 0% should be 28", 28, RainPreferences.calculateFontSize(0));
        assertEquals("Font size at 50% should be 42", 42, RainPreferences.calculateFontSize(50));
        assertEquals("Font size at 70% should be 47", 47, RainPreferences.calculateFontSize(70));
        assertEquals("Font size at 100% should be 56", 56, RainPreferences.calculateFontSize(100));

        // Upper bounds > 100 clamped to 100 -> 56
        assertEquals("Font size for 101% should be clamped to 56", 56, RainPreferences.calculateFontSize(101));
        assertEquals("Font size for 200% should be clamped to 56", 56, RainPreferences.calculateFontSize(200));
    }

    @Test
    public void testDefaultPreferencesInstance() {
        RainPreferences prefs = new RainPreferences();

        assertEquals(RainPreferences.DEFAULT_THEME, prefs.getCurrentTheme());
        assertEquals(33, prefs.getDrawDelay());
        assertEquals(42, prefs.getFontSize());
        assertFalse(prefs.isThemeChanged());

        assertTrue(prefs.isVirusEnabled());
        assertTrue(prefs.isGoldEnabled());
        assertTrue(prefs.isMessagesEnabled());
        assertTrue(prefs.isDepthEnabled());
        assertTrue(prefs.isGlitchEnabled());
        assertTrue(prefs.isTouchEnabled());
    }

    @Test
    public void testSettersAndGetters() {
        RainPreferences prefs = new RainPreferences();

        prefs.setDrawDelay(50);
        assertEquals(50, prefs.getDrawDelay());

        prefs.setFontSize(30);
        assertEquals(30, prefs.getFontSize());

        prefs.setCurrentTheme(RainPreferences.THEME_CYAN);
        assertEquals(RainPreferences.THEME_CYAN, prefs.getCurrentTheme());
        assertTrue(prefs.isThemeChanged());

        prefs.setVirusEnabled(false);
        assertFalse(prefs.isVirusEnabled());

        prefs.setGoldEnabled(false);
        assertFalse(prefs.isGoldEnabled());

        prefs.setMessagesEnabled(false);
        assertFalse(prefs.isMessagesEnabled());

        prefs.setDepthEnabled(false);
        assertFalse(prefs.isDepthEnabled());

        prefs.setGlitchEnabled(false);
        assertFalse(prefs.isGlitchEnabled());

        prefs.setTouchEnabled(false);
        assertFalse(prefs.isTouchEnabled());
    }

    @Test
    public void testThemePalettes() {
        String[] themes = {
                RainPreferences.THEME_CLASSIC,
                RainPreferences.THEME_CYAN,
                RainPreferences.THEME_CRIMSON,
                RainPreferences.THEME_AMBER,
                RainPreferences.THEME_VIOLET,
                RainPreferences.THEME_MONOCHROME
        };

        for (String theme : themes) {
            RainPreferences.Palette palette = RainPreferences.getPalette(theme);
            assertNotNull("Palette for " + theme + " must not be null", palette);
            assertEquals(theme, palette.name);
        }

        // Null and unknown fall back to CLASSIC
        RainPreferences.Palette nullPalette = RainPreferences.getPalette(null);
        assertNotNull(nullPalette);
        assertEquals(RainPreferences.THEME_CLASSIC, nullPalette.name);

        RainPreferences.Palette unknownPalette = RainPreferences.getPalette("unknown_theme");
        assertNotNull(unknownPalette);
        assertEquals(RainPreferences.THEME_CLASSIC, unknownPalette.name);
    }
}
