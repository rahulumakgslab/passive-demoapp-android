package com.sonde.base.presentation

import android.app.Application
import android.content.Context

open class BaseApplication : Application() {


    companion object {
        var instance: BaseApplication? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}