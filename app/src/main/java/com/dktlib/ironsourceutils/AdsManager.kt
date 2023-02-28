package com.dktlib.ironsourceutils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.dktlib.ironsourcelib.*
import com.dktlib.ironsourcelib.R

object AdsManager {
    var inter: MaxInterstitialAd?=null
    val mutable_inter: MutableLiveData<MaxInterstitialAd> = MutableLiveData()
    var check_inter = false

    fun loadInter(context: Context,  id : String){
        ApplovinUtil.loadAnGetInterstitials(context,id,object : InterstititialCallbackNew{
            override fun onInterstitialReady(interstitialAd: MaxInterstitialAd) {
                inter = interstitialAd
                mutable_inter.value = interstitialAd
                check_inter = false
                Toast.makeText(context,"Loaded",Toast.LENGTH_SHORT).show()
            }

            override fun onInterstitialClosed() {

            }

            override fun onInterstitialLoadFail(error: String) {
                check_inter = false
                mutable_inter.value = null
                Toast.makeText(context,"LoadFailed",Toast.LENGTH_SHORT).show()
            }

            override fun onInterstitialShowSucceed() {

            }

            override fun onAdRevenuePaid(ad: MaxAd?) {

            }
        })
    }

    fun showInter(context: AppCompatActivity, interstitialAd: MaxInterstitialAd?){
        ApplovinUtil.showInterstitialsWithDialogCheckTimeNew(context, 800,interstitialAd, mutable_inter, check_inter,object : InterstititialCallback {
            override fun onInterstitialReady() {
                Toast.makeText(context,"Ready",Toast.LENGTH_SHORT).show()
            }

            override fun onInterstitialClosed() {
                Toast.makeText(context,"Closed",Toast.LENGTH_SHORT).show()
            }

            override fun onInterstitialLoadFail(error: String) {
                Toast.makeText(context,"Failed",Toast.LENGTH_SHORT).show()
            }

            override fun onInterstitialShowSucceed() {
                Toast.makeText(context,"Show",Toast.LENGTH_SHORT).show()
            }

            override fun onAdRevenuePaid(ad: MaxAd?) {

            }
        })
    }

    var nativeAdLoader : MaxNativeAdLoader?=null
    var nativeAd: MaxAd? = null

    fun loadNativeAdsNew(activity: Activity, idAd: String) {
        if (!ApplovinUtil.enableAds || !ApplovinUtil.isNetworkConnected(activity)) {
            return
        }
        nativeAdLoader = MaxNativeAdLoader(idAd, activity)
    }

    fun showNativeAds(activity: Activity, nativeAdView: MaxNativeAdLoader, nativeAdContainer: ViewGroup, size: GoogleENative, adCallback: NativeCallBackNew) {
        if (!ApplovinUtil.enableAds || !ApplovinUtil.isNetworkConnected(activity)) {
            adCallback.onAdFail()
            return
        }
        val tagView: View = if (size === GoogleENative.UNIFIED_MEDIUM) {
            activity.layoutInflater.inflate(R.layout.layoutnative_loading_medium, null, false)
        } else {
            activity.layoutInflater.inflate(R.layout.layoutnative_loading_small, null, false)
        }
        nativeAdContainer.addView(tagView, 0)
        nativeAdView.setNativeAdListener(object : MaxNativeAdListener() {

            override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, ad: MaxAd) {
                // Clean up any pre-existing native ad to prevent memory leaks.
                if (nativeAd != null) {
                    nativeAdLoader?.destroy(nativeAd)
                }
                // Save ad for cleanup.
                nativeAd = ad
                nativeAdContainer.removeAllViews()
                nativeAdContainer.addView(nativeAdView)
                // Add ad view to view.
                adCallback.onNativeAdLoaded(ad,nativeAdView)
                nativeAdLoader = null
                nativeAd = null
                loadNativeAdsNew(activity,"8aec97f172bce4a6")
            }

            override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                adCallback.onAdFail()
            }

            override fun onNativeAdClicked(ad: MaxAd) {
            }
        })
        nativeAdLoader?.loadAd()
    }
}