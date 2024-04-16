package com.dktlib.ironsourcelib.utils.admod

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd

open class RewardedInterstitialHolderAdmob(var ads: String) {
    var inter: RewardedInterstitialAd? = null
    val mutable: MutableLiveData<RewardedInterstitialAd> = MutableLiveData(null)
    var isLoading = false
}