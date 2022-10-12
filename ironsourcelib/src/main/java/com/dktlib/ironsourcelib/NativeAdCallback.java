package com.dktlib.ironsourcelib;

import com.applovin.mediation.MaxAd;

public interface NativeAdCallback {
    void onNativeAdLoaded();
    void onAdRevenuePaid(MaxAd ad);
    void onAdFail();
}
