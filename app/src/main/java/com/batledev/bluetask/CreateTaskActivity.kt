package com.batledev.bluetask

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class CreateTaskActivity : AppCompatActivity() {

    private lateinit var editTextTitle: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var buttonAddTask: Button

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task)

        // Initialize views
        editTextTitle = findViewById(R.id.editTextTitle)
        editTextDescription = findViewById(R.id.editTextDescription)
        buttonAddTask = findViewById(R.id.buttonAddTask)

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Set click listeners
        buttonAddTask.setOnClickListener { addTask() }
    }

    private fun addTask() {
        val title = editTextTitle.text.toString().trim()
        val description = editTextDescription.text.toString().trim()
        val userId = firebaseAuth.currentUser!!.uid

        if (title.isEmpty()) {
            editTextTitle.error = "Title required"
            editTextTitle.requestFocus()
            return
        }

        val task = hashMapOf(
            "title" to title,
            "description" to description,
            "createdAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("users").document(userId).collection("tasks")
            .add(task)
            .addOnSuccessListener { documentReference ->
                println("Task added with ID: ${documentReference.id}")
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                println("Error adding task: $e")
                Toast.makeText(this, "Error adding task", Toast.LENGTH_SHORT).show()
            }
    }
}
