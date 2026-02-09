package com.ita24.yumly

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log

class Preloader : Application() {

    override fun onCreate() {
        super.onCreate()
        val imageloader = Imageloader()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                imageloader.preloadImgs(this@Preloader)
            } catch (e: Exception) {
                Log.e("Preloader", "preload failed", e)
            }
        }
    }
}
    