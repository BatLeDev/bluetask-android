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

        val loginButton = findViewById<Button>(R.id.login_btn)
        val signUpButton = findViewById<Button>(R.id.signup_btn)
        val doTasksTextView = findViewById<TextView>(R.id.do_tasks_text)

        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        signUpButton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        // Set up the spannable text for "Do your tasks quickly and easy"
        val text = "Do your tasks quickly and easy"
        val spannable = SpannableString(text)

        val typedValue = TypedValue()
        theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
        val primaryColor = typedValue.data

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

        doTasksTextView.text = spannable
    }
}
