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
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

class CreateTaskActivity : AppCompatActivity() {

    private lateinit var editTextTitle: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var buttonColorPicker: Button
    private lateinit var buttonAddTask: Button

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var taskColor: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task)

        // Initialize views
        editTextTitle = findViewById(R.id.editTextTitle)
        editTextDescription = findViewById(R.id.editTextDescription)
        buttonColorPicker = findViewById(R.id.buttonColorPicker)
        buttonAddTask = findViewById(R.id.buttonAddTask)

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Set click listeners
        buttonColorPicker.setOnClickListener { showColorPicker() }
        buttonAddTask.setOnClickListener { addTask() }
    }

    private fun showColorPicker() {
        ColorPickerDialog.Builder(this)
            .setTitle("Pick a color")
            .setPreferenceName("MyColorPickerDialog")
            .setPositiveButton("Confirm", ColorEnvelopeListener { envelope, _ ->
                val selectedColor = envelope.color
                taskColor = "#" + envelope.hexCode
                buttonColorPicker.setBackgroundColor(selectedColor)
            })
            .setNegativeButton("Clear") { dialogInterface, _ ->
                taskColor = null
                buttonColorPicker.setBackgroundResource(android.R.drawable.btn_default)
                dialogInterface.dismiss()
            }
            .attachAlphaSlideBar(false) // Disable the alpha slider
            .attachBrightnessSlideBar(true)
            .setBottomSpace(12)
            .show()
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
            "color" to taskColor,
            "endDate" to null,
            "labels" to emptyList<String>(),
            "lines" to emptyList<String>(),
            "linesChecked" to emptyList<String>(),
            "priority" to -1,
            "startDate" to null,
            "state" to -1,
            "status" to "active",
            "createAt" to FieldValue.serverTimestamp()
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
