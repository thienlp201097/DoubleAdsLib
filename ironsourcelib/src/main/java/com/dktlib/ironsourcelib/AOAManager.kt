package com.dktlib.ironsourcelib

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.util.Log
import android.view.Window
import android.widget.LinearLayout
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AOAManager(private val activity: Activity, val id : String,val timeOut: Long, val appOpenAdsListener: AppOpenAdsListener) {
    private var appOpenAd: AppOpenAd? = null
    private var loadCallback: AppOpenAd.AppOpenAdLoadCallback? = null
    var isShowingAd = true
    var isLoading = true
    var dialogFullScreen: Dialog? = null
    var isStart = true
    private val adRequest: AdRequest
        get() = AdRequest.Builder().build()

    private val isAdAvailable: Boolean
        get() = appOpenAd != null

    fun loadAndShowAoA() {
        var idAoa = id
        if (AdmodUtils.isTesting){
             idAoa = activity.getString(R.string.test_ads_admob_app_open)
        }
        if (!AdmodUtils.isShowAds){
            appOpenAdsListener.onAdsFailed()
            return
        }
        //Check timeout show inter
        CoroutineScope(Dispatchers.Main).launch() {
            delay(timeOut)
            if (isLoading && isStart) {
                isStart = false
                isLoading = false
                appOpenAdsListener.onAdsFailed()
                Log.d("====Timeout", "TimeOut")
            }
        }
        if (isAdAvailable) {
            appOpenAdsListener.onAdsFailed()
            return
        } else {
            Log.d("====Timeout", "fetching... ")
            isShowingAd = false
            loadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    isLoading = false
                    super.onAdFailedToLoad(p0)
                    if (isStart){
                        isStart = false
                        appOpenAdsListener.onAdsFailed()
                    }

                    Log.d("====Timeout", "onAppOpenAdFailedToLoad: ")
                }

                override fun onAdLoaded(ad: AppOpenAd) {
                    super.onAdLoaded(ad)
                    appOpenAd = ad
                    Log.d("====Timeout", "isAdAvailable = true")
                    showAdIfAvailable()
                }
            }
            val request = adRequest
            AppOpenAd.load(activity, idAoa, request, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback!!)
        }
    }

    fun showAdIfAvailable() {
        Log.d("====Timeout", "$isShowingAd - $isAdAvailable")
        if (!isShowingAd && isAdAvailable && isLoading) {
            isLoading = false
            Log.d("====Timeout", "will show ad ")
            val fullScreenContentCallback: FullScreenContentCallback =
                object : FullScreenContentCallback() {

                    override fun onAdDismissedFullScreenContent() {
                        try {
                            dialogFullScreen?.dismiss()
                        } catch (ignored: Exception) {
                        }
                        appOpenAd = null
                        isShowingAd = true
                        Log.d("====Timeout", "Dismiss... ")
                        if (isStart){
                            isStart = false
                            appOpenAdsListener.onAdsClose()
                        }
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        try {
                            dialogFullScreen?.dismiss()
                        } catch (ignored: Exception) {
                        }
                        isShowingAd = true
                        Log.d("====Timeout", "Failed... ")
                        if (isStart){
                            isStart = false
                            appOpenAdsListener.onAdsFailed()
                        }
                    }

                    override fun onAdShowedFullScreenContent() {
                        isShowingAd = true
                    }
                }
            appOpenAd?.run {
                this.fullScreenContentCallback = fullScreenContentCallback
                dialogFullScreen = Dialog(activity)
                dialogFullScreen?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialogFullScreen?.setContentView(R.layout.dialog_full_screen)
                dialogFullScreen?.setCancelable(false)
                dialogFullScreen?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
                dialogFullScreen?.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                try {
                    if (!activity.isFinishing && dialogFullScreen != null && dialogFullScreen?.isShowing == false) {
                        dialogFullScreen?.show()
                    }
                } catch (ignored: Exception) {
                }
                Handler().postDelayed({
                    setOnPaidEventListener { appOpenAdsListener.onPaid(it) }
                    if (!AppOpenManager.getInstance().isShowingAd && !isShowingAd){
                        show(activity)
                    }
                },800)
            }
        }
    }

    fun onAoaDestroyed(){
        isShowingAd = true
        isLoading = false
        try {
            if (!activity.isFinishing && dialogFullScreen != null && dialogFullScreen?.isShowing == true) {
                dialogFullScreen?.dismiss()
            }
            appOpenAd?.fullScreenContentCallback?.onAdDismissedFullScreenContent()
        } catch (ignored: Exception) {
        }
    }
    interface AppOpenAdsListener {
        fun onAdsClose()
        fun onAdsFailed()
        fun onPaid(adValue : AdValue)
    }

}