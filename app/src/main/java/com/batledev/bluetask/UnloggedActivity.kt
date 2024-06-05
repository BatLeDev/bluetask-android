package com.batledev.bluetask

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class UnloggedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unlogged)

        // Get UI elements
        val loginButton = findViewById<Button>(R.id.login_btn)
        val signUpButton = findViewById<Button>(R.id.signup_btn)
        val doTasksTextView = findViewById<TextView>(R.id.do_tasks_text)

        // Set up the click listeners for the buttons
        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        signUpButton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        // ----------------- Set up the spannable text -----------------
        val text = "Do your tasks quickly and easy"
        val spannable = SpannableString(text)

        // Get the primary color of the app
        val typedValue = TypedValue()
        theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
        val primaryColor = typedValue.data

        // Set the color of the words "quickly" and "easy" to the primary color
        spannable.setSpan(
            ForegroundColorSpan(primaryColor),
            text.indexOf("quickly"),
            text.indexOf("quickly") + "quickly".length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            ForegroundColorSpan(primaryColor),
            text.indexOf("easy"),
            text.indexOf("easy") + "easy".length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Set the spannable text to the TextView
        doTasksTextView.text = spannable

        // -------------------------------------------------------------
    }
}
