package com.menac1ngmonkeys.monkeyslimit

import android.app.Application
import com.menac1ngmonkeys.monkeyslimit.data.AppContainer
import com.menac1ngmonkeys.monkeyslimit.data.DefaultAppContainer

class MonkeyslimitApplication : Application() {

    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
