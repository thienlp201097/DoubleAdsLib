package com.dktlib.ironsourceutils

import android.app.Activity
import android.content.Context
import android.util.Log
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
import com.dktlib.ironsourcelib.utils.InterHolder

object AdsManager {
    var inter: MaxInterstitialAd?=null
    val mutable_inter: MutableLiveData<MaxInterstitialAd> = MutableLiveData()
    var check_inter = false
    var interHolder = InterHolder("134656413e36e374")
    fun loadInter(context: Context,  id : String){
        ApplovinUtil.loadAnGetInterstitials(context,interHolder,object : InterstititialCallbackNew{
            override fun onInterstitialReady(interstitialAd: MaxInterstitialAd) {
                Toast.makeText(context,"Loaded",Toast.LENGTH_SHORT).show()
            }

            override fun onInterstitialClosed() {

            }

            override fun onInterstitialLoadFail(error: String) {
                Toast.makeText(context,"LoadFailed",Toast.LENGTH_SHORT).show()
            }

            override fun onInterstitialShowSucceed() {

            }

            override fun onAdRevenuePaid(ad: MaxAd?) {

            }
        })
    }

    fun showInter(context: AppCompatActivity,interHolder: InterHolder,adsOnClick: AdsOnClick){
        ApplovinUtil.showInterstitialsWithDialogCheckTimeNew(context, 800,interHolder ,object : InterstititialCallback {
            override fun onInterstitialReady() {
                Toast.makeText(context,"Ready",Toast.LENGTH_SHORT).show()
            }

            override fun onInterstitialClosed() {
                adsOnClick.onAdsCloseOrFailed()
                Toast.makeText(context,"Closed",Toast.LENGTH_SHORT).show()
            }

            override fun onInterstitialLoadFail(error: String) {
                adsOnClick.onAdsCloseOrFailed()
                Toast.makeText(context,"Failed",Toast.LENGTH_SHORT).show()
            }

            override fun onInterstitialShowSucceed() {
                Toast.makeText(context,"Show",Toast.LENGTH_SHORT).show()
            }

            override fun onAdRevenuePaid(ad: MaxAd?) {

            }
        })
    }

    interface AdsOnClick{
        fun onAdsCloseOrFailed()
    }

    var nativeAdLoader : MaxNativeAdLoader?=null
    var native: MaxAd? = null
    var isLoad = false
    var native_mutable: MutableLiveData<MaxAd> = MutableLiveData()

    fun loadNativeAds(activity: Activity, idAd: String) {
        isLoad = true
        nativeAdLoader = MaxNativeAdLoader(idAd, activity)
        nativeAdLoader?.setNativeAdListener(object : MaxNativeAdListener() {
            override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, ad: MaxAd) {
                // Cleanup any pre-existing native ad to prevent memory leaks.
                if (native != null) {
                    nativeAdLoader?.destroy(native)
                }
                isLoad = false
                // Save ad to be rendered later.
                native = ad
                native_mutable.value = ad
                Toast.makeText(activity,"Loaded", Toast.LENGTH_SHORT).show()
            }

            override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                isLoad = false
                native_mutable.value = null
                Toast.makeText(activity,"Failed",Toast.LENGTH_SHORT).show()
            }

            override fun onNativeAdClicked(ad: MaxAd) {
            }

            override fun onNativeAdExpired(ad: MaxAd?) {

            }
        })
        nativeAdLoader?.loadAd()
    }
}