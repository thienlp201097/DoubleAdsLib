package com.dktlib.ironsourcelib.utils.unityads

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dktlib.ironsourcelib.R
import com.dktlib.ironsourcelib.utils.callback.IUnityAdsCallBack
import com.dktlib.ironsourcelib.utils.callback.InitComplete
import com.dktlib.ironsourcelib.utils.callback.LoadCallBack
import com.facebook.shimmer.ShimmerFrameLayout
import com.unity3d.ads.IUnityAdsInitializationListener
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions
import com.unity3d.ads.metadata.MediationMetaData
import com.unity3d.ads.metadata.PlayerMetaData
import com.unity3d.services.banners.BannerErrorInfo
import com.unity3d.services.banners.BannerView
import com.unity3d.services.banners.UnityBannerSize

object UnityAdsUtils {
    private val interstitialPlacementId = "video"
    private val rewardedPlacementId = "rewardedVideo"
    var ordinal = 1
    private val LOGTAG = "UnityAdsExample"
    var dialogFullScreen: Dialog? = null
    var shimmerFrameLayout: ShimmerFrameLayout?=null
    fun initUnityAds(application: Application, id : String, isTestMode : Boolean, initComplete: InitComplete){
        if (id.isEmpty()) {
            Toast.makeText(
                application,
                "Missing Game ID",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        UnityAds.initialize(
            application,
            id,
            isTestMode,
            object : IUnityAdsInitializationListener {
                override fun onInitializationComplete() {
                    initComplete.onInitializationComplete()
                }

                override fun onInitializationFailed(
                    error: UnityAds.UnityAdsInitializationError,
                    message: String
                ) {
                    Log.e(LOGTAG, "Unity Ads initialization failed: [$error] $message")
                    initComplete.onInitializationFailed()
                }
            })

        // Store entered Game ID in App Settings
        val preferences: SharedPreferences = application.getSharedPreferences("Settings",
            AppCompatActivity.MODE_PRIVATE
        )
        val preferencesEdit = preferences.edit()
        preferencesEdit.putString("gameId", id)
        preferencesEdit.apply()
    }

    fun loadAndShowAd(activity: Activity, canSkip: Boolean, iUnityAdsCallBack: IUnityAdsCallBack){
        val placementToLoad: String = if (canSkip) interstitialPlacementId else rewardedPlacementId
        dialogLoading(activity)
        val playerMetaData = PlayerMetaData(activity)
        playerMetaData.setServerId("rikshot")
        playerMetaData.commit()
        val ordinalMetaData = MediationMetaData(activity)
        ordinalMetaData.setOrdinal(ordinal++)
        ordinalMetaData.commit()
        Log.v(LOGTAG, "Loading ad for $placementToLoad...")
        UnityAds.load(placementToLoad, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String) {
                Log.v(LOGTAG, "Ad for $placementId loaded")
                UnityAds.show(
                    activity,
                    placementToLoad,
                    UnityAdsShowOptions(),
                    object : IUnityAdsShowListener {
                        override fun onUnityAdsShowFailure(
                            placementId: String,
                            error: UnityAds.UnityAdsShowError,
                            message: String
                        ) {
                            Log.e(LOGTAG, "onUnityAdsShowFailure: $error - $message")
                            dismissAdDialog()
                            iUnityAdsCallBack.onFailure()
                        }

                        override fun onUnityAdsShowStart(placementId: String) {
                            Handler(Looper.getMainLooper()).postDelayed({
                                dismissAdDialog()
                            },500)
                            Log.v(LOGTAG, "onUnityAdsShowStart: $placementId")
                            iUnityAdsCallBack.onShow()
                        }

                        override fun onUnityAdsShowClick(placementId: String) {
                            Log.v(LOGTAG, "onUnityAdsShowClick: $placementId")
                        }

                        override fun onUnityAdsShowComplete(
                            placementId: String,
                            state: UnityAds.UnityAdsShowCompletionState
                        ) {
                            Log.v(LOGTAG, "onUnityAdsShowComplete: $placementId")
                            iUnityAdsCallBack.onClose()
                        }
                    })
            }

            override fun onUnityAdsFailedToLoad(
                placementId: String,
                error: UnityAds.UnityAdsLoadError,
                message: String
            ) {
                Log.e(LOGTAG, "Ad for $placementId failed to load: [$error] $message")
                dismissAdDialog()
                iUnityAdsCallBack.onFailure()
            }
        })
    }

