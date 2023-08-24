package com.dktlib.ironsourcelib

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.dktlib.ironsourcelib.NativeFunc.Companion.populateNativeAdView
import com.dktlib.ironsourcelib.R
import com.dktlib.ironsourcelib.utils.SweetAlert.SweetAlertDialog
import com.dktlib.ironsourcelib.utils.admod.AdCallBackInterLoad
import com.dktlib.ironsourcelib.utils.admod.AdCallbackNew
import com.dktlib.ironsourcelib.utils.admod.AdsInterCallBack
import com.dktlib.ironsourcelib.utils.admod.InterHolderAdmod
import com.dktlib.ironsourcelib.utils.admod.NativeHolderAdmod
import com.dktlib.ironsourcelib.utils.admod.RewardAdCallback
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoadCallback
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Date
import java.util.Locale

object AdmodUtils {
    //Dialog loading
    @JvmField
    var dialog: SweetAlertDialog? = null
    var dialogFullScreen: Dialog? = null
    // Biến check lần cuối hiển thị quảng cáo
    var lastTimeShowInterstitial: Long = 0
    // Timeout init admob
    var timeOut = 0
    //Check quảng cáo đang show hay không
    @JvmField
    var isAdShowing = false
    var isClick = false
    //Ẩn hiện quảng cáo
    @JvmField
    var isShowAds = true
    //Dùng ID Test để hiển thị quảng cáo
    @JvmField
    var isTesting = false
    //List device test
    var testDevices: MutableList<String> = ArrayList()
    //Reward Ads
    @JvmField
    var mRewardedAd: RewardedAd? = null
    var mInterstitialAd: InterstitialAd? = null
    var shimmerFrameLayout: ShimmerFrameLayout?=null
    //id thật
    var idIntersitialReal: String? = null
    //Hàm Khởi tạo admob
    @JvmStatic
    fun initAdmob(context: Context?, timeout: Int, isDebug: Boolean, isEnableAds: Boolean) {
        timeOut = timeout
        if (timeOut < 5000 && timeout != 0) {
            Toast.makeText(context, "Nên để limit time ~10000", Toast.LENGTH_LONG).show()
        }
        timeOut = if (timeout > 0) {
            timeout
        } else {
            10000
        }
        isTesting = isDebug
        isShowAds = isEnableAds
        MobileAds.initialize(context!!) { initializationStatus: InitializationStatus? -> }
        initListIdTest()
        val requestConfiguration = RequestConfiguration.Builder()
            .setTestDeviceIds(testDevices)
            .build()
        MobileAds.setRequestConfiguration(requestConfiguration)
        initAdRequest(timeout)
    }

    var adRequest: AdRequest? = null
    // get AdRequest
    @JvmStatic
    fun initAdRequest(timeOut: Int) {
        adRequest = AdRequest.Builder()
            .setHttpTimeoutMillis(timeOut)
            .build()
    }

    fun initListIdTest() {
        testDevices.add("727D4F658B63BDFA0EFB164261AAE54")
        testDevices.add("3FA34D6F6B2DCF88DED51A6AF263E3F0")
        testDevices.add("482996BF6946FBE1B9FFD3975144D084")
        testDevices.add("8619926A823916A224795141B93B7E0B")
        testDevices.add("6399D5AEE5C75205B6C0F6755365CF21")
        testDevices.add("2E379568A9F147A64B0E0C9571DE812D")
        testDevices.add("A0518C6FA4396B91F82B9656DE83AFC7")
        testDevices.add("C8EEFFC32272E3F1018FC72ECBD46F0C")
        testDevices.add("284A7F7624F1131E7341ECDCBBCDF9A8")
        testDevices.add("FEECD9793CCCE1E0FF8D392B0DB65559")
        testDevices.add("D34AE6EC4CBA619D6243B03D4E31EED6")
        testDevices.add("25F9EEACB11D46869D2854923615D839")
        testDevices.add("A5CB09DBBE486E3421502DFF53070339")
        testDevices.add("5798E06F645D797640A9C4B90B6CBEA7")
        testDevices.add("E91FD94E971864C3880FB434D1C39A03")
        testDevices.add("50ACF2DAA0884FF8B08F7C823E046DEA")
        testDevices.add("97F07D4A6D0145F9DB7114B63D3D8E9B")
        testDevices.add("4C96668EC6F204034D0CDCE1B94A4E65")
        testDevices.add("00A52C89E14694316247D3CA3DF19F6B")
        testDevices.add("C38A7BF0A80E31BD6B76AF6D0C1EE4A1")
        testDevices.add("CE604BDCEFEE2B9125CCFFC53E96022E")
        testDevices.add("39D7026016640CEA1502836C6EF3776D")
        testDevices.add("A99C99C378EE9BDE5D3DE404D3A4A812")
        testDevices.add("EB28F4CCC32F14DC98068A063B97E6CE")
        //Oneplus GM1910
        testDevices.add("D94D5042C9CC42DA75DCC0C4C233A500")
        //Redmi note 4
        testDevices.add("3FA34D6F6B2DCF88DED51A6AF263E3F0")
        //Galaxy M11
        testDevices.add("AF6ABEDE9EE7719295BF5E6F19A40452")
        //Samsung SM-G610F
        testDevices.add("2B018C52668CBA0B033F411955A5B561")
        //Realme RMX1851
        testDevices.add("39D7026016640CEA1502836C6EF3776D")
        //Redmi 5 Plus
        testDevices.add("CE604BDCEFEE2B9125CCFFC53E96022E")
        //Redmi 9A
        testDevices.add("13D67F452A299DB825A348917D52D640")
        //POCO X3
        testDevices.add("7D94825002E2407B75A9D5378194CFA9")
        //Galaxy A21S
        testDevices.add("98EFC23E56FA228C791F8C3AFBEE44D4")
        //Oppo CPH1825
        testDevices.add("805702C1D9D4FD957AFE14F3D69E79F7")
        //Xiaomi Redmi Note 7
        testDevices.add("9C62AAC36B9F23413AF4D66FE48F9E9B")
    }

