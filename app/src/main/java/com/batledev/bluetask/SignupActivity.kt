package com.batledev.bluetask

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignupActivity : AppCompatActivity() {
    // View elements
    private var signupEmail: EditText? = null
    private var signupPassword: EditText? = null
    private var signupConfirmPassword: EditText? = null

    // Firebase authentication
    private var firebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set up the view
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Get view elements
        val signupBtn = findViewById<Button>(R.id.signup_btn)
        val signupGoogleBtn = findViewById<Button>(R.id.signup_google_btn)
        val alreadyLoginBtn = findViewById<TextView>(R.id.already_login)

        signupEmail = findViewById(R.id.signup_email)
        signupPassword = findViewById(R.id.signup_password)
        signupConfirmPassword = findViewById(R.id.signup_confirm_password)

        // Set up Firebase authentication
        firebaseAuth = FirebaseAuth.getInstance()

        // Set up listeners on buttons
        signupBtn.setOnClickListener { registerUser() }
        signupGoogleBtn.setOnClickListener {  } // TODO: Implement Google sign in
        alreadyLoginBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Register a user
     * - Trim the email, password and confirm password
     * - Check if the email is empty
     * - Check if the password is empty
     * - Check if the password and confirm password match
     */
    private fun registerUser() {
        val email = signupEmail!!.text.toString().trim()
        val password = signupPassword!!.text.toString().trim()
        val confirmPassword = signupConfirmPassword!!.text.toString().trim()

        if (TextUtils.isEmpty(email)) {
            signupEmail!!.error = "Email is required."
            return
        }
        if (TextUtils.isEmpty(password)) {
            signupPassword!!.error = "Password is required."
            return
        }
        if (password != confirmPassword) {
            signupConfirmPassword!!.error = "Passwords do not match."
            return
        }

        firebaseAuth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        baseContext,
                        "User registered successfully.",
                        Toast.LENGTH_SHORT
                    ).show()
                    val user = firebaseAuth!!.currentUser
                    updateUI(user)
                } else {
                    Toast.makeText(
                        baseContext,
                        "Registration failed: " + task.exception!!.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}