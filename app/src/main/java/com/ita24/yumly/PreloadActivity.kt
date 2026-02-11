package com.ita24.yumly

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PreloadActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preload)
        userdatapref.init(applicationContext)

        val username = intent.getStringExtra(LoginActivity.EXTRA_USERNAME)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                UserDataOnline(username ?: "").start()
                imageloader.loadList()
                imageloader.preloadImgs(this@PreloadActivity)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            withContext(Dispatchers.Main) {
                val intent = Intent(this@PreloadActivity, MainActivity::class.java)
                intent.putExtra(LoginActivity.EXTRA_USERNAME, username)
                startActivity(intent)
                finish()
            }
        }
    }
}