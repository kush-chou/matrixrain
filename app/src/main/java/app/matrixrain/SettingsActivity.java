package app.matrixrain;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.SeekBar;
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

        btnApply.setOnClickListener(v -> {
            saveSettings();
            finish();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSettings();
    }

    private void saveSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

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
