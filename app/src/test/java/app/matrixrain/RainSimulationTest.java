package app.matrixrain;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for RainSimulation running on pure JVM.
 */
public class RainSimulationTest {

    private RainSimulation simulation;
    private RainPreferences preferences;

    @Before
    public void setUp() {
        simulation = new RainSimulation();
        preferences = new RainPreferences();
    }

    @Test
    public void testInitialization() {
        int columns = 10;
        int rows = 20;
        simulation.resize(columns, rows, preferences);

        assertEquals("Column count must match requested size", columns, simulation.getColumnSize());
        assertEquals("Height rows must match requested size", rows, simulation.getHeightRows());

        for (int i = 0; i < columns; i++) {
            int dropY = simulation.getDropY(i);
            assertTrue("Drop Y must be within [0, rows): got " + dropY, dropY >= 0 && dropY < rows);

            int speed = simulation.getSpeed(i);
            assertTrue("Speed must be positive: got " + speed, speed >= 1);

            int frameCount = simulation.getFrameCount(i);
            assertTrue("Initial frameCount must equal speed: got " + frameCount, frameCount == speed);

            int type = simulation.getColumnType(i);
            assertTrue("Column type must be between 0 and 3: got " + type, type >= 0 && type <= 3);

            char c = simulation.getPreviousChar(i);
            assertFalse("Initial character should not be null char", c == '\0');
        }
    }

    @Test
    public void testStepAdvances() {
        int columns = 15;
        int rows = 25;
        simulation.resize(columns, rows, preferences);

        // Run multiple simulation steps and verify array bounds and continuous progression
        boolean anyColumnStepped = false;
        int steps = 100;

        for (int step = 0; step < steps; step++) {
            simulation.step(preferences);

            for (int i = 0; i < columns; i++) {
                int dropY = simulation.getDropY(i);
                assertTrue("Drop Y must be non-negative: got " + dropY, dropY >= 0);

                int type = simulation.getColumnType(i);
                assertTrue("Column type must be valid: got " + type, type >= 0 && type <= 3);

                char c = simulation.getPreviousChar(i);
                assertFalse("Previous char must not be null char", c == '\0');

                if (simulation.hasStepped(i)) {
                    anyColumnStepped = true;
                    char trail = simulation.getTrailChar(i);
                    assertFalse("Trail char must not be null char", trail == '\0');
                }
            }
        }

        assertTrue("Over " + steps + " steps, columns should have stepped and advanced", anyColumnStepped);
    }

    @Test
    public void testTouchDeflection() {
        int columns = 10;
        int rows = 20;
        simulation.resize(columns, rows, preferences);

        // Let simulation advance first so dropY > 0
        for (int i = 0; i < 20; i++) {
            simulation.step(preferences);
        }

        int targetCol = 3;
        simulation.deflect(targetCol, false);

        assertEquals("Deflected column dropY must reset to top (0)", 0, simulation.getDropY(targetCol));
        assertEquals("Deflected column speed must be 1", 1, simulation.getSpeed(targetCol));
        assertEquals("Deflected column frameCount must be 0", 0, simulation.getFrameCount(targetCol));
        assertEquals("Deflected column with enableGold=false must be normal type (0)", 0, simulation.getColumnType(targetCol));

        // Test boundary conditions: negative index or out of bounds index should not throw
        simulation.deflect(-1, false);
        simulation.deflect(100, false);
    }

    @Test
    public void testKatakanaCharGeneration() {
        int katakanaCount = 0;
        int digitCount = 0;
        int asciiCount = 0;
        int totalIterations = 10000;

        for (int i = 0; i < totalIterations; i++) {
            char c = simulation.getRandomMatrixChar();

            boolean isKatakana = (c >= 0xFF66 && c <= 0xFF9D);
            boolean isDigit = (c >= '0' && c <= '9');
            boolean isAscii = (c >= 0x21 && c <= 0x7A);

            assertTrue("Character must fall in Katakana, Digits, or ASCII range: code " + (int) c,
                    isKatakana || isDigit || isAscii);

            if (isKatakana) katakanaCount++;
            else if (isDigit) digitCount++;
            else if (isAscii) asciiCount++;
        }

        assertTrue("Should generate Katakana characters (expected ~60%): got " + katakanaCount, katakanaCount > 4000);
        assertTrue("Should generate digits (expected ~25%): got " + digitCount, digitCount > 1000);
        assertTrue("Should generate ASCII symbols (expected ~15%): got " + asciiCount, asciiCount > 500);
    }

    @Test
    public void testPreferencesDefaults() {
        RainPreferences prefs = new RainPreferences();
        assertEquals("Default drawDelay should be 22ms", 22, prefs.getDrawDelay());
        assertEquals("Calculated drawDelay for default should be 22ms",
                RainPreferences.calculateDrawDelay(RainPreferences.DEFAULT_SPEED_PROGRESS), prefs.getDrawDelay());
        assertEquals("Draw delay at 0% should be 36ms", 36, RainPreferences.calculateDrawDelay(0));
        assertEquals("Draw delay at 100% should be 16ms", 16, RainPreferences.calculateDrawDelay(100));
        assertTrue("Draw delay should never exceed 36ms", RainPreferences.calculateDrawDelay(-10) <= 36);
        assertEquals("Default fontSize should be 42", 42, prefs.getFontSize());
        assertEquals("Font size at 0% should be 28", 28, RainPreferences.calculateFontSize(0));
        assertEquals("Font size at 100% should be 56", 56, RainPreferences.calculateFontSize(100));
        assertTrue("Virus should be enabled by default", prefs.isVirusEnabled());
        assertTrue("Gold should be enabled by default", prefs.isGoldEnabled());
        assertTrue("Messages should be enabled by default", prefs.isMessagesEnabled());
        assertTrue("Depth should be enabled by default", prefs.isDepthEnabled());
        assertTrue("Glitch should be enabled by default", prefs.isGlitchEnabled());
        assertTrue("Touch should be enabled by default", prefs.isTouchEnabled());
    }

    @Test
    public void testHiddenWordsConstants() {
        assertNotNull(RainSimulation.HIDDEN_WORDS);
        assertTrue(RainSimulation.HIDDEN_WORDS.length >= 5);
        Set<String> words = new HashSet<>();
        for (String w : RainSimulation.HIDDEN_WORDS) {
            words.add(w);
        }
        assertTrue(words.contains("MATRIX"));
        assertTrue(words.contains("NEO"));
    }

    @Test
    public void testResizeZeroOrNegativeDimensions() {
        simulation.resize(0, 0, preferences);
        assertEquals(0, simulation.getColumnSize());
        assertEquals(0, simulation.getHeightRows());

        simulation.resize(-5, -10, preferences);
        assertEquals(0, simulation.getColumnSize());
        assertEquals(0, simulation.getHeightRows());

        // Stepping empty simulation should not throw
        simulation.step(preferences);
    }
}
