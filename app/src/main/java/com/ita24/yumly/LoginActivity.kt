package com.ita24.yumly

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    companion object {
        // Key to pass the username to the MainActivity
        const val EXTRA_USERNAME = "LOGGED_IN_USERNAME"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val usernameEditText = findViewById<TextInputEditText>(R.id.usernameEditText)
        val passwordEditText = findViewById<TextInputEditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val skipButton = findViewById<Button>(R.id.skipButton)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Bitte Nutzername und Passwort eingeben", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Use the specific database URL provided by the user
            val database = FirebaseDatabase.getInstance("https://yumly-874a5-default-rtdb.europe-west1.firebasedatabase.app/")
            val usersRef = database.getReference("users")
            val userQuery = usersRef.child(username)

            userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // User exists, check password
                        val storedPassword = snapshot.child("password").getValue(String::class.java)
                        if (storedPassword == password) {
                            Toast.makeText(this@LoginActivity, "Anmeldung erfolgreich!", Toast.LENGTH_SHORT).show()
                            navigateToMainApp(username) // Pass username to main activity
                        } else {
                            Toast.makeText(this@LoginActivity, "Falsches Passwort", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // User does not exist, create new account
                        val newUser = mapOf("password" to password)
                        userQuery.setValue(newUser).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this@LoginActivity, "Account erstellt!", Toast.LENGTH_SHORT).show()
                                navigateToMainApp(username) // Pass username to main activity
                            } else {
                                Toast.makeText(this@LoginActivity, "Fehler bei der Accounterstellung", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@LoginActivity, "Datenbankfehler: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        skipButton.setOnClickListener {
            // User skips login, navigate without a username
            navigateToMainApp(null)
        }
    }

    private fun navigateToMainApp(username: String?) {
        val intent = Intent(this, MainActivity::class.java)
        if (username != null) {
            intent.putExtra(EXTRA_USERNAME, username)
        }
        startActivity(intent)
        finish() // Finish LoginActivity so the user can't navigate back to it
    }
}
