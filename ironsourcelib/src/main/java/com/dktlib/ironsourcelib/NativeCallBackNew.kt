package com.dktlib.ironsourcelib

import com.applovin.mediation.MaxAd
import com.applovin.mediation.nativeAds.MaxNativeAdView

interface NativeCallBackNew {
    fun onNativeAdLoaded(nativeAd: MaxAd?, nativeAdView: MaxNativeAdView?)
    fun onAdFail()
    fun onAdRevenuePaid(ad: MaxAd?)
}