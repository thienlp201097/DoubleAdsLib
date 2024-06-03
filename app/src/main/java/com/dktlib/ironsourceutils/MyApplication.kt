package com.dktlib.ironsourceutils

import com.dktlib.ironsourcelib.adjust.AdjustUtils
import com.dktlib.ironsourcelib.application.AdsApplication

class MyApplication : AdsApplication() {
    override fun onCreateApplication() {
        AdjustUtils.initAdjust(this,"",false)
    }
}