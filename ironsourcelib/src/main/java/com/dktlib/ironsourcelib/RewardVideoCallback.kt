package com.dktlib.ironsourcelib

interface RewardVideoCallback {
    fun onRewardClosed()
    fun onRewardEarned()
    fun onRewardFailed()
    fun onRewardNotAvailable()
}