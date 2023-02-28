package com.dktlib.ironsourceutils

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.applovin.mediation.MaxAd
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.dktlib.ironsourcelib.*

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
    var native: MaxAd? = null
    var nativeView: MaxNativeAdView? = null

    fun loadNativeAdsNew(activity: Activity, idAd: String) {
        ApplovinUtil.loadAndGetNativeAds(activity,idAd,object : NativeCallBackNew{
            override fun onNativeAdLoaded(nativeAd: MaxAd?,nativeAdView : MaxNativeAdView?) {
                native = nativeAd
                nativeView = nativeAdView
            }

            override fun onAdFail() {

            }

            override fun onAdRevenuePaid(ad: MaxAd?) {

            }
        })
    }

    fun showNativeAds(activity: Activity, nativeAdContainer: ViewGroup, size: GoogleENative) {
        ApplovinUtil.showNativeAds(activity, native,nativeAdContainer,size,nativeView)
    }
}