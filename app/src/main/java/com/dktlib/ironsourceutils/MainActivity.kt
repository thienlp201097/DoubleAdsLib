package com.dktlib.ironsourceutils

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.dktlib.ironsourcelib.*
import com.dktlib.ironsourcelib.utils.NativeHolder
import com.dktlib.ironsourceutils.AdsManager.nativeAdLoader

class MainActivity : AppCompatActivity() {
    lateinit var bannerContainer: ViewGroup
    lateinit var nativeLoader : MaxNativeAdLoader
    var nativeHolder = NativeHolder("3805534b02308f23")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btnLoad = findViewById<Button>(R.id.btn_load_inter)
        val btnshow = findViewById<Button>(R.id.btn_show_inter)
        val btnCallback2 = findViewById<Button>(R.id.btn_show_inter_callback2)
        val btnLoadAndShow = findViewById<Button>(R.id.btn_load_show_inter_callback2)
        val nativeAds = findViewById<FrameLayout>(R.id.nativead)

        val btnReward = findViewById<Button>(R.id.btn_show_reward)
        val btnLoadNative = findViewById<Button>(R.id.load_native)
        val btnShowNative = findViewById<Button>(R.id.show_native)

        bannerContainer = findViewById<FrameLayout>(R.id.banner_container)
        val bannerContainer = findViewById<FrameLayout>(R.id.banner_container)

        btnLoad.setOnClickListener {
//            ApplovinUtil.loadInterstitials(this,"134656413e36e374",12000, object :InterstititialCallback {
//                override fun onInterstitialReady() {
//                    Toast.makeText(this@MainActivity,"load success",Toast.LENGTH_SHORT).show()
//                }
//
//                override fun onInterstitialClosed() {
//
//                }
//
//                override fun onInterstitialLoadFail(error: String) {
//                    Toast.makeText(this@MainActivity,"load fail ${error.toString()}",Toast.LENGTH_SHORT).show()
//
//                }
//
//                override fun onInterstitialShowSucceed() {
//
//                }
//
//                override fun onAdRevenuePaid(ad: MaxAd?) {
//                    TODO("Not yet implemented")
//                }
//            })
            AdsManager.loadInter(this,"134656413e36e374")
        }
        btnshow.setOnClickListener {
            AdsManager.showInter(this,AdsManager.interHolder,object : AdsManager.AdsOnClick{
                override fun onAdsCloseOrFailed() {
                    startActivity(Intent(this@MainActivity, MainActivity2::class.java))
                }
            })
        }
        btnCallback2.setOnClickListener {
            ApplovinUtil.showInterstitialsWithDialogCheckTime(
                this,
                500,
                object : InterstititialCallback {
                    override fun onInterstitialShowSucceed() {

                    }

                    override fun onAdRevenuePaid(ad: com.applovin.mediation.MaxAd?) {
                    }

                    override fun onInterstitialReady() {

                    }

                    override fun onInterstitialClosed() {
                        startActivity(Intent(this@MainActivity, MainActivity3::class.java))
                    }

                    override fun onInterstitialLoadFail(error: String) {
                        onInterstitialClosed()
                    }
                })
        }

        btnLoadAndShow.setOnClickListener(){
//            AdsManager.showInter(this,AdsManager.inter)
        }

        btnReward.setOnClickListener {
            ApplovinUtil.loadReward(this,"c10d259dcb47378d",15000, object :RewardCallback{
                override fun onRewardReady() {
                    ApplovinUtil.showRewardWithDialogCheckTime(this@MainActivity,1500, object :RewardCallback{
                        override fun onRewardReady() {

                        }

                        override fun onRewardClosed() {
                            Toast.makeText(this@MainActivity,"onRewardClosed",Toast.LENGTH_SHORT).show()
                        }

                        override fun onRewardLoadFail(error: String) {
                        }

                        override fun onRewardShowSucceed() {
                        }

                        override fun onUserRewarded() {
                            Toast.makeText(this@MainActivity,"onUserRewarded",Toast.LENGTH_SHORT).show()
                        }

                        override fun onRewardedVideoStarted() {
                        }

                        override fun onRewardedVideoCompleted() {
                            Toast.makeText(this@MainActivity,"onRewardedVideoCompleted",Toast.LENGTH_SHORT).show()
                        }

                        override fun onAdRevenuePaid(ad: MaxAd?) {
                        }
                    })
                }

                override fun onRewardClosed() {
                }

                override fun onRewardLoadFail(error: String) {
                }

                override fun onRewardShowSucceed() {
                }

                override fun onUserRewarded() {
                }

                override fun onRewardedVideoStarted() {
                }

                override fun onRewardedVideoCompleted() {
                }

                override fun onAdRevenuePaid(ad: MaxAd?) {
                }
            })
        }

