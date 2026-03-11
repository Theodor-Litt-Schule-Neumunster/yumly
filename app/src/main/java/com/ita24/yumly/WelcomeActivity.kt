package com.ita24.yumly

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userdatapref.init(applicationContext)
        userdataprefrecipes.init(this)
        Filter.initFilters(this)
        setContentView(R.layout.activity_welcome)

        val welcomeTextView = findViewById<TextView>(R.id.welcomeTextView)
        val newSessionButton = findViewById<Button>(R.id.newSessionButton)
        val uploadButton = findViewById<Button>(R.id.uploadButton)
        val manageAccountButton = findViewById<Button>(R.id.manageAccountButton)
        val logoutButton = findViewById<Button>(R.id.logoutButton)

        val username = SessionManager.getSession(this)

        if (username != null) {
            welcomeTextView.text = getString(R.string.welcome_greeting, username)
            manageAccountButton.text = getString(R.string.manage_account_button_text)
            logoutButton.text = getString(R.string.logout_button_text)
        } else {
            welcomeTextView.text = getString(R.string.welcome_greeting_guest)
            manageAccountButton.text = getString(R.string.welcome_register_button_text)
            logoutButton.text = getString(R.string.welcome_login_button_text)
        }

        newSessionButton.setOnClickListener {
            val intent = Intent(this, PreloadActivity::class.java)
            intent.putExtra(LoginActivity.EXTRA_USERNAME, username)
            startActivity(intent)
        }

        manageAccountButton.setOnClickListener {
            val intent = Intent(this, ManageAccountActivity::class.java)
            startActivity(intent)
        }

        uploadButton.setOnClickListener {
            val intent = Intent(this, UploadRecipesActivity::class.java)
            startActivity(intent)
        }

        logoutButton.setOnClickListener {
            SessionManager.clearSession(this)
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}