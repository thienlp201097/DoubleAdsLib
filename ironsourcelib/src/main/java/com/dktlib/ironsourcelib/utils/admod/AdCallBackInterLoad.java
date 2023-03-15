package com.dktlib.ironsourcelib.utils.admod;

import com.google.android.gms.ads.interstitial.InterstitialAd;

public interface AdCallBackInterLoad {
    void onAdClosed();
    void onEventClickAdClosed();
    void onAdShowed();
    void onAdLoaded(InterstitialAd interstitialAd, boolean isLoading);
    void onAdFail( boolean isLoading);
}
