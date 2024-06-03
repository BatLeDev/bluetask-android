package com.batledev.bluetask

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Check if user is not logged in
        if (auth.currentUser == null) {
            startActivity(Intent(this, UnloggedActivity::class.java))
            finish()
        }

        // The user is logged in
        setContentView(R.layout.activity_main)


    }
}
