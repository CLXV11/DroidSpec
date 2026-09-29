package com.droidspec

import android.app.Application
import com.droidspec.core.di.AppContainer

class DroidSpecApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
