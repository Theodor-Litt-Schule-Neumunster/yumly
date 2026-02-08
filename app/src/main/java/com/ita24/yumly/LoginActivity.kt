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
        const val EXTRA_USERNAME = "LOGGED_IN_USERNAME"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val loggedInUser = SessionManager.getSession(this)
        if (loggedInUser != null) {
            navigateToWelcomeActivity(loggedInUser)
            return
        }

        setContentView(R.layout.activity_login)

        val usernameEditText = findViewById<TextInputEditText>(R.id.usernameEditText)
        val passwordEditText = findViewById<TextInputEditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val skipButton = findViewById<Button>(R.id.skipButton)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Bitte Nutzername und Passwort eingeben.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val database = FirebaseDatabase.getInstance("https://yumly-874a5-default-rtdb.europe-west1.firebasedatabase.app/")
            val usersRef = database.getReference("users")
            val userQuery = usersRef.child(username)

            userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val storedPassword = snapshot.child("password").getValue(String::class.java)
                        if (storedPassword == password) {
                            SessionManager.saveSession(this@LoginActivity, username)
                            Toast.makeText(this@LoginActivity, "Anmeldung erfolgreich.", Toast.LENGTH_SHORT).show()
                            navigateToWelcomeActivity(username)
                        } else {
                            Toast.makeText(this@LoginActivity, "Nutzername vergeben; Falsches Passwort.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val newUser = mapOf("password" to password)
                        userQuery.setValue(newUser).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                SessionManager.saveSession(this@LoginActivity, username)
                                Toast.makeText(this@LoginActivity, "Registrierung erfolgreich.", Toast.LENGTH_SHORT).show()
                                navigateToWelcomeActivity(username)
                            } else {
                                Toast.makeText(this@LoginActivity, "Registrierung fehlgeschlagen.", Toast.LENGTH_SHORT).show()
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
            navigateToMainApp(null)
        }
    }

    private fun navigateToMainApp(username: String?) {
        val intent = Intent(this, MainActivity::class.java)
        if (username != null) {
            intent.putExtra(EXTRA_USERNAME, username)
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToWelcomeActivity(username: String) {
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
