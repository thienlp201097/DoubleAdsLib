package com.dktlib.ironsourcelib.adjust

import android.content.Context
import android.util.Log
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustConfig
import com.adjust.sdk.LogLevel
import com.applovin.mediation.MaxAd
import com.google.android.gms.ads.AdValue

object AdjustUtils {
    fun initAdjust(context: Context, key : String, debug: Boolean) {
        val config = if (debug){
            AdjustConfig.ENVIRONMENT_SANDBOX
        }else{
            AdjustConfig.ENVIRONMENT_PRODUCTION
        }
        val adjustConfig = AdjustConfig(
            context,
            key,
            config
        )
        adjustConfig.setLogLevel(LogLevel.WARN)
        Adjust.onCreate(adjustConfig)
    }

    fun postRevenueAdjustMax(ad : MaxAd){
        val adjustAdRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_APPLOVIN_MAX)
        adjustAdRevenue.setRevenue(ad.revenue, "USD")
        adjustAdRevenue.setAdRevenueNetwork(ad.networkName)
        Log.d("==postRevenueAdjustMax==", "postRevenueAdjustMax: ${ad.networkName}")
        if (ad.networkName == "AdMob" || ad.networkName == "Google Ad Manager"){
            Log.d("==postRevenueAdjustMax==", " ${ad.networkName} no post revenue")
            return
        }
        Log.d("==postRevenueAdjustMax==", " ${ad.networkName} post revenue")
        adjustAdRevenue.setAdRevenueUnit(ad.adUnitId)
        adjustAdRevenue.setAdRevenuePlacement(ad.placement)
        Adjust.trackAdRevenue(adjustAdRevenue)
    }

    fun postRevenueAdjust(ad: AdValue, adUnit: String?) {
        val adjustAdRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_ADMOB)
        val rev = ad.valueMicros.toDouble() / 1000000
        adjustAdRevenue.setRevenue(rev, ad.currencyCode)
        adjustAdRevenue.setAdRevenueUnit(adUnit)
        Adjust.trackAdRevenue(adjustAdRevenue)
    }
}