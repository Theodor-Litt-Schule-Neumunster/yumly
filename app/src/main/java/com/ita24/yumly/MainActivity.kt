package com.ita24.yumly

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    public var loggedInUsername: String? = null

    private var isCardUpFlipped = false
    private var isCardDownFlipped = false

    // Views for card flipping
    private lateinit var cardFrontUp: ConstraintLayout
    private lateinit var cardBackUp: ConstraintLayout
    private lateinit var cardFrontDown: ConstraintLayout
    private lateinit var cardBackDown: ConstraintLayout

    private lateinit var viewFlipperUp: ViewFlipper
    private lateinit var attributeGridUp: GridLayout
    private lateinit var allergenGridUp: GridLayout
    private lateinit var zutatenTextUp: TextView
    private lateinit var recipeWebsiteButtonUp: Button

    private lateinit var viewFlipperDown: ViewFlipper
    private lateinit var attributeGridDown: GridLayout
    private lateinit var allergenGridDown: GridLayout
    private lateinit var zutatenTextDown: TextView
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

        // Menu button setup
        val menuButton = findViewById<ImageView>(R.id.menuButton)
        menuButton.setOnClickListener { view ->
            showMenu(view)
        }

        // Card views
        val imgupcard = findViewById<MaterialCardView>(R.id.imageUpCard)
        val imgdowncard = findViewById<MaterialCardView>(R.id.imageDownCard)

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
        viewFlipperUp = findViewById(R.id.viewFlipperUp)
        attributeGridUp = findViewById(R.id.attribute_grid_up)
        allergenGridUp = findViewById(R.id.allergen_grid_up)
        zutatenTextUp = findViewById(R.id.ingredients_text_up)
        recipeWebsiteButtonUp = findViewById(R.id.recipe_website_button_up)

        viewFlipperDown = findViewById(R.id.viewFlipperDown)
        attributeGridDown = findViewById(R.id.attribute_grid_down)
        allergenGridDown = findViewById(R.id.allergen_grid_down)
        zutatenTextDown = findViewById(R.id.ingredients_text_down)
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

        if (shouldShowTutorial()) {
            val tutorialOverlay = findViewById<View>(R.id.tutorialOverlay)
            tutorialOverlay.visibility = View.VISIBLE
            showTutorial("swipe")
        }
    }

    private fun shouldShowTutorial(): Boolean {
        val prefs = getSharedPreferences("tutorialPrefs", MODE_PRIVATE)
        return !prefs.getBoolean("hasSeenTutorial", false)
    }

    private fun showMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.main_menu, popup.menu)

        try {
            val fieldPopupDelegate = PopupMenu::class.java.getDeclaredField("mPopup")
            fieldPopupDelegate.isAccessible = true
            val mPopup = fieldPopupDelegate.get(popup)
            mPopup.javaClass
                .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(mPopup, true)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error showing menu icons", e)
        }

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_home -> {
                    val intent = Intent(this, WelcomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }
                R.id.action_filter -> {
                    val intent = Intent(this, FilterActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.action_tutorial -> {
                    showTutorial("swipe")
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun getAttributeDrawableId(attribute: String): Int? {
        return when (attribute.lowercase().trim()) {
            "gekocht" -> R.drawable.cooked_att
            "gebraten" -> R.drawable.fried_att
            "frittiert" -> R.drawable.deep_fried_att
            "scharf" -> R.drawable.spicy_att
            "spicy" -> R.drawable.spicy_att
            "warme gerichte" -> R.drawable.hot_att
            "warm" -> R.drawable.hot_att
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

    private fun getAllergenDrawableId(allergen: String): Int? {
        return when (allergen.lowercase().trim()) {
            "gluten", "gluten_all" -> R.drawable.gluten_all
            "ei", "eggs_all" -> R.drawable.eggs_all
            "fisch", "fish_all" -> R.drawable.fish_all
            "erdnüsse", "nüsse", "schalenfrüchte", "nuts_all" -> R.drawable.nuts_all
            "soja", "soy_all" -> R.drawable.soy_all
            "laktose", "lactose_all" -> R.drawable.lactose_all
            "sellerie", "celery_all" -> R.drawable.celery_all
            "senf", "mustard_all" -> R.drawable.mustard_all
            "sesam", "sesame_all" -> R.drawable.sesame_all
            "schwefeldioxid", "sulfite", "schwefel", "sulphur_all", "sulphites_all" -> R.drawable.sulphites_all
            "weichtiere", "molluscs_all" -> R.drawable.molluscs_all
            "krebstiere", "crustacea_all" -> R.drawable.crustacea_all
            else -> null
        }
    }

    private fun applyBackCardTouchListenerRecursively(view: View, listener: View.OnTouchListener) {
        if (view is Button) return
        view.setOnTouchListener(listener)
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                applyBackCardTouchListenerRecursively(view.getChildAt(i), listener)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun flipCard(isUpCard: Boolean) {
        val cardView: MaterialCardView
        val cardFront: ConstraintLayout
        val cardBack: ConstraintLayout
        val flipper: ViewFlipper
        val attrGrid: GridLayout
        val allGrid: GridLayout
        val ingText: TextView
        val recipeImageView: ImageView
        val websiteButton: Button

        if (isUpCard) {
            cardView = findViewById(R.id.imageUpCard)
            cardFront = cardFrontUp
            cardBack = cardBackUp
            flipper = viewFlipperUp
            attrGrid = attributeGridUp
            allGrid = allergenGridUp
            ingText = zutatenTextUp
            recipeImageView = findViewById(R.id.imageUp)
            websiteButton = recipeWebsiteButtonUp
        } else {
            cardView = findViewById(R.id.imageDownCard)
            cardFront = cardFrontDown
            cardBack = cardBackDown
            flipper = viewFlipperDown
            attrGrid = attributeGridDown
            allGrid = allergenGridDown
            ingText = zutatenTextDown
            recipeImageView = findViewById(R.id.imageDown)
            websiteButton = recipeWebsiteButtonDown
        }

        val isFlipped = if (isUpCard) isCardUpFlipped else isCardDownFlipped

        if (!isFlipped) {
            if(tutorialrunning) stopTutorial()
            
            flipper.displayedChild = 0 
            attrGrid.removeAllViews()
            allGrid.removeAllViews()

            val recipe = recipeImageView.tag as? List<*>
            if (recipe != null) {
                // Attributes
                val attributesList = recipe.getOrNull(5) as? List<*>
                attributesList?.forEach { attr ->
                    if (attr is String) {
                        getAttributeDrawableId(attr)?.let { resId ->
                            val iv = ImageView(this).apply {
                                setImageResource(resId)
                                layoutParams = GridLayout.LayoutParams().apply {
                                    width = 0
                                    height = 0
                                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                                    setMargins(8, 8, 8, 8)
                                }
                            }
                            attrGrid.addView(iv)
                        }
                    }
                }

                // Allergens
                val allergensList = recipe.getOrNull(4) as? List<*>
                allergensList?.forEach { all ->
                    if (all is String) {
                        getAllergenDrawableId(all)?.let { resId ->
                            val iv = ImageView(this).apply {
                                setImageResource(resId)
                                // WICHTIG: Hier exakt das gleiche Verhalten wie bei Attributen
                                layoutParams = GridLayout.LayoutParams().apply {
                                    width = 0
                                    height = 0
                                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                                    setMargins(8, 8, 8, 8)
                                }
                            }
                            allGrid.addView(iv)
                        }
                    }
                }

                // Ingredients
                val ingredientsList = recipe.getOrNull(3) as? List<*>
                ingText.text = ingredientsList?.joinToString(", ") ?: ""

                websiteButton.setOnClickListener {
                    val recipeSource = recipe.getOrNull(8) as? String
                    if (recipeSource != null) {
                        RecipeWebsite.openSource(this, recipeSource)
                    } else {
                        val idObject = recipe.getOrNull(7)
                        val recipeId = (idObject as? Number)?.toInt() ?: idObject.toString().toIntOrNull()
                        if (recipeId != null) RecipeWebsite.sendToWebsite(this, recipeId)
                    }
                }
            }

            // Swipe & Tap Detection
            val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean = true 
                
                override fun onFling(e1: MotionEvent?, e2: MotionEvent, vx: Float, vy: Float): Boolean {
                    val diffX = (e1?.x ?: 0f) - e2.x
                    if(tutorialrunning) stopTutorial()
                    if (Math.abs(diffX) > 100) {
                        if (diffX > 0) { // Swipe Left -> Show Allergens
                            flipper.setInAnimation(this@MainActivity, R.anim.slide_in_right)
                            flipper.setOutAnimation(this@MainActivity, R.anim.slide_out_left)
                            if (flipper.displayedChild == 0) flipper.showNext()
                        } else { // Swipe Right -> Show Attributes
                            flipper.setInAnimation(this@MainActivity, R.anim.slide_in_left)
                            flipper.setOutAnimation(this@MainActivity, R.anim.slide_out_right)
                            if (flipper.displayedChild == 1) flipper.showPrevious()
                        }
                        return true
                    }
                    return false
                }
                
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    flipCard(isUpCard)
                    return true
                }
            })
            
            val touchListener = View.OnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
            applyBackCardTouchListenerRecursively(cardBack, touchListener)

        } else {
            applyBackCardTouchListenerRecursively(cardBack, View.OnTouchListener { _, _ -> false })
            websiteButton.setOnClickListener(null)
        }

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
                    .withEndAction {
                        if(firstuse)showTutorial("secondswipe")
                    }
                    .start()
            }.start()


        if (isUpCard) isCardUpFlipped = !isCardUpFlipped else isCardDownFlipped = !isCardDownFlipped
    }

    private fun resetCardFlip(isUpCard: Boolean) {
        val cardView = if (isUpCard) findViewById<MaterialCardView>(R.id.imageUpCard) else findViewById<MaterialCardView>(R.id.imageDownCard)
        cardView.rotationY = 0f
        if (isUpCard) {
            if (isCardUpFlipped) {
                isCardUpFlipped = false
                cardFrontUp.visibility = View.VISIBLE
                cardBackUp.visibility = View.GONE
                applyBackCardTouchListenerRecursively(cardFrontUp, View.OnTouchListener { _, _ -> false })
                recipeWebsiteButtonUp.setOnClickListener(null)
            }
        } else {
            if (isCardDownFlipped) {
                isCardDownFlipped = false
                cardFrontDown.visibility = View.VISIBLE
                cardBackDown.visibility = View.GONE
                applyBackCardTouchListenerRecursively(cardFrontDown, View.OnTouchListener { _, _ -> false })
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
                    dishNameView.text = recipe[0] as? String ?: ""
                    zzView.text = "${recipe[2]} Min."
                    EloManager.starttimer()
                    dishNameView.isSelected = true
                    dishNameView.requestFocus()
                }
            } catch (e: Exception) {
                Log.e("Yumly", "Error loading new recipe: $e")
            }
        }
    }

    var tutorialrunning = false;
    var firstuse = false
    private fun showTutorial(swipe: String) {

        tutorialrunning = true;
        val tutorialOverlay = findViewById<View>(R.id.tutorialOverlay)
        val swipeTutorial = findViewById<LinearLayout>(R.id.swipeTutorial)
        val secondSwipeTutorial = findViewById<LinearLayout>(R.id.secondSwipeTutorial)

        val swipeArrow = findViewById<ImageView>(R.id.swipeArrow)
        val secondSwipeArrow = findViewById<ImageView>(R.id.secondSwipeArrow)

        val clickTutorial = findViewById<LinearLayout>(R.id.clickTutorial)
        val clickArrow = findViewById<ImageView>(R.id.clickArrow)

        tutorialOverlay.visibility = View.VISIBLE


        fun startSwipeanimation(whichTut: ImageView){

            val animator = ValueAnimator.ofFloat(0f, 50f, -50f, 0f).apply {
                duration = 2000
                interpolator = LinearInterpolator()
                repeatCount = ValueAnimator.INFINITE
                addUpdateListener { animation -> whichTut.translationX = animation.animatedValue as Float }
            }
            animator.start()
        }

        if(swipe == "swipe") {
            firstuse = true;
            swipeTutorial.visibility = View.VISIBLE
            clickTutorial.visibility = View.GONE
            secondSwipeTutorial.visibility = View.GONE
            startSwipeanimation(swipeArrow)

        }
        if(swipe == "click"){
            swipeTutorial.visibility = View.GONE
            clickTutorial.visibility = View.VISIBLE
            val animator = ObjectAnimator.ofFloat(clickArrow, "translationY", 0f, 20f)
            animator.setDuration(500)
            animator.setRepeatMode(ValueAnimator.REVERSE)
            animator.setRepeatCount(ValueAnimator.INFINITE)
            animator.start()
        }
        if (swipe == "secondswipe"){
            firstuse = false;
            secondSwipeTutorial.visibility = View.VISIBLE
            clickTutorial.visibility = View.GONE
            startSwipeanimation(secondSwipeArrow)
        }

    }


    private fun stopTutorial() {
        tutorialrunning = false;
        findViewById<View>(R.id.tutorialOverlay).visibility = View.GONE
        findViewById<ImageView>(R.id.swipeArrow).animate().cancel()
        findViewById<ImageView>(R.id.secondSwipeArrow).animate().cancel()
        findViewById<ImageView>(R.id.clickArrow).animate().cancel()
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

            if (isFlipped) return@setOnTouchListener false

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    touchDownMs = System.currentTimeMillis()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if(tutorialrunning) stopTutorial()
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
                                val winner = otherview.tag as? MutableList<Any?>
                                val loser = imageView.tag as? MutableList<Any?>
                                if (winner != null && loser != null) {
                                    lifecycleScope.launch { EloManager.updateElo(winner, loser, EloManager.wasFastEnough()) }
                                }
                                loadNewRecipe(imageView, dishNameView, zzView)
                                if(firstuse)showTutorial("click")
                            }.start()
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
