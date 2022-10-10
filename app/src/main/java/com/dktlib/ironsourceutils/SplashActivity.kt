package com.dktlib.ironsourceutils

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.applovin.mediation.MaxAd
import com.dktlib.ironsourcelib.*
import com.dktlib.ironsourceutils.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AdmodUtils.getInstance().initAdmob(this, 10000, true, true)
        AppOpenManager.getInstance().init(application, getString(R.string.test_ads_admob_app_open))
        AppOpenManager.getInstance().disableAppResumeWithActivity(SplashActivity::class.java)
        val binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ApplovinUtil.initApplovin(this,  true)
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
//        ApplovinUtil.loadAndShowInterstitialsWithDialogCheckTime(this, "134656413e36e374", 0, object : InterstititialCallback {
//            override fun onInterstitialReady() {
//
//            }
//
//            override fun onInterstitialClosed() {
//
//
//                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
//            }
//
//            override fun onInterstitialLoadFail(error: String) {
//                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
//            }
//
//            override fun onInterstitialShowSucceed() {
//
//            }
//
//            override fun onAdRevenuePaid(ad: MaxAd?) {
//
//            }
//        })


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

//    val requestPermissionLauncher =
//        registerForActivityResult(
//            ActivityResultContracts.RequestPermission()
//        ) { isGranted: Boolean ->
//            if (isGranted) {
//                IronSourceUtil.loadAndShowInterstitialsWithDialogCheckTime(this, "aplssh", 1500, 0, object : InterstititialCallback {
//                    override fun onInterstitialReady() {
//
//                    }
//
//                    override fun onInterstitialClosed() {
//                        onInterstitialLoadFail()
//                    }
//
//                    override fun onInterstitialLoadFail() {
//                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
//                    }
//
//                    override fun onInterstitialShowSucceed() {
//
//                    }
//                })
//            } else {
//                // Explain to the user that the feature is unavailable because the
//                // features requires a permission that the user has denied. At the
//                // same time, respect the user's decision. Don't link to system
//                // settings in an effort to convince the user to change their
//                // decision.
//            }
//        }
}