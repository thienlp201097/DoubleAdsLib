package com.dktlib.unityutils

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dktlib.ironsourcelib.utils.callback.IUnityAdsCallBack
import com.dktlib.ironsourcelib.utils.callback.LoadCallBack
import com.dktlib.ironsourcelib.utils.unityads.UnityAdsUtils
import com.dktlib.unityutils.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.loadAds.setOnClickListener {
            UnityAdsUtils.loadAd(this,true,object : LoadCallBack{
                override fun onUnityAdsAdLoaded() {

                }

                override fun onUnityAdsFailedToLoad() {

                }
            })
        }

        binding.showAds.setOnClickListener {
            UnityAdsUtils.showAd(this,true,object : IUnityAdsCallBack{
                override fun onShow() {

                }

                override fun onFailure() {
                    startActivity(Intent(this@MainActivity,OtherActivity::class.java))
                }

                override fun onClose() {
                    startActivity(Intent(this@MainActivity,OtherActivity::class.java))
                }
            })
        }

        binding.loadAndShow.setOnClickListener {
            UnityAdsUtils.loadAndShowAd(this,true,object : IUnityAdsCallBack{
                override fun onShow() {

                }

                override fun onFailure() {
                    startActivity(Intent(this@MainActivity,OtherActivity::class.java))
                }

                override fun onClose() {
                    startActivity(Intent(this@MainActivity,OtherActivity::class.java))
                }
            })
        }

        binding.showBanner.setOnClickListener {
            UnityAdsUtils.showBanner(this,binding.bannerContainer,320,50)
        }
    }

    override fun onResume() {
        val bannerContainer = findViewById<FrameLayout>(R.id.banner_container)
        super.onResume()
    }
}