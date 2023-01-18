package com.dktlib.ironsourcelib;

import com.google.android.gms.ads.AdSize;

public interface BannerAdCallback {
    void onBannerAdLoaded(AdSize adSize);
    void onAdFail();
}
