package app.matrixrain.billing;

import android.app.Activity;

/**
 * Universal interface for checking and managing Pro features.
 * Separates Google Play In-App Billing from F-Droid FOSS implementation.
 */
public interface BillingManager {
    interface BillingCallback {
        void onProStatusChanged(boolean isPro);
        void onBillingError(String message);
    }

    void initialize(Activity activity, BillingCallback callback);
    boolean isProUnlocked();
    void launchPurchaseFlow(Activity activity);
    String getDistributionName();
    void destroy();
}
