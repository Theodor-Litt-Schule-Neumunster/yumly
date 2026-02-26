package com.ita24.yumly

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase

class ManageAccountActivity : AppCompatActivity() {

    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_account)

        val newPasswordEditText = findViewById<TextInputEditText>(R.id.newPasswordEditText)
        val changePasswordButton = findViewById<Button>(R.id.changePasswordButton)
        val deleteAccountButton = findViewById<Button>(R.id.deleteAccountButton)

        // Retrieve the current user from the session
        val currentUser = SessionManager.getSession(this)
        if (currentUser == null) {
            // No user is logged in, something is wrong. Go back to login.
            navigateToLogin()
            return
        }
        username = currentUser

        val database = FirebaseDatabase.getInstance("https://yumly-874a5-default-rtdb.europe-west1.firebasedatabase.app/")
        val userRef = database.getReference("users").child(username)

        changePasswordButton.setOnClickListener {
            val newPassword = newPasswordEditText.text.toString().trim()
            if (newPassword.length < 6) {
                val text = getString(R.string.password_length_error_toast)
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userRef.child("password").setValue(newPassword).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val text = getString(R.string.password_change_success_toast)
                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
                } else {
                    val text = getString(R.string.password_change_error_toast)
                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
                }
            }
        }
        val title = getString(R.string.delete_account_dialog_title)
        val message = getString(R.string.delete_account_dialog_message)
        val deleteAction = getString(R.string.delete_action)
        val successMessage = getString(R.string.account_deleted_toast)
        val errorMessage = getString(R.string.account_deletion_error_toast)
        val cancelAction = getString(R.string.cancel_action)



        deleteAccountButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(deleteAction) { _, _ ->
                    userRef.removeValue().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            SessionManager.clearSession(this)
                            Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show()
                            navigateToLogin()
                        } else {
                            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton(cancelAction, null)
                .show()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
