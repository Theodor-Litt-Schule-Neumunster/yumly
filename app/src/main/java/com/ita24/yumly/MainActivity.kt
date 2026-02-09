package com.ita24.yumly

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
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

        userdatapref.init(applicationContext)


        loggedInUsername = intent.getStringExtra(LoginActivity.EXTRA_USERNAME)


        val imgdown = findViewById<ImageView>(R.id.imageDown)
        val imgup = findViewById<ImageView>(R.id.imageUp)
        val button = findViewById<Button>(R.id.btnNext)

        setupSwipeableImage(imgdown)
        setupSwipeableImage(imgup)

        lifecycleScope.launch {
            try {
                UserDataOnline(loggedInUsername ?: "").start()
                imageloader.loadList()

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

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSwipeableImage(imageView: ImageView) {
        var dX = 0f
        var originalX = 0f
        imageView.post {
            originalX = imageView.x
        }

        imageView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val newX = event.rawX + dX
                    view.x = newX
                    val rotation = (newX - originalX) / (view.width.toFloat() / 2) * 15f
                    view.rotation = rotation
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val screenWidth = resources.displayMetrics.widthPixels
                    val displacement = view.x - originalX

                    if (Math.abs(displacement) > screenWidth / 4) {
                        val endX = if (displacement > 0) screenWidth.toFloat() else -view.width.toFloat()
                        view.animate()
                            .x(endX)
                            .alpha(0f)
                            .setDuration(300)
                            .withEndAction {
                                view.x = originalX
                                view.rotation = 0f
                                view.alpha = 1f
                                lifecycleScope.launch {
                                    imageloader.loadnewImg(imageView)
                                }
                            }
                            .start()
                    } else {
                        view.animate()
                            .x(originalX)
                            .rotation(0f)
                            .setDuration(200)
                            .start()
                    }
                    true
                }
                else -> false
            }
        }
    }
}
