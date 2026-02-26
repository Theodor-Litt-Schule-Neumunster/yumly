package com.ita24.yumly

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
class MainActivity : AppCompatActivity() {

    private var loggedInUsername: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        Imageloader.resetExcludedList()

        loggedInUsername = intent.getStringExtra(LoginActivity.EXTRA_USERNAME)

        val imgdown = findViewById<ImageView>(R.id.imageDown)
        val imgup = findViewById<ImageView>(R.id.imageUp)

        val imgupcard = findViewById<com.google.android.material.card.MaterialCardView>(R.id.imageUpCard)
        val imgdowncard = findViewById<com.google.android.material.card.MaterialCardView>(R.id.imageDownCard)



        val dishNameDown = findViewById<TextView>(R.id.dishNameDown)
        val dishNameUp = findViewById<TextView>(R.id.dishNameTop)

        val zzDown = findViewById<TextView>(R.id.zzDown)
        val zzUp = findViewById<TextView>(R.id.zzTop)

        setupSwipeableCard(imgdowncard,imgdown, dishNameDown, zzDown, imgup)
        setupSwipeableCard(imgupcard,imgup, dishNameUp, zzUp, imgdown)

        lifecycleScope.launch {
            loadNewRecipe(imgdown, dishNameDown, zzDown)
            loadNewRecipe(imgup, dishNameUp, zzUp)
        }
    }

    private fun loadNewRecipe(imageView: ImageView, dishNameView: TextView, zzView: TextView) {
        lifecycleScope.launch {
            try {
                val recipe = Imageloader.loadnewImg(imageView)
                if (recipe != null) {
                    imageView.tag = recipe
                    dishNameView.text = recipe[0] as String
                    zzView.text = "${recipe[2]} Min."
                    EloManager.starttimer()
                }
            } catch (e: Exception) {
                Log.e("testbutton", "${e}")
            }
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setupSwipeableCard(
        cardView: View,
        imageView: ImageView,
        dishNameView: TextView,
        zzView: TextView,
        otherview: ImageView
    ) {
        var dX = 0f
        var originalX = 0f

        cardView.post { originalX = cardView.x }

        cardView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val newX = event.rawX + dX
                    view.x = newX
                    view.rotation = (newX - originalX) / (view.width.toFloat() / 2) * 15f
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

                                val winner = otherview.tag as? MutableList<Any>
                                val loser = imageView.tag as? MutableList<Any>

                                if (winner != null && loser != null) {
                                    EloManager.updateElo(winner, loser, EloManager.wasFastEnough())
                                }

                                loadNewRecipe(imageView, dishNameView, zzView)
                            }
                            .start()
                    } else {
                        view.animate().x(originalX).rotation(0f).setDuration(200).start()
                    }
                    true
                }
                else -> false
            }
        }
    }
}
