package com.ita24.yumly

import android.util.Log
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
            val cameraOption = getString(R.string.camera_option)
            val galleryOption = getString(R.string.gallery_option)
            val options = arrayOf(cameraOption, galleryOption)
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.select_image_dialog_title))
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
        val timefield = findViewById<TextInputEditText>(R.id.preparationTimeEditText)

        userdataprefrecipes.init(this)

        saveButton.setOnClickListener {
            var zeit = timefield.text.toString().trim().toIntOrNull()
            if (zeit == null) {
                Log.e("test", "zeit ist null")
                zeit = 0;
            }

            val attributlist = attributeAdapter.getAttributes()

            val imgurl = imageUri.toString()

            val allergies = emptyList<String>()
            val name = nameEditText.text.toString().trim()

            val currentUri = imageUri
            if (name.isEmpty() || currentUri == null) {
                val message = getString(R.string.upload_error_name_image_toast)
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (zeit == 0) {
                val text = getString(R.string.upload_error_duration_toast)
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val permanentImgUrl = saveImageToInternalStorage(currentUri)
            if (permanentImgUrl == null) {
                Toast.makeText(this, "Fehler beim Speichern des Bildes", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val rezept = localSavedRecipe(
                name,
                zeit,
                permanentImgUrl,
                ingredients,
                allergies,
                attributlist,
                1100
            )
            userdataprefrecipes.saveRecipe(rezept)

            val text = getString(R.string.recipe_saved_toast, name)
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val fileName = "recipe_image_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)

            file.outputStream().use { outputStream ->
                inputStream.use { it.copyTo(outputStream) }
            }

            Uri.fromFile(file).toString()
        } catch (e: Exception) {
            Log.e("UploadRecipes", "Fehler beim Kopieren", e)
            null
        }
    }

    private fun createImageUri(): Uri {
        val image = File(filesDir, "temp_camera_photo.png")
        return FileProvider.getUriForFile(this, "com.ita24.yumly.fileprovider", image)
    }

    private fun createAttributeList(): List<AttributeItem> {
        return listOf(
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
