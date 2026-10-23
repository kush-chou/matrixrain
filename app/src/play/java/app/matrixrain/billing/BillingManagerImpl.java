package app.matrixrain.billing;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import java.util.ArrayList;
import java.util.List;

/**
 * Google Play Store Implementation of BillingManager.
 * Manages Google Play In-App Purchases for Pro customizations.
 */
public class BillingManagerImpl implements BillingManager, PurchasesUpdatedListener {

    private static final String TAG = "BillingManagerPlay";
    public static final String SKU_PRO = "matrix_pro_lifetime";
    private static final String PREF_IS_PRO = "pref_is_pro_purchased";

    private BillingClient billingClient;
    private BillingCallback callback;
    private Context context;
    private ProductDetails proProductDetails;
    private boolean isPro = false;

    @Override
    public void initialize(Activity activity, BillingCallback callback) {
        this.context = activity.getApplicationContext();
        this.callback = callback;

        // Load cached Pro status for instant offline startup
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        isPro = prefs.getBoolean(PREF_IS_PRO, false);
        if (callback != null) {
            callback.onProStatusChanged(isPro);
        }

        billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases()
                .build();

        startConnection();
    }

    private void startConnection() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing setup successful. Querying purchases and products...");
                    queryPurchases();
                    queryProductDetails();
                } else {
                    Log.w(TAG, "Billing setup failed: " + billingResult.getDebugMessage());
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected.");
            }
        });
    }

    private void queryPurchases() {
        QueryPurchasesParams params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build();

        billingClient.queryPurchasesAsync(params, (billingResult, purchases) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                boolean foundPro = false;
                for (Purchase purchase : purchases) {
                    if (purchase.getProducts().contains(SKU_PRO) &&
                            purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                        foundPro = true;
                        handlePurchase(purchase);
                    }
                }
                updateProStatus(foundPro);
            }
        });
    }

    private void queryProductDetails() {
        List<QueryProductDetailsParams.Product> productList = new ArrayList<>();
        productList.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SKU_PRO)
                .setProductType(BillingClient.ProductType.INAPP)
                .build());

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, productDetailsList) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && !productDetailsList.isEmpty()) {
                proProductDetails = productDetailsList.get(0);
                Log.d(TAG, "Product details loaded for: " + proProductDetails.getName());
            }
        });
    }

    @Override
    public boolean isProUnlocked() {
        return isPro;
    }

    @Override
    public void launchPurchaseFlow(Activity activity) {
        if (isPro) {
            Toast.makeText(activity, "Pro features are already unlocked!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (proProductDetails == null) {
            Toast.makeText(activity, "Connecting to Google Play... please retry in a moment.", Toast.LENGTH_SHORT).show();
            queryProductDetails();
            return;
        }

        List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = new ArrayList<>();
        productDetailsParamsList.add(BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(proProductDetails)
                .build());

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();

        billingClient.launchBillingFlow(activity, billingFlowParams);
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
            updateProStatus(true);
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User canceled purchase flow");
        } else {
            if (callback != null) {
                callback.onBillingError(billingResult.getDebugMessage());
            }
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (!purchase.isAcknowledged()) {
            AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build();
            billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
                Log.d(TAG, "Purchase acknowledged: " + billingResult.getResponseCode());
            });
        }
    }

    private void updateProStatus(boolean pro) {
        this.isPro = pro;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(PREF_IS_PRO, pro).apply();

        new Handler(Looper.getMainLooper()).post(() -> {
            if (callback != null) {
                callback.onProStatusChanged(pro);
            }
        });
    }

    @Override
    public String getDistributionName() {
        return "Google Play";
    }

    @Override
    public void destroy() {
        if (billingClient != null && billingClient.isReady()) {
            billingClient.endConnection();
        }
        callback = null;
    }
}
