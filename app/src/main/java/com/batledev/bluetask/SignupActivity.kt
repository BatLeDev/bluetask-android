package com.batledev.bluetask

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

private const val RC_SIGN_UP = 9001

class SignupActivity : AppCompatActivity() {

    // UI elements
    private lateinit var signupEmail: EditText
    private lateinit var signupPassword: EditText
    private lateinit var signupConfirmPassword: EditText

    // Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    // Activity result launcher for Google sign in
    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                super.onActivityResult(RC_SIGN_UP, result.resultCode, data)
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account.idToken)
                } catch (e: ApiException) {
                    Toast.makeText(this, "Google sign up failed: " + e.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    /**
     * Called when the activity is starting.
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
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set up event listeners
        signupBtn.setOnClickListener { registerUser() }
        signupGoogleBtn.setOnClickListener { signInWithGoogle() }
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
                    Toast.makeText(
                        baseContext,
                        "User registered successfully.",
                        Toast.LENGTH_SHORT
                    ).show()
                    val user = firebaseAuth.currentUser
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

    /**
     * Start the Google sign in flow
     */
    private fun signInWithGoogle() {
        val signInIntent: Intent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    /**
     * Sing in in with Google to Firebase, using the Google sign in token
     */
    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Google sign up successful.",
                        Toast.LENGTH_SHORT
                    ).show()
                    val user = firebaseAuth.currentUser
                    updateUI(user)
                } else {
                    Toast.makeText(
                        this,
                        "Google sign up failed: " + task.exception!!.message,
                        Toast.LENGTH_SHORT
                    ).show()
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
