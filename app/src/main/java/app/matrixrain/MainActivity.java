package app.matrixrain;

import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class MainActivity extends AppCompatActivity {

    private MatrixRainView matrixRainView;
    private Button setWallpaperButton;

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

        setContentView(R.layout.activity_main);

        matrixRainView = findViewById(R.id.matrix_rain_view);
        setWallpaperButton = findViewById(R.id.set_wallpaper_button);

        // Safe area insets for foreground content
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_content_container), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            float density = getResources().getDisplayMetrics().density;
            int horizontalPadding = (int) (24 * density);
            int topPadding = systemBars.top + (int) (16 * density);
            int bottomPadding = systemBars.bottom + (int) (20 * density);
            view.setPadding(systemBars.left + horizontalPadding, topPadding, systemBars.right + horizontalPadding, bottomPadding);
            return insets;
        });

        // Set Live Wallpaper button
        setWallpaperButton.setOnClickListener(v -> {
            if (isWallpaperActive()) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            } else {
                launchWallpaperPicker();
            }
        });

        setWallpaperButton.setOnLongClickListener(v -> {
            launchWallpaperPicker();
            return true;
        });

        // Settings Activity
        findViewById(R.id.settings_button).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });
    }

    private boolean isWallpaperActive() {
        try {
            WallpaperManager wm = WallpaperManager.getInstance(this);
            WallpaperInfo info = wm.getWallpaperInfo();
            return info != null && getPackageName().equals(info.getPackageName());
        } catch (Exception e) {
            return false;
        }
    }

    private void launchWallpaperPicker() {
        Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(MainActivity.this, MatrixRainWallpaperService.class));
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (matrixRainView != null) {
            matrixRainView.reloadPreferences();
            matrixRainView.startAnimation();
        }
        updateHeaderTheme();
        updateWallpaperButtonState();
    }

    private void updateWallpaperButtonState() {
        if (setWallpaperButton != null) {
            if (isWallpaperActive()) {
                setWallpaperButton.setText("✓  Wallpaper Active");
            } else {
                setWallpaperButton.setText("⚡  Set Live Wallpaper");
            }
        }
    }

    private void updateHeaderTheme() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = sp.getString(RainPreferences.KEY_THEME, RainPreferences.THEME_CLASSIC);
        RainPreferences.Palette palette = RainPreferences.getPalette(theme);
        TextView headerPrompt = findViewById(R.id.header_prompt);
        if (headerPrompt != null) {
            headerPrompt.setTextColor(palette.textColor);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (matrixRainView != null) {
            matrixRainView.stopAnimation();
        }
    }
}
