package com.ita24.yumly

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.joinToString
import kotlin.collections.map
import kotlin.text.isNotEmpty
import kotlin.text.split
import kotlin.text.trim

class UploadRecipesActivity : AppCompatActivity() {

    private val ingredients = mutableListOf<String>()
    private var imageUri: Uri? = null
    private lateinit var recipeImageView: ImageView
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_recipes)

        recipeImageView = findViewById(R.id.recipeImageView)

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let {
                    imageUri = it
                    recipeImageView.setImageURI(it)
                }
            }
        }

        val uploadImageButton = findViewById<Button>(R.id.uploadImageButton)
        uploadImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        }

        // --- Attribute Selection Setup ---
        val recyclerView = findViewById<RecyclerView>(R.id.attributesRecyclerView)
        val attributes = createAttributeList()
        val attributeAdapter = AttributeAdapter(attributes)
        recyclerView.adapter = attributeAdapter

        // --- Ingredient-Adding Setup ---
        val ingredientEditText = findViewById<EditText>(R.id.ingredientEditText)
        val addIngredientButton = findViewById<ImageButton>(R.id.addIngredientButton)
        val ingredientChipGroup = findViewById<ChipGroup>(R.id.ingredientChipGroup)

        addIngredientButton.setOnClickListener {
            val ingredientsText = ingredientEditText.text.toString().trim()
            if (ingredientsText.isNotEmpty()) {
                // Split the input by commas and trim whitespace from each ingredient
                val newIngredients = ingredientsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                
                newIngredients.forEach { ingredient ->
                    if (!ingredients.contains(ingredient)) {
                        addIngredientChip(ingredient, ingredientChipGroup)
                        ingredients.add(ingredient)
                    }
                }
                ingredientEditText.text.clear() // Clear the input field
            }
        }

        // --- Save Button Setup ---
        val saveButton = findViewById<Button>(R.id.saveRecipe)
        saveButton.setOnClickListener {
            val selectedAttributeNames = attributeAdapter.selectedAttributes
            // TODO: Add logic to save the recipe with the selected attributes and ingredients
            Toast.makeText(this, "Ausgewählte Attribute: ${'$'}{selectedAttributeNames.joinToString()}", Toast.LENGTH_LONG).show()
            Toast.makeText(this, "Zutaten: ${'$'}{ingredients.joinToString()}", Toast.LENGTH_LONG).show()
        }
    }

    private fun createAttributeList(): List<AttributeItem> {
        return listOf(
            AttributeItem("baked_att", R.drawable.baked_att),
            AttributeItem("cold_att", R.drawable.cold_att),
            AttributeItem("cooked_att", R.drawable.cooked_att),
            AttributeItem("fast_food_att", R.drawable.fast_food_att),
            AttributeItem("gluten_free_att", R.drawable.gluten_free_att),
            AttributeItem("grilled_att", R.drawable.grilled_att),
            AttributeItem("hearty_att", R.drawable.hearty_att),
            AttributeItem("hot_att", R.drawable.hot_att),
            AttributeItem("lactose_free_att", R.drawable.lactose_free_att),
            AttributeItem("spicy_att", R.drawable.spicy_att),
            AttributeItem("sweet_att", R.drawable.sweet_att),
            AttributeItem("vegan_att", R.drawable.vegan_att),
            AttributeItem("veggie_att", R.drawable.veggie_att)
        )
    }

    private fun addIngredientChip(text: String, chipGroup: ChipGroup) {
        val chip = Chip(this)
        chip.text = text
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            chipGroup.removeView(chip)
            ingredients.remove(text)
        }
        chipGroup.addView(chip)
    }
}
