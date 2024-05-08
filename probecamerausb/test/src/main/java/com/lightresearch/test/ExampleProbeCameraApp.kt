package com.lightresearch.test

import android.content.Context
import com.jiangdg.ausbc.base.BaseApplication
import androidx.multidex.MultiDex
import com.facebook.drawee.backends.pipeline.Fresco
import com.lightresearch.probecamera.MMKVUtils

class ExampleProbeCameraApp: BaseApplication() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        if(!Fresco.hasBeenInitialized()){
            Fresco.initialize(this)
        }
        MMKVUtils.init(this)
    }
}