        btnLoadNative.setOnClickListener {
//            AdsManager.loadAndShowNativeAdsNew(this,"8aec97f172bce4a6")
            ApplovinUtil.loadNativeAds(this,nativeHolder,object : NativeCallBackNew{
                override fun onNativeAdLoaded(nativeAd: MaxAd?, nativeAdView: MaxNativeAdView?) {
                    Toast.makeText(this@MainActivity,"Loaded", Toast.LENGTH_SHORT).show()
                }

                override fun onAdFail() {
                    Toast.makeText(this@MainActivity,"Failed", Toast.LENGTH_SHORT).show()
                }

                override fun onAdRevenuePaid(ad: MaxAd?) {
                }
            })
        }

        btnShowNative.setOnClickListener {
//            AdsManager.showNativeAds(this,nativeAds,GoogleENative.UNIFIED_MEDIUM)
            ApplovinUtil.showNativeWithLayout(nativeAds,this,nativeHolder,R.layout.native_custom_ad_view,GoogleENative.UNIFIED_MEDIUM,object : NativeCallBackNew{
                override fun onNativeAdLoaded(nativeAd: MaxAd?, nativeAdView: MaxNativeAdView?) {
                    Toast.makeText(this@MainActivity,"show success", Toast.LENGTH_SHORT).show()
                }

                override fun onAdFail() {
                    Toast.makeText(this@MainActivity,"Show failed", Toast.LENGTH_SHORT).show()
                }

                override fun onAdRevenuePaid(ad: MaxAd?) {
                }

            })
            Log.d("===Native",AdsManager.native.toString() +"/"+nativeAdLoader.toString())
        }
//        btnLoadAndShow.setOnClickListener {
//            ApplovinUtil.loadAndShowInterstitialsWithDialogCheckTime(this,"134656413e36e374",500,object : InterstititialCallback {
//                override fun onInterstitialReady() {
//
//                }
//
//                override fun onInterstitialClosed() {
//                    startActivity(Intent(this@MainActivity, MainActivity2::class.java))
//
//
//                }
//
//                override fun onInterstitialLoadFail(error: String) {
//                    startActivity(Intent(this@MainActivity, MainActivity2::class.java))
//
//
//                }
//
//                override fun onInterstitialShowSucceed() {
//
//                }
//
//                override fun onAdRevenuePaid(ad: MaxAd?) {
//                    TODO("Not yet implemented")
//                }
//            })
//        }

//        ApplovinUtil.loadAndShowNativeAds(this@MainActivity, "3805534b02308f23", nativeAds,GoogleENative.UNIFIED_MEDIUM, object : NativeAdCallback {
//                override fun onNativeAdLoaded() {
//                    Toast.makeText(this@MainActivity,"onNativeAdLoaded",Toast.LENGTH_SHORT).show()
//                }
//
//            override fun onLoadedAndGetNativeAd(ad: MaxAd?, adView: MaxNativeAdView?) {
//            }
//
//            override  fun onAdFail() {
//                    Toast.makeText(this@MainActivity,"onAdFail",Toast.LENGTH_SHORT).show()
//                }
//
//            override fun onAdRevenuePaid(ad: MaxAd?) {
//
//            }
//        })

//        AdmodUtils.getInstance().loadNativeAds(this@MainActivity,
//            getString(R.string.test_ads_admob_native_id), nativeAds,
//            GoogleENative.UNIFIED_MEDIUM, object : NativeAdCallback {
//           override fun onNativeAdLoaded() {}
//            override  fun onAdFail() {}
//        })


    }

    //    override fun onPause() {
//        if(this::bannerContainer.isInitialized){
//            IronSourceUtil.destroyBanner(bannerContainer)
//        }
//        super.onPause()
//    }
    override fun onResume() {
        val bannerContainer = findViewById<FrameLayout>(R.id.banner_container)
//        ApplovinUtil.showBanner(this, bannerContainer, "5c902521afdeef72", object : BannerCallback{
//            override fun onBannerLoadFail(error: String) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onBannerShowSucceed() {
//                TODO("Not yet implemented")
//            }
//
//            override fun onAdRevenuePaid(ad: MaxAd?) {
//                TODO("Not yet implemented")
//            }
//        })
        super.onResume()
    }
}