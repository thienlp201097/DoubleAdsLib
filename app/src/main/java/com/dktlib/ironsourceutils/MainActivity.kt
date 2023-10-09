package com.dktlib.ironsourceutils

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.applovin.mediation.MaxAd
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.dktlib.ironsourcelib.*
import com.dktlib.ironsourcelib.utils.NativeHolder
import com.dktlib.ironsourcelib.utils.Utils

class MainActivity : AppCompatActivity() {
    lateinit var bannerContainer: ViewGroup
    lateinit var nativeLoader: MaxNativeAdLoader
    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btnLoad = findViewById<Button>(R.id.btn_load_inter)
        val btn_load_inter_app_lovin = findViewById<Button>(R.id.btn_load_inter_app_lovin)
        val btn_show_inter_app_lovin = findViewById<Button>(R.id.btn_show_inter_app_lovin)
        val btnshow = findViewById<Button>(R.id.btn_show_inter)
        val btnCallback2 = findViewById<Button>(R.id.btn_show_inter_callback2)
        val btnLoadAndShow = findViewById<Button>(R.id.btn_load_show_inter_callback2)
        val nativeAds = findViewById<FrameLayout>(R.id.nativead)

        val btnReward = findViewById<Button>(R.id.btn_show_reward)
        val btnLoadNative = findViewById<Button>(R.id.load_native)
        val load_native_max = findViewById<Button>(R.id.load_native_max)
        val btnShowNative = findViewById<Button>(R.id.show_native)
        val show_native_max = findViewById<Button>(R.id.show_native_max)
        val btn_show_native = findViewById<Button>(R.id.btn_show_native)



        bannerContainer = findViewById<FrameLayout>(R.id.banner_container)
        val bannerContainer = findViewById<FrameLayout>(R.id.banner_container)

        btn_show_native.setOnClickListener {
            AdsManager.showAdsNative(this,AdsManager.nativeHolder,nativeAds)
        }
        btnLoad.setOnClickListener {
            AdsManagerAdmod.loadInter(this, AdsManagerAdmod.interholder)
        }
        btn_load_inter_app_lovin.setOnClickListener {
            AdsManager.loadInter(this)
        }

        btn_show_inter_app_lovin.setOnClickListener {
            AdsManager.showInter(this, AdsManager.interHolder, object : AdsManager.AdsOnClick {
                override fun onAdsCloseOrFailed() {
                    startActivity(Intent(this@MainActivity, MainActivity2::class.java))
                }
            })
        }

        btnshow.setOnClickListener {
            AdsManagerAdmod.showInter(
                this,
                AdsManagerAdmod.interholder,
                object : AdsManagerAdmod.AdListener {
                    override fun onAdClosed() {
                        startActivity(Intent(this@MainActivity, MainActivity2::class.java))
                    }

                    override fun onFailed() {
                        startActivity(Intent(this@MainActivity, MainActivity2::class.java))
                    }
                },
                true
            )

        }
        btnCallback2.setOnClickListener {
            ApplovinUtil.showInterstitialsWithDialogCheckTime(
                this,
                500,
                object : InterstititialCallback {
                    override fun onInterstitialShowSucceed() {

                    }

                    override fun onAdRevenuePaid(ad: MaxAd?) {
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

        btnLoadAndShow.setOnClickListener {
            AdsManager.loadAndShowIntersial(this,"134656413e36e374",object : AdsManager.AdsOnClick{
                override fun onAdsCloseOrFailed() {
                    startActivity(Intent(this@MainActivity, MainActivity3::class.java))
                }
            })
        }

        btnReward.setOnClickListener {
            ApplovinUtil.loadReward(this, "c10d259dcb47378d", 15000, object : RewardCallback {
                override fun onRewardReady() {
                    ApplovinUtil.showRewardWithDialogCheckTime(
                        this@MainActivity,
                        1500,
                        object : RewardCallback {
                            override fun onRewardReady() {

                            }

                            override fun onRewardClosed() {
                                Toast.makeText(
                                    this@MainActivity,
                                    "onRewardClosed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            override fun onRewardLoadFail(error: String) {
                            }

                            override fun onRewardShowSucceed() {
                            }

                            override fun onUserRewarded() {
                                Toast.makeText(
                                    this@MainActivity,
                                    "onUserRewarded",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            override fun onRewardedVideoStarted() {
                            }

                            override fun onRewardedVideoCompleted() {
                                Toast.makeText(
                                    this@MainActivity,
                                    "onRewardedVideoCompleted",
                                    Toast.LENGTH_SHORT
                                ).show()
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
            AdsManagerAdmod.loadAdsNativeNew(this, AdsManagerAdmod.nativeHolder)
        }

        load_native_max.setOnClickListener {
            ApplovinUtil.loadNativeAds(this,AdsManager.nativeHolder,object : NativeCallBackNew{
                override fun onNativeAdLoaded(nativeAd: MaxAd?, nativeAdView: MaxNativeAdView?) {
                    Toast.makeText(this@MainActivity,"Loaded", Toast.LENGTH_SHORT).show()
                }

                override fun onAdFail(error: String) {
                    Toast.makeText(this@MainActivity,"Failed", Toast.LENGTH_SHORT).show()
                }

                override fun onAdRevenuePaid(ad: MaxAd?) {
                }
            })
        }

        btnShowNative.setOnClickListener {
            AdsManagerAdmod.showNative(this, nativeAds, AdsManagerAdmod.nativeHolder)
        }

        show_native_max.setOnClickListener {
            ApplovinUtil.showNativeWithLayout(nativeAds,this,AdsManager.nativeHolder,R.layout.native_custom_ad_view,GoogleENative.UNIFIED_MEDIUM,object : NativeCallBackNew{
                override fun onNativeAdLoaded(nativeAd: MaxAd?, nativeAdView: MaxNativeAdView?) {
                    Toast.makeText(this@MainActivity,"show success", Toast.LENGTH_SHORT).show()
                }

                override fun onAdFail(error: String) {
                    Toast.makeText(this@MainActivity,"Show failed", Toast.LENGTH_SHORT).show()
                }

                override fun onAdRevenuePaid(ad: MaxAd?) {
                }

            })
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
        ApplovinUtil.showBanner(this, bannerContainer, "f443c90308f39f17", object : BannerCallback {
            override fun onBannerLoadFail(error: String) {
            }

            override fun onBannerShowSucceed() {
            }

            override fun onAdRevenuePaid(ad: MaxAd?) {
            }
        })
        super.onResume()
    }
}