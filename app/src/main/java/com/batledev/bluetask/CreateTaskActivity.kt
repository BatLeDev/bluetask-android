package com.batledev.bluetask

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.security.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateTaskActivity : AppCompatActivity() {

    private lateinit var editTextTitle: EditText
    private lateinit var editTextDescription: EditText

    private lateinit var buttonStartDate: Button
    private lateinit var buttonEndDate: Button
    private lateinit var spinnerPriority: Spinner
    private lateinit var buttonAddTask: Button

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task)

        // Initialize views
        editTextTitle = findViewById(R.id.editTextTitle)
        editTextDescription = findViewById(R.id.editTextDescription)
        buttonStartDate = findViewById(R.id.buttonStartDate)
        buttonEndDate = findViewById(R.id.buttonEndDate)
        spinnerPriority = findViewById(R.id.spinnerPriority)
        buttonAddTask = findViewById(R.id.buttonAddTask)

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Set up priority spinner
        val priorities = arrayOf("Clear", "Low", "Medium", "High")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPriority.adapter = adapter

        // Set click listeners
        buttonStartDate.setOnClickListener { showDatePicker(buttonStartDate) }
        buttonEndDate.setOnClickListener { showDatePicker(buttonEndDate) }
        buttonAddTask.setOnClickListener { addTask() }
    }

    private fun showDatePicker(button: Button) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val date = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                button.text = date
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun addTask() {
    }
}
