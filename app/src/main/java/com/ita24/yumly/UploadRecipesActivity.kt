package com.ita24.yumly

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import java.io.File

class UploadRecipesActivity : AppCompatActivity() {

    private val ingredients = mutableListOf<String>()
    private var imageUri: Uri? = null
    private var tempImageUri: Uri? = null
    private lateinit var recipeImageView: ImageView
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_recipes)

        recipeImageView = findViewById(R.id.recipeImageView)

        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageUri = it
                recipeImageView.setImageURI(it)
            }
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess: Boolean ->
            if (isSuccess) {
                tempImageUri?.let {
                    imageUri = it
                    recipeImageView.setImageURI(it)
                }
            }
        }

        val uploadImageButton = findViewById<Button>(R.id.uploadImageButton)
        uploadImageButton.setOnClickListener {
            val options = arrayOf("Kamera", "Galerie")
            AlertDialog.Builder(this)
                .setTitle("Bild auswählen")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> {
                            tempImageUri = createImageUri()
                            tempImageUri?.let { cameraLauncher.launch(it) }
                        }
                        1 -> galleryLauncher.launch("image/*")
                    }
                }
                .show()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.attributesRecyclerView)
        val attributes = createAttributeList()
        val attributeAdapter = AttributeAdapter(attributes)
        recyclerView.adapter = attributeAdapter

        val ingredientEditText = findViewById<EditText>(R.id.ingredientEditText)
        val addIngredientButton = findViewById<ImageButton>(R.id.addIngredientButton)
        val ingredientChipGroup = findViewById<ChipGroup>(R.id.ingredientChipGroup)

        addIngredientButton.setOnClickListener {
            val ingredientsText = ingredientEditText.text.toString().trim()
            if (ingredientsText.isNotEmpty()) {
                val newIngredients = ingredientsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                newIngredients.forEach { ingredient ->
                    if (!ingredients.contains(ingredient)) {
                        addIngredientChip(ingredient, ingredientChipGroup)
                        ingredients.add(ingredient)
                    }
                }
                ingredientEditText.text.clear()
            }
        }

        val saveButton = findViewById<Button>(R.id.saveRecipe)
        val nameEditText = findViewById<TextInputEditText>(R.id.nameEditText)

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Bitte geben Sie dem Rezept einen Namen", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Rezept \"$name\" hinzugefügt", Toast.LENGTH_SHORT).show()
            finish() // Go back to the previous activity
        }
    }

    private fun createImageUri(): Uri {
        val image = File(filesDir, "camera_photo.png")
        return FileProvider.getUriForFile(this, "com.ita24.yumly.fileprovider", image)
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