    //check open network
    @JvmStatic
    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }

    interface BannerCallBack {
        fun onLoad()
        fun onFailed()
        fun onPaid(adValue: AdValue?, mAdView: String?)
    }
    @JvmStatic
    fun loadAdBanner(
        activity: Activity,
        bannerId: String?,
        viewGroup: ViewGroup,
        bannerAdCallback: BannerCallBack
    ) {
        var bannerId = bannerId
        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            bannerAdCallback.onFailed()
            return
        }
        val mAdView = AdView(activity)
        if (isTesting) {
            bannerId = activity.getString(R.string.test_ads_admob_banner_id)
        }
        mAdView.adUnitId = bannerId!!
        val adSize = getAdSize(activity)
        mAdView.setAdSize(adSize)
        viewGroup.removeAllViews()
        val tagView = activity.layoutInflater.inflate(R.layout.banner_shimmer_layout, null, false)
        viewGroup.addView(tagView, 0)
        viewGroup.addView(mAdView, 1)
        shimmerFrameLayout = tagView.findViewById(R.id.shimmer_view_container)
        shimmerFrameLayout?.startShimmer()
        mAdView.onPaidEventListener =
            OnPaidEventListener { adValue -> bannerAdCallback.onPaid(adValue, mAdView.adUnitId) }
        mAdView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                shimmerFrameLayout?.stopShimmer()
                viewGroup.removeView(tagView)
                bannerAdCallback.onLoad()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(" Admod", "failloadbanner" + adError.message)
                shimmerFrameLayout?.stopShimmer()
                viewGroup.removeView(tagView)
                bannerAdCallback.onFailed()
            }

            override fun onAdOpened() {}
            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        }
        if (adRequest != null) {
            mAdView.loadAd(adRequest!!)
        }
        Log.e(" Admod", "loadAdBanner")
    }
    interface BannerCollapsibleAdCallback {
        fun onBannerAdLoaded(adSize: AdSize)
        fun onAdFail()
        fun onAdPaid(adValue: AdValue, adUnit: String)
    }
    @JvmStatic
    fun loadAdBannerCollapsible(
        activity: Activity,
        bannerId: String?,
        collapsibleBannersize: CollapsibleBanner,
        viewGroup: ViewGroup,
        callback: BannerCollapsibleAdCallback
    ) {
        var bannerId = bannerId
        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            return
        }
        val mAdView = AdView(activity)
        if (isTesting) {
            bannerId = activity.getString(R.string.test_ads_admob_banner_id)
        }
        mAdView.adUnitId = bannerId!!
        val adSize = getAdSize(activity)
        mAdView.setAdSize(adSize)
        viewGroup.removeAllViews()
        val tagView = activity.layoutInflater.inflate(R.layout.banner_shimmer_layout, null, false)
        viewGroup.addView(tagView, 0)
        viewGroup.addView(mAdView, 1)
        shimmerFrameLayout = tagView.findViewById(R.id.shimmer_view_container)
        shimmerFrameLayout?.startShimmer()

        mAdView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                mAdView.onPaidEventListener = OnPaidEventListener { adValue -> callback.onAdPaid(adValue,mAdView.adUnitId) }
                shimmerFrameLayout?.stopShimmer()
                viewGroup.removeView(tagView)
                callback.onBannerAdLoaded(adSize)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(" Admod", "failloadbanner" + adError.message)
                shimmerFrameLayout?.stopShimmer()
                viewGroup.removeView(tagView)
                callback.onAdFail()
            }

            override fun onAdOpened() {}
            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        }
        val extras = Bundle()
        var anchored = "top"
        anchored = if (collapsibleBannersize === CollapsibleBanner.TOP) {
            "top"
        } else {
            "bottom"
        }
        extras.putString("collapsible", anchored)
        val adRequest2 = AdRequest.Builder().addNetworkExtrasBundle(
            AdMobAdapter::class.java, extras
        )
            .build()
        if (adRequest2 != null) {
            mAdView.loadAd(adRequest2)
        }
        Log.e(" Admod", "loadAdBanner")
    }

    @JvmStatic
    fun loadAdBannerCollapsibleNoShimmer(
        activity: Activity,
        bannerId: String?,
        collapsibleBannersize: CollapsibleBanner,
        viewGroup: ViewGroup,
        callback: BannerAdCallback
    ) {
        var bannerId = bannerId
        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            return
        }
        val mAdView = AdView(activity)
        if (isTesting) {
            bannerId = activity.getString(R.string.test_ads_admob_banner_id)
        }
        mAdView.adUnitId = bannerId!!
        val adSize = getAdSize(activity)
        mAdView.setAdSize(adSize)
        viewGroup.addView(mAdView, 0)
        mAdView.onPaidEventListener = OnPaidEventListener { adValue -> callback.onAdPaid(adValue,mAdView.adUnitId) }
        mAdView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                callback.onBannerAdLoaded(adSize)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(" Admod", "failloadbanner" + adError.message)
                callback.onAdFail()
            }

            override fun onAdOpened() {}
            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        }
        val extras = Bundle()
        var anchored = "top"
        anchored = if (collapsibleBannersize === CollapsibleBanner.TOP) {
            "top"
        } else {
            "bottom"
        }
        extras.putString("collapsible", anchored)
        val adRequest2 = AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
        if (adRequest2 != null) {
            mAdView.loadAd(adRequest2)
        }
        Log.e(" Admod", "loadAdBanner")
    }

    private fun getAdSize(context: Activity): AdSize {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        val display = context.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val widthPixels = outMetrics.widthPixels.toFloat()
        val density = outMetrics.density
        val adWidth = (widthPixels / density).toInt()
        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
    }
    interface NativeAdCallback {
        fun onLoadedAndGetNativeAd(ad: NativeAd?)
        fun onNativeAdLoaded()
        fun onAdFail(error: String?)
        fun onAdPaid(adValue: AdValue?, adUnit : String)
    }
    //Load native 1 in here
    @JvmStatic
    fun loadAndGetNativeAds(
        context: Context,
        nativeHolder: NativeHolderAdmod,
        adCallback: NativeAdCallback
    ) {
        if (!isShowAds || !isNetworkConnected(context)) {
            adCallback.onAdFail("No internet")
            return
        }
        //If native is loaded return
        if (nativeHolder.nativeAd != null) {
            Log.d("===AdsLoadsNative", "Native not null")
            return
        }
        if (isTesting) {
            nativeHolder.ads = context.getString(R.string.test_ads_admob_native_id)
        }
        nativeHolder.isLoad = true
        val adLoader: AdLoader = AdLoader.Builder(context, nativeHolder.ads)
            .forNativeAd { nativeAd ->
                nativeHolder.nativeAd = nativeAd
                nativeHolder.isLoad = false
                nativeHolder.native_mutable.value = nativeAd
                nativeAd.setOnPaidEventListener { adValue: AdValue? -> adCallback.onAdPaid(adValue,nativeHolder.ads) }
                adCallback.onLoadedAndGetNativeAd(nativeAd)
            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adCallback.onAdFail("errorId1_"+adError.message)
                    Log.e("Admodfail", "onAdFailedToLoad" + adError.message)
                    Log.e("Admodfail", "errorCodeAds" + adError.cause)
                    loadAndGetNativeAds2(context, nativeHolder, adCallback)
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build()).build()
        if (adRequest != null) {
            adLoader.loadAd(adRequest!!)
        }
    }

    //Load native 2 in here
    @JvmStatic
    fun loadAndGetNativeAds2(
        context: Context,
        nativeHolder: NativeHolderAdmod,
        adCallback: NativeAdCallback
    ) {
        if (!isShowAds || !isNetworkConnected(context)) {
            return
        }
        if (isTesting) {
            nativeHolder.ads2 = context.getString(R.string.test_ads_admob_native_id)
        }
        val adLoader: AdLoader = AdLoader.Builder(context, nativeHolder.ads2)
            .forNativeAd { nativeAd ->
                nativeHolder.nativeAd = nativeAd
                nativeHolder.isLoad = false
                nativeHolder.native_mutable.value = nativeAd
                nativeAd.setOnPaidEventListener { adValue: AdValue? -> adCallback.onAdPaid(adValue,nativeHolder.ads2) }
                adCallback.onLoadedAndGetNativeAd(nativeAd)
                //viewGroup.setVisibility(View.VISIBLE);
            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("Admodfail", "onAdFailedToLoad" + adError.message)
                    Log.e("Admodfail", "errorCodeAds" + adError.cause)
                    nativeHolder.nativeAd = null
                    nativeHolder.isLoad = false
                    nativeHolder.native_mutable.value = null
                    adCallback.onAdFail("errorId2_"+adError.message)
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build()).build()
        if (adRequest != null) {
            adLoader.loadAd(adRequest!!)
        }
        Log.e("Admod", "loadAdNativeAds")
    }

    interface AdsNativeCallBackAdmod {
        fun NativeLoaded()
        fun NativeFailed()
    }

    @JvmStatic
    fun showNativeAdsWithLayout(
        activity: Activity,
        nativeHolder: NativeHolderAdmod,
        viewGroup: ViewGroup,
        layout: Int,
        size: GoogleENative,
        callback: AdsNativeCallBackAdmod
    ) {
        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            return
        }
        if (shimmerFrameLayout != null) {
            shimmerFrameLayout?.stopShimmer()
        }
        viewGroup.removeAllViews()
        if (!nativeHolder.isLoad) {
            if (nativeHolder.nativeAd != null) {
                val adView = activity.layoutInflater
                    .inflate(layout, null) as NativeAdView
                populateNativeAdView(nativeHolder.nativeAd!!, adView, GoogleENative.UNIFIED_MEDIUM)
                if (shimmerFrameLayout != null) {
                    shimmerFrameLayout?.stopShimmer()
                }
                nativeHolder.native_mutable.removeObservers((activity as LifecycleOwner))
                viewGroup.removeAllViews()
                viewGroup.addView(adView)
                callback.NativeLoaded()
            } else {
                if (shimmerFrameLayout != null) {
                    shimmerFrameLayout?.stopShimmer()
                }
                nativeHolder.native_mutable.removeObservers((activity as LifecycleOwner))
                callback.NativeFailed()
            }
        } else {
            val tagView: View = if (size === GoogleENative.UNIFIED_MEDIUM) {
                activity.layoutInflater.inflate(R.layout.layoutnative_loading_medium, null, false)
            } else {
                activity.layoutInflater.inflate(R.layout.layoutnative_loading_small, null, false)
            }
            viewGroup.addView(tagView, 0)
            if (shimmerFrameLayout == null) shimmerFrameLayout =
                tagView.findViewById(R.id.shimmer_view_container)
            shimmerFrameLayout?.startShimmer()
            nativeHolder.native_mutable.observe((activity as LifecycleOwner)) { nativeAd: NativeAd? ->
                if (nativeAd != null) {
                    val adView = activity.layoutInflater.inflate(layout, null) as NativeAdView
                    populateNativeAdView(nativeAd, adView, GoogleENative.UNIFIED_MEDIUM)
                    if (shimmerFrameLayout != null) {
                        shimmerFrameLayout?.stopShimmer()
                    }
                    viewGroup.removeAllViews()
                    viewGroup.addView(adView)
                    callback.NativeLoaded()
                    nativeHolder.native_mutable.removeObservers((activity as LifecycleOwner))
                } else {
                    if (shimmerFrameLayout != null) {
                        shimmerFrameLayout?.stopShimmer()
                    }
                    callback.NativeFailed()
                    nativeHolder.native_mutable.removeObservers((activity as LifecycleOwner))
                }
            }
        }
    }

    // ads native
    @JvmStatic
    fun loadAndShowNativeAdsWithLayout(
        activity: Activity,
        s: String?,
        viewGroup: ViewGroup,
        layout: Int,
        size: GoogleENative,
        adCallback: NativeAdCallback
    ) {
        var s = s
        val tagView: View = if (size === GoogleENative.UNIFIED_MEDIUM) {
            activity.layoutInflater.inflate(R.layout.layoutnative_loading_medium, null, false)
        } else {
            activity.layoutInflater.inflate(R.layout.layoutnative_loading_small, null, false)
        }
        viewGroup.addView(tagView, 0)
        val shimmerFrameLayout =
            tagView.findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)
        shimmerFrameLayout.startShimmer()
        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            return
        }
        if (isTesting) {
            s = activity.getString(R.string.test_ads_admob_native_id)
        }
        val adLoader: AdLoader = AdLoader.Builder(activity, s!!)
            .forNativeAd { nativeAd ->
                adCallback.onNativeAdLoaded()
                val adView = activity.layoutInflater
                    .inflate(layout, null) as NativeAdView
                populateNativeAdView(nativeAd, adView, GoogleENative.UNIFIED_MEDIUM)
                shimmerFrameLayout.stopShimmer()
                viewGroup.removeAllViews()
                viewGroup.addView(adView)
                //viewGroup.setVisibility(View.VISIBLE);
            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("Admodfail", "onAdFailedToLoad" + adError.message)
                    Log.e("Admodfail", "errorCodeAds" + adError.cause)
                    shimmerFrameLayout.stopShimmer()
                    viewGroup.removeAllViews()
                    adCallback.onAdFail(adError.message)
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build()).build()
        if (adRequest != null) {
            adLoader.loadAd(adRequest!!)
        }
        Log.e("Admod", "loadAdNativeAds")
    }
    @JvmStatic
    fun loadAndShowNativeAdsWithLayoutMultiAds(
        activity: Activity,
        nativeHolder: NativeHolderAdmod,
        viewGroup: ViewGroup,
        layout: Int,
        size: GoogleENative,
        adCallback: NativeAdCallback
    ) {
        Log.d("===Native","Native1")
        var s = nativeHolder.ads
        val tagView: View = if (size === GoogleENative.UNIFIED_MEDIUM) {
            activity.layoutInflater.inflate(R.layout.layoutnative_loading_medium, null, false)
        } else {
            activity.layoutInflater.inflate(R.layout.layoutnative_loading_small, null, false)
        }
        viewGroup.addView(tagView, 0)
        val shimmerFrameLayout =
            tagView.findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)
        shimmerFrameLayout.startShimmer()
        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            return
        }
        if (isTesting) {
            s = activity.getString(R.string.test_ads_admob_native_id)
        }
        val adLoader: AdLoader = AdLoader.Builder(activity, s)
            .forNativeAd { nativeAd ->
                adCallback.onNativeAdLoaded()
                val adView = activity.layoutInflater
                    .inflate(layout, null) as NativeAdView
                populateNativeAdView(nativeAd, adView, GoogleENative.UNIFIED_MEDIUM)
                shimmerFrameLayout.stopShimmer()
                viewGroup.removeAllViews()
                viewGroup.addView(adView)
                //viewGroup.setVisibility(View.VISIBLE);
            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("Admodfail", "onAdFailedToLoad" + adError.message)
                    Log.e("Admodfail", "errorCodeAds" + adError.cause)
                    loadAndShowNativeAdsWithLayout2(activity,nativeHolder.ads2,nativeHolder,viewGroup,layout,shimmerFrameLayout,adCallback)
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build()).build()
        if (adRequest != null) {
            adLoader.loadAd(adRequest!!)
        }
        Log.e("Admod", "loadAdNativeAds")
    }
    @JvmStatic
    fun loadAndShowNativeAdsWithLayout2(
        activity: Activity,
        s: String?,nativeHolder: NativeHolderAdmod,
        viewGroup: ViewGroup,
        layout: Int,shimmerFrameLayout: ShimmerFrameLayout,
        adCallback: NativeAdCallback
    ) {
        Log.d("===Native","Native2")
        var s = s
        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            return
        }
        if (isTesting) {
            s = activity.getString(R.string.test_ads_admob_native_id)
        }
        val adLoader: AdLoader = AdLoader.Builder(activity, s!!)
            .forNativeAd { nativeAd ->
                adCallback.onNativeAdLoaded()
                val adView = activity.layoutInflater
                    .inflate(layout, null) as NativeAdView
                populateNativeAdView(nativeAd, adView, GoogleENative.UNIFIED_MEDIUM)
                shimmerFrameLayout.stopShimmer()
                viewGroup.removeAllViews()
                viewGroup.addView(adView)
                nativeHolder.isLoad = false
                //viewGroup.setVisibility(View.VISIBLE);
            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("Admodfail", "onAdFailedToLoad" + adError.message)
                    Log.e("Admodfail", "errorCodeAds" + adError.cause)
                    shimmerFrameLayout.stopShimmer()
                    viewGroup.removeAllViews()
                    nativeHolder.isLoad = false
                    adCallback.onAdFail(adError.message)
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build()).build()
        if (adRequest != null) {
            adLoader.loadAd(adRequest!!)
        }
        Log.e("Admod", "loadAdNativeAds")
    }

    // ads native
    @SuppressLint("StaticFieldLeak")
    @JvmStatic
    fun loadAndShowNativeAds(
        activity: Activity,
        s: String?,
        viewGroup: ViewGroup,
        size: GoogleENative,
        adCallback: NativeAdCallback
    ) {
        var s = s
        val tagView: View = if (size === GoogleENative.UNIFIED_MEDIUM) {
            activity.layoutInflater.inflate(R.layout.layoutnative_loading_medium, null, false)
        } else {
            activity.layoutInflater.inflate(R.layout.layoutnative_loading_small, null, false)
        }
        viewGroup.addView(tagView, 0)
        val shimmerFrameLayout =
            tagView.findViewById<ShimmerFrameLayout>(R.id.shimmer_view_container)
        shimmerFrameLayout.startShimmer()
        if (!isShowAds || !isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            return
        }
        if (isTesting) {
            s = activity.getString(R.string.test_ads_admob_native_id)
        }
        val adLoader: AdLoader = AdLoader.Builder(activity, s!!)
            .forNativeAd { nativeAd ->
                adCallback.onNativeAdLoaded()
                var id = 0
                id = if (size === GoogleENative.UNIFIED_MEDIUM) {
                    R.layout.ad_unified_medium
                } else {
                    R.layout.ad_unified_small
                }
                val adView = activity.layoutInflater
                    .inflate(id, null) as NativeAdView
                populateNativeAdView(nativeAd, adView, size)
                shimmerFrameLayout.stopShimmer()
                viewGroup.removeAllViews()
                viewGroup.addView(adView)
                //viewGroup.setVisibility(View.VISIBLE);
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("Admodfail", "onAdFailedToLoad" + adError.message)
                    Log.e("Admodfail", "errorCodeAds" + adError.cause)
                    shimmerFrameLayout.stopShimmer()
                    viewGroup.removeAllViews()
                    adCallback.onAdFail(adError.message)
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build()).build()
        if (adRequest != null) {
            adLoader.loadAd(adRequest!!)
        }
        Log.e("Admod", "loadAdNativeAds")
    }

    //Load Inter in here
    @JvmStatic
    fun loadAndGetAdInterstitial(
        activity: Context,
        interHolder: InterHolderAdmod,
        adLoadCallback: AdCallBackInterLoad
    ) {
        isAdShowing = false
        if (!isShowAds || !isNetworkConnected(activity)) {
            adLoadCallback.onAdFail(false)
            return
        }
        if (interHolder.inter != null) {
            Log.d("===AdsInter", "inter not null")
            return
        }
        interHolder.check = true
        if (adRequest == null) {
            initAdRequest(timeOut)
        }
        if (isTesting) {
            interHolder.ads = activity.getString(R.string.test_ads_admob_inter_id)
            interHolder.ads2 = activity.getString(R.string.test_ads_admob_inter_id)
        }
        idIntersitialReal = interHolder.ads
        val idLoadInter2 = interHolder.ads2
        InterstitialAd.load(
            activity,
            idIntersitialReal!!,
            adRequest!!,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    if (isClick) {
                        interHolder.mutable.value = interstitialAd
                    }
                    interHolder.inter = interstitialAd
                    interHolder.check = false
                    adLoadCallback.onAdLoaded(interstitialAd, false)
                    Log.i("adLog", "onAdLoaded")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    loadAndGetAdInterstitialId2(
                        activity,
                        idLoadInter2,
                        adLoadCallback,
                        interHolder
                    )
                }
            })
    }

    //Load Inter 2 in here if inter 1 false
    @JvmStatic
    fun loadAndGetAdInterstitialId2(
        activity: Context,
        admobId2: String,
        adLoadCallback: AdCallBackInterLoad,
        interHolder: InterHolderAdmod
    ) {
        var admobId2 = admobId2
        isAdShowing = false
        if (!isShowAds || !isNetworkConnected(activity)) {
            adLoadCallback.onAdFail(false)
            return
        }
        if (isTesting) {
            admobId2 = activity.getString(R.string.test_ads_admob_inter_id)
        }
        if (adRequest == null) {
            initAdRequest(timeOut)
        }
        idIntersitialReal = admobId2
        InterstitialAd.load(
            activity,
            idIntersitialReal!!,
            adRequest!!,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    if (isClick) {
                        interHolder.mutable.value = interstitialAd
                    }
                    interHolder.inter = interstitialAd
                    interHolder.check = false
                    adLoadCallback.onAdLoaded(interstitialAd, false)
                    Log.i("adLog", "onAdLoaded")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isAdShowing = false
                    if (mInterstitialAd != null) {
                        mInterstitialAd = null
                    }
                    interHolder.check = false
                    if (isClick) {
                        interHolder.mutable.value = null
                    }
                    adLoadCallback.onAdFail(false)
                }
            })
    }

    //Show Inter in here
    @JvmStatic
    fun showAdInterstitialWithCallbackNotLoadNew(
        activity: Activity,
        interHolder: InterHolderAdmod,
        timeout: Long,
        adCallback: AdsInterCallBack?,
        enableLoadingDialog: Boolean
    ) {
        isClick = true
        //Check internet
        if (!isShowAds || !isNetworkConnected(activity)) {
            isAdShowing = false
            if (AppOpenManager.getInstance().isInitialized) {
                AppOpenManager.getInstance().isAppResumeEnabled = true
            }
            adCallback!!.onAdFail("No internet")
            return
        }
        adCallback!!.onAdLoaded()
        val handler = Handler(Looper.getMainLooper())
        //Check timeout show inter
        val runnable = Runnable {
            if (interHolder.check) {
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                isClick = false
                interHolder.mutable.removeObservers((activity as LifecycleOwner))
                isAdShowing = false
                dismissAdDialog()
                adCallback.onAdFail("timeout")
            }
        }
        handler.postDelayed(runnable, timeout)
        //Inter is Loading...
        if (interHolder.check) {
            if (enableLoadingDialog) {
                dialogLoading(activity)
            }
            interHolder.mutable.observe((activity as LifecycleOwner)) { aBoolean: InterstitialAd? ->
                if (aBoolean != null) {
                    interHolder.mutable.removeObservers((activity as LifecycleOwner))
                    isClick = false
                    Handler(Looper.getMainLooper()).postDelayed({
                        Log.d("===DelayLoad", "delay")
                        aBoolean.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                isAdShowing = false
                                if (AppOpenManager.getInstance().isInitialized) {
                                    AppOpenManager.getInstance().isAppResumeEnabled = true
                                }
                                isClick = false
                                //Set inter = null
                                interHolder.inter = null
                                interHolder.mutable.removeObservers((activity as LifecycleOwner))
                                interHolder.mutable.value = null
                                adCallback.onEventClickAdClosed()
                                dismissAdDialog()
                                Log.d("TAG", "The ad was dismissed.")
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                isAdShowing = false
                                if (AppOpenManager.getInstance().isInitialized) {
                                    AppOpenManager.getInstance().isAppResumeEnabled = true
                                }
                                isClick = false
                                isAdShowing = false
                                //Set inter = null
                                interHolder.inter = null
                                dismissAdDialog()
                                Log.e("Admodfail", "onAdFailedToLoad" + adError.message)
                                Log.e("Admodfail", "errorCodeAds" + adError.cause)
                                interHolder.mutable.removeObservers((activity as LifecycleOwner))
                                interHolder.mutable.value = null
                                adCallback.onAdFail(adError.message)
                            }

                            override fun onAdShowedFullScreenContent() {
                                handler.removeCallbacksAndMessages(null)
                                isAdShowing = true
                                adCallback.onAdShowed()
                                try {
                                    aBoolean.onPaidEventListener =
                                        OnPaidEventListener { adValue -> adCallback.onPaid(adValue,aBoolean.adUnitId) }
                                } catch (e: Exception) {
                                }
                            }
                        }
                        showInterstitialAdNew(activity, aBoolean, adCallback)
                    }, 400)
                }
            }
            return
        }
        //Load inter done
        if (interHolder.inter == null) {
            if (adCallback != null) {
                isAdShowing = false
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                adCallback.onAdFail("inter null")
            }
        } else {
            if (enableLoadingDialog) {
                dialogLoading(activity)
            }
            Handler(Looper.getMainLooper()).postDelayed({
                interHolder.inter!!.fullScreenContentCallback =
                    object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            isAdShowing = false
                            if (AppOpenManager.getInstance().isInitialized) {
                                AppOpenManager.getInstance().isAppResumeEnabled = true
                            }
                            isClick = false
                            interHolder.mutable.removeObservers((activity as LifecycleOwner))
                            interHolder.inter = null
                            adCallback.onEventClickAdClosed()
                            dismissAdDialog()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            isAdShowing = false
                            if (AppOpenManager.getInstance().isInitialized) {
                                AppOpenManager.getInstance().isAppResumeEnabled = true
                            }
                            isClick = false
                            interHolder.inter = null
                            interHolder.mutable.removeObservers((activity as LifecycleOwner))
                            isAdShowing = false
                            dismissAdDialog()
                            adCallback.onAdFail(adError.message)
                            Log.e("Admodfail", "onAdFailedToLoad" + adError.message)
                            Log.e("Admodfail", "errorCodeAds" + adError.cause)
                        }

                        override fun onAdShowedFullScreenContent() {
                            handler.removeCallbacksAndMessages(null)
                            isAdShowing = true
                            adCallback.onAdShowed()
                            try {
                                interHolder.inter!!.onPaidEventListener = OnPaidEventListener { adValue: AdValue? -> adCallback.onPaid(adValue,interHolder.inter!!.adUnitId) }
                            } catch (e: Exception) {
                            }
                        }
                    }
                showInterstitialAdNew(activity, interHolder.inter, adCallback)
            }, 400)
        }
    }

    @JvmStatic
    private fun showInterstitialAdNew(
        activity: Activity,
        mInterstitialAd: InterstitialAd?,
        callback: AdsInterCallBack?
    ) {
        if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && mInterstitialAd != null) {
            isAdShowing = true
            Handler().postDelayed({

                //Start activity before showing the ad
                callback!!.onStartAction()
                mInterstitialAd.onPaidEventListener =
                    OnPaidEventListener { adValue: AdValue? -> callback.onPaid(adValue,mInterstitialAd.adUnitId) }
                //Showing the ads
                mInterstitialAd.show(activity)
            }, 400)
        } else {
            isAdShowing = false
            if (AppOpenManager.getInstance().isInitialized) {
                AppOpenManager.getInstance().isAppResumeEnabled = true
            }
            dismissAdDialog()
            callback!!.onAdFail("onResum")
        }
    }

    @JvmStatic
    fun dismissAdDialog() {
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
        }
        if (dialogFullScreen != null && dialogFullScreen?.isShowing == true) {
            dialogFullScreen?.dismiss()
        }
    }
    @JvmStatic
    fun loadAndShowAdRewardWithCallback(
        activity: Activity,
        admobId: String?,
        adCallback2: RewardAdCallback,
        enableLoadingDialog: Boolean
    ) {
        var admobId = admobId
        mInterstitialAd = null
        isAdShowing = false
        if (!isShowAds || !isNetworkConnected(activity)) {
            adCallback2.onAdClosed()
            return
        }
        if (adRequest == null) {
            initAdRequest(timeOut)
        }
        if (isTesting) {
            admobId = activity.getString(R.string.test_ads_admob_reward_id)
        }
        if (enableLoadingDialog) {
            dialogLoading(activity)
        }
        isAdShowing = false
        if (AppOpenManager.getInstance().isInitialized) {
            AppOpenManager.getInstance().isAppResumeEnabled = false
        }
        RewardedAd.load(activity, admobId!!,
            adRequest!!, object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Handle the error.
                    mRewardedAd = null
                    adCallback2.onAdFail()
                    dismissAdDialog()
                    if (AppOpenManager.getInstance().isInitialized) {
                        AppOpenManager.getInstance().isAppResumeEnabled = true
                    }
                    isAdShowing = false
                    Log.e("Admodfail", "onAdFailedToLoad" + loadAdError.message)
                    Log.e("Admodfail", "errorCodeAds" + loadAdError.cause)
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    mRewardedAd = rewardedAd
                    if (mRewardedAd != null) {
                        mRewardedAd!!.fullScreenContentCallback =
                            object : FullScreenContentCallback() {
                                override fun onAdShowedFullScreenContent() {
                                    isAdShowing = true
                                    if (AppOpenManager.getInstance().isInitialized) {
                                        AppOpenManager.getInstance().isAppResumeEnabled = false
                                    }
                                }

                                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                    // Called when ad fails to show.
                                    if (adError.code != 1) {
                                        isAdShowing = false
                                        adCallback2.onAdFail()
                                        mRewardedAd = null
                                        dismissAdDialog()
                                    }
                                    if (AppOpenManager.getInstance().isInitialized) {
                                        AppOpenManager.getInstance().isAppResumeEnabled = true
                                    }
                                    Log.e("Admodfail", "onAdFailedToLoad" + adError.message)
                                    Log.e("Admodfail", "errorCodeAds" + adError.cause)
                                }

                                override fun onAdDismissedFullScreenContent() {
                                    // Called when ad is dismissed.
                                    // Set the ad reference to null so you don't show the ad a second time.
                                    mRewardedAd = null
                                    isAdShowing = false
                                    adCallback2.onAdClosed()
                                    if (AppOpenManager.getInstance().isInitialized) {
                                        AppOpenManager.getInstance().isAppResumeEnabled = true
                                    }
                                }
                            }
                        if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                            if (AppOpenManager.getInstance().isInitialized) {
                                AppOpenManager.getInstance().isAppResumeEnabled = false
                            }
                            mRewardedAd!!.show(activity) { adCallback2.onEarned() }
                            isAdShowing = true
                        } else {
                            mRewardedAd = null
                            dismissAdDialog()
                            isAdShowing = false
                            if (AppOpenManager.getInstance().isInitialized) {
                                AppOpenManager.getInstance().isAppResumeEnabled = true
                            }
                        }
                    } else {
                        isAdShowing = false
                        adCallback2.onAdFail()
                        dismissAdDialog()
                        if (AppOpenManager.getInstance().isInitialized) {
                            AppOpenManager.getInstance().isAppResumeEnabled = true
                        }
                    }
                }
            })
    }
    interface AdLoadCallback {
        fun onAdFail()
        fun onAdLoaded()
    }
    //Interstitial Reward ads
    @JvmField
    var mInterstitialRewardAd: RewardedInterstitialAd? = null
    @JvmStatic
    fun loadAdInterstitialReward(
        activity: Context,
        admobId: String,
        adLoadCallback: AdLoadCallback
    ) {
        var admobId = admobId
        if (!isShowAds || !isNetworkConnected(activity)) {
            return
        }
        if (isTesting) {
            admobId = activity.getString(R.string.test_ads_admob_inter_reward_id)
        } else {
            if (admobId == activity.getString(R.string.test_ads_admob_inter_reward_id) && !BuildConfig.DEBUG) {
//                Utils.getInstance().showDialogTitle(
//                    activity,
//                    "Warning",
//                    "Build bản release nhưng đang để id test ads",
//                    "Đã biết",
//                    DialogType.WARNING_TYPE,
//                    false,
//                    "",
//                    object : DialogCallback {
//                        override fun onClosed() {}
//                        override fun cancel() {}
//                    })
                return
            }
        }
        RewardedInterstitialAd.load(
            activity,
            admobId,
            adRequest!!,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialRewardAd: RewardedInterstitialAd) {
                    mInterstitialRewardAd = interstitialRewardAd
                    adLoadCallback.onAdLoaded()
                    Log.i("adLog", "onAdLoaded")
                    // Toast.makeText(activity, "success load ads", Toast.LENGTH_SHORT).show();
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Handle the error
                    Log.i("adLog", loadAdError.message)
                    val error = String.format(
                        "domain: %s, code: %d, message: %s",
                        loadAdError.domain, loadAdError.message, loadAdError.message
                    )
                    //                Toast.makeText(
//                        activity, "onAdFailedToLoad() with error: " + error, Toast.LENGTH_SHORT)
//                        .show();
                    adLoadCallback.onAdFail()
                }
            })
    }
    @JvmStatic
    fun showAdInterstitialRewardWithCallback(
        kInterstitialRewardAd: RewardedInterstitialAd?,
        activity: Activity,
        adCallback: RewardAdCallback
    ) {
        if (!isShowAds || !isNetworkConnected(activity)) {
            adCallback.onAdClosed()
            return
        }
        if (kInterstitialRewardAd != null) {
            kInterstitialRewardAd.show(
                activity,
                OnUserEarnedRewardListener { adCallback.onEarned() })
            kInterstitialRewardAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    adCallback.onAdClosed()
                    isAdShowing = false
                    Log.d("TAG", "The ad was dismissed.")
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    adCallback.onAdFail()
                    isAdShowing = false
                    mInterstitialAd = null
                    Log.d("TAG", "The ad failed to show.")
                }

                override fun onAdShowedFullScreenContent() {
                    mInterstitialAd = null
                    isAdShowing = true
                    Log.d("TAG", "The ad was shown.")
                }
            }
        } else {
            // Toast.makeText(activity, "Ad did not load", Toast.LENGTH_SHORT).show();
        }
    }
    @JvmStatic
    fun loadAdInterstitial(
        activity: Context,
        admobId: String?,
        adLoadCallback: AdCallbackNew,
        enableLoadingDialog: Boolean
    ) {
        var admobId = admobId
        mInterstitialAd = null
        isAdShowing = false
        if (!isShowAds || !isNetworkConnected(activity)) {
            adLoadCallback.onAdFail()
            return
        }
        if (AppOpenManager.getInstance().isInitialized) {
            if (!AppOpenManager.getInstance().isAppResumeEnabled) {
                return
            } else {
                isAdShowing = false
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = false
                }
            }
        }
        if (enableLoadingDialog) {
            dialog = SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE)
            dialog!!.progressHelper.barColor = Color.parseColor("#A5DC86")
            dialog!!.titleText = "Loading ads. Please wait..."
            dialog!!.setCancelable(false)
            dialog!!.show()
        }
        if (isTesting) {
            admobId = activity.getString(R.string.test_ads_admob_inter_id)
        }
        if (adRequest == null) {
            initAdRequest(timeOut)
        }
        idIntersitialReal = admobId
        InterstitialAd.load(
            activity,
            idIntersitialReal!!,
            adRequest!!,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    adLoadCallback.onAdLoaded()
                    Log.i("adLog", "onAdLoaded")
                    // Toast.makeText(activity, "success load ads", Toast.LENGTH_SHORT).show();
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isAdShowing = false
                    if (AppOpenManager.getInstance().isInitialized) {
                        AppOpenManager.getInstance().isAppResumeEnabled = true
                    }
                    isAdShowing = false
                    if (mInterstitialAd != null) {
                        mInterstitialAd = null
                    }
                    adLoadCallback.onAdFail()
                }
            })
    }
    @JvmStatic
    fun showAdInterstitialWithCallbackNotLoad(
        kInterstitialAd: InterstitialAd?,
        activity: Activity,
        adCallback: AdCallbackNew?
    ) {
        if (!isShowAds || !isNetworkConnected(activity)) {
            isAdShowing = false
            if (mInterstitialAd != null) {
                mInterstitialAd = null
            }
            if (AppOpenManager.getInstance().isInitialized) {
                AppOpenManager.getInstance().isAppResumeEnabled = true
            }
            adCallback!!.onAdClosed()
            return
        }
        if (kInterstitialAd == null) {
            if (adCallback != null) {
                isAdShowing = false
                if (mInterstitialAd != null) {
                    mInterstitialAd = null
                }
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                adCallback.onAdFail()
            }
            return
        }
        kInterstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                isAdShowing = false
                if (mInterstitialAd != null) {
                    mInterstitialAd = null
                }
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                adCallback!!.onEventClickAdClosed()
                adCallback.onAdClosed()
                Log.d("TAG", "The ad was dismissed.")
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                isAdShowing = false
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = true
                }
                isAdShowing = false
                if (mInterstitialAd != null) {
                    mInterstitialAd = null
                }
                Log.e("Admodfail", "onAdFailedToLoad" + adError.message)
                Log.e("Admodfail", "errorCodeAds" + adError.cause)
                adCallback!!.onAdFail()
            }

            override fun onAdShowedFullScreenContent() {
                mInterstitialAd = null
                isAdShowing = true
                adCallback!!.onAdShowed()
            }
        }
        showInterstitialAd(activity, kInterstitialAd, adCallback)
    }

    @JvmStatic
    fun showAdInterstitialWithCallback(
        kInterstitialAd: InterstitialAd?,
        activity: Activity,
        adCallback: AdCallbackNew?
    ) {
        if (!isShowAds || !isNetworkConnected(activity)) {
            adCallback!!.onAdClosed()
            return
        }
        if (kInterstitialAd == null) {
            adCallback?.onAdFail()
            return
        }
        kInterstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                isAdShowing = false
                adCallback!!.onAdClosed()
                adCallback.onEventClickAdClosed()
                Log.d("TAG", "The ad was dismissed.")
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                adCallback!!.onAdFail()
                isAdShowing = false
                mInterstitialAd = null
                loadAdInterstitial(activity, idIntersitialReal, object : AdCallbackNew {
                    override fun onAdClosed() {}
                    override fun onEventClickAdClosed() {}
                    override fun onAdShowed() {}
                    override fun onAdFail() {
                        Log.d("TAG", "Ad loaded again fails")
                    }

                    override fun onAdLoaded() {
                        Log.d("TAG", "Ad loaded again success")
                    }
                }, false)
                Log.d("TAG", "The ad failed to show.")
            }

            override fun onAdShowedFullScreenContent() {
                mInterstitialAd = null
                isAdShowing = true
                adCallback!!.onAdShowed()
                Log.d("TAG", "The ad was shown.")
                loadAdInterstitial(activity, idIntersitialReal, object : AdCallbackNew {
                    override fun onAdClosed() {}
                    override fun onEventClickAdClosed() {}
                    override fun onAdShowed() {}
                    override fun onAdFail() {
                        Log.d("TAG", "Ad loaded again fails")
                    }

                    override fun onAdLoaded() {
                        Log.d("TAG", "Ad loaded again success")
                    }
                }, false)
            }
        }
        showInterstitialAd(activity, kInterstitialAd, adCallback)
    }
    @JvmStatic
    private fun showInterstitialAd(
        activity: Activity,
        mInterstitialAd: InterstitialAd?,
        callback: AdCallbackNew?
    ) {
        if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && mInterstitialAd != null) {
            isAdShowing = true
            callback?.onAdClosed()
            Handler().postDelayed({ mInterstitialAd.show(activity) }, 400)
        } else {
            isAdShowing = false
            if (AppOpenManager.getInstance().isInitialized) {
                AppOpenManager.getInstance().isAppResumeEnabled = true
            }
            dismissAdDialog()
            callback!!.onAdFail()
        }
    }
    @JvmStatic
    fun loadAndShowAdInterstitialWithCallback(
        activity: AppCompatActivity,
        admobId: String?,
        limitTime: Int,
        adCallback: AdCallbackNew,
        enableLoadingDialog: Boolean
    ) {
        var admobId = admobId
        mInterstitialAd = null
        isAdShowing = false
        if (adRequest == null) {
            initAdRequest(timeOut)
        }
        if (!isShowAds || !isNetworkConnected(activity)) {
            adCallback.onAdClosed()
            //            handlerTimeOut.removeCallbacksAndMessages(null);
            return
        }
        if (AppOpenManager.getInstance().isInitialized) {
            if (!AppOpenManager.getInstance().isAppResumeEnabled) {
                return
            } else {
                isAdShowing = false
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = false
                }
            }
        }
        if (enableLoadingDialog) {
            dialog = SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE)
            dialog!!.progressHelper.barColor = Color.parseColor("#A5DC86")
            dialog!!.titleText = "Loading ads. Please wait..."
            dialog!!.setCancelable(false)
            dialog!!.show()
        }
        if (isTesting) {
            admobId = activity.getString(R.string.test_ads_admob_inter_id)
        } else {
            checkIdTest(activity, admobId)
        }
        InterstitialAd.load(
            activity,
            admobId!!,
            adRequest!!,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    super.onAdLoaded(interstitialAd)
                    adCallback.onAdLoaded()
                    Handler(Looper.getMainLooper()).postDelayed({
                        mInterstitialAd = interstitialAd
                        if (mInterstitialAd != null) {
                            mInterstitialAd?.fullScreenContentCallback =
                                object : FullScreenContentCallback() {
                                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                        adCallback.onAdFail()
                                        isAdShowing = false
                                        if (AppOpenManager.getInstance().isInitialized) {
                                            AppOpenManager.getInstance().isAppResumeEnabled = true
                                        }
                                        isAdShowing = false
                                        if (mInterstitialAd != null) {
                                            mInterstitialAd = null
                                        }
                                        Log.e("Admodfail", "onAdFailedToLoad" + adError.message)
                                        Log.e("Admodfail", "errorCodeAds" + adError.cause)
                                    }

                                    override fun onAdDismissedFullScreenContent() {
                                        lastTimeShowInterstitial = Date().time
                                        adCallback.onAdClosed()
                                        adCallback.onEventClickAdClosed()
                                        if (mInterstitialAd != null) {
                                            mInterstitialAd = null
                                        }
                                        isAdShowing = false
                                        if (AppOpenManager.getInstance().isInitialized) {
                                            AppOpenManager.getInstance().isAppResumeEnabled = true
                                        }
                                    }

                                    override fun onAdShowedFullScreenContent() {
                                        adCallback.onAdShowed()
                                        super.onAdShowedFullScreenContent()
                                    }
                                }
                            if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && mInterstitialAd != null) {
                                mInterstitialAd!!.show(activity)
                                isAdShowing = true
                            } else {
                                mInterstitialAd = null
                                dismissAdDialog()
                                isAdShowing = false
                                if (AppOpenManager.getInstance().isInitialized) {
                                    AppOpenManager.getInstance().isAppResumeEnabled = true
                                }
                            }
                        } else {
                            dismissAdDialog()
                            adCallback.onAdFail()
                            isAdShowing = false
                            if (AppOpenManager.getInstance().isInitialized) {
                                AppOpenManager.getInstance().isAppResumeEnabled = true
                            }
                        }
                    }, 800)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    mInterstitialAd = null
                    adCallback.onAdFail()
                    if (AppOpenManager.getInstance().isInitialized) {
                        AppOpenManager.getInstance().isAppResumeEnabled = true
                    }
                    isAdShowing = false
                    dismissAdDialog()
                }
            })
    }
    @JvmStatic
    fun loadAndShowAdInterstitialWithCallbackMultiAds(
        activity: AppCompatActivity,
        admobId: String?,
        admobId2: String,
        adCallback: AdsInterCallBack,
        enableLoadingDialog: Boolean
    ) {
        var admobId = admobId
        mInterstitialAd = null
        isAdShowing = false
        if (adRequest == null) {
            initAdRequest(timeOut)
        }
        if (!isShowAds || !isNetworkConnected(activity)) {
            adCallback.onAdFail("No internet")
            return
        }
        if (AppOpenManager.getInstance().isInitialized) {
            if (!AppOpenManager.getInstance().isAppResumeEnabled) {
                return
            } else {
                isAdShowing = false
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = false
                }
            }
        }
        if (enableLoadingDialog) {
            dialogLoading(activity)
        }
        if (isTesting) {
            admobId = activity.getString(R.string.test_ads_admob_inter_id)
        } else {
            checkIdTest(activity, admobId)
        }
        InterstitialAd.load(
            activity,
            admobId!!,
            adRequest!!,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    super.onAdLoaded(interstitialAd)
                    adCallback.onAdLoaded()
                    Handler(Looper.getMainLooper()).postDelayed({
                        mInterstitialAd = interstitialAd
                        if (mInterstitialAd != null) {
                            mInterstitialAd!!.onPaidEventListener =
                                OnPaidEventListener { adValue: AdValue? -> adCallback.onPaid(adValue,mInterstitialAd?.adUnitId) }
                            mInterstitialAd!!.fullScreenContentCallback =
                                object : FullScreenContentCallback() {
                                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                        adCallback.onAdFail(adError.message)
                                        isAdShowing = false
                                        if (AppOpenManager.getInstance().isInitialized) {
                                            AppOpenManager.getInstance().isAppResumeEnabled = true
                                        }
                                        isAdShowing = false
                                        if (mInterstitialAd != null) {
                                            mInterstitialAd = null
                                        }
                                        dismissAdDialog()
                                        Log.e("Admodfail", "onAdFailedToLoad" + adError.message)
                                        Log.e("Admodfail", "errorCodeAds" + adError.cause)
                                    }

                                    override fun onAdDismissedFullScreenContent() {
                                        lastTimeShowInterstitial = Date().time
                                        adCallback.onEventClickAdClosed()
                                        dismissAdDialog()
                                        if (mInterstitialAd != null) {
                                            mInterstitialAd = null
                                        }
                                        isAdShowing = false
                                        if (AppOpenManager.getInstance().isInitialized) {
                                            AppOpenManager.getInstance().isAppResumeEnabled = true
                                        }
                                    }

                                    override fun onAdShowedFullScreenContent() {
                                        adCallback.onAdShowed()
                                        super.onAdShowedFullScreenContent()
                                    }
                                }
                            if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && mInterstitialAd != null) {
                                adCallback.onStartAction()
                                mInterstitialAd!!.show(activity)
                                isAdShowing = true
                            } else {
                                mInterstitialAd = null
                                dismissAdDialog()
                                isAdShowing = false
                                if (AppOpenManager.getInstance().isInitialized) {
                                    AppOpenManager.getInstance().isAppResumeEnabled = true
                                }
                            }
                        } else {
                            dismissAdDialog()
                            adCallback.onAdFail("mInterstitialAd null")
                            isAdShowing = false
                            if (AppOpenManager.getInstance().isInitialized) {
                                AppOpenManager.getInstance().isAppResumeEnabled = true
                            }
                        }
                    }, 800)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    mInterstitialAd = null
                    if (AppOpenManager.getInstance().isInitialized) {
                        AppOpenManager.getInstance().isAppResumeEnabled = true
                    }
                    loadAndShowAdInterstitialWithCallback2(activity, admobId2, adCallback)
                }
            })
    }
    @JvmStatic
    private fun loadAndShowAdInterstitialWithCallback2(
        activity: AppCompatActivity,
        admobId: String,
        adCallback: AdsInterCallBack
    ) {
        var admobId: String? = admobId
        mInterstitialAd = null
        isAdShowing = false
        if (adRequest == null) {
            initAdRequest(timeOut)
        }
        if (!isShowAds || !isNetworkConnected(activity)) {
            adCallback.onAdFail("No internet")
            //            handlerTimeOut.removeCallbacksAndMessages(null);
            return
        }
        if (AppOpenManager.getInstance().isInitialized) {
            if (!AppOpenManager.getInstance().isAppResumeEnabled) {
                return
            } else {
                isAdShowing = false
                if (AppOpenManager.getInstance().isInitialized) {
                    AppOpenManager.getInstance().isAppResumeEnabled = false
                }
            }
        }
        if (isTesting) {
            admobId = activity.getString(R.string.test_ads_admob_inter_id)
        } else {
            checkIdTest(activity, admobId)
        }
        InterstitialAd.load(
            activity,
            admobId!!,
            adRequest!!,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    super.onAdLoaded(interstitialAd)
                    adCallback.onAdLoaded()
                    Handler(Looper.getMainLooper()).postDelayed({
                        mInterstitialAd = interstitialAd
                        if (mInterstitialAd != null) {
                            mInterstitialAd!!.onPaidEventListener =
                                OnPaidEventListener { adValue -> adCallback.onPaid(adValue,mInterstitialAd?.adUnitId) }
                            mInterstitialAd!!.fullScreenContentCallback =
                                object : FullScreenContentCallback() {
                                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                        adCallback.onAdFail(adError.message)
                                        isAdShowing = false
                                        if (AppOpenManager.getInstance().isInitialized) {
                                            AppOpenManager.getInstance().isAppResumeEnabled = true
                                        }
                                        isAdShowing = false
                                        if (mInterstitialAd != null) {
                                            mInterstitialAd = null
                                        }
                                        Log.e("Admodfail", "onAdFailedToLoad" + adError.message)
                                        Log.e("Admodfail", "errorCodeAds" + adError.cause)
                                    }

                                    override fun onAdDismissedFullScreenContent() {
                                        lastTimeShowInterstitial = Date().time
                                        adCallback.onStartAction()
                                        adCallback.onEventClickAdClosed()
                                        if (mInterstitialAd != null) {
                                            mInterstitialAd = null
                                        }
                                        isAdShowing = false
                                        if (AppOpenManager.getInstance().isInitialized) {
                                            AppOpenManager.getInstance().isAppResumeEnabled = true
                                        }
                                    }

                                    override fun onAdShowedFullScreenContent() {
                                        adCallback.onAdShowed()
                                        super.onAdShowedFullScreenContent()
                                    }
                                }
                            if (activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && mInterstitialAd != null) {
                                mInterstitialAd!!.show(activity)
                                isAdShowing = true
                            } else {
                                mInterstitialAd = null
                                dismissAdDialog()
                                isAdShowing = false
                                if (AppOpenManager.getInstance().isInitialized) {
                                    AppOpenManager.getInstance().isAppResumeEnabled = true
                                }
                            }
                        } else {
                            dismissAdDialog()
                            adCallback.onAdFail("mInterstitialAd null")
                            isAdShowing = false
                            if (AppOpenManager.getInstance().isInitialized) {
                                AppOpenManager.getInstance().isAppResumeEnabled = true
                            }
                        }
                    }, 800)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    mInterstitialAd = null
                    if (AppOpenManager.getInstance().isInitialized) {
                        AppOpenManager.getInstance().isAppResumeEnabled = true
                    }
                    isAdShowing = false
                    adCallback.onAdFail(loadAdError.message)
                    dismissAdDialog()
                }
            })
    }

    //Update New Lib
    private fun checkIdTest(activity: Activity, admobId: String?) {
//        if (admobId.equals(activity.getString(R.string.test_ads_admob_inter_id)) && !BuildConfig.DEBUG) {
//            if (dialog != null) {
//                dialog.dismiss();
//            }
//            Utils.getInstance().showDialogTitle(activity, "Warning", "Build bản release nhưng đang để id test ads", "Đã biết", DialogType.WARNING_TYPE, false, "", new DialogCallback() {
//                @Override
//                public void onClosed() {
//                }
//
//                @Override
//                public void cancel() {
//                }
//            });
//        }
    }

    private val currentTime: Long
        private get() = System.currentTimeMillis()

    fun getDeviceID(context: Context): String {
        val android_id = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        return md5(android_id).uppercase(Locale.getDefault())
    }

    fun md5(s: String): String {
        try {
            // Create MD5 Hash
            val digest = MessageDigest.getInstance("MD5")
            digest.update(s.toByteArray())
            val messageDigest = digest.digest()

            // Create Hex String
            val hexString = StringBuffer()
            for (i in messageDigest.indices) hexString.append(Integer.toHexString(0xFF and messageDigest[i].toInt()))
            return hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }

    fun dialogLoading(context: Activity) {
        dialogFullScreen = Dialog(context)
        dialogFullScreen?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogFullScreen?.setContentView(R.layout.dialog_full_screen)
        dialogFullScreen?.setCancelable(false)
        dialogFullScreen?.window!!.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        dialogFullScreen?.window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        try {
            if (!context.isFinishing && dialogFullScreen != null && dialogFullScreen?.isShowing == false) {
                dialogFullScreen?.show()
            }
        } catch (ignored: Exception) {
        }

    }
}