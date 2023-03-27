package com.dktlib.ironsourcelib

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

class AOAManager(private val activity: Activity, val id : String, val appOpenAdsListener: AppOpenAdsListener) {

    private var appOpenAd: AppOpenAd? = null
    private var loadCallback: AppOpenAd.AppOpenAdLoadCallback? = null
    private var isShowingAd = false
    private val adRequest: AdRequest
        get() = AdRequest.Builder().build()

    private val isAdAvailable: Boolean
        get() = appOpenAd != null

    private fun fetchAd() {
        if (isAdAvailable) {
            return
        } else {
            Log.d("tag", "fetching... ")
            loadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    appOpenAdsListener.onAdClosedOrFail()
                    Log.d("tag", "onAppOpenAdFailedToLoad: ")
                }

                override fun onAdLoaded(ad: AppOpenAd) {
                    super.onAdLoaded(ad)
                    appOpenAd = ad
                    Log.d("tag", "isAdAvailable = true")
                    showAdIfAvailable()
                }
            }
            val request = adRequest
            AppOpenAd.load(
                activity,
                id,
                request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                loadCallback!!
            )
        }
    }

    fun showAdIfAvailable() {
        Log.d("tag", "$isShowingAd - $isAdAvailable")

        if (!isShowingAd && isAdAvailable) {
            Log.d("tag", "will show ad ")
            val fullScreenContentCallback: FullScreenContentCallback =
                object : FullScreenContentCallback() {

                    override fun onAdDismissedFullScreenContent() {
                        appOpenAd = null
                        isShowingAd = false
//                        fetchAd()
                        appOpenAdsListener.onAdClosedOrFail()
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        appOpenAdsListener.onAdClosedOrFail()
                    }

                    override fun onAdShowedFullScreenContent() {
                        isShowingAd = true
                    }
                }
            appOpenAd?.run {
                this.fullScreenContentCallback = fullScreenContentCallback
                show(activity)
            }
        } else {
            Log.d("tag", "can't show ad ")
            fetchAd()
        }
    }

    interface AppOpenAdsListener {
        fun onAdClosedOrFail()
    }

}