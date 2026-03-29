package com.example.desafio2

import android.app.Application
import com.cloudinary.android.MediaManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val config = mapOf(
            "cloud_name" to "du0nm4hsj",
            "api_key" to "864696861758682",
            "api_secret" to "B5ujKoIv4DMV3xV3smUkrYI1OMQ"
        )
        MediaManager.init(this, config)
    }
}