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
                Toast.makeText(this, "Das Passwort muss mindestens 6 Zeichen lang sein", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userRef.child("password").setValue(newPassword).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Passwort erfolgreich geändert", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Fehler beim Ändern des Passworts", Toast.LENGTH_SHORT).show()
                }
            }
        }

        deleteAccountButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Konto löschen")
                .setMessage("Bist du sicher, dass du dein Konto endgültig löschen möchtest? Diese Aktion kann nicht rückgängig gemacht werden.")
                .setPositiveButton("Löschen") { _, _ ->
                    userRef.removeValue().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            SessionManager.clearSession(this)
                            Toast.makeText(this, "Konto gelöscht", Toast.LENGTH_SHORT).show()
                            navigateToLogin()
                        } else {
                            Toast.makeText(this, "Fehler beim Löschen des Kontos", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Abbrechen", null)
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
