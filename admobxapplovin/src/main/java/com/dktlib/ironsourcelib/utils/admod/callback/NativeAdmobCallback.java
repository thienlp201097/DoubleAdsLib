package com.dktlib.ironsourcelib.utils.admod.callback;

import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.nativead.NativeAd;

public interface NativeAdmobCallback {
    void onLoadedAndGetNativeAd(NativeAd ad );
    void onNativeAdLoaded();
    void onAdFail(String error);
    void onAdPaid(AdValue adValue, String adUnitAds);
}
