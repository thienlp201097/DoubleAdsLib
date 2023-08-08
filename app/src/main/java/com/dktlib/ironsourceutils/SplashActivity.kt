package com.dktlib.ironsourceutils

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.applovin.mediation.MaxAd
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.dktlib.ironsourcelib.*
import com.dktlib.ironsourcelib.utils.Utils
import com.dktlib.ironsourceutils.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AdmodUtils.initAdmob(this, 10000, isDebug = true, isEnableAds = true)
        AppOpenManager.getInstance().init(application, getString(R.string.test_ads_admob_app_open))
        AppOpenManager.getInstance().disableAppResumeWithActivity(SplashActivity::class.java)
        val binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ApplovinUtil.initApplovin(this,  true)
        val aoaManager = AOAManager(this, "", 10000, object : AOAManager.AppOpenAdsListener {
            override fun onAdsClose() {
                Utils.getInstance().addActivity(this@SplashActivity, MainActivity::class.java)
            }

            override fun onAdsFailed() {
                Utils.getInstance().addActivity(this@SplashActivity, MainActivity::class.java)
            }
        })
        aoaManager.loadAndShowAoA()

//        ApplovinUtil.loadAndGetNativeAds(this@SplashActivity, "8aec97f172bce4a6",object : NativeCallBackNew {
//
//            override fun onAdRevenuePaid(ad: MaxAd?) {
//
//            }
//
//            override fun onNativeAdLoaded(nativeAd: MaxAd?, nativeAdView: MaxNativeAdView?) {
//
//            }
//
//            override fun onAdFail() {
//                loadInter();
//            }
//
//        })

//        loadInter();





        binding.btnNext.setOnClickListener {
          //  requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
//        IronSourceUtil.loadInterstitials(this,15000,object : InterstititialCallback {
//            override fun onInterstitialReady() {
//                binding.btnNext.visibility = View.VISIBLE
//                binding.progressBar.visibility = View.INVISIBLE
//            }
//
//
//
//            override fun onInterstitialClosed() {
//                val i = Intent(this@SplashActivity, MainActivity::class.java)
//                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                startActivity(i)
//            }
//
//            override fun onInterstitialLoadFail() {
//                onInterstitialClosed()
//            }
//
//            override fun onInterstitialShowSucceed() {
//
//            }
//        })

    }

    fun loadInter(){
        ApplovinUtil.loadInterstitials(this, "134656413e36e374", 0, object : InterstititialCallback {
            override fun onInterstitialReady() {

            }

            override fun onInterstitialClosed() {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            }

            override fun onInterstitialLoadFail(error: String) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            }

            override fun onInterstitialShowSucceed() {

            }

            override fun onAdRevenuePaid(ad: MaxAd?) {

            }
        })
    }
}