    fun loadAd(context: Context, canSkip: Boolean, loadCallBack: LoadCallBack) {
        val placementToLoad: String = if (canSkip) interstitialPlacementId else rewardedPlacementId
        val playerMetaData = PlayerMetaData(context)
        playerMetaData.setServerId("rikshot")
        playerMetaData.commit()
        val ordinalMetaData = MediationMetaData(context)
        ordinalMetaData.setOrdinal(ordinal++)
        ordinalMetaData.commit()
        Log.v(LOGTAG, "Loading ad for $placementToLoad...")
        UnityAds.load(placementToLoad, object : IUnityAdsLoadListener {
            override fun onUnityAdsAdLoaded(placementId: String) {
                Log.v(LOGTAG, "Ad for $placementId loaded")
                loadCallBack.onUnityAdsAdLoaded()
            }

            override fun onUnityAdsFailedToLoad(
                placementId: String,
                error: UnityAds.UnityAdsLoadError,
                message: String
            ) {
                Log.e(LOGTAG, "Ad for $placementId failed to load: [$error] $message")
                loadCallBack.onUnityAdsFailedToLoad()
            }
        })
    }

    fun showAd(activity: Activity, canSkip: Boolean, iUnityAdsCallBack: IUnityAdsCallBack) {
        val placementToShow: String = if (canSkip) interstitialPlacementId else rewardedPlacementId
        dialogLoading(activity)
        Handler(Looper.getMainLooper()).postDelayed({
            UnityAds.show(
                activity,
                placementToShow,
                UnityAdsShowOptions(),
                object : IUnityAdsShowListener {
                    override fun onUnityAdsShowFailure(
                        placementId: String,
                        error: UnityAds.UnityAdsShowError,
                        message: String
                    ) {
                        Log.e(LOGTAG, "onUnityAdsShowFailure: $error - $message")
                        dismissAdDialog()
                        iUnityAdsCallBack.onFailure()
                    }

                    override fun onUnityAdsShowStart(placementId: String) {
                        Log.v(LOGTAG, "onUnityAdsShowStart: $placementId")
                        Handler(Looper.getMainLooper()).postDelayed({
                            dismissAdDialog()
                        },500)
                        iUnityAdsCallBack.onShow()
                    }

                    override fun onUnityAdsShowClick(placementId: String) {
                        Log.v(LOGTAG, "onUnityAdsShowClick: $placementId")
                    }

                    override fun onUnityAdsShowComplete(
                        placementId: String,
                        state: UnityAds.UnityAdsShowCompletionState
                    ) {
                        Log.v(LOGTAG, "onUnityAdsShowComplete: $placementId")
                        iUnityAdsCallBack.onClose()
                    }
                })
        },800)
    }

    fun showBanner(activity: Activity, viewGroup: ViewGroup, width : Int, height : Int){
        val tagView: View =
            activity.layoutInflater.inflate(R.layout.banner_shimmer_layout, null, false)
        viewGroup.addView(tagView, 0)
        val shimmerFrameLayout: ShimmerFrameLayout =
            tagView.findViewById(R.id.shimmer_view_container)
        shimmerFrameLayout.startShimmer()
        val bottomBanner = BannerView(activity, "bannerads", UnityBannerSize(width, height))
        val bannerListener: BannerView.IListener = object : BannerView.IListener {
            override fun onBannerLoaded(bannerAdView: BannerView) {
                Log.v(LOGTAG, "onBannerLoaded: " + bannerAdView.placementId)
                shimmerFrameLayout.stopShimmer()
            }

            fun onBannerShown(bannerAdView: BannerView) {
                Log.v(LOGTAG, "onBannerShown : " + bannerAdView.placementId)
            }

            override fun onBannerFailedToLoad(bannerAdView: BannerView, errorInfo: BannerErrorInfo) {
                shimmerFrameLayout.stopShimmer()
                Log.e(LOGTAG, "Unity Ads failed to load banner for " + bannerAdView.placementId + " with error: [" + errorInfo.errorCode + "] " + errorInfo.errorMessage)
            }

            override fun onBannerClick(bannerAdView: BannerView) {
                Log.v(LOGTAG, "onBannerClick: " + bannerAdView.placementId)
            }

            override fun onBannerLeftApplication(bannerAdView: BannerView) {
                Log.v(LOGTAG, "onBannerLeftApplication: " + bannerAdView.placementId)
            }
        }
        bottomBanner.listener = bannerListener
        bottomBanner.load()
        viewGroup.addView(bottomBanner,1)
    }

    fun dialogLoading(context: Activity) {
        dialogFullScreen = Dialog(context)
        dialogFullScreen?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogFullScreen?.setContentView(R.layout.dialog_full_screen)
        dialogFullScreen?.setCancelable(false)
        dialogFullScreen?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        dialogFullScreen?.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        try {
            if (!context.isFinishing && dialogFullScreen != null && dialogFullScreen?.isShowing == false) {
                dialogFullScreen?.show()
            }
        }catch (ignored: Exception) {
        }
    }

    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var vau = cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
        Log.e("isNetworkConnected", "0" + vau)
        return vau
    }

    fun dismissAdDialog() {
        try {
            if (dialogFullScreen != null && dialogFullScreen?.isShowing == true) {
                dialogFullScreen?.dismiss()
            }
        }catch (_: Exception){

        }
    }
}