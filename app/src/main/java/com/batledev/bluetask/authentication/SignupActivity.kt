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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Activity for signing up a user.
 * Provides a form for the user to enter their email, password and confirm password,
 * Or to sign up with Google.
 */
class SignupActivity : AppCompatActivity() {

    // UI elements
    private lateinit var signupEmail: EditText
    private lateinit var signupPassword: EditText
    private lateinit var signupConfirmPassword: EditText

    // Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    /**
     * This function is called when the activity is created.
     * - Get the UI elements and set up the event listeners.
     * - Initialize Firebase and Google sign in client.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Get UI elements
        signupEmail = findViewById(R.id.signup_email)
        signupPassword = findViewById(R.id.signup_password)
        signupConfirmPassword = findViewById(R.id.signup_confirm_password)
        val signupBtn = findViewById<Button>(R.id.signup_btn)
        val signupGoogleBtn = findViewById<Button>(R.id.signup_google_btn)
        val alreadyLoginBtn = findViewById<TextView>(R.id.already_account)

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        val googleLauncher = TaskUtils.registerGoogleSignInLauncher(this)

        // Set up click listeners
        signupBtn.setOnClickListener { registerUser() }
        signupGoogleBtn.setOnClickListener { TaskUtils.googleAuth(this, googleLauncher) }
        alreadyLoginBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    /**
     * Register a user with email and password.
     * - Trim the email, password and confirm password
     * - Check if the email is empty
     * - Check if the password is empty
     * - Check if the password and confirm password match
     * - Create a user with the email and password
     * - If the user is created successfully, show a toast message and update the UI
     */
    private fun registerUser() {
        // Get and trim the email, password and confirm password
        val email = signupEmail.text.toString().trim()
        val password = signupPassword.text.toString().trim()
        val confirmPassword = signupConfirmPassword.text.toString().trim()

        // Check if data is valid
        if (TextUtils.isEmpty(email)) {
            signupEmail.error = "Email is required."
            return
        }
        if (TextUtils.isEmpty(password)) {
            signupPassword.error = "Password is required."
            return
        }
        if (password != confirmPassword) {
            signupConfirmPassword.error = "Passwords do not match."
            return
        }

        // Create a user with the email and password
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Initialize the user in Firestore
                    val currentUser = firebaseAuth.currentUser!!
                    val user = hashMapOf(
                        "email" to currentUser.email,
                        "labels" to emptyList<String>(),
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                    firestore.collection("users").document(currentUser.uid)
                        .set(user)
                        .addOnSuccessListener {
                            TaskUtils.updateUI(this, currentUser)
                        }
                } else {
                    Toast.makeText(
                        baseContext,
                        "Registration failed: " + task.exception!!.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

}