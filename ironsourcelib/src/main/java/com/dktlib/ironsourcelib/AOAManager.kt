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
    var isShowingAd = false
    var isLoading = true
    var dialogFullScreen: Dialog? = null
    private val adRequest: AdRequest
        get() = AdRequest.Builder().build()

    private val isAdAvailable: Boolean
        get() = appOpenAd != null

    fun loadAndShowAoA() {
        var idAoa = id
        if (AdmodUtils.getInstance().isTesting){
            idAoa = activity.getString(R.string.test_ads_admob_app_open)
        }
        if (!AdmodUtils.getInstance().isShowAds){
            appOpenAdsListener.onAdsFailed()
            return
        }
        //Check timeout show inter
        CoroutineScope(Dispatchers.Main).launch() {
            delay(timeOut)
            if (isLoading) {
                isLoading = false
                appOpenAdsListener.onAdsFailed()
            }
        }
        if (isAdAvailable) {
            appOpenAdsListener.onAdsFailed()
            return
        } else {
            Log.d("tag", "fetching... ")
            loadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    isLoading = false
                    super.onAdFailedToLoad(p0)
                    appOpenAdsListener.onAdsFailed()
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
            AppOpenAd.load(activity, idAoa, request, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback!!)
        }
    }

    fun showAdIfAvailable() {
        Log.d("tag", "$isShowingAd - $isAdAvailable")

        if (!isShowingAd && isAdAvailable && isLoading) {
            Log.d("tag", "will show ad ")
            val fullScreenContentCallback: FullScreenContentCallback =
                object : FullScreenContentCallback() {

                    override fun onAdDismissedFullScreenContent() {
                        dialogFullScreen?.dismiss()
                        appOpenAd = null
                        isShowingAd = true
                        appOpenAdsListener.onAdsClose()
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        dialogFullScreen?.dismiss()
                        isShowingAd = true
                        appOpenAdsListener.onAdsFailed()
                    }

                    override fun onAdShowedFullScreenContent() {
                        isShowingAd = true
                    }
                }
            appOpenAd?.run {
                isLoading = false
                this.fullScreenContentCallback = fullScreenContentCallback
                dialogFullScreen = Dialog(activity)
                dialogFullScreen?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialogFullScreen?.setContentView(R.layout.dialog_full_screen)
                dialogFullScreen?.setCancelable(false)
                dialogFullScreen?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
                dialogFullScreen?.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                if (!activity.isFinishing) {
                    dialogFullScreen?.show()
                }
                Handler().postDelayed({
                    show(activity)
                },800)
            }
        }
    }

    interface AppOpenAdsListener {
        fun onAdsClose()
        fun onAdsFailed()
    }

}