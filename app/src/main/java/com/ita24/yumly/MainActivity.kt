package com.ita24.yumly

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import android.widget.LinearLayout
import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import android.R.attr.animation


class MainActivity : AppCompatActivity() {

    public var loggedInUsername: String? = null

    private var isCardUpFlipped = false
    private var isCardDownFlipped = false

    // Views for card flipping
    private lateinit var cardFrontUp: ConstraintLayout
    private lateinit var cardBackUp: ConstraintLayout
    private lateinit var cardFrontDown: ConstraintLayout
    private lateinit var cardBackDown: ConstraintLayout
    private lateinit var attributeGridUp: GridLayout
    private lateinit var recipeWebsiteButtonUp: Button
    private lateinit var attributeGridDown: GridLayout
    private lateinit var recipeWebsiteButtonDown: Button

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

        // Card views
        val imgupcard = findViewById<MaterialCardView>(R.id.imageUpCard)
        val imgdowncard = findViewById<MaterialCardView>(R.id.imageDownCard)

        // Funktion prüfen, ob Tutorial angezeigt werden soll
        fun shouldShowTutorial(): Boolean {
            val prefs = getSharedPreferences("tutorialPrefs", MODE_PRIVATE)
            return !prefs.getBoolean("hasSeenTutorial", false)
        }

        if(shouldShowTutorial()){
            val tutorialOverlay = findViewById<View>(R.id.tutorialOverlay)
            tutorialOverlay.visibility = View.VISIBLE
            showTutorial(true)
        }
        // Set camera distance for 3D rotation
        val scale = resources.displayMetrics.density
        imgupcard.cameraDistance = 8000 * scale
        imgdowncard.cameraDistance = 8000 * scale

        // Front and Back layouts
        cardFrontUp = findViewById(R.id.card_front_up)
        cardBackUp = findViewById(R.id.card_back_up)
        cardFrontDown = findViewById(R.id.card_front_down)
        cardBackDown = findViewById(R.id.card_back_down)

        // Back-of-card views
        attributeGridUp = findViewById(R.id.attribute_grid_up)
        recipeWebsiteButtonUp = findViewById(R.id.recipe_website_button_up)
        attributeGridDown = findViewById(R.id.attribute_grid_down)
        recipeWebsiteButtonDown = findViewById(R.id.recipe_website_button_down)

        // Original views
        val imgdown = findViewById<ImageView>(R.id.imageDown)
        val imgup = findViewById<ImageView>(R.id.imageUp)
        val dishNameDown = findViewById<TextView>(R.id.dishNameDown)
        val dishNameUp = findViewById<TextView>(R.id.dishNameTop)
        val zzDown = findViewById<TextView>(R.id.zzDown)
        val zzUp = findViewById<TextView>(R.id.zzTop)


        setupSwipeableCard(imgdowncard, imgdown, dishNameDown, zzDown, imgup)
        setupSwipeableCard(imgupcard, imgup, dishNameUp, zzUp, imgdown)

