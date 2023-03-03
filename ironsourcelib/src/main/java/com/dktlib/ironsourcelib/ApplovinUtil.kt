package com.dktlib.ironsourcelib

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.applovin.mediation.*
import com.applovin.mediation.ads.MaxAdView
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.ads.MaxRewardedAd
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkConfiguration
import com.applovin.sdk.AppLovinSdkUtils
import com.dktlib.ironsourcelib.utils.SweetAlert.SweetAlertDialog
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


object ApplovinUtil : LifecycleObserver {
    var enableAds = true
    var isInterstitialAdShowing = false
    private var banner: MaxAdView? = null
    var lastTimeInterstitialShowed: Long = 0L
    var lastTimeCallInterstitial: Long = 0L
    var isLoadInterstitialFailed = false
    lateinit var interstitialAd: MaxInterstitialAd
    lateinit var rewardAd: MaxRewardedAd
    var dialogFullScreen: Dialog? = null

    private lateinit var nativeAdLoader: MaxNativeAdLoader
    private var nativeAd: MaxAd? = null

    fun initApplovin(activity: Activity, enableAds: Boolean) {
        this.enableAds = enableAds
        AppLovinSdk.getInstance(activity).mediationProvider = "max"
        AppLovinSdk.getInstance(activity).initializeSdk({ configuration: AppLovinSdkConfiguration -> })

    }

    val TAG: String = "IronSourceUtil"


