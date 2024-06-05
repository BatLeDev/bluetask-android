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

private const val RC_LOGIN = 9002

class LoginActivity : AppCompatActivity() {

    // UI elements
    private lateinit var loginEmail: EditText
    private lateinit var loginPassword: EditText

    // Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    // Activity result launcher for Google sign in
    private val loginLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                super.onActivityResult(RC_LOGIN, result.resultCode, data)
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account.idToken)
                } catch (e: ApiException) {
                    Toast.makeText(this, "Google login failed: " + e.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    /**
     * Called when the activity is starting.
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
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set up event listeners
        loginBtn.setOnClickListener { loginUser() }
        loginGoogleBtn.setOnClickListener { loginWithGoogle() }
        noAccountBtn.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Login a user
     */
    private fun loginUser() {
        // Get and trim the email and password
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
                    val user = firebaseAuth.currentUser
                    updateUI(user)
                } else {
                    Toast.makeText(
                        baseContext,
                        "Login failed: " + task.exception?.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    /**
     * Start the Google sign in flow
     */
    private fun loginWithGoogle() {
        val signInIntent: Intent = googleSignInClient.signInIntent
        loginLauncher.launch(signInIntent)
    }

    /**
     * Login with Google to Firebase, using the Google sign in token
     */
    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Google login successful.",
                        Toast.LENGTH_SHORT
                    ).show()
                    val user = firebaseAuth.currentUser
                    updateUI(user)
                } else {
                    Toast.makeText(
                        this,
                        "Google login failed: " + task.exception!!.message,
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
