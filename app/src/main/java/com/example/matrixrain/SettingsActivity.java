package com.example.matrixrain;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
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
        
        // Density: Lower value = Larger text = Less dense. 
        // We want SeekBar to show "Density", so High Progress = Small Text.
        // Let's map SeekBar 1-100. 
        // Default fontSize was 16. 
        // Let's say min size 10 (Dense), max size 60 (Sparse).
        // Saved value is fontSize.
        int currentFontSize = prefs.getInt("pref_density", 16);
        // If font is small (10), progress should be high.
        // If font is large (60), progress should be low.
        // Let's simplify: Save the SeekBar progress directly, convert to font size in Service.
        // SeekBar: 0 (Sparse) to 100 (Dense).
        int savedDensityProgress = prefs.getInt("pref_density_progress", 80); 
        seekDensity.setProgress(savedDensityProgress);

        // Speed: 10ms delay (Fast) to 100ms delay (Slow).
        // SeekBar: 0 (Slow) to 100 (Fast).
        int savedSpeedProgress = prefs.getInt("pref_speed_progress", 70);
        seekSpeed.setProgress(savedSpeedProgress);

        switchVirus.setChecked(prefs.getBoolean("pref_virus", true));
        switchGold.setChecked(prefs.getBoolean("pref_gold", true));
        switchMessages.setChecked(prefs.getBoolean("pref_messages", true));
        switchDepth.setChecked(prefs.getBoolean("pref_depth", true));
        switchGlitch.setChecked(prefs.getBoolean("pref_glitch", true));
        switchTouch.setChecked(prefs.getBoolean("pref_touch", true));

        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
                finish();
            }
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
