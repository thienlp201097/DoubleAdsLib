package com.dktlib.ironsourcelib

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.*
import com.airbnb.lottie.LottieAnimationView
import com.applovin.mediation.*
import com.applovin.mediation.ads.MaxAdView
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.ads.MaxRewardedAd
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkInitializationConfiguration
import com.applovin.sdk.AppLovinSdkUtils
import com.dktlib.ironsourcelib.callback_applovin.BannerCallback
import com.dktlib.ironsourcelib.callback_applovin.InterstititialCallback
import com.dktlib.ironsourcelib.callback_applovin.InterstititialCallbackNew
import com.dktlib.ironsourcelib.callback_applovin.NativeAdCallback
import com.dktlib.ironsourcelib.callback_applovin.NativeCallBackNew
import com.dktlib.ironsourcelib.callback_applovin.RewardCallback
import com.dktlib.ironsourcelib.utils.InterHolder
import com.dktlib.ironsourcelib.utils.NativeHolder
import com.dktlib.ironsourcelib.utils.SweetAlert.SweetAlertDialog
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Collections
import java.util.concurrent.Executors


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

    fun initApplovin(application: Application,SDK_KEY : String, testAds : Boolean, enableAds: Boolean, initialization: Initialization) {
        this.enableAds = enableAds
        val executor = Executors.newSingleThreadExecutor();
        executor.execute {
            val initConfigBuilder = AppLovinSdkInitializationConfiguration.builder(SDK_KEY, application)
            initConfigBuilder.mediationProvider = AppLovinMediationProvider.MAX

            // Enable test mode by default for the current device. Cannot be run on the main thread.
            if (testAds){
                val currentGaid = AdvertisingIdClient.getAdvertisingIdInfo(application).id
                if (currentGaid != null) {
                    initConfigBuilder.testDeviceAdvertisingIds = Collections.singletonList(currentGaid)
                }
            }

            // Initialize the AppLovin SDK
            val sdk = AppLovinSdk.getInstance(application)
            sdk.initialize(initConfigBuilder.build()) {
                initialization.onInitSuccessful()
            }
            executor.shutdown()
        }
    }

    interface Initialization{
        fun onInitSuccessful()
    }

    val TAG: String = "IronSourceUtil"


    //Only use for splash interstitial
    fun loadInterstitials(
        activity: AppCompatActivity,
        idAd: String,
        timeout: Long,
        callback: InterstititialCallback
    ) {
        interstitialAd = MaxInterstitialAd(idAd, activity)
        if (!enableAds || !isNetworkConnected(activity)) {
            callback.onInterstitialLoadFail("null")
            return
        }

        interstitialAd.setListener(object : MaxAdListener {
            override fun onAdLoaded(ad: MaxAd) {
                callback.onInterstitialReady()
                isLoadInterstitialFailed = false
            }

            override fun onAdDisplayed(ad: MaxAd) {
                callback.onInterstitialShowSucceed()
                lastTimeInterstitialShowed = System.currentTimeMillis()
                isInterstitialAdShowing = true
            }

            override fun onAdHidden(ad: MaxAd) {
                callback.onInterstitialClosed()
                isInterstitialAdShowing = false
            }

            override fun onAdClicked(ad: MaxAd) {

            }

            override fun onAdLoadFailed(p0: String, p1: MaxError) {
                callback.onInterstitialLoadFail(p1.toString())
                isLoadInterstitialFailed = true
                isInterstitialAdShowing = false
            }

            override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                callback.onInterstitialLoadFail(p1.toString())
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
            override fun onAdRevenuePaid(p0: MaxAd) {
                callback.onAdRevenuePaid(p0)
            }
        })
        interstitialAd.setListener(object : MaxAdListener {
            override fun onAdLoaded(p0: MaxAd) {
                activity.lifecycleScope.launch(Dispatchers.Main) {
                    isLoadInterstitialFailed = false
                    callback.onInterstitialReady()
                }
            }

            override fun onAdDisplayed(p0: MaxAd) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = false
                }
                callback.onInterstitialShowSucceed()
                lastTimeInterstitialShowed = System.currentTimeMillis()
                isInterstitialAdShowing = true
            }

            override fun onAdHidden(p0: MaxAd) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                callback.onInterstitialClosed()
                isInterstitialAdShowing = false
            }

            override fun onAdClicked(p0: MaxAd) {
            }

            override fun onAdLoadFailed(p0: String, p1: MaxError) {
                isLoadInterstitialFailed = true
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                callback.onInterstitialLoadFail(p1.toString())
            }

            override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                callback.onInterstitialLoadFail(p1.toString())
            }
        })


        if (interstitialAd.isReady) {
            activity.lifecycleScope.launch {
                if (dialogShowTime > 0) {
                    val dialog = SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE)
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
                callback.onInterstitialLoadFail("error")
                isInterstitialAdShowing = false
                isLoadInterstitialFailed = true
            }
        }
    }

    @MainThread
    fun  loadAndShowInterstitialsWithDialogCheckTime(
        activity: AppCompatActivity,
        idAd: String,
        dialogShowTime: Long,
        callback: InterstititialCallback
    ) {

        if (!enableAds || !isNetworkConnected(activity)) {
            callback.onInterstitialClosed()
            return
        }

        val dialogFullScreen = Dialog(activity)
        dialogFullScreen.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogFullScreen.setContentView(R.layout.dialog_full_screen)
        dialogFullScreen.setCancelable(false)
        dialogFullScreen.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        dialogFullScreen.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        interstitialAd = MaxInterstitialAd(idAd, activity)
        interstitialAd.loadAd()

        if (AppOpenManager.getInstance().isInitialized) {
            if (!AppOpenManager.getInstance().isAppResumeEnabled) {
                return
            } else {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = false
                }
            }
        }

        lastTimeCallInterstitial = System.currentTimeMillis()
        if (!enableAds || !isNetworkConnected(activity)) {
            Log.e("isNetworkConnected", "1" + AppOpenManager.getInstance().isAppResumeEnabled)
            if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && dialogFullScreen.isShowing) {
                dialogFullScreen.dismiss()
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
            override fun onAdRevenuePaid(p0: MaxAd) {
                callback.onAdRevenuePaid(p0)
            }
        })
        interstitialAd.setListener(object : MaxAdListener {

            override fun onAdLoaded(p0: MaxAd) {
                activity.lifecycleScope.launch {
                    if (dialogShowTime > 0) {
                        activity.lifecycle.addObserver(
                            DialogHelperActivityLifeCycle(
                                dialogFullScreen
                            )
                        )
                        if (!activity.isFinishing) {
                            dialogFullScreen.show()
                        }
                        delay(dialogShowTime)
//                        if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && dialogFullScreen.isShowing) {
//                            dialogFullScreen.dismiss()
//                        }
                    }
                    if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        Log.d(TAG, "onInterstitialAdReady")
                        if (interstitialAd.isReady) {
                            interstitialAd.showAd()
                        }
                    }
                }
            }

            override fun onAdDisplayed(p0: MaxAd) {
                Handler(Looper.getMainLooper()).postDelayed({
                    if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && dialogFullScreen.isShowing) {
                        dialogFullScreen.dismiss()
                    }
                },500)
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = false
                    Log.e(
                        "isAppResumeEnabled",
                        "6" + AppOpenManager.getInstance().isAppResumeEnabled
                    )

                }
                callback.onInterstitialShowSucceed()

                lastTimeInterstitialShowed = System.currentTimeMillis()
                isInterstitialAdShowing = true
            }

            override fun onAdHidden(p0: MaxAd) {
                if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && dialogFullScreen.isShowing) {
                    dialogFullScreen.dismiss()
                }
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                    Log.e(
                        "isAppResumeEnabled",
                        "5" + AppOpenManager.getInstance().isAppResumeEnabled
                    )

                }
                isInterstitialAdShowing = false

                callback.onInterstitialClosed()
            }

            override fun onAdClicked(p0: MaxAd) {

            }

            override fun onAdLoadFailed(p0: String, p1: MaxError) {
                activity.lifecycleScope.launch(Dispatchers.Main) {
                    if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && dialogFullScreen.isShowing) {
                        dialogFullScreen.dismiss()
                    }
                    isLoadInterstitialFailed = true
                    if (AppOpenManager.getInstance().isInitialized) {
                        AppOpenManager.getInstance().isAppResumeEnabled = true
                        Log.e(
                            "isAppResumeEnabled",
                            "4" + AppOpenManager.getInstance().isAppResumeEnabled
                        )

                    }
                    isInterstitialAdShowing = false
                    callback.onInterstitialLoadFail(p1.code.toString().replace("-", ""))
                }
            }

            override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                    Log.e(
                        "isAppResumeEnabled",
                        "7" + AppOpenManager.getInstance().isAppResumeEnabled
                    )

                }
                isInterstitialAdShowing = false
                callback.onInterstitialClosed()

                if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && dialogFullScreen.isShowing) {
                    dialogFullScreen.dismiss()
                }
            }
        })

        if (interstitialAd.isReady) {
            activity.lifecycleScope.launch {
                if (dialogShowTime > 0) {
                    activity.lifecycle.addObserver(DialogHelperActivityLifeCycle(dialogFullScreen))
                    if (!activity.isFinishing) {
                        dialogFullScreen.show()
                    }
                    delay(dialogShowTime)
                    if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && dialogFullScreen.isShowing) {
                        dialogFullScreen.dismiss()
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
                    activity.lifecycle.addObserver(DialogHelperActivityLifeCycle(dialogFullScreen))
                    if (!activity.isFinishing) {
                        dialogFullScreen.show()
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
        val heightPx = AppLovinSdkUtils.dpToPx(activity, 50)

        banner?.layoutParams = FrameLayout.LayoutParams(width, heightPx)
        banner?.setExtraParameter("adaptive_banner", "true")

        val tagView: View =
            activity.layoutInflater.inflate(R.layout.banner_shimmer_layout, null, false)
        bannerContainer.addView(tagView, 0)
        bannerContainer.addView(banner, 1)
        val shimmerFrameLayout: ShimmerFrameLayout =
            tagView.findViewById(R.id.shimmer_view_container)
        shimmerFrameLayout.startShimmer()

        banner?.setRevenueListener { ad -> callback.onAdRevenuePaid(ad) }

        banner?.setListener(object : MaxAdViewAdListener {
            override fun onAdLoaded(p0: MaxAd) {
                shimmerFrameLayout.stopShimmer()
                bannerContainer.removeView(tagView)
            }

            override fun onAdDisplayed(p0: MaxAd) {
                callback.onBannerShowSucceed()
            }

            override fun onAdHidden(p0: MaxAd) {
            }

            override fun onAdClicked(p0: MaxAd) {
            }

            override fun onAdLoadFailed(p0: String, p1: MaxError) {
                bannerContainer.removeAllViews()
                callback.onBannerLoadFail(p1.code.toString().replace("-", ""))
            }

            override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                callback.onBannerLoadFail(p1.code.toString().replace("-", ""))

            }

            override fun onAdExpanded(p0: MaxAd) {
            }

            override fun onAdCollapsed(p0: MaxAd) {
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
            override fun onAdRevenuePaid(p0: MaxAd) {
                callback.onAdRevenuePaid(p0)
            }
        })
        rewardAd.setListener(object : MaxRewardedAdListener {
            override fun onAdLoaded(p0: MaxAd) {
                activity.lifecycleScope.launch(Dispatchers.Main) {
                    isLoadInterstitialFailed = false
                    callback.onRewardReady()
                }
            }

            override fun onAdDisplayed(p0: MaxAd) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = false
                }
                callback.onRewardShowSucceed()
                lastTimeInterstitialShowed = System.currentTimeMillis()
                isInterstitialAdShowing = true
            }

            override fun onAdHidden(p0: MaxAd) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                callback.onRewardClosed()
                isInterstitialAdShowing = false
            }

            override fun onAdClicked(p0: MaxAd) {

            }

            override fun onAdLoadFailed(p0: String, p1: MaxError) {
                isLoadInterstitialFailed = true
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                callback.onRewardLoadFail(p1.toString())
            }

            override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                callback.onRewardClosed()
            }

            override fun onUserRewarded(p0: MaxAd, p1: MaxReward) {
                callback.onUserRewarded()
            }

            override fun onRewardedVideoStarted(p0: MaxAd) {
                callback.onRewardedVideoStarted()
            }

            override fun onRewardedVideoCompleted(p0: MaxAd) {
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


    fun loadReward(
        activity: AppCompatActivity,
        idAd: String,
        timeout: Long,
        callback: RewardCallback
    ) {

        rewardAd = MaxRewardedAd.getInstance(idAd, activity)
        if (!enableAds || !isNetworkConnected(activity)) {
            callback.onRewardClosed()
            return
        }

        rewardAd.setListener(object : MaxRewardedAdListener {
            override fun onAdLoaded(p0: MaxAd) {
                isLoadInterstitialFailed = false
                callback.onRewardReady()
            }

            override fun onAdDisplayed(p0: MaxAd) {
                callback.onRewardShowSucceed()
                lastTimeInterstitialShowed = System.currentTimeMillis()
                isInterstitialAdShowing = true
            }

            override fun onAdHidden(p0: MaxAd) {
                callback.onRewardClosed()
                isInterstitialAdShowing = false
            }

            override fun onAdClicked(p0: MaxAd) {

            }

            override fun onAdLoadFailed(p0: String, p1: MaxError) {
                callback.onRewardLoadFail(p1.toString())
                isLoadInterstitialFailed = true
                isInterstitialAdShowing = false
            }

            override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                callback.onRewardLoadFail(p1.toString())
            }

            override fun onUserRewarded(p0: MaxAd, p1: MaxReward) {
                callback.onUserRewarded()
            }

            override fun onRewardedVideoStarted(p0: MaxAd) {
                callback.onRewardedVideoStarted()
            }

            override fun onRewardedVideoCompleted(p0: MaxAd) {
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

    fun loadAndGetNativeAds(activity: Activity, idAd: String, adCallback: NativeCallBackNew) {
        if (!enableAds || !isNetworkConnected(activity)) {
            adCallback.onAdFail("No internet")
        }

        nativeAdLoader = MaxNativeAdLoader(idAd, activity)
        nativeAdLoader.setRevenueListener { ad -> adCallback.onAdRevenuePaid(ad) }
        nativeAdLoader.setNativeAdListener(object : MaxNativeAdListener() {

            override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, p1: MaxAd) {
                // Clean up any pre-existing native ad to prevent memory leaks.
                if (nativeAd != null) {
                    nativeAdLoader.destroy(nativeAd)
                }
                nativeAd = p1
                adCallback.onNativeAdLoaded(nativeAd, nativeAdView)
            }

            override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                adCallback.onAdFail(error.toString())
            }

            override fun onNativeAdClicked(ad: MaxAd) {
            }
        })
        nativeAdLoader.loadAd()
    }


    fun loadAndShowNativeAds(
        activity: Activity,
        idAd: String,
        nativeAdContainer: ViewGroup,
        size: GoogleENative,
        adCallback: NativeAdCallback
    ) {
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
        val shimmerFrameLayout: ShimmerFrameLayout =
            tagView.findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)
        shimmerFrameLayout.startShimmer()
        nativeAdLoader.setRevenueListener { p0 -> adCallback.onAdRevenuePaid(p0) }
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
    fun loadAnGetInterstitials(
        activity: Context,
        interHolder: InterHolder,
        callback: InterstititialCallbackNew
    ) {
        if (interHolder.inter != null) {
            Log.d("===AdsInter", "Inter not null")
            return
        }
        interHolder.inter = MaxInterstitialAd(interHolder.adsId, activity as Activity?)
        if (!enableAds || !isNetworkConnected(activity)) {
            callback.onInterstitialClosed()
            return
        }
        interHolder.check = true
        interHolder.inter!!.setListener(object : MaxAdListener {
            override fun onAdLoaded(p0: MaxAd) {
                callback.onInterstitialReady(interHolder.inter!!)
                isLoadInterstitialFailed = false
                interHolder.check = false
                interHolder.mutable.value = interHolder.inter
            }

            override fun onAdDisplayed(p0: MaxAd) {
                callback.onInterstitialShowSucceed()
                lastTimeInterstitialShowed = System.currentTimeMillis()
                isInterstitialAdShowing = true
            }

            override fun onAdHidden(p0: MaxAd) {
                callback.onInterstitialClosed()
                isInterstitialAdShowing = false
            }

            override fun onAdClicked(p0: MaxAd) {

            }

            override fun onAdLoadFailed(p0: String, p1: MaxError) {
                callback.onInterstitialLoadFail(p1.code.toString().replace("-", ""))
                isLoadInterstitialFailed = true
                isInterstitialAdShowing = false
                interHolder.check = false
                interHolder.mutable.value = null
            }

            override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                callback.onInterstitialLoadFail(p1.code.toString().replace("-", ""))
            }

        })
        // Load the first ad
        interHolder.inter?.loadAd()
    }

    @MainThread
    fun showInterstitialsWithDialogCheckTimeNew(
        activity: AppCompatActivity,
        dialogShowTime: Long, interHolder: InterHolder,
        callback: InterstititialCallbackNew
    ) {
        if (!enableAds || !isNetworkConnected(activity)) {
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

        if (!enableAds) {
            if (AppOpenManager.getInstance().isInitialized) {
                AppOpenManager.getInstance().isAppResumeEnabled = true
            }
            callback.onInterstitialLoadFail("\"isNetworkConnected\"")
            return
        }
        interHolder.inter?.setRevenueListener { ad -> callback.onAdRevenuePaid(ad) }

        if (!interHolder.check) {
            interHolder.mutable.removeObservers(activity as LifecycleOwner)
            if (interHolder.inter?.isReady == true) {
                activity.lifecycleScope.launch {
                    dialogFullScreen = Dialog(activity)
                    dialogFullScreen?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialogFullScreen?.setContentView(R.layout.dialog_full_screen)
                    dialogFullScreen?.setCancelable(false)
                    dialogFullScreen?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
                    dialogFullScreen?.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                    if (!activity.isFinishing) {
                        dialogFullScreen?.show()
                    }
                    delay(dialogShowTime)

                    if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        Log.d(TAG, "onInterstitialAdReady")
                        interHolder.inter?.setListener(object : MaxAdListener {
                            override fun onAdLoaded(p0: MaxAd) {
                                activity.lifecycleScope.launch(Dispatchers.Main) {
                                    isLoadInterstitialFailed = false
                                    callback.onInterstitialReady(interHolder.inter!!)
                                }
                            }

                            override fun onAdDisplayed(p0: MaxAd) {
                                try {
                                    val txt = dialogFullScreen?.findViewById<TextView>(R.id.txtLoading)
                                    val gif = dialogFullScreen?.findViewById<LottieAnimationView>(R.id.imageView3)
                                    val bg = dialogFullScreen?.findViewById<ConstraintLayout>(R.id.main_container)
                                    txt?.visibility = View.GONE
                                    gif?.visibility = View.GONE
                                    bg?.setBackgroundResource(R.drawable.bg_main_dialog)
                                }catch (ignored: Exception) {
                                }
                                if (AppOpenManager.getInstance().isInitialized) {
                                    AppOpenManager.getInstance().isAppResumeEnabled = false
                                }
                                callback.onInterstitialShowSucceed()
                                lastTimeInterstitialShowed = System.currentTimeMillis()
                                isInterstitialAdShowing = true
                            }

                            override fun onAdHidden(p0: MaxAd) {
                                Handler(Looper.getMainLooper()).postDelayed({
                                    try {
                                        if (dialogFullScreen?.isShowing == true) {
                                            dialogFullScreen?.dismiss()
                                        }
                                    }catch (e : Exception){

                                    }
                                },800)
                                if (AppOpenManager.getInstance().isInitialized) {
                                    AppOpenManager.getInstance().isAppResumeEnabled = true
                                }
                                interHolder.inter = null
                                callback.onInterstitialClosed()
                                isInterstitialAdShowing = false
                            }

                            override fun onAdClicked(p0: MaxAd) {
                            }

                            override fun onAdLoadFailed(p0: String, p1: MaxError) {
                                isLoadInterstitialFailed = true
                                if (AppOpenManager.getInstance().isInitialized) {
                                    AppOpenManager.getInstance().isAppResumeEnabled = true
                                }
                                interHolder.inter = null
                                callback.onInterstitialLoadFail(
                                    p1.code.toString().replace("-", "")
                                )
                            }

                            override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                                if (AppOpenManager.getInstance().isInitialized) {
                                    AppOpenManager.getInstance().isAppResumeEnabled = true
                                }
                                callback.onInterstitialLoadFail(
                                    p1.code.toString().replace("-", "")
                                )
                            }

                        })
                        interHolder.inter?.showAd()
                    } else {
                        try {
                            if (dialogFullScreen?.isShowing == true) {
                                dialogFullScreen?.dismiss()
                            }
                        }catch (ignored: Exception) {
                        }
                    }
                }
            } else {
                activity.lifecycleScope.launch(Dispatchers.Main) {
                    if (AppOpenManager.getInstance().isInitialized) {
                        AppOpenManager.getInstance().isAppResumeEnabled = true
                    }
                    callback.onInterstitialLoadFail("interHolder.inter not ready")
                    isInterstitialAdShowing = false
                    isLoadInterstitialFailed = true
                }
            }
        } else {
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
                try {
                    if (!activity.isFinishing && dialogFullScreen != null && dialogFullScreen?.isShowing == false) {
                        dialogFullScreen?.show()
                    }
                }catch (ignored: Exception) {
                }

                delay(dialogShowTime)
                interHolder.mutable.observe(activity as LifecycleOwner) {
                    if (it != null) {
                        if (it.isReady) {
                            interHolder.mutable.removeObservers(activity as LifecycleOwner)
                            if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                                Log.d(TAG, "onInterstitialAdReady")
                                interHolder.inter?.setListener(object : MaxAdListener {
                                    override fun onAdLoaded(p0: MaxAd) {
                                        activity.lifecycleScope.launch(Dispatchers.Main) {
                                            isLoadInterstitialFailed = false
                                            callback.onInterstitialReady(interHolder.inter!!)
                                        }
                                    }

                                    override fun onAdDisplayed(p0: MaxAd) {
                                        if (AppOpenManager.getInstance().isInitialized) {
                                            AppOpenManager.getInstance().isAppResumeEnabled = false
                                        }
                                        try {
                                            val txt = dialogFullScreen?.findViewById<TextView>(R.id.txtLoading)
                                            val gif = dialogFullScreen?.findViewById<LottieAnimationView>(R.id.imageView3)
                                            val bg = dialogFullScreen?.findViewById<ConstraintLayout>(R.id.main_container)
                                            txt?.visibility = View.GONE
                                            gif?.visibility = View.GONE
                                            bg?.setBackgroundResource(R.drawable.bg_main_dialog)
                                        }catch (ignored: Exception) {
                                        }
                                        callback.onInterstitialShowSucceed()
                                        lastTimeInterstitialShowed = System.currentTimeMillis()
                                        isInterstitialAdShowing = true
                                    }

                                    override fun onAdHidden(p0: MaxAd) {
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            try {
                                                if (dialogFullScreen?.isShowing == true) {
                                                    dialogFullScreen?.dismiss()
                                                }
                                            }catch (e : Exception){

                                            }
                                        },800)
                                        if (AppOpenManager.getInstance().isInitialized) {
                                            AppOpenManager.getInstance().isAppResumeEnabled = true
                                        }
                                        interHolder.inter = null
                                        callback.onInterstitialClosed()
                                        isInterstitialAdShowing = false
                                    }

                                    override fun onAdClicked(p0: MaxAd) {
                                    }

                                    override fun onAdLoadFailed(p0: String, p1: MaxError) {
                                        isLoadInterstitialFailed = true
                                        if (AppOpenManager.getInstance().isInitialized) {
                                            AppOpenManager.getInstance().isAppResumeEnabled = true
                                        }
                                        interHolder.inter = null
                                        callback.onInterstitialLoadFail(
                                            p1.code.toString().replace("-", "")
                                        )
                                    }

                                    override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                                        if (AppOpenManager.getInstance().isInitialized) {
                                            AppOpenManager.getInstance().isAppResumeEnabled = true
                                        }
                                        callback.onInterstitialLoadFail(
                                            p1.code.toString().replace("-", "")
                                        )
                                    }

                                })
                                interHolder.inter?.showAd()
                            } else {
                                callback.onInterstitialClosed()
                                dialogFullScreen?.dismiss()
                            }
                        } else {
                            activity.lifecycleScope.launch(Dispatchers.Main) {
                                interHolder.mutable.removeObservers(activity as LifecycleOwner)
                                dialogFullScreen?.dismiss()
                                if (AppOpenManager.getInstance().isInitialized) {
                                    AppOpenManager.getInstance().isAppResumeEnabled = true
                                }
                                callback.onInterstitialLoadFail("inter not ready")
                                isInterstitialAdShowing = false
                                isLoadInterstitialFailed = true
                            }
                        }
                    } else {
                        activity.lifecycleScope.launch(Dispatchers.Main) {
                            interHolder.mutable.removeObservers(activity as LifecycleOwner)
                            dialogFullScreen?.dismiss()
                            if (AppOpenManager.getInstance().isInitialized) {
                                AppOpenManager.getInstance().isAppResumeEnabled = true
                            }
                            callback.onInterstitialLoadFail("inter null")
                            isInterstitialAdShowing = false
                            isLoadInterstitialFailed = true
                        }
                    }
                }
            }
        }

        activity.lifecycleScope.launch(Dispatchers.Main) {
            delay(10000)
            if ((!interHolder.inter!!.isReady) && (!isInterstitialAdShowing) && !isLoadInterstitialFailed) {
                dialogFullScreen?.dismiss()
                interHolder.inter = null
                interHolder.check = false
                interHolder.mutable.removeObservers(activity)
                callback.onInterstitialLoadFail("inter not ready")
            }
        }
    }

    fun loadNativeAds(
        activity: Context,
        nativeHolder: NativeHolder,
        adCallback: NativeCallBackNew
    ) {
        if (!enableAds || !isNetworkConnected(activity)) {
            adCallback.onAdFail("No internet")
            return
        }
        if (nativeHolder.nativeAdLoader != null) {
            Log.d("===AdsInter", "Native not null")
            return
        }
        nativeHolder.isLoad = true
        nativeHolder.nativeAdLoader = MaxNativeAdLoader(nativeHolder.adsId, activity)
        nativeHolder.nativeAdLoader?.setRevenueListener { ad -> adCallback.onAdRevenuePaid(ad) }
        nativeHolder.nativeAdLoader?.setNativeAdListener(object : MaxNativeAdListener() {
            override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, ad: MaxAd) {
                // Cleanup any pre-existing native ad to prevent memory leaks.
                if (nativeHolder.native != null) {
                    nativeHolder.nativeAdLoader?.destroy(nativeHolder.native)
                }
                // Save ad to be rendered later.
                nativeHolder.native = ad
                nativeHolder.isLoad = false
                nativeHolder.native_mutable.value = ad
                adCallback.onNativeAdLoaded(nativeAd, nativeAdView)
            }

            override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                nativeHolder.native_mutable.value = null
                nativeHolder.isLoad = false
                adCallback.onAdFail(error.code.toString().replace("-", ""))
            }

            override fun onNativeAdClicked(ad: MaxAd) {
            }

            override fun onNativeAdExpired(p0: MaxAd) {
                nativeHolder.nativeAdLoader?.loadAd()
            }
        })
        nativeHolder.nativeAdLoader?.loadAd()
    }

    fun showNativeWithLayout(
        view: ViewGroup,
        context: Activity,
        nativeHolder: NativeHolder,
        layout: Int,
        size: GoogleENative,
        callback: NativeCallBackNew
    ) {
        if (!enableAds || !isNetworkConnected(context)) {
            callback.onAdFail("No internet")
            return
        }
        val adView = createNativeAdView(context, layout)
        // Check if ad is expired before rendering
        if (true == nativeAd?.nativeAd?.isExpired) {
            Log.d("==Applovin", "isExpired")
            nativeHolder.nativeAdLoader?.destroy(nativeAd)
            nativeHolder.nativeAdLoader?.setNativeAdListener(object : MaxNativeAdListener() {
                override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, ad: MaxAd) {
                    // Cleanup any pre-existing native ad to prevent memory leaks.
                    nativeHolder.nativeAdLoader?.destroy(nativeAd)
                    // Save ad to be rendered later.
                    callback.onNativeAdLoaded(ad, adView)
                }

                override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                    callback.onAdFail(error.code.toString().replace("-", ""))
                }

                override fun onNativeAdClicked(ad: MaxAd) {
                }

                override fun onNativeAdExpired(p0: MaxAd) {
                    nativeHolder.nativeAdLoader?.loadAd()
                }
            })
            nativeHolder.nativeAdLoader?.loadAd()
            return
        }
        if (!nativeHolder.isLoad) {
            if (nativeHolder.native != null) {
                Log.d("==Applovin", "Load")
                nativeHolder.nativeAdLoader?.render(adView, nativeHolder.native)
                view.removeAllViews()
                view.addView(adView)
                callback.onNativeAdLoaded(nativeHolder.native, adView)
            } else {
                callback.onAdFail("NativeAd Null")
            }
        } else {
            val tagView: View = if (size === GoogleENative.UNIFIED_MEDIUM) {
                context.layoutInflater.inflate(R.layout.layoutnative_loading_medium, null, false)
            } else {
                context.layoutInflater.inflate(R.layout.layoutnative_loading_small, null, false)
            }
            view.addView(tagView, 0)
            val shimmerFrameLayout: ShimmerFrameLayout = tagView.findViewById(R.id.shimmer_view_container)
            shimmerFrameLayout.startShimmer()
            nativeHolder.native_mutable.observe(context as LifecycleOwner) {
                if (it != null) {
                    if (it.nativeAd != null) {
                        nativeHolder.nativeAdLoader?.render(adView, nativeHolder.native)
                        view.removeAllViews()
                        view.addView(adView)
                        callback.onNativeAdLoaded(nativeHolder.native, adView)
                    } else {
                        shimmerFrameLayout.stopShimmer()
                        callback.onAdFail("NativeAd null")
                    }
                } else {
                    shimmerFrameLayout.stopShimmer()
                    callback.onAdFail("NativeAd null")
                }
            }
            Handler().postDelayed({
                if (!nativeHolder.isLoad && nativeHolder.native == null) {
                    callback.onAdFail("NativeAd null")
                }
            }, 3000)
        }
    }


    fun loadAndShowNativeAdsWithLayout(
        activity: Activity,
        nativeHolder: NativeHolder,
        layout: Int,
        view: ViewGroup,
        size: GoogleENative,
        adCallback: NativeCallBackNew
    ) {
        if (!enableAds || !isNetworkConnected(activity)) {
            adCallback.onAdFail("No internet")
            return
        }
        nativeHolder.nativeAdLoader = MaxNativeAdLoader(nativeHolder.adsId, activity)
        view.removeAllViews()
        val tagView: View = if (size === GoogleENative.UNIFIED_MEDIUM) {
            activity.layoutInflater.inflate(R.layout.layoutnative_loading_medium, null, false)
        } else {
            activity.layoutInflater.inflate(R.layout.layoutnative_loading_small, null, false)
        }
        view.addView(tagView, 0)
        val shimmerFrameLayout: ShimmerFrameLayout =
            tagView.findViewById(R.id.shimmer_view_container)
        shimmerFrameLayout.startShimmer()

        nativeHolder.nativeAdLoader?.setRevenueListener { ad -> adCallback.onAdRevenuePaid(ad) }
        nativeHolder.nativeAdLoader?.setNativeAdListener(object : MaxNativeAdListener() {
            override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, ad: MaxAd) {
                if (nativeHolder.native != null) {
                    nativeHolder.nativeAdLoader?.destroy(nativeHolder.native)
                }
                nativeHolder.native = ad
                val adView = createNativeAdView(activity, layout)
                nativeHolder.nativeAdLoader?.render(adView, nativeHolder.native)
                shimmerFrameLayout.stopShimmer()
                view.removeAllViews()
                view.addView(adView)
                adCallback.onNativeAdLoaded(nativeHolder.native, adView)
            }

            override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                shimmerFrameLayout.stopShimmer()
                adCallback.onAdFail(error.code.toString().replace("-", ""))
            }

            override fun onNativeAdClicked(ad: MaxAd) {
            }

            override fun onNativeAdExpired(p0: MaxAd) {
                nativeHolder.nativeAdLoader?.loadAd()
            }
        })
        nativeHolder.nativeAdLoader?.loadAd()
    }

    private fun createNativeAdView(context: Context, layout: Int): MaxNativeAdView {
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

    fun loadAndShowNativeAds(
        activity: Activity,
        nativeHolder: NativeHolder,
        view: ViewGroup,
        size: GoogleENative,
        adCallback: NativeCallBackNew
    ) {
        if (!enableAds || !isNetworkConnected(activity)) {
            adCallback.onAdFail("No internet")
            return
        }
        nativeHolder.nativeAdLoader = MaxNativeAdLoader(nativeHolder.adsId, activity)
        view.removeAllViews()
        val tagView: View = if (size === GoogleENative.UNIFIED_MEDIUM) {
            activity.layoutInflater.inflate(R.layout.layoutnative_loading_medium, null, false)
        } else {
            activity.layoutInflater.inflate(R.layout.layoutnative_loading_small, null, false)
        }
        view.addView(tagView, 0)
        val shimmerFrameLayout: ShimmerFrameLayout =
            tagView.findViewById(R.id.shimmer_view_container)
        shimmerFrameLayout.startShimmer()

        nativeHolder.nativeAdLoader?.setRevenueListener { ad -> adCallback.onAdRevenuePaid(ad) }
        nativeHolder.nativeAdLoader?.setNativeAdListener(object : MaxNativeAdListener() {
            override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, ad: MaxAd) {
                shimmerFrameLayout.stopShimmer()
                view.removeAllViews()
                if (nativeHolder.native != null) {
                    nativeHolder.nativeAdLoader?.destroy(nativeHolder.native)
                }
                nativeHolder.native = ad
                view.addView(nativeAdView)
                adCallback.onNativeAdLoaded(nativeHolder.native, nativeAdView)
            }

            override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                shimmerFrameLayout.stopShimmer()
                adCallback.onAdFail(error.code.toString().replace("-", ""))
            }

            override fun onNativeAdClicked(ad: MaxAd) {
            }

            override fun onNativeAdExpired(p0: MaxAd) {
                nativeHolder.nativeAdLoader?.loadAd()
            }
        })
        nativeHolder.nativeAdLoader?.loadAd()
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