    //Only use for splash interstitial
    fun loadInterstitials(activity: AppCompatActivity, idAd: String, timeout: Long, callback: InterstititialCallback) {
        interstitialAd = MaxInterstitialAd(idAd, activity)
        if (!enableAds || !isNetworkConnected(activity)) {
            callback.onInterstitialClosed()
            return
        }

        interstitialAd.setListener(object : MaxAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                callback.onInterstitialReady()
                isLoadInterstitialFailed = false
            }

            override fun onAdDisplayed(ad: MaxAd?) {
                callback.onInterstitialShowSucceed()
                lastTimeInterstitialShowed = System.currentTimeMillis()
                isInterstitialAdShowing = true
            }

            override fun onAdHidden(ad: MaxAd?) {
                callback.onInterstitialClosed()
                isInterstitialAdShowing = false
            }

            override fun onAdClicked(ad: MaxAd?) {

            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                callback.onInterstitialLoadFail(error.toString())
                isLoadInterstitialFailed = true
                isInterstitialAdShowing = false
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                callback.onInterstitialLoadFail(error.toString())
            }

        })

        // Load the first ad
        interstitialAd.loadAd()

        activity.lifecycleScope.launch(Dispatchers.Main) {
            delay(timeout)
            if ((!interstitialAd.isReady) && (!isInterstitialAdShowing)) {
                callback.onInterstitialLoadFail("!IronSource.isInterstitialReady()")
            }
        }

    }


    @MainThread
    fun showInterstitialsWithDialogCheckTime(
        activity: AppCompatActivity,
        dialogShowTime: Long,
        callback: InterstititialCallback
    ) {

        if (!enableAds || !isNetworkConnected(activity)) {
            callback.onInterstitialClosed()
            return
        }

        if (interstitialAd == null) {
            callback.onInterstitialLoadFail("null")
            return
        }

        if (AppOpenManager.getInstance().isInitialized) {
            if (!AppOpenManager.getInstance().isAppResumeEnabled) {
                return
            } else {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = false
                }
            }
        }

        if (System.currentTimeMillis() - 1000 < lastTimeCallInterstitial) {
            return
        }
        lastTimeCallInterstitial = System.currentTimeMillis()
        if (!enableAds) {
            if (AppOpenManager.getInstance().isInitialized) {
                AppOpenManager.getInstance().isAppResumeEnabled = true
            }
            callback.onInterstitialLoadFail("\"isNetworkConnected\"")
            return
        }

        interstitialAd.setRevenueListener(object : MaxAdRevenueListener {
            override fun onAdRevenuePaid(ad: MaxAd?) {
                callback.onAdRevenuePaid(ad)
            }
        })
        interstitialAd.setListener(object : MaxAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                activity.lifecycleScope.launch(Dispatchers.Main) {
                    isLoadInterstitialFailed = false
                    callback.onInterstitialReady()
                }
            }

            override fun onAdDisplayed(ad: MaxAd?) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = false
                }
                callback.onInterstitialShowSucceed()
                lastTimeInterstitialShowed = System.currentTimeMillis()
                isInterstitialAdShowing = true
            }

            override fun onAdHidden(ad: MaxAd?) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                callback.onInterstitialClosed()
                isInterstitialAdShowing = false
            }

            override fun onAdClicked(ad: MaxAd?) {
                TODO("Not yet implemented")
            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                isLoadInterstitialFailed = true
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                callback.onInterstitialLoadFail(error.toString())
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                callback.onInterstitialClosed()
            }
        })


        if (interstitialAd.isReady) {
            activity.lifecycleScope.launch {
                if (dialogShowTime > 0) {
                    var dialog = SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE)
                    dialog.progressHelper.barColor = Color.parseColor("#A5DC86")
                    dialog.titleText = "Loading ads. Please wait..."
                    dialog.setCancelable(false)
                    activity.lifecycle.addObserver(DialogHelperActivityLifeCycle(dialog))
                    if (!activity.isFinishing) {
                        dialog.show()
                    }
                    delay(dialogShowTime)
                    if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && dialog.isShowing) {
                        dialog.dismiss()
                    }
                }
                if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    Log.d(TAG, "onInterstitialAdReady")
                    interstitialAd.showAd()
                }
            }
        } else {
            activity.lifecycleScope.launch(Dispatchers.Main) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                callback.onInterstitialClosed()
                isInterstitialAdShowing = false
                isLoadInterstitialFailed = true
            }
        }
    }

    @MainThread
    fun loadAndShowInterstitialsWithDialogCheckTime(
        activity: AppCompatActivity,
        idAd: String,
        dialogShowTime: Long,
        callback: InterstititialCallback
    ) {

        if (!enableAds || !isNetworkConnected(activity)) {
            callback.onInterstitialClosed()
            return
        }

        var dialog = SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE)
        dialog.progressHelper.barColor = Color.parseColor("#A5DC86")
        dialog.titleText = "Loading ads. Please wait..."
        dialog.setCancelable(false)

        interstitialAd = MaxInterstitialAd(idAd, activity)
        interstitialAd.loadAd()

        if (AppOpenManager.getInstance().isInitialized) {
            if (!AppOpenManager.getInstance().isAppResumeEnabled) {
                return
            } else {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = false
                    Log.e("isAppResumeEnabled", "2" + AppOpenManager.getInstance().isAppResumeEnabled)
                }
            }
        }

        lastTimeCallInterstitial = System.currentTimeMillis()
        if (!enableAds || !isNetworkConnected(activity)) {
            Log.e("isNetworkConnected", "1" + AppOpenManager.getInstance().isAppResumeEnabled)
            if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && dialog.isShowing) {
                dialog.dismiss()
            }
            Log.e("isNetworkConnected", "2" + AppOpenManager.getInstance().isAppResumeEnabled)

            if (AppOpenManager.getInstance().isInitialized) {
                AppOpenManager.getInstance().isAppResumeEnabled = true
                Log.e("isNetworkConnected", "3" + AppOpenManager.getInstance().isAppResumeEnabled)

                Log.e("isAppResumeEnabled", "3" + AppOpenManager.getInstance().isAppResumeEnabled)
            }
            Log.e("isNetworkConnected", "4" + AppOpenManager.getInstance().isAppResumeEnabled)

            isInterstitialAdShowing = false
            Log.e("isNetworkConnected", "5" + AppOpenManager.getInstance().isAppResumeEnabled)

            callback.onInterstitialLoadFail("isNetworkConnected")
            return
        }

        interstitialAd.setRevenueListener(object : MaxAdRevenueListener {
            override fun onAdRevenuePaid(ad: MaxAd?) {
                callback.onAdRevenuePaid(ad)
            }
        })
        interstitialAd.setListener(object : MaxAdListener {


            override fun onAdLoaded(ad: MaxAd?) {
                if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    Log.d(TAG, "onInterstitialAdReady")
                    if (interstitialAd.isReady) {
                        dialog.dismiss()
                        interstitialAd.showAd()
                    }
                }
            }

            override fun onAdDisplayed(ad: MaxAd?) {
                if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && dialog.isShowing) {
                    dialog.dismiss()
                }
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = false
                    Log.e("isAppResumeEnabled", "6" + AppOpenManager.getInstance().isAppResumeEnabled)

                }
                callback.onInterstitialShowSucceed()

                lastTimeInterstitialShowed = System.currentTimeMillis()
                isInterstitialAdShowing = true
            }

            override fun onAdHidden(ad: MaxAd?) {
                if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && dialog.isShowing) {
                    dialog.dismiss()
                }
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                    Log.e("isAppResumeEnabled", "5" + AppOpenManager.getInstance().isAppResumeEnabled)

                }
                isInterstitialAdShowing = false

                callback.onInterstitialClosed()
            }

            override fun onAdClicked(ad: MaxAd?) {

            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                activity.lifecycleScope.launch(Dispatchers.Main) {
                    if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && dialog.isShowing) {
                        dialog.dismiss()
                    }
                    isLoadInterstitialFailed = true
                    if (AppOpenManager.getInstance().isInitialized) {
                        AppOpenManager.getInstance().isAppResumeEnabled = true
                        Log.e("isAppResumeEnabled", "4" + AppOpenManager.getInstance().isAppResumeEnabled)

                    }
                    isInterstitialAdShowing = false
                    callback.onInterstitialLoadFail(error.toString())
                }
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                    Log.e("isAppResumeEnabled", "7" + AppOpenManager.getInstance().isAppResumeEnabled)

                }
                isInterstitialAdShowing = false
                callback.onInterstitialClosed()

                if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && dialog.isShowing) {
                    dialog.dismiss()
                }
            }
        })




        if (interstitialAd.isReady) {
            activity.lifecycleScope.launch {
                if (dialogShowTime > 0) {
                    activity.lifecycle.addObserver(DialogHelperActivityLifeCycle(dialog))
                    if (!activity.isFinishing) {
                        dialog.show()
                    }
                    delay(dialogShowTime)
                    if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && dialog.isShowing) {
                        dialog.dismiss()
                    }
                }
                if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    Log.d(TAG, "onInterstitialAdReady")
                    if (interstitialAd.isReady) {
                        interstitialAd.showAd()
                    }
                }
            }
        } else {
            if (dialogShowTime > 0) {
                activity.lifecycleScope.launch(Dispatchers.Main) {
                    activity.lifecycle.addObserver(DialogHelperActivityLifeCycle(dialog))
                    if (!activity.isFinishing) {
                        dialog.show()
                    }
                }
            }

        }
    }


    fun showBanner(
        activity: AppCompatActivity, bannerContainer: ViewGroup, idAd: String,
        callback: BannerCallback
    ) {

        if (!enableAds || !isNetworkConnected(activity)) {
            callback.onBannerLoadFail("")
            return
        }

        bannerContainer.removeAllViews()
        banner = MaxAdView(idAd, activity)

        val width = ViewGroup.LayoutParams.MATCH_PARENT

        // Get the adaptive banner height.
        val heightDp = MaxAdFormat.BANNER.getAdaptiveSize(activity).height
        val heightPx = AppLovinSdkUtils.dpToPx(activity, heightDp)

        banner?.layoutParams = FrameLayout.LayoutParams(width, heightPx)
        banner?.setExtraParameter("adaptive_banner", "true")

        val tagView: View =
            activity.layoutInflater.inflate(R.layout.banner_shimmer_layout, null, false)
        bannerContainer.addView(tagView, 0)
        bannerContainer.addView(banner, 1)
        val shimmerFrameLayout: ShimmerFrameLayout =
            tagView.findViewById(R.id.shimmer_view_container)
        shimmerFrameLayout.startShimmer()

        banner?.setRevenueListener(object : MaxAdRevenueListener {
            override fun onAdRevenuePaid(ad: MaxAd?) {
                callback.onAdRevenuePaid(ad)
            }
        })

        banner?.setListener(object : MaxAdViewAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                shimmerFrameLayout.stopShimmer()
                bannerContainer.removeView(tagView)
            }

            override fun onAdDisplayed(ad: MaxAd?) {
                callback.onBannerShowSucceed()
            }

            override fun onAdHidden(ad: MaxAd?) {
            }

            override fun onAdClicked(ad: MaxAd?) {
            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                bannerContainer.removeAllViews()
                callback.onBannerLoadFail(error.toString())
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                callback.onBannerLoadFail(error.toString())

            }

            override fun onAdExpanded(ad: MaxAd?) {
            }

            override fun onAdCollapsed(ad: MaxAd?) {
            }

        })

        banner?.loadAd()

    }


    @MainThread
    fun showRewardWithDialogCheckTime(
        activity: AppCompatActivity,
        dialogShowTime: Long,
        callback: RewardCallback
    ) {

        if (!enableAds || !isNetworkConnected(activity)) {
            callback.onRewardClosed()
            return
        }

        if (rewardAd == null) {
            callback.onRewardLoadFail("null")
            return
        }

        if (AppOpenManager.getInstance().isInitialized) {
            if (!AppOpenManager.getInstance().isAppResumeEnabled) {
                return
            } else {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = false
                }
            }
        }

        if (System.currentTimeMillis() - 1000 < lastTimeCallInterstitial) {
            return
        }
        lastTimeCallInterstitial = System.currentTimeMillis()
        if (!enableAds) {
            if (AppOpenManager.getInstance().isInitialized) {
                AppOpenManager.getInstance().isAppResumeEnabled = true
            }
            callback.onRewardLoadFail("\"isNetworkConnected\"")
            return
        }

        rewardAd.setRevenueListener(object : MaxAdRevenueListener {
            override fun onAdRevenuePaid(ad: MaxAd?) {
                callback.onAdRevenuePaid(ad)
            }
        })
        rewardAd.setListener(object : MaxRewardedAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                activity.lifecycleScope.launch(Dispatchers.Main) {
                    isLoadInterstitialFailed = false
                    callback.onRewardReady()
                }
            }

            override fun onAdDisplayed(ad: MaxAd?) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = false
                }
                callback.onRewardShowSucceed()
                lastTimeInterstitialShowed = System.currentTimeMillis()
                isInterstitialAdShowing = true
            }

            override fun onAdHidden(ad: MaxAd?) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                callback.onRewardClosed()
                isInterstitialAdShowing = false
            }

            override fun onAdClicked(ad: MaxAd?) {

            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                isLoadInterstitialFailed = true
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                callback.onRewardLoadFail(error.toString())
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                callback.onRewardClosed()
            }

            override fun onUserRewarded(ad: MaxAd?, reward: MaxReward?) {
                callback.onUserRewarded()
            }

            override fun onRewardedVideoStarted(ad: MaxAd?) {
                callback.onRewardedVideoStarted()
            }

            override fun onRewardedVideoCompleted(ad: MaxAd?) {
                callback.onRewardedVideoCompleted()
            }
        })


        if (rewardAd.isReady) {
            activity.lifecycleScope.launch {
                if (dialogShowTime > 0) {
                    var dialog = SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE)
                    dialog.progressHelper.barColor = Color.parseColor("#A5DC86")
                    dialog.titleText = "Loading ads. Please wait..."
                    dialog.setCancelable(false)
                    activity.lifecycle.addObserver(DialogHelperActivityLifeCycle(dialog))
                    if (!activity.isFinishing) {
                        dialog.show()
                    }
                    delay(dialogShowTime)
                    if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && dialog.isShowing) {
                        dialog.dismiss()
                    }
                }
                if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    Log.d(TAG, "onInterstitialAdReady")
                    rewardAd.showAd()
                }
            }
        } else {
            activity.lifecycleScope.launch(Dispatchers.Main) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                callback.onRewardClosed()
                isInterstitialAdShowing = false
                isLoadInterstitialFailed = true
            }
        }
    }


    fun loadReward(activity: AppCompatActivity, idAd: String, timeout: Long, callback: RewardCallback) {

        rewardAd = MaxRewardedAd.getInstance(idAd, activity)
        if (!enableAds || !isNetworkConnected(activity)) {
            callback.onRewardClosed()
            return
        }

        rewardAd.setListener(object : MaxRewardedAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                isLoadInterstitialFailed = false
                callback.onRewardReady()
            }

            override fun onAdDisplayed(ad: MaxAd?) {
                callback.onRewardShowSucceed()
                lastTimeInterstitialShowed = System.currentTimeMillis()
                isInterstitialAdShowing = true
            }

            override fun onAdHidden(ad: MaxAd?) {
                callback.onRewardClosed()
                isInterstitialAdShowing = false
            }

            override fun onAdClicked(ad: MaxAd?) {

            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                callback.onRewardLoadFail(error.toString())
                isLoadInterstitialFailed = true
                isInterstitialAdShowing = false
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                callback.onRewardLoadFail(error.toString())
            }

            override fun onUserRewarded(ad: MaxAd?, reward: MaxReward?) {
                callback.onUserRewarded()
            }

            override fun onRewardedVideoStarted(ad: MaxAd?) {
                callback.onRewardedVideoStarted()
            }

            override fun onRewardedVideoCompleted(ad: MaxAd?) {
                callback.onRewardedVideoCompleted()
            }

        })

        // Load the first ad
        rewardAd.loadAd()

        activity.lifecycleScope.launch(Dispatchers.Main) {
            delay(timeout)
            if ((!rewardAd.isReady) && (!isInterstitialAdShowing)) {
                callback.onRewardLoadFail("!IronSource.isInterstitialReady()")
            }
        }

    }


