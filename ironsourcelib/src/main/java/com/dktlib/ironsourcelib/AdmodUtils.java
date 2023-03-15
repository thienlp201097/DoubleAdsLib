package com.dktlib.ironsourcelib;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.dktlib.ironsourcelib.utils.SweetAlert.SweetAlertDialog;
import com.dktlib.ironsourcelib.utils.admod.AdCallBackInterLoad;
import com.dktlib.ironsourcelib.utils.admod.AdCallbackNew;
import com.dktlib.ironsourcelib.utils.admod.AdsInterCallBack;
import com.dktlib.ironsourcelib.utils.admod.InterHolderAdmod;
import com.dktlib.ironsourcelib.utils.admod.NativeAdCallbackAdmod;
import com.dktlib.ironsourcelib.utils.admod.NativeHolderAdmod;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnPaidEventListener;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;
import com.dktlib.ironsourcelib.GoogleENative;

import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class AdmodUtils {
    //Dialog loading
    public SweetAlertDialog dialog;
    // Biến check lần cuối hiển thị quảng cáo
    public long lastTimeShowInterstitial = 0;
    // Timeout init admob
    ShimmerFrameLayout shimmerFrameLayout;
    public boolean isClick = false;
    public int timeOut = 0;
    //Check quảng cáo đang show hay không
    public boolean isAdShowing = false;
    //Ẩn hiện quảng cáo
    public boolean isShowAds = true;
    //Dùng ID Test để hiển thị quảng cáo
    public boolean isTesting = false;
    //List device test
    public List<String> testDevices = new ArrayList<>();
    //INSTANCE AdmodUtils
    public Dialog dialogFullScreen;
    private static volatile AdmodUtils INSTANCE;
    //Reward Ads
    public RewardedAd mRewardedAd = null;
    public InterstitialAd mInterstitialAd;

    //id thật
    public String idIntersitialReal;

    public static synchronized AdmodUtils getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AdmodUtils();
        }
        return INSTANCE;
    }

    //Hàm Khởi tạo admob
    public void initAdmob(Context context, int timeout, boolean isDebug, boolean isEnableAds) {
        timeOut = timeout;
        if (timeOut < 5000 && timeout != 0) {
            Toast.makeText(context, "Nên để limit time ~10000", Toast.LENGTH_LONG).show();
        }

        if (timeout > 0) {
            timeOut = timeout;
        } else {
            timeOut = 10000;
        }

        if (isDebug) {
            isTesting = true;
        } else {
            isTesting = false;
        }

        if (!isEnableAds) {
            isShowAds = false;
        }

        MobileAds.initialize(context, initializationStatus -> {
        });

        initListIdTest();
        RequestConfiguration requestConfiguration
                = new RequestConfiguration.Builder()
                .setTestDeviceIds(testDevices)
                .build();
        MobileAds.setRequestConfiguration(requestConfiguration);
        initAdRequest(timeout);


    }

    public AdRequest adRequest;

    // get AdRequest
    public void initAdRequest(int timeOut) {
        adRequest = new AdRequest.Builder()
                .setHttpTimeoutMillis(timeOut)
                .build();
    }

    public void initListIdTest() {
        testDevices.add("727D4F658B63BDFA0EFB164261AAE54");
        testDevices.add("3FA34D6F6B2DCF88DED51A6AF263E3F0");
        testDevices.add("482996BF6946FBE1B9FFD3975144D084");
        testDevices.add("8619926A823916A224795141B93B7E0B");
        testDevices.add("6399D5AEE5C75205B6C0F6755365CF21");
        testDevices.add("2E379568A9F147A64B0E0C9571DE812D");
        testDevices.add("A0518C6FA4396B91F82B9656DE83AFC7");
        testDevices.add("C8EEFFC32272E3F1018FC72ECBD46F0C");
        testDevices.add("284A7F7624F1131E7341ECDCBBCDF9A8");
        testDevices.add("FEECD9793CCCE1E0FF8D392B0DB65559");
        testDevices.add("D34AE6EC4CBA619D6243B03D4E31EED6");
        testDevices.add("25F9EEACB11D46869D2854923615D839");
        testDevices.add("A5CB09DBBE486E3421502DFF53070339");
        testDevices.add("5798E06F645D797640A9C4B90B6CBEA7");
        testDevices.add("E91FD94E971864C3880FB434D1C39A03");
        testDevices.add("50ACF2DAA0884FF8B08F7C823E046DEA");
        testDevices.add("97F07D4A6D0145F9DB7114B63D3D8E9B");
        testDevices.add("4C96668EC6F204034D0CDCE1B94A4E65");
        testDevices.add("00A52C89E14694316247D3CA3DF19F6B");
        testDevices.add("C38A7BF0A80E31BD6B76AF6D0C1EE4A1");
        testDevices.add("CE604BDCEFEE2B9125CCFFC53E96022E");
        testDevices.add("39D7026016640CEA1502836C6EF3776D");
        testDevices.add("A99C99C378EE9BDE5D3DE404D3A4A812");
        testDevices.add("EB28F4CCC32F14DC98068A063B97E6CE");
        //Oneplus GM1910
        testDevices.add("D94D5042C9CC42DA75DCC0C4C233A500");
        //Redmi note 4
        testDevices.add("3FA34D6F6B2DCF88DED51A6AF263E3F0");
        //Galaxy M11
        testDevices.add("AF6ABEDE9EE7719295BF5E6F19A40452");
        //Samsung SM-G610F
        testDevices.add("2B018C52668CBA0B033F411955A5B561");
        //Realme RMX1851
        testDevices.add("39D7026016640CEA1502836C6EF3776D");
        //Redmi 5 Plus
        testDevices.add("CE604BDCEFEE2B9125CCFFC53E96022E");
        //Redmi 9A
        testDevices.add("13D67F452A299DB825A348917D52D640");
        //POCO X3
        testDevices.add("7D94825002E2407B75A9D5378194CFA9");
        //Galaxy A21S
        testDevices.add("98EFC23E56FA228C791F8C3AFBEE44D4");
        //Oppo CPH1825
        testDevices.add("805702C1D9D4FD957AFE14F3D69E79F7");
        //Xiaomi Redmi Note 7
        testDevices.add("9C62AAC36B9F23413AF4D66FE48F9E9B");

    }

    //check open network
    public boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }


    // ads native
    public void loadNativeAdsWithLayout(Activity activity, String s, ViewGroup viewGroup, int layout, GoogleENative size, NativeAdCallback adCallback) {

        View tagView;
        if (size == GoogleENative.UNIFIED_MEDIUM) {
            tagView = activity.getLayoutInflater().inflate(R.layout.layoutnative_loading_medium, null, false);
        } else {
            tagView = activity.getLayoutInflater().inflate(R.layout.layoutnative_loading_small, null, false);
        }
        viewGroup.addView(tagView, 0);
        ShimmerFrameLayout shimmerFrameLayout = tagView.findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout.startShimmer();

        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.setVisibility(View.GONE);
            return;
        }

        AdLoader adLoader;
        if (isTesting) {
            s = activity.getString(R.string.test_ads_admob_native_id);
        }

        adLoader = new AdLoader.Builder(activity, s)
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {

                    @Override
                    public void onNativeAdLoaded(@NonNull @NotNull NativeAd nativeAd) {
                        adCallback.onNativeAdLoaded();

                        NativeAdView adView = (NativeAdView) activity.getLayoutInflater()
                                .inflate(layout, null);

                        NativeFunc.Companion.populateNativeAdView(nativeAd, adView, GoogleENative.UNIFIED_MEDIUM);

                        shimmerFrameLayout.stopShimmer();
                        viewGroup.removeAllViews();
                        viewGroup.addView(adView);
                        //viewGroup.setVisibility(View.VISIBLE);
                    }

                }).withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        Log.e("Admodfail", "onAdFailedToLoad" + adError.getMessage());
                        Log.e("Admodfail", "errorCodeAds" + adError.getCause());
                        shimmerFrameLayout.stopShimmer();
                        viewGroup.removeAllViews();
                        adCallback.onAdFail();
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder().build()).build();

        if (adRequest != null) {
            adLoader.loadAd(adRequest);
        }
        Log.e("Admod", "loadAdNativeAds");
    }

    // ads native
    @SuppressLint("StaticFieldLeak")
    public void loadNativeAds(Activity activity, String s, ViewGroup viewGroup, GoogleENative size, NativeAdCallback adCallback) {
        View tagView;
        if (size == GoogleENative.UNIFIED_MEDIUM) {
            tagView = activity.getLayoutInflater().inflate(R.layout.layoutnative_loading_medium, null, false);
        } else {
            tagView = activity.getLayoutInflater().inflate(R.layout.layoutnative_loading_small, null, false);
        }
        viewGroup.addView(tagView, 0);
        ShimmerFrameLayout shimmerFrameLayout = tagView.findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout.startShimmer();
        if (!isShowAds) {
            viewGroup.setVisibility(View.GONE);
            return;
        }

        AdLoader adLoader;
        if (isTesting) {
            s = activity.getString(R.string.test_ads_admob_native_id);
        }

        adLoader = new AdLoader.Builder(activity, s)
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {

                    @Override
                    public void onNativeAdLoaded(@NonNull @NotNull NativeAd nativeAd) {
                        adCallback.onNativeAdLoaded();
                        int id = 0;
                        if (size == GoogleENative.UNIFIED_MEDIUM) {
                            id = R.layout.ad_unified_medium;
                        } else {
                            id = R.layout.ad_unified_small;
                        }

                        NativeAdView adView = (NativeAdView) activity.getLayoutInflater()
                                .inflate(id, null);

                        NativeFunc.Companion.populateNativeAdView(nativeAd, adView, size);
                        shimmerFrameLayout.stopShimmer();
                        viewGroup.removeAllViews();
                        viewGroup.addView(adView);
                        //  viewGroup.setVisibility(View.VISIBLE);
                    }

                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        Log.e("Admodfail", "onAdFailedToLoad" + adError.getMessage());
                        Log.e("Admodfail", "errorCodeAds" + adError.getCause());
                        shimmerFrameLayout.stopShimmer();
                        viewGroup.removeAllViews();
                        adCallback.onAdFail();
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder().build()).build();

        if (adRequest != null) {
            adLoader.loadAd(adRequest);
        }
        Log.e("Admod", "loadAdNativeAds");
    }

    public void loadAdBannerCollapsible(Activity activity, String bannerId, CollapsibleBanner collapsibleBannersize, ViewGroup viewGroup, BannerAdCallback callback) {
        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.setVisibility(View.GONE);
            return;
        }
        AdView mAdView = new AdView(activity);
        if (isTesting) {
            bannerId = activity.getString(R.string.test_ads_admob_banner_id);
        }
        mAdView.setAdUnitId(bannerId);
        AdSize adSize = getAdSize(activity);
        mAdView.setAdSize(adSize);
        viewGroup.removeAllViews();
        View tagView = activity.getLayoutInflater().inflate(R.layout.banner_shimmer_layout, null, false);
        viewGroup.addView(tagView, 0);
        viewGroup.addView(mAdView, 1);
        ShimmerFrameLayout shimmerFrameLayout = tagView.findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout.startShimmer();
        mAdView.setOnPaidEventListener(callback::onAdPaid);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                shimmerFrameLayout.stopShimmer();
                viewGroup.removeView(tagView);
                callback.onBannerAdLoaded(adSize);
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                Log.e(" Admod", "failloadbanner" + adError.getMessage());
                shimmerFrameLayout.stopShimmer();
                viewGroup.removeView(tagView);
                callback.onAdFail();
            }

            @Override
            public void onAdOpened() {
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        });
        Bundle extras = new Bundle();
        String anchored = "top";
        if (collapsibleBannersize == CollapsibleBanner.TOP) {
            anchored = "top";
        } else {
            anchored = "bottom";
        }
        extras.putString("collapsible", anchored);
        AdRequest adRequest2 = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, extras)
                .build();
        if (adRequest2 != null) {
            mAdView.loadAd(adRequest2);
        }

        Log.e(" Admod", "loadAdBanner");
    }

    public void loadAndGetNativeAds(Context context, NativeHolderAdmod nativeHolder, NativeAdCallbackAdmod adCallback) {
        if (!isShowAds || !isNetworkConnected(context)) {
            return;
        }
        AdLoader adLoader;
        if (isTesting) {
            nativeHolder.setAds(context.getString(R.string.test_ads_admob_native_id));
        }
        nativeHolder.setLoad(true);
        adLoader = new AdLoader.Builder(context, nativeHolder.getAds())
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {

                    @Override
                    public void onNativeAdLoaded(@NonNull @NotNull NativeAd nativeAd) {
                        nativeHolder.setNativeAd(nativeAd);
                        nativeHolder.setLoad(false);
                        nativeHolder.getNative_mutable().setValue(nativeAd);
                        nativeAd.setOnPaidEventListener(adCallback::onAdPaid);
                        adCallback.onLoadedAndGetNativeAd(nativeAd);
                        //viewGroup.setVisibility(View.VISIBLE);
                    }

                }).withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        Log.e("Admodfail", "onAdFailedToLoad" + adError.getMessage());
                        Log.e("Admodfail", "errorCodeAds" + adError.getCause());
                        nativeHolder.setNativeAd(null);
                        nativeHolder.setLoad(false);
                        nativeHolder.getNative_mutable().setValue(null);
                        loadAndGetNativeAds2(context,nativeHolder,adCallback);
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder().build()).build();

        if (adRequest != null) {
            adLoader.loadAd(adRequest);
        }
        Log.e("Admod", "loadAdNativeAds");
    }

    public void loadAndGetNativeAds2(Context context, NativeHolderAdmod nativeHolder, NativeAdCallbackAdmod adCallback) {
        if (!isShowAds || !isNetworkConnected(context)) {
            return;
        }
        AdLoader adLoader;
        if (isTesting) {
            nativeHolder.setAds2(context.getString(R.string.test_ads_admob_native_id));
        }

        adLoader = new AdLoader.Builder(context, nativeHolder.getAds2())
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {

                    @Override
                    public void onNativeAdLoaded(@NonNull @NotNull NativeAd nativeAd) {
                        nativeHolder.setNativeAd(nativeAd);
                        nativeAd.setOnPaidEventListener(adCallback::onAdPaid);
                        adCallback.onLoadedAndGetNativeAd(nativeAd);
                        //viewGroup.setVisibility(View.VISIBLE);
                    }

                }).withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        Log.e("Admodfail", "onAdFailedToLoad" + adError.getMessage());
                        Log.e("Admodfail", "errorCodeAds" + adError.getCause());
                        adCallback.onAdFail();
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder().build()).build();

        if (adRequest != null) {
            adLoader.loadAd(adRequest);
        }
        Log.e("Admod", "loadAdNativeAds");
    }

    public interface AdsNativeCallBackAdmod{
        void NativeLoaded();
        void NativeFailed();
    }

    public void showNativeAdsWithLayout(Activity activity, NativeHolderAdmod nativeHolder, ViewGroup viewGroup, int layout, GoogleENative size,AdsNativeCallBackAdmod callback) {
        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.setVisibility(View.GONE);
            return;
        }
        if (shimmerFrameLayout != null) {
            shimmerFrameLayout.stopShimmer();
        }
        viewGroup.removeAllViews();
        if (!nativeHolder.isLoad()){
            if (nativeHolder.getNativeAd() != null){
                NativeAdView adView = (NativeAdView) activity.getLayoutInflater()
                        .inflate(layout, null);

                NativeFunc.Companion.populateNativeAdView(nativeHolder.getNativeAd(), adView, GoogleENative.UNIFIED_MEDIUM);
                if (shimmerFrameLayout != null) {
                    shimmerFrameLayout.stopShimmer();
                }
                viewGroup.addView(adView);
                callback.NativeLoaded();
            }else {
                if (shimmerFrameLayout != null) {
                    shimmerFrameLayout.stopShimmer();
                }
                callback.NativeFailed();
            }
        }else {
            View tagView;
            if (size == GoogleENative.UNIFIED_MEDIUM) {
                tagView = activity.getLayoutInflater().inflate(R.layout.layoutnative_loading_medium, null, false);
            } else {
                tagView = activity.getLayoutInflater().inflate(R.layout.layoutnative_loading_small, null, false);
            }
            viewGroup.addView(tagView, 0);
            if (shimmerFrameLayout == null)
                shimmerFrameLayout = tagView.findViewById(R.id.shimmer_view_container);
            shimmerFrameLayout.startShimmer();
            nativeHolder.getNative_mutable().observe((LifecycleOwner) activity, nativeAd -> {
                if (nativeAd!=null){
                    NativeAdView adView = (NativeAdView) activity.getLayoutInflater()
                            .inflate(layout, null);
                    NativeFunc.Companion.populateNativeAdView(nativeAd, adView, GoogleENative.UNIFIED_MEDIUM);
                    if (shimmerFrameLayout != null) {
                        shimmerFrameLayout.stopShimmer();
                    }
                    viewGroup.addView(adView);
                    callback.NativeLoaded();
                }else {
                    if (shimmerFrameLayout != null) {
                        shimmerFrameLayout.stopShimmer();
                    }
                    callback.NativeFailed();
                }
            });
        }
    }

    public void loadAndGetAdInterstitial(Context activity,InterHolderAdmod interHolder, AdCallBackInterLoad adLoadCallback) {
        AdmodUtils.getInstance().isAdShowing = false;
        if (!isShowAds || !isNetworkConnected(activity)) {
            adLoadCallback.onAdFail(false);
            return;
        }
        interHolder.setCheck(true);
        if (adRequest == null) {
            initAdRequest(timeOut);
        }
        if (isTesting) {
            interHolder.setAds(activity.getString(R.string.test_ads_admob_inter_id));
            interHolder.setAds2(activity.getString(R.string.test_ads_admob_inter_id));
        }
        idIntersitialReal = interHolder.getAds();
        String idLoadInter2 = interHolder.getAds2();
        InterstitialAd.load(activity, idIntersitialReal, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull @org.jetbrains.annotations.NotNull InterstitialAd interstitialAd) {
                adLoadCallback.onAdLoaded(interstitialAd, false);
                if (AdmodUtils.getInstance().isClick) {
                    interHolder.getMutable().setValue(interstitialAd);
                }
                Log.i("adLog", "onAdLoaded");
            }

            @Override
            public void onAdFailedToLoad(@NonNull @org.jetbrains.annotations.NotNull LoadAdError loadAdError) {
                isAdShowing = false;
                AdmodUtils.getInstance().isAdShowing = false;
                if (AdmodUtils.getInstance().mInterstitialAd != null) {
                    AdmodUtils.getInstance().mInterstitialAd = null;
                }
                if (AdmodUtils.getInstance().isClick) {
                    interHolder.getMutable().setValue(null);
                }
                loadAndGetAdInterstitialId2(activity, idLoadInter2, adLoadCallback,  interHolder.getMutable());
            }
        });
    }

    public void loadAndGetAdInterstitialId2(Context activity, String admobId2, AdCallBackInterLoad adLoadCallback, MutableLiveData<InterstitialAd> isAdsLoaded) {
        AdmodUtils.getInstance().isAdShowing = false;

        if (!isShowAds || !isNetworkConnected(activity)) {
            adLoadCallback.onAdFail(false);
            return;
        }

        if (isTesting) {
            admobId2 = activity.getString(R.string.test_ads_admob_inter_id);
        }
        if (adRequest == null) {
            initAdRequest(timeOut);
        }
        idIntersitialReal = admobId2;
        InterstitialAd.load(activity, idIntersitialReal, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull @org.jetbrains.annotations.NotNull InterstitialAd interstitialAd) {
                adLoadCallback.onAdLoaded(interstitialAd, false);
                if (AdmodUtils.getInstance().isClick) {
                    isAdsLoaded.setValue(interstitialAd);
                }
                Log.i("adLog", "onAdLoaded");
            }

            @Override
            public void onAdFailedToLoad(@NonNull @org.jetbrains.annotations.NotNull LoadAdError loadAdError) {
                isAdShowing = false;
                AdmodUtils.getInstance().isAdShowing = false;
                if (AdmodUtils.getInstance().mInterstitialAd != null) {
                    AdmodUtils.getInstance().mInterstitialAd = null;
                }
                if (AdmodUtils.getInstance().isClick) {
                    isAdsLoaded.setValue(null);
                }
                adLoadCallback.onAdFail(false);
            }
        });
    }

    public void showAdInterstitialWithCallbackNotLoadNew(Activity activity, InterHolderAdmod interHolder, AdsInterCallBack adCallback, boolean enableLoadingDialog) {
        AdmodUtils.getInstance().isClick = true;
        if (!isShowAds || !isNetworkConnected(activity)) {
            isAdShowing = false;
            if (AppOpenManager.getInstance().isInitialized()) {
                AppOpenManager.getInstance().isAppResumeEnabled = true;
            }
            adCallback.onAdFail();
            return;
        }
        adCallback.onAdLoaded();
        //check Ads Load
        if (interHolder.getCheck()) {
            if (enableLoadingDialog) {
                dialogLoading(activity);
            }
            interHolder.getMutable().observe((LifecycleOwner) activity, aBoolean -> {
                if (aBoolean != null) {
                    interHolder.getMutable().removeObservers((LifecycleOwner) activity);
                    AdmodUtils.getInstance().isClick = false;
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        Log.d("===DelayLoad", "delay");
                        aBoolean.setOnPaidEventListener(adCallback::onPaid);
                        aBoolean.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        isAdShowing = false;
                                        if (AppOpenManager.getInstance().isInitialized()) {
                                            AppOpenManager.getInstance().isAppResumeEnabled = true;
                                        }
                                        AdmodUtils.getInstance().isClick = false;
                                        //set intersitial
                                        interHolder.getMutable().setValue(null);
                                        adCallback.onEventClickAdClosed();
                                        dismissAdDialog();
                                        Log.d("TAG", "The ad was dismissed.");
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                                        isAdShowing = false;
                                        if (AppOpenManager.getInstance().isInitialized()) {
                                            AppOpenManager.getInstance().isAppResumeEnabled = true;
                                        }
                                        //check click showintersitial
                                        AdmodUtils.getInstance().isClick = false;
                                        AdmodUtils.getInstance().isAdShowing = false;
                                        dismissAdDialog();
                                        Log.e("Admodfail", "onAdFailedToLoad" + adError.getMessage());
                                        Log.e("Admodfail", "errorCodeAds" + adError.getCause());
                                        adCallback.onAdFail();
                                        //set intersitial
                                        interHolder.getMutable().setValue(null);
                                    }

                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                        dismissAdDialog();
                                        isAdShowing = true;
                                        adCallback.onAdShowed();
                                    }
                                });
                        showInterstitialAdNew(activity, aBoolean, adCallback);
                    }, 400);
                }
            });
            return;
        }
        if (interHolder.getInter() == null) {
            if (adCallback != null) {
                isAdShowing = false;
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true;
                }
                adCallback.onAdFail();
            }
        } else {
            if (enableLoadingDialog) {
                dialogLoading(activity);
            }
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Log.d("===DelayLoad", "no-delay");
                interHolder.getInter().setOnPaidEventListener(adCallback::onPaid);
                interHolder.getInter().setFullScreenContentCallback(
                        new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                isAdShowing = false;
                                if (AppOpenManager.getInstance().isInitialized()) {
                                    AppOpenManager.getInstance().isAppResumeEnabled = true;
                                }
                                AdmodUtils.getInstance().isClick = false;
                                interHolder.getMutable().removeObservers((LifecycleOwner) activity);
                                adCallback.onEventClickAdClosed();
                                dismissAdDialog();
                                Log.d("TAG", "The ad was dismissed.");
                                Log.d("===Admod", "Closed1");
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                isAdShowing = false;
                                if (AppOpenManager.getInstance().isInitialized()) {
                                    AppOpenManager.getInstance().isAppResumeEnabled = true;
                                }
                                AdmodUtils.getInstance().isClick = false;
                                interHolder.getMutable().removeObservers((LifecycleOwner) activity);
                                AdmodUtils.getInstance().isAdShowing = false;
                                dismissAdDialog();
                                Log.e("Admodfail", "onAdFailedToLoad" + adError.getMessage());
                                Log.e("Admodfail", "errorCodeAds" + adError.getCause());
                                Log.d("===Admod", "Failed1");
                                adCallback.onAdFail();
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                isAdShowing = true;
                                dismissAdDialog();
                                adCallback.onAdShowed();
                            }
                        });
                showInterstitialAdNew(activity, interHolder.getInter(), adCallback);
            }, 400);
        }
    }

    private void showInterstitialAdNew(Activity activity, InterstitialAd mInterstitialAd, AdsInterCallBack callback) {
        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED) && mInterstitialAd != null) {
            AdmodUtils.getInstance().isAdShowing = true;
            new Handler().postDelayed(() -> {
                if (callback != null) {
                    callback.onStartAction();
                }
                mInterstitialAd.setOnPaidEventListener(callback::onPaid);
                mInterstitialAd.show(activity);
            }, 400);
        } else {
            AdmodUtils.getInstance().isAdShowing = false;
            if (AppOpenManager.getInstance().isInitialized()) {
                AppOpenManager.getInstance().isAppResumeEnabled = true;
            }
            dismissAdDialog();
            callback.onAdFail();
        }
    }

    public void loadAdInterstitial(Context activity, String admobId, AdCallbackNew adLoadCallback, boolean enableLoadingDialog) {

        AdmodUtils.getInstance().mInterstitialAd = null;
        AdmodUtils.getInstance().isAdShowing = false;

        if (!isShowAds || !isNetworkConnected(activity)) {
            adLoadCallback.onAdFail();
            return;
        }


        if (AppOpenManager.getInstance().isInitialized()) {
            if (!AppOpenManager.getInstance().isAppResumeEnabled) {
                return;
            } else {
                isAdShowing = false;
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().isAppResumeEnabled = false;
                }
            }
        }

        if (enableLoadingDialog) {
            dialog = new SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE);
            dialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            dialog.setTitleText("Loading ads. Please wait...");
            dialog.setCancelable(false);
            dialog.show();
        }

        if (isTesting) {
            admobId = activity.getString(R.string.test_ads_admob_inter_id);
        }
        if (adRequest == null) {
            initAdRequest(timeOut);
        }
        idIntersitialReal = admobId;
        InterstitialAd.load(activity, idIntersitialReal, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull @org.jetbrains.annotations.NotNull InterstitialAd interstitialAd) {
                mInterstitialAd = interstitialAd;
                adLoadCallback.onAdLoaded();
                Log.i("adLog", "onAdLoaded");
                // Toast.makeText(activity, "success load ads", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdFailedToLoad(@NonNull @org.jetbrains.annotations.NotNull LoadAdError loadAdError) {

                isAdShowing = false;
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true;
                }
                AdmodUtils.getInstance().isAdShowing = false;
                if (AdmodUtils.getInstance().mInterstitialAd != null) {
                    AdmodUtils.getInstance().mInterstitialAd = null;
                }
                adLoadCallback.onAdFail();

            }
        });
    }

    public void showAdInterstitialWithCallbackNotLoad(InterstitialAd kInterstitialAd, Activity activity, AdCallbackNew adCallback) {
        if (!isShowAds || !isNetworkConnected(activity)) {
            isAdShowing = false;
            if (AdmodUtils.getInstance().mInterstitialAd != null) {
                AdmodUtils.getInstance().mInterstitialAd = null;
            }
            if (AppOpenManager.getInstance().isInitialized()) {
                AppOpenManager.getInstance().isAppResumeEnabled = true;
            }
            adCallback.onAdClosed();
            return;

        }
        if (kInterstitialAd == null) {
            if (adCallback != null) {
                isAdShowing = false;
                if (AdmodUtils.getInstance().mInterstitialAd != null) {
                    AdmodUtils.getInstance().mInterstitialAd = null;
                }
                if (AppOpenManager.getInstance().isInitialized()) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true;
                }
                adCallback.onAdFail();
            }
            return;
        }
        kInterstitialAd.setFullScreenContentCallback(
                new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        isAdShowing = false;
                        if (AdmodUtils.getInstance().mInterstitialAd != null) {
                            AdmodUtils.getInstance().mInterstitialAd = null;
                        }
                        if (AppOpenManager.getInstance().isInitialized()) {
                            AppOpenManager.getInstance().isAppResumeEnabled = true;
                        }
                        adCallback.onEventClickAdClosed();
                        adCallback.onAdClosed();

                        Log.d("TAG", "The ad was dismissed.");

                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        isAdShowing = false;
                        if (AppOpenManager.getInstance().isInitialized()) {
                            AppOpenManager.getInstance().isAppResumeEnabled = true;
                        }
                        AdmodUtils.getInstance().isAdShowing = false;
                        if (AdmodUtils.getInstance().mInterstitialAd != null) {
                            AdmodUtils.getInstance().mInterstitialAd = null;
                        }
                        Log.e("Admodfail", "onAdFailedToLoad" + adError.getMessage());
                        Log.e("Admodfail", "errorCodeAds" + adError.getCause());
                        adCallback.onAdFail();

                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        mInterstitialAd = null;
                        isAdShowing = true;
                        adCallback.onAdShowed();

                    }
                });
        showInterstitialAd(activity, kInterstitialAd, adCallback);
    }

    private void showInterstitialAd(Activity activity, InterstitialAd mInterstitialAd, AdCallbackNew callback) {
        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED) && mInterstitialAd != null) {
            AdmodUtils.getInstance().isAdShowing = true;
            if (callback != null) {
                callback.onAdClosed();
            }
            new Handler().postDelayed(() -> {
                mInterstitialAd.show(activity);
            }, 400);
        } else {
            AdmodUtils.getInstance().isAdShowing = false;
            if (AppOpenManager.getInstance().isInitialized()) {
                AppOpenManager.getInstance().isAppResumeEnabled = true;
            }
            dismissAdDialog();
            callback.onAdFail();
        }
    }

    public interface BannerCallBack{
        void onLoad();
        void onFailed();
        void onPaid(AdValue adValue);
    }

    public void loadAdBanner(Activity activity, String bannerId, ViewGroup viewGroup,BannerCallBack bannerAdCallback) {

        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.setVisibility(View.GONE);
            bannerAdCallback.onFailed();
            return;
        }

        AdView mAdView = new AdView(activity);
        if (isTesting) {
            bannerId = activity.getString(R.string.test_ads_admob_banner_id);
        }
        mAdView.setAdUnitId(bannerId);
        AdSize adSize = getAdSize(activity);

        mAdView.setAdSize(adSize);
        viewGroup.removeAllViews();
        View tagView = activity.getLayoutInflater().inflate(R.layout.banner_shimmer_layout, null, false);
        viewGroup.addView(tagView, 0);
        viewGroup.addView(mAdView, 1);
        shimmerFrameLayout = tagView.findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout.startShimmer();
        mAdView.setOnPaidEventListener(new OnPaidEventListener() {
            @Override
            public void onPaidEvent(@NonNull AdValue adValue) {
                bannerAdCallback.onPaid(adValue);
            }
        });
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                shimmerFrameLayout.stopShimmer();
                viewGroup.removeView(tagView);
                bannerAdCallback.onLoad();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                Log.e(" Admod", "failloadbanner" + adError.getMessage());
                shimmerFrameLayout.stopShimmer();
                viewGroup.removeView(tagView);
                bannerAdCallback.onFailed();
            }

            @Override
            public void onAdOpened() {
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        });

        if (adRequest != null) {
            mAdView.loadAd(adRequest);
        }
        Log.e(" Admod", "loadAdBanner");
    }

    public void loadAndShowNativeAds(Activity activity, String s, ViewGroup viewGroup, GoogleENative size, NativeAdCallback adCallback) {
        View tagView;
        if (size == GoogleENative.UNIFIED_MEDIUM) {
            tagView = activity.getLayoutInflater().inflate(R.layout.layoutnative_loading_medium, null, false);
        } else {
            tagView = activity.getLayoutInflater().inflate(R.layout.layoutnative_loading_small, null, false);
        }
        viewGroup.addView(tagView, 0);
        ShimmerFrameLayout shimmerFrameLayout = tagView.findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout.startShimmer();
        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.setVisibility(View.GONE);
            return;
        }

        AdLoader adLoader;
        if (isTesting) {
            s = activity.getString(R.string.test_ads_admob_native_id);
        }

        adLoader = new AdLoader.Builder(activity, s)
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {

                    @Override
                    public void onNativeAdLoaded(@NonNull @NotNull NativeAd nativeAd) {
                        adCallback.onNativeAdLoaded();
                        int id = 0;
                        if (size == GoogleENative.UNIFIED_MEDIUM) {
                            id = R.layout.ad_unified_medium;
                        } else {
                            id = R.layout.ad_unified_small;
                        }

                        NativeAdView adView = (NativeAdView) activity.getLayoutInflater()
                                .inflate(id, null);

                        NativeFunc.Companion.populateNativeAdView(nativeAd, adView, size);
                        shimmerFrameLayout.stopShimmer();
                        viewGroup.removeAllViews();
                        viewGroup.addView(adView);
                        //viewGroup.setVisibility(View.VISIBLE);
                    }

                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        Log.e("Admodfail", "onAdFailedToLoad" + adError.getMessage());
                        Log.e("Admodfail", "errorCodeAds" + adError.getCause());
                        shimmerFrameLayout.stopShimmer();
                        viewGroup.removeAllViews();
                        adCallback.onAdFail();
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder().build()).build();

        if (adRequest != null) {
            adLoader.loadAd(adRequest);
        }
        Log.e("Admod", "loadAdNativeAds");
    }
    private AdSize getAdSize(Activity context) {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = context.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;
        int adWidth = (int) (widthPixels / density);
        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth);
    }


    public void dismissAdDialog() {
        if (AdmodUtils.getInstance().dialog != null && AdmodUtils.getInstance().dialog.isShowing()) {
            AdmodUtils.getInstance().dialog.dismiss();
        }
        if (AdmodUtils.getInstance().dialogFullScreen != null && AdmodUtils.getInstance().dialogFullScreen.isShowing()) {
            AdmodUtils.getInstance().dialogFullScreen.dismiss();
        }
    }

    private long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public String getDeviceID(Context context) {
        String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String deviceId = md5(android_id).toUpperCase();
        return deviceId;
    }

    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void dialogLoading(Context context) {
        dialogFullScreen = new Dialog(context);
        dialogFullScreen.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogFullScreen.setContentView(R.layout.dialog_full_screen);
        dialogFullScreen.setCancelable(false);
        dialogFullScreen.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialogFullScreen.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        dialogFullScreen.show();
    }
}
