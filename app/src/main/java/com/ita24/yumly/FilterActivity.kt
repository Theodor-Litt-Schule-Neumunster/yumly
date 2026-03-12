package com.ita24.yumly

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText

class FilterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)

        val toolbar = findViewById<Toolbar>(R.id.filterToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // --- Attributes ---
        setupRecyclerView(
            findViewById(R.id.rvWhiteAttributes),
            createAttributeList(),
            Filter.whitelistAttributes
        ) { item -> Filter.saveWhiteAttribute(item) }

        setupRecyclerView(
            findViewById(R.id.rvBlackAttributes),
            createAttributeList(),
            Filter.blacklistAttributes
        ) { item -> Filter.saveBlackAttribute(item) }

        // --- Allergies ---
        setupRecyclerView(
            findViewById(R.id.rvBlackAllergies),
            createAllergyList(),
            Filter.blacklistAllergies
        ) { item -> Filter.saveBlackAllergy(item) }

        // --- Ingredients Blacklist ---
        val cgBlackIngredients = findViewById<ChipGroup>(R.id.cgBlackIngredients)
        val etBlackIngredient = findViewById<TextInputEditText>(R.id.etBlackIngredient)
        val btnAddBlackIngredient = findViewById<ImageButton>(R.id.btnAddBlackIngredient)

        Filter.blacklistIngredients.forEach { ingredient ->
            addIngredientChip(ingredient, cgBlackIngredients)
        }

        btnAddBlackIngredient.setOnClickListener {
            val text = etBlackIngredient.text.toString().trim()
            if (text.isNotEmpty() && !Filter.blacklistIngredients.contains(text)) {
                Filter.saveBlackIngredient(text)
                addIngredientChip(text, cgBlackIngredients)
                etBlackIngredient.text?.clear()
            }
        }

        findViewById<Button>(R.id.btnApplyFilter).setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView(
        rv: RecyclerView,
        items: List<AttributeItem>,
        activeSet: Set<String>,
        onToggle: (String) -> Unit
    ) {
        items.forEach { it.isSelected = activeSet.contains(it.name) }
        rv.adapter = FilterAttributeAdapter(items, onToggle)
    }

    private fun addIngredientChip(text: String, chipGroup: ChipGroup) {
        val chip = Chip(this)
        chip.text = text
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            Filter.saveBlackIngredient(text)
            chipGroup.removeView(chip)
        }
        chipGroup.addView(chip)
    }

    private fun createAttributeList() = listOf(
        AttributeItem("baked_att", R.drawable.baked_att),
        AttributeItem("cooked_att", R.drawable.cooked_att),
        AttributeItem("fried_att", R.drawable.fried_att),
        AttributeItem("deep_fried_att", R.drawable.deep_fried_att),
        AttributeItem("grilled_att", R.drawable.grilled_att),
        AttributeItem("fast_food_att", R.drawable.fast_food_att),
        AttributeItem("cold_att", R.drawable.cold_att),
        AttributeItem("hot_att", R.drawable.hot_att),
        AttributeItem("gluten_free_att", R.drawable.gluten_free_att),
        AttributeItem("lactose_free_att", R.drawable.lactose_free_att),
        AttributeItem("spicy_att", R.drawable.spicy_att),
        AttributeItem("sweet_att", R.drawable.sweet_att),
        AttributeItem("vegan_att", R.drawable.vegan_att),
        AttributeItem("veggie_att", R.drawable.veggie_att),
        AttributeItem("hearty_att", R.drawable.hearty_att)
    )

    private fun createAllergyList() = listOf(
        AttributeItem("gluten_all", R.drawable.gluten_all),
        AttributeItem("lactose_all", R.drawable.lactose_all),
        AttributeItem("milk_all", R.drawable.milk_all),
        AttributeItem("eggs_all", R.drawable.eggs_all),
        AttributeItem("fish_all", R.drawable.fish_all),
        AttributeItem("crustacea_all", R.drawable.crustacea_all),
        AttributeItem("molluscs_all", R.drawable.molluscs_all),
        AttributeItem("nuts_all", R.drawable.nuts_all),
        AttributeItem("soy_all", R.drawable.soy_all),
        AttributeItem("mustard_all", R.drawable.mustard_all),
        AttributeItem("celery_all", R.drawable.celery_all),
        AttributeItem("sesame_all", R.drawable.sesame_all),
        AttributeItem("sulphites_all", R.drawable.sulphites_all)
    )

    // Internal specialized adapter to avoid modifying the generic AttributeAdapter
    private class FilterAttributeAdapter(
        private val items: List<AttributeItem>,
        private val onToggle: (String) -> Unit
    ) : RecyclerView.Adapter<FilterAttributeAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.findViewById(R.id.attributeImageView)
            val overlay: View = view.findViewById(R.id.selectionOverlay)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_attribute, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.imageView.setImageResource(item.drawableId)
            holder.overlay.visibility = if (item.isSelected) View.VISIBLE else View.GONE
            holder.itemView.setOnClickListener {
                item.isSelected = !item.isSelected
                notifyItemChanged(position)
                onToggle(item.name)
            }
        }

        override fun getItemCount() = items.size
    }
}
