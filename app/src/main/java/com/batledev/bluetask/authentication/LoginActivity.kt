package com.batledev.bluetask.authentication

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.batledev.bluetask.R
import com.google.firebase.auth.FirebaseAuth

/**
 * Activity for logging in a user.
 * Provides a form for the user to enter their email and password,
 * Or to log in with Google.
 */
class LoginActivity : AppCompatActivity() {

    // UI elements
    private lateinit var loginEmail: EditText
    private lateinit var loginPassword: EditText

    // Firebase
    private lateinit var firebaseAuth: FirebaseAuth

    /**
     * This function is called when the activity is created.
     * - Get the UI elements and set up the event listeners.
     * - Initialize Firebase and Google sign in client.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Get UI elements
        loginEmail = findViewById(R.id.login_email)
        loginPassword = findViewById(R.id.login_password)
        val loginBtn = findViewById<Button>(R.id.login_btn)
        val loginGoogleBtn = findViewById<Button>(R.id.login_google_btn)
        val noAccountBtn = findViewById<TextView>(R.id.no_account)

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        val googleLauncher = registerGoogleSignInLauncher(this)

        // Set up click listeners
        loginBtn.setOnClickListener { loginUser() }
        loginGoogleBtn.setOnClickListener { googleAuth(this, googleLauncher) }
        noAccountBtn.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }
    }

    /**
     * Login the user with email and password.
     */
    private fun loginUser() {
        // Get and trim the email and password (remove leading and trailing spaces)
        val email = loginEmail.text.toString().trim()
        val password = loginPassword.text.toString().trim()

        // Check if data is valid
        if (TextUtils.isEmpty(email)) {
            loginEmail.error = "Email is required."
            return
        }
        if (TextUtils.isEmpty(password)) {
            loginPassword.error = "Password is required."
            return
        }

        // Log in the user with email and password
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    updateUI(this, firebaseAuth.currentUser)
                } else {
                    Toast.makeText(
                        baseContext,
                        "Login failed: " + task.exception?.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}