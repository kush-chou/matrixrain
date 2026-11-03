package app.matrixrain;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class SettingsActivity extends AppCompatActivity {

    private SeekBar seekDensity, seekSpeed;
    private SwitchCompat switchVirus, switchGold, switchMessages, switchDepth, switchGlitch, switchTouch;
    private String selectedTheme = RainPreferences.DEFAULT_THEME;

    private static class ThemeDotView {
        final String themeKey;
        final FrameLayout btn;
        final View ring;
        final View dot;

        ThemeDotView(String themeKey, FrameLayout btn, View ring, View dot) {
            this.themeKey = themeKey;
            this.btn = btn;
            this.ring = ring;
            this.dot = dot;
        }
    }

    private ThemeDotView[] themeViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Edge-to-edge pitch-black window
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (insetsController != null) {
            insetsController.setAppearanceLightStatusBars(false);
            insetsController.setAppearanceLightNavigationBars(false);
        }

        setContentView(R.layout.activity_settings);

        // Apply safe status bar and navigation bar insets to content container
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings_content_container), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            float density = getResources().getDisplayMetrics().density;
            int horizontalPadding = (int) (24 * density);
            int topPadding = systemBars.top + (int) (16 * density);
            int bottomPadding = systemBars.bottom + (int) (24 * density);
            view.setPadding(systemBars.left + horizontalPadding, topPadding, systemBars.right + horizontalPadding, bottomPadding);
            return insets;
        });

        seekDensity = findViewById(R.id.seekbar_density);
        seekSpeed = findViewById(R.id.seekbar_speed);
        switchVirus = findViewById(R.id.switch_virus);
        switchGold = findViewById(R.id.switch_gold);
        switchMessages = findViewById(R.id.switch_messages);
        switchDepth = findViewById(R.id.switch_depth);
        switchGlitch = findViewById(R.id.switch_glitch);
        switchTouch = findViewById(R.id.switch_touch);
        Button btnApply = findViewById(R.id.btn_apply);

        // Load current settings
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int savedDensityProgress = prefs.getInt(RainPreferences.KEY_DENSITY, RainPreferences.DEFAULT_DENSITY_PROGRESS);
        seekDensity.setProgress(savedDensityProgress);

        int savedSpeedProgress = prefs.getInt(RainPreferences.KEY_SPEED, RainPreferences.DEFAULT_SPEED_PROGRESS);
        seekSpeed.setProgress(savedSpeedProgress);

        switchVirus.setChecked(prefs.getBoolean(RainPreferences.KEY_VIRUS, true));
        switchGold.setChecked(prefs.getBoolean(RainPreferences.KEY_GOLD, true));
        switchMessages.setChecked(prefs.getBoolean(RainPreferences.KEY_MESSAGES, true));
        switchDepth.setChecked(prefs.getBoolean(RainPreferences.KEY_DEPTH, true));
        switchGlitch.setChecked(prefs.getBoolean(RainPreferences.KEY_GLITCH, true));
        switchTouch.setChecked(prefs.getBoolean(RainPreferences.KEY_TOUCH, true));

        // Setup 6-color theme dots selector
        setupThemeSelector(prefs);

        btnApply.setOnClickListener(v -> {
            saveSettings();
            finish();
        });
    }

    private void setupThemeSelector(SharedPreferences prefs) {
        selectedTheme = prefs.getString(RainPreferences.KEY_THEME, RainPreferences.DEFAULT_THEME);

        themeViews = new ThemeDotView[] {
            new ThemeDotView(RainPreferences.THEME_CLASSIC, findViewById(R.id.btn_theme_classic), findViewById(R.id.ring_theme_classic), findViewById(R.id.dot_theme_classic)),
            new ThemeDotView(RainPreferences.THEME_CYAN, findViewById(R.id.btn_theme_cyan), findViewById(R.id.ring_theme_cyan), findViewById(R.id.dot_theme_cyan)),
            new ThemeDotView(RainPreferences.THEME_CRIMSON, findViewById(R.id.btn_theme_crimson), findViewById(R.id.ring_theme_crimson), findViewById(R.id.dot_theme_crimson)),
            new ThemeDotView(RainPreferences.THEME_AMBER, findViewById(R.id.btn_theme_amber), findViewById(R.id.ring_theme_amber), findViewById(R.id.dot_theme_amber)),
            new ThemeDotView(RainPreferences.THEME_VIOLET, findViewById(R.id.btn_theme_violet), findViewById(R.id.ring_theme_violet), findViewById(R.id.dot_theme_violet)),
            new ThemeDotView(RainPreferences.THEME_MONOCHROME, findViewById(R.id.btn_theme_monochrome), findViewById(R.id.ring_theme_monochrome), findViewById(R.id.dot_theme_monochrome))
        };

        float density = getResources().getDisplayMetrics().density;
        int strokeWidth = Math.max(2, Math.round(2.5f * density));

        for (ThemeDotView item : themeViews) {
            RainPreferences.Palette palette = RainPreferences.getPalette(item.themeKey);

            // Circular inner color dot
            GradientDrawable dotDrawable = new GradientDrawable();
            dotDrawable.setShape(GradientDrawable.OVAL);
            dotDrawable.setColor(palette.textColor);
            item.dot.setBackground(dotDrawable);

            // Outer radiant glow/stroke ring
            GradientDrawable ringDrawable = new GradientDrawable();
            ringDrawable.setShape(GradientDrawable.OVAL);
            ringDrawable.setColor(Color.TRANSPARENT);
            ringDrawable.setStroke(strokeWidth, palette.glowColor);
            item.ring.setBackground(ringDrawable);

            item.btn.setOnClickListener(v -> selectTheme(item.themeKey));
        }

        updateThemeSelectionUI();
    }

    private void selectTheme(String themeKey) {
        this.selectedTheme = themeKey;
        updateThemeSelectionUI();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putString(RainPreferences.KEY_THEME, themeKey).apply();
    }

    private void updateThemeSelectionUI() {
        if (themeViews == null) return;

        for (ThemeDotView item : themeViews) {
            boolean isSelected = item.themeKey.equals(selectedTheme);
            item.ring.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);
            item.btn.setScaleX(isSelected ? 1.08f : 1.0f);
            item.btn.setScaleY(isSelected ? 1.08f : 1.0f);
        }

        RainPreferences.Palette palette = RainPreferences.getPalette(selectedTheme);
        TextView prompt = findViewById(R.id.settings_prompt);
        TextView title = findViewById(R.id.settings_title);
        if (prompt != null) {
            prompt.setTextColor(palette.textColor);
        }
        if (title != null) {
            title.setTextColor(palette.textColor);
            title.setShadowLayer(12, 0, 0, palette.glowColor);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSettings();
    }

    private void saveSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(RainPreferences.KEY_THEME, selectedTheme);
        editor.putInt(RainPreferences.KEY_DENSITY, seekDensity.getProgress());
        editor.putInt(RainPreferences.KEY_SPEED, seekSpeed.getProgress());

        editor.putBoolean(RainPreferences.KEY_VIRUS, switchVirus.isChecked());
        editor.putBoolean(RainPreferences.KEY_GOLD, switchGold.isChecked());
        editor.putBoolean(RainPreferences.KEY_MESSAGES, switchMessages.isChecked());
        editor.putBoolean(RainPreferences.KEY_DEPTH, switchDepth.isChecked());
        editor.putBoolean(RainPreferences.KEY_GLITCH, switchGlitch.isChecked());
        editor.putBoolean(RainPreferences.KEY_TOUCH, switchTouch.isChecked());

        editor.apply();
        Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show();
    }
}
