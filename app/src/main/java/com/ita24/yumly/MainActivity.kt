package com.ita24.yumly

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.widget.*
import android.view.View

class MainActivity : AppCompatActivity() {

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

        val imgdown = findViewById<ImageView>(R.id.imageDown)
        val imgup = findViewById<ImageView>(R.id.imageUp)

        lifecycleScope.launch {
            try {
            imageloader.preloadImgs(this@MainActivity)
            imageloader.loadnewImg(imgdown)
            imageloader.loadnewImg(imgup)
            }catch (e: Exception){}
        }
    }

}