//    fun loadAndShowRewardsAds(placementId: String, callback: RewardVideoCallback) {
//        IronSource.setRewardedVideoListener(object : RewardedVideoListener {
//            override fun onRewardedVideoAdOpened() {
//
//            }
//
//            override fun onRewardedVideoAdClosed() {
//                callback.onRewardClosed()
//            }
//
//            override fun onRewardedVideoAvailabilityChanged(p0: Boolean) {
//
//            }
//
//            override fun onRewardedVideoAdStarted() {
//
//            }
//
//            override fun onRewardedVideoAdEnded() {
//
//            }
//
//            override fun onRewardedVideoAdRewarded(p0: Placement?) {
//                callback.onRewardEarned()
//            }
//
//            override fun onRewardedVideoAdShowFailed(p0: IronSourceError?) {
//                callback.onRewardFailed()
//            }
//
//            override fun onRewardedVideoAdClicked(p0: Placement?) {
//
//            }
//        })
//        if (IronSource.isRewardedVideoAvailable()) {
//            IronSource.showRewardedVideo(placementId)
//        } else {
//            callback.onRewardNotAvailable()
//        }
//    }

    fun loadAndGetNativeAds(activity: Activity, idAd: String, adCallback: NativeCallBackNew) {
        if (!enableAds || !isNetworkConnected(activity)) {
            adCallback.onAdFail()
        }
        nativeAdLoader = MaxNativeAdLoader(idAd, activity)
        nativeAdLoader.setRevenueListener { ad -> adCallback.onAdRevenuePaid(ad) }
        nativeAdLoader.setNativeAdListener(object : MaxNativeAdListener() {

            override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, ad: MaxAd?) {
                // Clean up any pre-existing native ad to prevent memory leaks.
                if (nativeAd != null) {
                    nativeAdLoader.destroy(nativeAd)
                }
                nativeAd = ad
                adCallback.onNativeAdLoaded(nativeAd,nativeAdView)
            }

            override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                adCallback.onAdFail()
            }

            override fun onNativeAdClicked(ad: MaxAd) {
            }
        })
        nativeAdLoader.loadAd()
    }



    fun loadAndShowNativeAds(activity: Activity, idAd: String, nativeAdContainer: ViewGroup, size: GoogleENative, adCallback: NativeAdCallback) {
        if (!enableAds || !isNetworkConnected(activity)) {
            adCallback.onAdFail()
            return
        }

        nativeAdLoader = MaxNativeAdLoader(idAd, activity)
        val tagView: View = if (size === GoogleENative.UNIFIED_MEDIUM) {
            activity.layoutInflater.inflate(R.layout.layoutnative_loading_medium, null, false)
        } else {
            activity.layoutInflater.inflate(R.layout.layoutnative_loading_small, null, false)
        }
        nativeAdContainer.addView(tagView, 0)
        val shimmerFrameLayout: ShimmerFrameLayout = tagView.findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)
        shimmerFrameLayout.startShimmer()
        nativeAdLoader.setRevenueListener(object : MaxAdRevenueListener {
            override fun onAdRevenuePaid(ad: MaxAd?) {
                adCallback.onAdRevenuePaid(ad)
            }
        })
        nativeAdLoader.setNativeAdListener(object : MaxNativeAdListener() {

            override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, ad: MaxAd) {
                // Clean up any pre-existing native ad to prevent memory leaks.
                if (nativeAd != null) {
                    nativeAdLoader.destroy(nativeAd)
                }

                // Save ad for cleanup.
                nativeAd = ad

                // Add ad view to view.
                shimmerFrameLayout.stopShimmer()
                nativeAdContainer.removeAllViews()
                nativeAdContainer.addView(nativeAdView)
                adCallback.onNativeAdLoaded()

            }

            override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                shimmerFrameLayout.stopShimmer()
                nativeAdContainer.removeAllViews()
                adCallback.onAdFail()
            }

            override fun onNativeAdClicked(ad: MaxAd) {
            }
        })
        nativeAdLoader.loadAd()
    }

    //New thienlp
    fun loadAnGetInterstitials(activity: Context, idAd: String, callback: InterstititialCallbackNew) {
        val interstitialAd = MaxInterstitialAd(idAd, activity as Activity?)
        if (!enableAds || !isNetworkConnected(activity)) {
            callback.onInterstitialClosed()
            return
        }

        interstitialAd.setListener(object : MaxAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                callback.onInterstitialReady(interstitialAd)
                isLoadInterstitialFailed = false
            }

            override fun onAdDisplayed(ad: MaxAd?) {
                callback.onInterstitialShowSucceed()
                lastTimeInterstitialShowed = System.currentTimeMillis()
                isInterstitialAdShowing = true
            }

            override fun onAdHidden(ad: MaxAd?) {
                callback.onInterstitialClosed()
                isInterstitialAdShowing = false
            }

            override fun onAdClicked(ad: MaxAd?) {

            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                callback.onInterstitialLoadFail(error.toString())
                isLoadInterstitialFailed = true
                isInterstitialAdShowing = false
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                callback.onInterstitialLoadFail(error.toString())
            }

        })
        // Load the first ad
        interstitialAd.loadAd()
    }

    @MainThread
    fun showInterstitialsWithDialogCheckTimeNew(
        activity: AppCompatActivity,
        dialogShowTime: Long, interstitialAd : MaxInterstitialAd?, isAdsLoaded : MutableLiveData<MaxInterstitialAd>, isLoading : Boolean,
        callback: InterstititialCallback
    ) {
        if (!enableAds || !isNetworkConnected(activity)) {
            callback.onInterstitialClosed()
            return
        }

        if (interstitialAd == null) {
            callback.onInterstitialLoadFail("null")
            return
        }

        if (AppOpenManager.getInstance().isInitialized) {
            if (!AppOpenManager.getInstance().isAppResumeEnabled) {
                return
            } else {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = false
                }
            }
        }

        if (System.currentTimeMillis() - 1000 < lastTimeCallInterstitial) {
            return
        }
        lastTimeCallInterstitial = System.currentTimeMillis()
        if (!enableAds) {
            if (AppOpenManager.getInstance().isInitialized) {
                AppOpenManager.getInstance().isAppResumeEnabled = true
            }
            callback.onInterstitialLoadFail("\"isNetworkConnected\"")
            return
        }

        interstitialAd.setRevenueListener { ad -> callback.onAdRevenuePaid(ad) }
        interstitialAd.setListener(object : MaxAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                activity.lifecycleScope.launch(Dispatchers.Main) {
                    isLoadInterstitialFailed = false
                    callback.onInterstitialReady()
                }
            }

            override fun onAdDisplayed(ad: MaxAd?) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = false
                }
                callback.onInterstitialShowSucceed()
                lastTimeInterstitialShowed = System.currentTimeMillis()
                isInterstitialAdShowing = true
            }

            override fun onAdHidden(ad: MaxAd?) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                callback.onInterstitialClosed()
                isInterstitialAdShowing = false
            }

            override fun onAdClicked(ad: MaxAd?) {
                TODO("Not yet implemented")
            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                isLoadInterstitialFailed = true
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                callback.onInterstitialLoadFail(error.toString())
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                callback.onInterstitialClosed()
            }
        })
        if (!isLoading){
            if (interstitialAd.isReady) {
                activity.lifecycleScope.launch {
                    dialogFullScreen = Dialog(activity)
                    dialogFullScreen?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialogFullScreen?.setContentView(R.layout.dialog_full_screen)
                    dialogFullScreen?.setCancelable(false)
                    dialogFullScreen?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
                    dialogFullScreen?.window?.setLayout(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                    if (!activity.isFinishing) {
                        dialogFullScreen?.show()
                    }
                    delay(dialogShowTime)
                    if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && dialogFullScreen?.isShowing == true) {
                        dialogFullScreen?.dismiss()
                    }
                    if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        Log.d(TAG, "onInterstitialAdReady")
                        interstitialAd.showAd()
                        dialogFullScreen?.dismiss()
                    }
                }
            }else {
                activity.lifecycleScope.launch(Dispatchers.Main) {
                    if (AppOpenManager.getInstance().isInitialized) {
                        AppOpenManager.getInstance().isAppResumeEnabled = true
                    }
                    callback.onInterstitialClosed()
                    isInterstitialAdShowing = false
                    isLoadInterstitialFailed = true
                }
            }
        }else{
            activity.lifecycleScope.launch {
                dialogFullScreen = Dialog(activity)
                dialogFullScreen?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialogFullScreen?.setContentView(R.layout.dialog_full_screen)
                dialogFullScreen?.setCancelable(false)
                dialogFullScreen?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
                dialogFullScreen?.window?.setLayout(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                if (!activity.isFinishing) {
                    dialogFullScreen?.show()
                }
                delay(dialogShowTime)
                if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && dialogFullScreen?.isShowing == true) {
                    dialogFullScreen?.dismiss()
                }
                isAdsLoaded.observe(activity as LifecycleOwner) {
                    if (it.isReady){
                        if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                            Log.d(TAG, "onInterstitialAdReady")
                            interstitialAd.showAd()
                        }
                        if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && dialogFullScreen?.isShowing == true) {
                            dialogFullScreen?.dismiss()
                        }
                    }else {
                        activity.lifecycleScope.launch(Dispatchers.Main) {
                            dialogFullScreen?.dismiss()
                            if (AppOpenManager.getInstance().isInitialized) {
                                AppOpenManager.getInstance().isAppResumeEnabled = true
                            }
                            callback.onInterstitialClosed()
                            isInterstitialAdShowing = false
                            isLoadInterstitialFailed = true
                        }
                    }
                }
            }
        }
    }

    fun loadNativeAds(activity: Activity, idAd: String, adCallback: NativeCallBackNew) {
        if (!enableAds || !isNetworkConnected(activity)) {
            adCallback.onAdFail()
            return
        }
        nativeAdLoader = MaxNativeAdLoader(idAd, activity)
        nativeAdLoader.setRevenueListener { ad -> adCallback.onAdRevenuePaid(ad) }
        nativeAdLoader.setNativeAdListener(object : MaxNativeAdListener() {
            override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, ad: MaxAd) {

                // Cleanup any pre-existing native ad to prevent memory leaks.
                if (nativeAd != null) {
                    nativeAdLoader.destroy(nativeAd)
                }
                // Save ad to be rendered later.
                nativeAd = ad
                adCallback.onNativeAdLoaded(nativeAd,nativeAdView)
            }

            override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
            }

            override fun onNativeAdClicked(ad: MaxAd) {
            }

            override fun onNativeAdExpired(ad: MaxAd?) {
                nativeAdLoader.loadAd()
            }
        })
    }

    fun showNativeWithLayout(view: ViewGroup, context: Activity, nativeAdLoader : MaxNativeAdLoader?,nativeAd : MaxAd?,native_mutable: MutableLiveData<MaxAd>, layout : Int,size: GoogleENative, callback : NativeCallBackNew, isLoad : Boolean) {
        if (!enableAds || !isNetworkConnected(context)) {
            callback.onAdFail()
            return
        }
        val adView = createNativeAdView(context,layout)
        // Check if ad is expired before rendering
        if (true == nativeAd?.nativeAd?.isExpired) {
            Log.d("==Applovin","isExpired")
            nativeAdLoader?.destroy(nativeAd)
            nativeAdLoader?.setNativeAdListener(object : MaxNativeAdListener() {
                override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, ad: MaxAd) {
                    // Cleanup any pre-existing native ad to prevent memory leaks.
                    nativeAdLoader.destroy(nativeAd)
                    // Save ad to be rendered later.
                    callback.onNativeAdLoaded(ad,adView)
                }

                override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                }

                override fun onNativeAdClicked(ad: MaxAd) {
                }

                override fun onNativeAdExpired(ad: MaxAd?) {
                    nativeAdLoader.loadAd()
                }
            })
            nativeAdLoader?.loadAd()
            return
        }
        if (isLoad){
            Log.d("==Applovin","Load")
            nativeAdLoader?.render(adView, nativeAd)
            view.removeAllViews()
            view.addView(adView)
        }else {
            val tagView: View = if (size === GoogleENative.UNIFIED_MEDIUM) {
                context.layoutInflater.inflate(R.layout.layoutnative_loading_medium, null, false)
            } else {
                context.layoutInflater.inflate(R.layout.layoutnative_loading_small, null, false)
            }
            view.addView(tagView, 0)
            val shimmerFrameLayout: ShimmerFrameLayout = tagView.findViewById(R.id.shimmer_view_container)
            shimmerFrameLayout.startShimmer()
            native_mutable.observe(context as LifecycleOwner){
                if (it!=null){
                    if (it.nativeAd != null){
                        nativeAdLoader?.render(adView, nativeAd)
                        view.removeAllViews()
                        view.addView(adView)
                    }else {
                        shimmerFrameLayout.stopShimmer()
                        callback.onAdFail()
                    }
                }else{
                    shimmerFrameLayout.stopShimmer()
                    callback.onAdFail()
                }
            }
        }
        // Render the ad separately
    }

    private fun createNativeAdView(context: Context,layout : Int): MaxNativeAdView {
        val binder: MaxNativeAdViewBinder = MaxNativeAdViewBinder.Builder(layout)
            .setTitleTextViewId(R.id.title_text_view)
            .setBodyTextViewId(R.id.body_text_view)
            .setAdvertiserTextViewId(R.id.advertiser_text_view)
            .setIconImageViewId(R.id.icon_image_view)
            .setMediaContentViewGroupId(R.id.media_view_container)
            .setOptionsContentViewGroupId(R.id.options_view)
            .setStarRatingContentViewGroupId(R.id.star_rating_view)
            .setCallToActionButtonId(R.id.cta_button)
            .build()
        return MaxNativeAdView(binder, context)
    }
    fun dialogLoading(context: Context?) {
        dialogFullScreen = Dialog(context!!)
        dialogFullScreen?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogFullScreen?.setContentView(R.layout.dialog_full_screen)
        dialogFullScreen?.setCancelable(false)
        dialogFullScreen?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        dialogFullScreen?.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        dialogFullScreen?.show()
    }

    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var vau = cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
        Log.e("isNetworkConnected", "0" + vau)
        return vau
    }
}