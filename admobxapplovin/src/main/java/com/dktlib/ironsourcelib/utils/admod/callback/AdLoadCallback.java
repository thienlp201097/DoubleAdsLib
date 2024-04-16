package com.dktlib.ironsourcelib.utils.admod.callback;

public interface AdLoadCallback {
    void onAdFail(String message);
    void onAdLoaded();
}
