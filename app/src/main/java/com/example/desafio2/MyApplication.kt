package com.example.desafio2

import android.app.Application
import com.cloudinary.android.MediaManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val config = mapOf(
            "cloud_name" to "",
            "api_key" to "",
            "api_secret" to ""
        )
        MediaManager.init(this, config)
    }
}