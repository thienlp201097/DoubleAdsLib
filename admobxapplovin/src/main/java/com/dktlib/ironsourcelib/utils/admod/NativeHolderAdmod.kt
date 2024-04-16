package com.dktlib.ironsourcelib.utils.admod

import androidx.lifecycle.MutableLiveData
import com.applovin.mediation.MaxAd
import com.google.android.gms.ads.nativead.NativeAd

class NativeHolderAdmod(var ads: String){
    var nativeAd : NativeAd?= null
    var isLoad = false
    var native_mutable: MutableLiveData<NativeAd> = MutableLiveData()
}