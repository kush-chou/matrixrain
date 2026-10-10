package com.example.matrixrain;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View mainRoot = findViewById(R.id.main_root);
        if (mainRoot != null) {
            final int basePadding = (int) (16 * getResources().getDisplayMetrics().density);
            ViewCompat.setOnApplyWindowInsetsListener(mainRoot, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(
                    basePadding + insets.left,
                    basePadding + insets.top,
                    basePadding + insets.right,
                    basePadding + insets.bottom
                );
                return windowInsets;
            });
        }

        Button setWallpaperButton = findViewById(R.id.set_wallpaper_button);
        setWallpaperButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
                intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        new ComponentName(MainActivity.this, MatrixRainWallpaperService.class));
                startActivity(intent);
            }
        });

        // Add a Settings button dynamically or I should update the layout.
        // Updating layout is cleaner.
        Button settingsButton = new Button(this);
        settingsButton.setText("Settings");
        settingsButton.setBackgroundColor(0xFF00FF00); // Green
        settingsButton.setTextColor(0xFF000000); // Black
        
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.addRule(RelativeLayout.BELOW, R.id.set_wallpaper_button);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.topMargin = 50;
        
        ((RelativeLayout) findViewById(R.id.set_wallpaper_button).getParent()).addView(settingsButton, params);
        
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
    }
}
