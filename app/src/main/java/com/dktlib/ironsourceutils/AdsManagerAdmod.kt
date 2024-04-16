package com.dktlib.ironsourceutils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import com.dktlib.ironsourcelib.AdmobUtils
import com.dktlib.ironsourcelib.GoogleENative
import com.dktlib.ironsourcelib.callback_applovin.NativeAdCallback
import com.dktlib.ironsourcelib.utils.Utils
import com.dktlib.ironsourcelib.utils.admod.callback.AdCallBackInterLoad
import com.dktlib.ironsourcelib.utils.admod.callback.AdsInterCallBack
import com.dktlib.ironsourcelib.utils.admod.InterHolderAdmod
import com.dktlib.ironsourcelib.utils.admod.NativeHolderAdmod
import com.dktlib.ironsourcelib.utils.admod.callback.NativeAdCallbackAdmod
import com.dktlib.ironsourcelib.utils.admod.callback.NativeAdmobCallback
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd

object AdsManagerAdmod {
    var interAds1: InterstitialAd? = null
    val mutable_inter1: MutableLiveData<InterstitialAd> = MutableLiveData()
    var check_inter1 = false

    var nativeHolder = NativeHolderAdmod("ca-app-pub-3940256099942544/2247696110", "ca-app-pub-3940256099942544/2247696110")
    var interholder = InterHolderAdmod("ca-app-pub-3940256099942544/1033173712","ca-app-pub-3940256099942544/1033173712")

    fun loadInter(context: Context, interHolder: InterHolderAdmod) {
        AdmobUtils.loadAndGetAdInterstitial(context,interHolder,
            object :
                AdCallBackInterLoad {
                override fun onAdClosed() {
                    Utils.getInstance().showMessenger(context, "onAdClosed")
                }

                override fun onEventClickAdClosed() {
                    Utils.getInstance().showMessenger(context, "onEventClickAdClosed")
                }

                override fun onAdShowed() {
                    Utils.getInstance().showMessenger(context, "onAdShowed")
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd, isLoad: Boolean) {
                    interholder.inter = interstitialAd
                    interHolder.check = isLoad
                    Utils.getInstance().showMessenger(context, "onAdLoaded")
                }

                override fun onAdFail(message: String?) {
                    Utils.getInstance().showMessenger(context, "onAdFail")
                }
            }
        )
    }


    fun showInter(
        context: Context,
        interHolder: InterHolderAdmod,
        adListener: AdListener,
        enableLoadingDialog: Boolean
    ) {
        AdmobUtils.showAdInterstitialWithCallbackNotLoadNew(
            context as Activity,interHolder,10000, object :
                AdsInterCallBack {
                override fun onAdLoaded() {
                    Utils.getInstance().showMessenger(context, "onAdLoaded")
                }

                override fun onStartAction() {
                    adListener.onAdClosed()
                }

                override fun onAdFail(error: String?) {
                    interHolder.inter = null
                    loadInter(context,interHolder)
                    adListener.onFailed()
                    Utils.getInstance().showMessenger(context, "onAdFail")
                }

                override fun onPaid(adValue: AdValue?, adsId: String?) {
                    Utils.getInstance().showMessenger(context, adValue.toString())
                }

                override fun onEventClickAdClosed() {
                    interHolder.inter = null
                    loadInter(context,interHolder)
//                    adListener.onAdClosed()
                    Utils.getInstance().showMessenger(context, "onEventClickAdClosed")
                }

                override fun onAdShowed() {
                    Utils.getInstance().showMessenger(context, "onAdShowed")
                }
            }, enableLoadingDialog)
    }

    fun loadAdsNativeNew(context: Context, holder: NativeHolderAdmod) {
        AdmobUtils.loadAndGetNativeAds(
            context,
            holder,
            object : NativeAdmobCallback {
                override fun onLoadedAndGetNativeAd(ad: NativeAd?) {
                }

                override fun onNativeAdLoaded() {
                }

                override fun onAdFail(error: String?) {
                }

                override fun onAdPaid(adValue: AdValue?, adUnitAds: String?) {
                }
            })
    }

    fun showNative(activity: Activity, viewGroup: ViewGroup, holder: NativeHolderAdmod) {
        if (!AdmobUtils.isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            return
        }
        AdmobUtils.showNativeAdsWithLayout(activity, holder, viewGroup, R.layout.ad_unified_medium, GoogleENative.UNIFIED_MEDIUM, object : AdmobUtils.AdsNativeCallBackAdmod {
            override fun NativeLoaded() {
                Utils.getInstance().showMessenger(activity, "onNativeShow")
            }

            override fun onPaidNative(adValue: AdValue, adUnitAds: String) {

            }

            override fun NativeFailed(massage: String) {
                Utils.getInstance().showMessenger(activity, "onAdsFailed")
            }
        })
    }

    interface AdListener {
        fun onAdClosed()
        fun onFailed()
    }
}
