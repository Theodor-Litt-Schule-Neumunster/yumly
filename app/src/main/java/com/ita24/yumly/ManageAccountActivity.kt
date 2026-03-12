package com.ita24.yumly

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.EmailAuthProvider

class ManageAccountActivity : AppCompatActivity() {

    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_account)

        val newPasswordEditText = findViewById<TextInputEditText>(R.id.newPasswordEditText)
        val changePasswordButton = findViewById<Button>(R.id.changePasswordButton)
        val deleteAccountButton = findViewById<Button>(R.id.deleteAccountButton)
        val recipeWebsiteButton = findViewById<Button>(R.id.recipe_website)

        val currentUser = SessionManager.getSession(this)
        if (currentUser == null) {
            navigateToLogin()
            return
        }
        username = currentUser

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {
            val text = getString(R.string.no_user_logged_in)
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
            navigateToLogin()
            return
        }

        val database = FirebaseDatabase.getInstance(
            "https://yumly-874a5-default-rtdb.europe-west1.firebasedatabase.app/"
        )
        val userRef = database.getReference("users").child(username)

        changePasswordButton.setOnClickListener {
            val newPassword = newPasswordEditText.text.toString().trim()
            if (newPassword.length < 6) {
                Toast.makeText(
                    this,
                    getString(R.string.password_length_error_toast),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            userRef.child("password").get().addOnSuccessListener { snapshot ->
                val oldPassword = snapshot.getValue(String::class.java)
                if (oldPassword == newPassword) {
                    val text = getString(R.string.password_must_be_different_toast)
                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val credential = EmailAuthProvider.getCredential(firebaseUser.email!!, oldPassword!!)
                firebaseUser.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {

                        firebaseUser.updatePassword(newPassword).addOnCompleteListener { authTask ->
                            if (authTask.isSuccessful) {
                                userRef.child("password").setValue(newPassword)
                                    .addOnCompleteListener { dbTask ->
                                        if (dbTask.isSuccessful) {
                                            Toast.makeText(
                                                this,
                                                getString(R.string.password_change_success_toast),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                this,
                                                getString(R.string.password_change_error_toast),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            } else {
                                val text = getString(R.string.error_changing_auth_password_toast, authTask.exception?.message)
                                Toast.makeText(
                                    this,
                                    text,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                    } else {
                        val text = getString(R.string.reauth_failed_toast, reauthTask.exception?.message)
                        Toast.makeText(
                            this,
                            text,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            }.addOnFailureListener {
                val text = getString(R.string.error_retrieving_old_password_toast)
                Toast.makeText(
                    this,
                    text,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        recipeWebsiteButton.setOnClickListener {
            userdataprefrecipes.init(this)
            val recipes = userdataprefrecipes.getAllRecipes()
            val text = getString(R.string.select_recipe_to_open_title)
            if (recipes.isNotEmpty()) {
                val recipeOptions = recipes.map { it.name }.toTypedArray()
                AlertDialog.Builder(this)
                    .setTitle(text)
                    .setItems(recipeOptions) { _, which ->
                        val selectedRecipe = recipes[which]
                        val source = selectedRecipe.recipeSource
                        if (source != null) {
                            RecipeWebsite.openSource(this, source)
                        } else {
                            val text = getString(R.string.error_open_file)
                            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
                        }
                    }
                    .show()
            } else {
                val text = getString(R.string.no_recipes_found)
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
            }
        }

        val title = getString(R.string.delete_account_dialog_title)
        val message = getString(R.string.delete_account_dialog_message)
        val deleteAction = getString(R.string.delete_action)
        val successMessage = getString(R.string.account_deleted_toast)
        val cancelAction = getString(R.string.cancel_action)

        deleteAccountButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(deleteAction) { _, _ ->

                    userRef.child("password").get().addOnSuccessListener { snapshot ->
                        val oldPassword = snapshot.getValue(String::class.java)
                        if (oldPassword.isNullOrBlank()) {
                            val text = getString(R.string.error_old_password_not_found_toast)
                            Toast.makeText(this, "Fehler: altes Passwort nicht gefunden", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }

                        val credential = EmailAuthProvider.getCredential(firebaseUser.email!!, oldPassword)
                        firebaseUser.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                            if (reauthTask.isSuccessful) {

                                firebaseUser.delete().addOnCompleteListener { deleteTask ->
                                    if (deleteTask.isSuccessful) {

                                        userRef.removeValue().addOnCompleteListener { dbTask ->
                                            if (dbTask.isSuccessful) {
                                                SessionManager.clearSession(this)
                                                Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show()
                                                navigateToLogin()
                                            } else {
                                                Toast.makeText(this, "Fehler beim Löschen im DB: ${dbTask.exception?.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }

                                    } else {
                                        Toast.makeText(this, "Fehler beim Löschen des Auth-Accounts: ${deleteTask.exception?.message}", Toast.LENGTH_LONG).show()
                                    }
                                }

                            } else {
                                Toast.makeText(this, "Reauthentifizierung fehlgeschlagen: ${reauthTask.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }

                    }.addOnFailureListener {
                        Toast.makeText(this, "Fehler beim Abrufen des alten Passworts", Toast.LENGTH_SHORT).show()
                    }

                }
                .setNegativeButton(cancelAction, null)
                .show()
        }
    }

    private fun navigateToLogin() {
        val intent = android.content.Intent(this, LoginActivity::class.java)
        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
