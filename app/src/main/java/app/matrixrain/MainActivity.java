package app.matrixrain;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class MainActivity extends AppCompatActivity {

    private MatrixRainView matrixRainView;

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

        // Safe area insets for foreground content
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_content_container), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            float density = getResources().getDisplayMetrics().density;
            int horizontalPadding = (int) (24 * density);
            int topPadding = systemBars.top + (int) (16 * density);
            int bottomPadding = systemBars.bottom + (int) (20 * density);
            view.setPadding(systemBars.left + horizontalPadding, topPadding, systemBars.right + horizontalPadding, bottomPadding);
            return insets;
        });

        // Set Live Wallpaper
        findViewById(R.id.set_wallpaper_button).setOnClickListener(v -> {
            Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    new ComponentName(MainActivity.this, MatrixRainWallpaperService.class));
            startActivity(intent);
        });

        // Settings Activity
        findViewById(R.id.settings_button).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (matrixRainView != null) {
            matrixRainView.reloadPreferences();
            matrixRainView.startAnimation();
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
