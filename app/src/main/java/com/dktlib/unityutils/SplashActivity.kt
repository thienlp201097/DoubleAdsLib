package com.dktlib.unityutils

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dktlib.ironsourcelib.utils.callback.InitComplete
import com.dktlib.ironsourcelib.utils.unityads.UnityAdsUtils
import com.dktlib.unityutils.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    val binding by lazy { ActivitySplashBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        UnityAdsUtils.initUnityAds(application,"14851",true,object : InitComplete{
            override fun onInitializationComplete() {
                startActivity(Intent(this@SplashActivity,MainActivity::class.java))
            }

            override fun onInitializationFailed() {
                startActivity(Intent(this@SplashActivity,MainActivity::class.java))
            }
        })
    }
}