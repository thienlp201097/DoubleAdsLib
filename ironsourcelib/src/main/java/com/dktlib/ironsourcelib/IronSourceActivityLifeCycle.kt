package com.dktlib.ironsourcelib

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.dktlib.ironsourcelib.utils.SweetAlert.SweetAlertDialog

class DialogHelperActivityLifeCycle(val dialog: SweetAlertDialog) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onDestroy(){
        if(dialog.isShowing){
            dialog.dismiss()
            if (AppOpenManager.getInstance().isInitialized) {
                AppOpenManager.getInstance().isAppResumeEnabled = true
            }
        }
    }
}