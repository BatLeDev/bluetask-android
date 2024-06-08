package com.batledev.bluetask.authentication

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.batledev.bluetask.MainActivity
import com.batledev.bluetask.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

/**
 * Utilities functions for authentication.
 */
object TaskUtils {

    /**
     * Authenticate with Google.
     * @param activity The activity that calls this function.
     * @param googleLauncher The activity result launcher for Google sign in.
     */
    fun googleAuth(activity: AppCompatActivity, googleLauncher: ActivityResultLauncher<Intent>) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(activity, gso)
        val signInIntent = googleSignInClient.signInIntent
        googleLauncher.launch(signInIntent)
    }

    /**
     * Register the activity result launcher for Google sign in.
     * @param activity The activity that calls this function.
     * @return The activity result launcher for Google sign in.
     */
    fun registerGoogleSignInLauncher(activity: AppCompatActivity): ActivityResultLauncher<Intent> {
        return activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(activity, FirebaseAuth.getInstance(), account.idToken)
                } catch (e: ApiException) {
                    Toast.makeText(
                        activity,
                        "Google login/sign up failed: " + e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    /**
     * Authenticate with Google.
     * @param activity The activity that calls this function.
     * @param firebaseAuth The Firebase authentication instance.
     * @param idToken The Google ID token.
     */
    private fun firebaseAuthWithGoogle(
        activity: AppCompatActivity,
        firebaseAuth: FirebaseAuth,
        idToken: String?
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        activity,
                        "Google login/sign up successful.",
                        Toast.LENGTH_SHORT
                    ).show()
                    val user = firebaseAuth.currentUser
                    updateUI(activity, user)
                } else {
                    Toast.makeText(
                        activity,
                        "Google login/sign up failed: " + task.exception!!.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    /**
     * Update the user interface.
     * @param activity The activity that calls this function.
     * @param user The Firebase user.
     */
    fun updateUI(activity: AppCompatActivity, user: FirebaseUser?) {
        if (user != null) {
            activity.startActivity(Intent(activity, MainActivity::class.java))
            activity.finishAffinity()
        }
    }
}