        lifecycleScope.launch {
            loadNewRecipe(imgdown, dishNameDown, zzDown)
            loadNewRecipe(imgup, dishNameUp, zzUp)
        }
    }

    fun sawTutorial() {
        val prefs = getSharedPreferences("tutorialPrefs", MODE_PRIVATE)
        prefs.edit().putBoolean("hasSeenTutorial", true).apply()
    }

    private fun getAttributeDrawableId(attribute: String): Int? {
        return when (attribute.lowercase().trim()) {
            "gekocht" -> R.drawable.cooked_att
            "gebraten" -> R.drawable.fried_att
            "frittiert" -> R.drawable.deep_fried_att
            "scharf" -> R.drawable.spicy_att
            "spicy" -> R.drawable.spicy_att
            "warme gerichte" -> R.drawable.hot_att
            "vegetarisch" -> R.drawable.veggie_att
            "herzhaft" -> R.drawable.hearty_att
            "glutenfrei" -> R.drawable.gluten_free_att
            "vegan" -> R.drawable.vegan_att
            "kalte gerichte" -> R.drawable.cold_att
            "laktosefrei" -> R.drawable.lactose_free_att
            "gebacken" -> R.drawable.baked_att
            "fast food" -> R.drawable.fast_food_att
            "süß" -> R.drawable.sweet_att
            "gegrillt" -> R.drawable.grilled_att
            "baked_att" -> R.drawable.baked_att
            "fried_att" -> R.drawable.fried_att
            "deep_fried_att" -> R.drawable.deep_fried_att
            "cold_att" -> R.drawable.cold_att
            "gluten_free_att" -> R.drawable.gluten_free_att
            "hot_att" -> R.drawable.hot_att
            "lactose_free_att" -> R.drawable.lactose_free_att
            "spicy_att" -> R.drawable.spicy_att
            "veggie_att" -> R.drawable.veggie_att
            "cooked_att" -> R.drawable.cooked_att
            "fast_food_att" -> R.drawable.fast_food_att
            "sweet_att" -> R.drawable.sweet_att
            "grilled_att" -> R.drawable.grilled_att
            "hearty_att" -> R.drawable.hearty_att
            "vegan_att" -> R.drawable.vegan_att

            else -> null
        }
    }

    private fun flipCard(isUpCard: Boolean) {
        val cardView: MaterialCardView
        val cardFront: ConstraintLayout
        val cardBack: ConstraintLayout
        val attributeGrid: GridLayout
        val recipeImageView: ImageView
        val websiteButton: Button

        if (isUpCard) {
            cardView = findViewById(R.id.imageUpCard)
            cardFront = cardFrontUp
            cardBack = cardBackUp
            attributeGrid = attributeGridUp
            recipeImageView = findViewById(R.id.imageUp)
            websiteButton = recipeWebsiteButtonUp
        } else {
            cardView = findViewById(R.id.imageDownCard)
            cardFront = cardFrontDown
            cardBack = cardBackDown
            attributeGrid = attributeGridDown
            recipeImageView = findViewById(R.id.imageDown)
            websiteButton = recipeWebsiteButtonDown
        }

        val isFlipped = if (isUpCard) isCardUpFlipped else isCardDownFlipped

        if (!isFlipped) {
            if (tutorialrunning) stopTutorial()
            attributeGrid.removeAllViews()
            val recipe = recipeImageView.tag as? List<*>
            if (recipe != null) {
                val attributesList = recipe.getOrNull(5) as? List<*>
                if (!attributesList.isNullOrEmpty()) {
                    attributesList.forEach { attribute ->
                        if (attribute is String) {
                            getAttributeDrawableId(attribute)?.let { drawableId ->
                                val imageView = ImageView(this).apply {
                                    setImageResource(drawableId)
                                    layoutParams = GridLayout.LayoutParams().apply {
                                        width = 0
                                        height = 0
                                        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                                        rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                                        setMargins(8, 8, 8, 8)
                                    }
                                }
                                attributeGrid.addView(imageView)
                            }
                        }
                    }
                }

                websiteButton.setOnClickListener {
                    val idObject = recipe.getOrNull(7)
                    val recipeId = when (idObject) {
                        is Number -> idObject.toInt()
                        is String -> idObject.toIntOrNull()
                        else -> null
                    }
                    if (recipeId != null) RecipeWebsite.sendToWebsite(this, recipeId)
                }
            }
            attributeGrid.setOnClickListener { flipCard(isUpCard) }

        } else {
            attributeGrid.setOnClickListener(null)
            websiteButton.setOnClickListener(null)
        }

        // Correct two-step animation to prevent mirror effect
        cardView.animate()
            .rotationY(90f)
            .setDuration(250)
            .withEndAction {
                cardView.rotationY = -90f
                if (isFlipped) {
                    cardBack.visibility = View.GONE
                    cardFront.visibility = View.VISIBLE
                } else {
                    cardFront.visibility = View.GONE
                    cardBack.visibility = View.VISIBLE
                }
                cardView.animate()
                    .rotationY(0f)
                    .setDuration(250)
                    .start()
            }.start()

        if (isUpCard) {
            isCardUpFlipped = !isCardUpFlipped
        } else {
            isCardDownFlipped = !isCardDownFlipped
        }
    }

    private fun resetCardFlip(isUpCard: Boolean) {
        val cardView = if (isUpCard) findViewById<MaterialCardView>(R.id.imageUpCard) else findViewById<MaterialCardView>(R.id.imageDownCard)
        cardView.rotationY = 0f
        if (isUpCard) {
            if (isCardUpFlipped) {
                isCardUpFlipped = false
                cardFrontUp.visibility = View.VISIBLE
                cardBackUp.visibility = View.GONE
                attributeGridUp.setOnClickListener(null)
                recipeWebsiteButtonUp.setOnClickListener(null)
            }
        } else {
            if (isCardDownFlipped) {
                isCardDownFlipped = false
                cardFrontDown.visibility = View.VISIBLE
                cardBackDown.visibility = View.GONE
                attributeGridDown.setOnClickListener(null)
                recipeWebsiteButtonDown.setOnClickListener(null)
            }
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

                    dishNameView.isSelected = true
                    dishNameView.isFocusable = true
                    dishNameView.isFocusableInTouchMode = true
                    dishNameView.requestFocus()
                }
            } catch (e: Exception) {
                Log.e("Yumly", "Error loading new recipe: $e")
            }
        }
    }

    var tutorialrunning = false;
    var firstuse = false
    private fun showTutorial(swipe: Boolean) {
        tutorialrunning = true;
        if(swipe) {
            firstuse = true;
            val tutorialOverlay = findViewById<View>(R.id.tutorialOverlay)
            val swipeTutorial = findViewById<LinearLayout>(R.id.swipeTutorial)
            val swipeArrow = findViewById<ImageView>(R.id.swipeArrow)
            val clickTutorial = findViewById<LinearLayout>(R.id.clickTutorial)
            val clickArrow = findViewById<ImageView>(R.id.clickArrow)

            tutorialOverlay.visibility = View.VISIBLE
            swipeTutorial.visibility = View.VISIBLE
            clickTutorial.visibility = View.GONE

            fun startSwipeAnimation(swipeArrow: ImageView) {
                val distance = 50f

                val animator = ValueAnimator.ofFloat(0f, distance, -distance, 0f).apply {
                    duration = 2000
                    interpolator = LinearInterpolator()
                    repeatCount = ValueAnimator.INFINITE

                    addUpdateListener { animation ->
                        swipeArrow.translationX = animation.animatedValue as Float
                    }
                }

                animator.start()
            }
            startSwipeAnimation(swipeArrow)

        }else{
            firstuse = false;
            val tutorialOverlay = findViewById<View>(R.id.tutorialOverlay)
            val clickTutorial = findViewById<LinearLayout>(R.id.clickTutorial)
            val clickArrow = findViewById<ImageView>(R.id.clickArrow)

            tutorialOverlay.visibility = View.VISIBLE
            clickTutorial.visibility = View.VISIBLE
            sawTutorial()
        }

    }


    private fun stopTutorial() {
        tutorialrunning = false;
        val tutorialOverlay = findViewById<View>(R.id.tutorialOverlay)
        val swipeTutorial = findViewById<LinearLayout>(R.id.swipeTutorial)
        val swipeArrow = findViewById<ImageView>(R.id.swipeArrow)
        val clickTutorial = findViewById<LinearLayout>(R.id.clickTutorial)
        val clickArrow = findViewById<ImageView>(R.id.clickArrow)

        // Tutorial-Overlay ausblenden
        tutorialOverlay.visibility = View.GONE
        swipeTutorial.visibility = View.GONE
        clickTutorial.visibility = View.GONE

        // Animationen abbrechen
        swipeArrow.animate().cancel()
        clickArrow.animate().cancel()

        swipeArrow.translationX = 0f
        clickArrow.translationX = 0f
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSwipeableCard(
        cardView: MaterialCardView,
        imageView: ImageView,
        dishNameView: TextView,
        zzView: TextView,
        otherview: ImageView
    ) {
        var dX = 0f
        var originalX = 0f
        var touchDownMs: Long = 0

        cardView.post { originalX = cardView.x }

        cardView.setOnTouchListener { view, event ->
            val isUpCard = cardView.id == R.id.imageUpCard
            val isFlipped = if (isUpCard) isCardUpFlipped else isCardDownFlipped

            if (isFlipped) {
                return@setOnTouchListener false
            }

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    touchDownMs = System.currentTimeMillis()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (tutorialrunning) stopTutorial()
                    val newX = event.rawX + dX
                    view.x = newX
                    view.rotation = (newX - originalX) / (view.width.toFloat() / 2) * 15f
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val touchDuration = System.currentTimeMillis() - touchDownMs
                    val screenWidth = resources.displayMetrics.widthPixels
                    val displacement = view.x - originalX

                    if (touchDuration < 200 && Math.abs(displacement) < 20) {
                        view.animate().x(originalX).rotation(0f).setDuration(100).start()
                        flipCard(isUpCard)
                    } else if (Math.abs(displacement) > screenWidth / 4) {
                        val endX = if (displacement > 0) screenWidth.toFloat() else -view.width.toFloat()
                        view.animate()
                            .x(endX)
                            .alpha(0f)
                            .setDuration(300)
                            .withEndAction {
                                view.x = originalX
                                view.rotation = 0f
                                view.alpha = 1f

                                resetCardFlip(isUpCard)

                                val winner = otherview.tag as? MutableList<Any>
                                val loser = imageView.tag as? MutableList<Any>

                                if (winner != null && loser != null) {
                                    lifecycleScope.launch { EloManager.updateElo(winner, loser, EloManager.wasFastEnough()) }
                                }

                                loadNewRecipe(imageView, dishNameView, zzView)
                                if(firstuse)showTutorial(false)
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
