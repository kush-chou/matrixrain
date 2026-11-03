package app.matrixrain;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends Activity {

    private SeekBar seekDensity, seekSpeed;
    private Switch switchVirus, switchGold, switchMessages, switchDepth, switchGlitch, switchTouch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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
        int savedDensityProgress = prefs.getInt("pref_density_progress", 80);
        seekDensity.setProgress(savedDensityProgress);

        int savedSpeedProgress = prefs.getInt("pref_speed_progress", 70);
        seekSpeed.setProgress(savedSpeedProgress);

        switchVirus.setChecked(prefs.getBoolean("pref_virus", true));
        switchGold.setChecked(prefs.getBoolean("pref_gold", true));
        switchMessages.setChecked(prefs.getBoolean("pref_messages", true));
        switchDepth.setChecked(prefs.getBoolean("pref_depth", true));
        switchGlitch.setChecked(prefs.getBoolean("pref_glitch", true));
        switchTouch.setChecked(prefs.getBoolean("pref_touch", true));

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

        editor.putInt("pref_density_progress", seekDensity.getProgress());
        editor.putInt("pref_speed_progress", seekSpeed.getProgress());

        editor.putBoolean("pref_virus", switchVirus.isChecked());
        editor.putBoolean("pref_gold", switchGold.isChecked());
        editor.putBoolean("pref_messages", switchMessages.isChecked());
        editor.putBoolean("pref_depth", switchDepth.isChecked());
        editor.putBoolean("pref_glitch", switchGlitch.isChecked());
        editor.putBoolean("pref_touch", switchTouch.isChecked());

        editor.apply();
        Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show();
    }
}
