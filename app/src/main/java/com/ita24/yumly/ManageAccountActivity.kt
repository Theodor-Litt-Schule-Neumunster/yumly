package com.ita24.yumly

import android.content.Intent
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

        val currentUser = SessionManager.getSession(this)
        if (currentUser == null) {
            navigateToLogin()
            return
        }
        username = currentUser

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {
            Toast.makeText(this, "Kein User angemeldet!", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this, "Neues Passwort muss anders sein!", Toast.LENGTH_SHORT).show()
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
                                Toast.makeText(
                                    this,
                                    "Fehler beim Ändern des Auth-Passworts: ${authTask.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                    } else {
                        Toast.makeText(
                            this,
                            "Reauth fehlgeschlagen: ${reauthTask.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            }.addOnFailureListener {
                Toast.makeText(
                    this,
                    "Fehler beim Abrufen des alten Passworts",
                    Toast.LENGTH_SHORT
                ).show()
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

                    userRef.child("password").get().addOnSuccessListener { snapshot ->
                        val oldPassword = snapshot.getValue(String::class.java)
                        if (oldPassword.isNullOrBlank()) {
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
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}