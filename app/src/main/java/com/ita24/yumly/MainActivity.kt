package com.ita24.yumly

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var loggedInUsername: String? = null
    val imageloader = Imageloader()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loggedInUsername = intent.getStringExtra(LoginActivity.EXTRA_USERNAME)

        val imgdown = findViewById<ImageView>(R.id.imageDown)
        val imgup = findViewById<ImageView>(R.id.imageUp)
        val button = findViewById<Button>(R.id.btnNext)

        lifecycleScope.launch {
            try {
                Log.e("testbutton", "lifescope an")
                imageloader.loadList()
                Log.e("testbutton", "lifescope aus")

                imageloader.preloadImgs(this@MainActivity)

                button.setOnClickListener {
                    lifecycleScope.launch {
                        imageloader.loadnewImg(imgdown)
                        imageloader.loadnewImg(imgup)
                        Log.e("testbutton", "buttonclick")
                    }
                }
            } catch (e: Exception) {
                Log.e("testbutton", "${e}")
            }
        }
    }
}
