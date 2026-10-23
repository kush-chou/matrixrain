package app.matrixrain;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import app.matrixrain.billing.BillingManager;
import app.matrixrain.billing.BillingManagerImpl;

public class SettingsActivity extends Activity {

    private SeekBar seekDensity, seekSpeed;
    private Switch switchVirus, switchGold, switchMessages, switchDepth, switchGlitch, switchTouch;
    private TextView textEditionBadge;
    private LinearLayout bannerProUpgrade;
    private Button btnUnlockPro;

    private BillingManager billingManager;
    private boolean isPro = false;

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
        textEditionBadge = findViewById(R.id.text_edition_badge);
        bannerProUpgrade = findViewById(R.id.banner_pro_upgrade);
        btnUnlockPro = findViewById(R.id.btn_unlock_pro);
        Button btnApply = findViewById(R.id.btn_apply);

        // Initialize Billing Manager (abstracted across play and fdroid flavors)
        billingManager = new BillingManagerImpl();
        billingManager.initialize(this, new BillingManager.BillingCallback() {
            @Override
            public void onProStatusChanged(boolean proStatus) {
                isPro = proStatus;
                updateProUI();
            }

            @Override
            public void onBillingError(String message) {
                Toast.makeText(SettingsActivity.this, "Billing error: " + message, Toast.LENGTH_SHORT).show();
            }
        });

        // Set distribution badge
        textEditionBadge.setText(billingManager.getDistributionName() + " Edition");
        textEditionBadge.setOnClickListener(v -> billingManager.launchPurchaseFlow(SettingsActivity.this));

        btnUnlockPro.setOnClickListener(v -> billingManager.launchPurchaseFlow(SettingsActivity.this));

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

        // Guard Pro switches on Google Play Free tier
        attachProGuard(switchVirus, "Red Virus Streams");
        attachProGuard(switchGold, "Gold Neo Drops");
        attachProGuard(switchMessages, "Hidden Messages");
        attachProGuard(switchTouch, "Touch Shockwave Ripples");

        updateProUI();

        btnApply.setOnClickListener(v -> {
            saveSettings();
            finish();
        });
    }

    private void attachProGuard(Switch switchWidget, String featureName) {
        switchWidget.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !isPro) {
                buttonView.setChecked(false);
                Toast.makeText(this, featureName + " is a Matrix Pro feature.", Toast.LENGTH_SHORT).show();
                billingManager.launchPurchaseFlow(this);
            }
        });
    }

    private void updateProUI() {
        if (isPro) {
            bannerProUpgrade.setVisibility(View.GONE);
            switchVirus.setEnabled(true);
            switchGold.setEnabled(true);
            switchMessages.setEnabled(true);
            switchTouch.setEnabled(true);
        } else {
            bannerProUpgrade.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSettings();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billingManager != null) {
            billingManager.destroy();
        }
    }

    private void saveSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt("pref_density_progress", seekDensity.getProgress());
        editor.putInt("pref_speed_progress", seekSpeed.getProgress());

        editor.putBoolean("pref_virus", isPro && switchVirus.isChecked());
        editor.putBoolean("pref_gold", isPro && switchGold.isChecked());
        editor.putBoolean("pref_messages", isPro && switchMessages.isChecked());
        editor.putBoolean("pref_depth", switchDepth.isChecked());
        editor.putBoolean("pref_glitch", switchGlitch.isChecked());
        editor.putBoolean("pref_touch", isPro && switchTouch.isChecked());

        editor.apply();
        Toast.makeText(this, "Settings Saved", Toast.LENGTH_SHORT).show();
    }
}
