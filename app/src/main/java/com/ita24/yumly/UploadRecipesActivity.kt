package com.ita24.yumly

import android.util.Log
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
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
    private var selectedFileUri: Uri? = null
    private lateinit var recipeImageView: ImageView
    private lateinit var selectedFileName: TextView
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var filePickerLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_recipes)

        recipeImageView = findViewById(R.id.recipeImageView)
        selectedFileName = findViewById(R.id.selectedFileName)

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

        filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedFileUri = it
                selectedFileName.text = getFileName(it) ?: "Datei ausgewählt"
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

        val selectFileButton = findViewById<Button>(R.id.selectFileButton)
        selectFileButton.setOnClickListener {
            filePickerLauncher.launch("*/*")
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
        val recipeUrlEditText = findViewById<TextInputEditText>(R.id.recipeUrlEditText)

        saveButton.setOnClickListener {
            var zeit = timefield.text.toString().trim().toIntOrNull() ?: 0
            val name = nameEditText.text.toString().trim()
            val currentUri = imageUri

            if (name.isEmpty() || currentUri == null) {
                Toast.makeText(this, getString(R.string.upload_error_name_image_toast), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (zeit == 0) {
                Toast.makeText(this, getString(R.string.upload_error_duration_toast), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val permanentImgUrl = saveFileToInternalStorage(currentUri, "recipe_img_${System.currentTimeMillis()}.jpg")
            if (permanentImgUrl == null) {
                Toast.makeText(this, getString(R.string.upload_error_save_image_toast), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val urlSource = recipeUrlEditText.text.toString().trim()
            var recipeSource: String? = null
            
            if (urlSource.isNotEmpty()) {
                recipeSource = urlSource
            } else if (selectedFileUri != null) {
                val originalName = getFileName(selectedFileUri!!) ?: "document"
                val fileName = "recipe_doc_${System.currentTimeMillis()}_$originalName"
                recipeSource = saveFileToInternalStorage(selectedFileUri!!, fileName)
            }

            val rezept = localSavedRecipe(
                name,
                zeit,
                permanentImgUrl,
                ingredients,
                emptyList(),
                attributeAdapter.getAttributes(),
                1100,
                recipeSource
            )
            userdataprefrecipes.saveRecipe(rezept)

            Toast.makeText(this, getString(R.string.recipe_saved_toast, name), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun saveFileToInternalStorage(uri: Uri, fileName: String): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val file = File(filesDir, fileName)
            file.outputStream().use { outputStream ->
                inputStream.use { it.copyTo(outputStream) }
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e("UploadRecipes", "Fehler beim Speichern", e)
            null
        }
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor.use {
                if (it != null && it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) result = it.getString(index)
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) result = result?.substring(cut + 1)
        }
        return result
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
