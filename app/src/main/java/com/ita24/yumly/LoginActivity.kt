package com.ita24.yumly

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_USERNAME = "LOGGED_IN_USERNAME"
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance(
            "https://yumly-874a5-default-rtdb.europe-west1.firebasedatabase.app/"
        )

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
                Toast.makeText(
                    this,
                    getString(R.string.login_empty_credentials_toast),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val usersRef = database.getReference("users")
            val userRef = usersRef.child(username)
            val email = "$username@yumly.app"

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val storedPassword = snapshot.child("password").getValue(String::class.java)

                        if (storedPassword == password) {
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { loginTask ->
                                    if (!loginTask.isSuccessful) {
                                        // Auth Account existiert noch nicht → erstellen
                                        auth.createUserWithEmailAndPassword(email, password)
                                            .addOnCompleteListener { createTask ->
                                                val uid = auth.currentUser?.uid
                                                if (uid != null) {
                                                    userRef.child("uid").setValue(uid)
                                                }
                                            }
                                    } else {
                                        val uid = auth.currentUser?.uid
                                        if (uid != null && !snapshot.hasChild("uid")) {
                                            userRef.child("uid").setValue(uid)
                                        }
                                    }

                                    SessionManager.saveSession(this@LoginActivity, username)
                                    Toast.makeText(
                                        this@LoginActivity,
                                        getString(R.string.login_successful_toast),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navigateToWelcomeActivity(username)
                                }
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                getString(R.string.login_wrong_password_toast),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { createTask ->
                                if (createTask.isSuccessful) {
                                    val uid = auth.currentUser?.uid
                                    val newUser = mapOf(
                                        "password" to password,
                                        "uid" to uid
                                    )
                                    userRef.setValue(newUser).addOnCompleteListener { dbTask ->
                                        if (dbTask.isSuccessful) {
                                            SessionManager.saveSession(this@LoginActivity, username)
                                            Toast.makeText(
                                                this@LoginActivity,
                                                getString(R.string.login_account_created_toast),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            navigateToWelcomeActivity(username)
                                        } else {
                                            Toast.makeText(
                                                this@LoginActivity,
                                                getString(R.string.login_account_creation_error_toast),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Auth Fehler: ${createTask.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.login_database_error_toast, error.message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

        skipButton.setOnClickListener {
            navigateToMainApp(null)
        }
    }

    private fun navigateToMainApp(username: String?) {
        val intent = Intent(this, PreloadActivity::class.java)
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