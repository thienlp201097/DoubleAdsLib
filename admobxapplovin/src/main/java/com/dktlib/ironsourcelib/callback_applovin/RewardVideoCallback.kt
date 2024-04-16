package com.dktlib.ironsourcelib.callback_applovin

interface RewardVideoCallback {
    fun onRewardClosed()
    fun onRewardEarned()
    fun onRewardFailed()
    fun onRewardNotAvailable()
}