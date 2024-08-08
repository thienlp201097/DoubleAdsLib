package com.dktlib.ironsourceutils

import android.content.Intent
import android.os.Bundle
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
import com.dktlib.ironsourcelib.callback_applovin.BannerCallback
import com.dktlib.ironsourcelib.callback_applovin.InterstititialCallback
import com.dktlib.ironsourcelib.callback_applovin.NativeCallBackNew
import com.dktlib.ironsourcelib.callback_applovin.RewardCallback
import com.dktlib.ironsourcelib.utils.admod.callback.AdsInterCallBack
import com.dktlib.ironsourceutils.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.nativead.NativeAd

class MainActivity : AppCompatActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    lateinit var bannerContainer: ViewGroup
    lateinit var nativeLoader: MaxNativeAdLoader
    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val nativeAds = findViewById<FrameLayout>(R.id.nativead)
        bannerContainer = findViewById<FrameLayout>(R.id.banner_container)

        binding.btnShowNative.setOnClickListener {
            AdsManager.showAdsNative(this,AdsManager.nativeHolder,nativeAds)
        }
        binding.btnLoadInter.setOnClickListener {
            AdsManagerAdmod.loadInter(this, AdsManagerAdmod.interholder)
        }
        binding.btnLoadInterAppLovin.setOnClickListener {
            AdsManager.loadInter(this)
        }

        binding.btnShowInterAppLovin.setOnClickListener {
            AdsManager.showInter(this, AdsManager.interHolder, object : AdsManager.AdsOnClick {
                override fun onAdsCloseOrFailed() {
                    startActivity(Intent(this@MainActivity, MainActivity2::class.java))
                }
            })
        }

        binding.btnShowInter.setOnClickListener {
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

        binding.btnLoadShowInterCallback2.setOnClickListener {
            AdsManager.loadAndShowIntersial(this,AdsManager.interHolder,object : AdsManager.AdsOnClick{
                override fun onAdsCloseOrFailed() {
                    startActivity(Intent(this@MainActivity, MainActivity3::class.java))
                }
            })
        }

        binding.btnShowReward.setOnClickListener {
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

        binding.loadNative.setOnClickListener {
            AdsManagerAdmod.loadAdsNativeNew(this, AdsManagerAdmod.nativeHolder)
        }

        binding.loadNativeMax.setOnClickListener {
            ApplovinUtil.loadNativeAds(this,AdsManager.nativeHolder,object : NativeCallBackNew {
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

        binding.showNative.setOnClickListener {
            AdsManagerAdmod.showNative(this, nativeAds, AdsManagerAdmod.nativeHolder)
        }

        binding.showNativeMax.setOnClickListener {
            ApplovinUtil.showNativeWithLayout(nativeAds,this,AdsManager.nativeHolder,R.layout.native_custom_ad_view,GoogleENative.UNIFIED_MEDIUM,object :
                NativeCallBackNew {
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
        binding.loadBanner.setOnClickListener {
//            AdmobUtils.loadAdBannerWithSize(this,"", AdSize.LARGE_BANNER,binding.bannerContainer,object : AdmobUtils.BannerCallBack{
//                override fun onClickAds() {
//
//                }
//
//                override fun onLoad() {
//                }
//
//                override fun onFailed(message: String) {
//                }
//
//                override fun onPaid(adValue: AdValue?, mAdView: AdView?) {
//                }
//            })
            
            AdmobUtils.loadAdBannerCollapsibleReload(this,AdsManager.bannerHolder,CollapsibleBanner.BOTTOM,binding.bannerContainer,object :
                AdmobUtils.BannerCollapsibleAdCallback {
                override fun onClickAds() {
                    
                }

                override fun onBannerAdLoaded(adSize: AdSize) {
                    
                }

                override fun onAdFail(message: String) {
                    
                }

                override fun onAdPaid(adValue: AdValue, mAdView: AdView) {
                    
                }

            })
        }

        binding.bannerMax.setOnClickListener {
            ApplovinUtil.showBannerMERC(this, binding.bannerContainer, "f443c90308f39f17", object : BannerCallback {
                override fun onBannerLoadFail(error: String) {
                }

                override fun onBannerShowSucceed() {
                }

                override fun onAdRevenuePaid(ad: MaxAd?) {
                }
            })
        }
        
        binding.btnLoadShowInterAdmob.setOnClickListener { 
            AdmobUtils.loadAndShowAdInterstitial(this, AdsManagerAdmod.interholder,object : AdsInterCallBack{
                override fun onStartAction() {
                    
                }

                override fun onEventClickAdClosed() {
                    startActivity(Intent(this@MainActivity, MainActivity2::class.java))
                }

                override fun onAdShowed() {
                    
                }

                override fun onAdLoaded() {
                    
                }

                override fun onAdFail(error: String?) {
                    startActivity(Intent(this@MainActivity, MainActivity2::class.java))
                }

                override fun onPaid(adValue: AdValue?, adsId: String?) {
                    
                }

            },true)
        }
        
        binding.loadAndShowNativeAdmob.setOnClickListener { 
            AdmobUtils.loadAndShowNativeAdsWithLayoutAds(this,AdsManagerAdmod.nativeHolder,binding.nativead,R.layout.ad_template_medium,GoogleENative.UNIFIED_MEDIUM,object :
                AdmobUtils.NativeAdCallbackNew {
                override fun onLoadedAndGetNativeAd(ad: NativeAd?) {
                    
                }

                override fun onNativeAdLoaded() {
                    
                }

                override fun onAdFail(error: String) {
                    
                }

                override fun onAdPaid(adValue: AdValue?, adUnitAds: String?) {
                    
                }

                override fun onClickAds() {
                    
                }

            })
        }
        ApplovinUtil.showNativeWithLayout(nativeAds,this,AdsManager.nativeHolder,R.layout.native_custom_ad_view,GoogleENative.UNIFIED_MEDIUM,object :
            NativeCallBackNew {
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

    override fun onResume() {
        super.onResume()
    }
}