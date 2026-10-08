package com.example.matrixrain.billing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;

/**
 * F-Droid / FOSS Implementation of BillingManager.
 * Zero proprietary dependencies.
 * All Pro features are unlocked by default for FOSS users.
 */
public class BillingManagerImpl implements BillingManager {

    private BillingCallback callback;

    @Override
    public void initialize(Activity activity, BillingCallback callback) {
        this.callback = callback;
        if (callback != null) {
            callback.onProStatusChanged(true);
        }
    }

    @Override
    public boolean isProUnlocked() {
        return true; // 100% unlocked in F-Droid FOSS flavor
    }

    @Override
    public void launchPurchaseFlow(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle("Matrix Rain FOSS Edition")
                .setMessage("All features are already 100% unlocked in this Free and Open Source edition!\n\nIf you enjoy this app and would like to support ongoing development, consider sponsoring on GitHub.")
                .setPositiveButton("Support on GitHub", (dialog, which) -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/kush-chou/neocoderain"));
                    activity.startActivity(browserIntent);
                })
                .setNegativeButton("Close", null)
                .show();
    }

    @Override
    public String getDistributionName() {
        return "F-Droid (FOSS)";
    }

    @Override
    public void destroy() {
        callback = null;
    }
}
