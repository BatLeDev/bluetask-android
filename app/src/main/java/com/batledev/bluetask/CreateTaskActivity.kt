package com.batledev.bluetask

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CreateTaskActivity : AppCompatActivity() {

    private lateinit var editTextTitle: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var buttonColorPicker: Button
    private lateinit var buttonAddTask: Button
    private lateinit var buttonStartDatePicker: Button
    private lateinit var buttonEndDatePicker: Button
    private lateinit var spinnerPriority: Spinner

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var taskColor: String? = null
    private var startDate: Date? = null
    private var endDate: Date? = null
    private var priority: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task)

        // Initialize views
        editTextTitle = findViewById(R.id.editTextTitle)
        editTextDescription = findViewById(R.id.editTextDescription)
        buttonColorPicker = findViewById(R.id.buttonColorPicker)
        buttonAddTask = findViewById(R.id.buttonAddTask)
        buttonStartDatePicker = findViewById(R.id.buttonStartDatePicker)
        buttonEndDatePicker = findViewById(R.id.buttonEndDatePicker)
        spinnerPriority = findViewById(R.id.spinnerPriority)

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Set click listeners
        buttonColorPicker.setOnClickListener { showColorPicker() }
        buttonStartDatePicker.setOnClickListener { showStartDatePicker() }
        buttonEndDatePicker.setOnClickListener { showEndDatePicker() }
        buttonAddTask.setOnClickListener { addTask() }

        // Set up priority spinner
        val priorities = arrayOf("No Priority", "High", "Medium", "Low")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPriority.adapter = adapter
        spinnerPriority.setSelection(0)
        spinnerPriority.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                priority = when (position) {
                    1 -> 2
                    2 -> 1
                    3 -> 0
                    else -> -1
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                priority = -1
            }
        }
    }

    private fun showStartDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                calendar.set(year, monthOfYear, dayOfMonth)
                startDate = calendar.time
                buttonStartDatePicker.text =
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(startDate!!)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        if (endDate != null) {
            datePickerDialog.datePicker.maxDate = endDate!!.time
        }
        datePickerDialog.show()
    }

    private fun showEndDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                calendar.set(year, monthOfYear, dayOfMonth)
                endDate = calendar.time
                buttonEndDatePicker.text =
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(endDate!!)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        if (startDate != null) {
            datePickerDialog.datePicker.minDate = startDate!!.time
        }
        datePickerDialog.show()
    }

    private fun showColorPicker() {
        ColorPickerDialog.Builder(this)
            .setTitle("Pick a color")
            .setPreferenceName("MyColorPickerDialog")
            .setPositiveButton("Confirm", ColorEnvelopeListener { envelope, _ ->
                val selectedColor = envelope.color
                taskColor = "#" + envelope.hexCode.substring(2, 8)
                buttonColorPicker.setBackgroundColor(selectedColor)
            })
            .setNegativeButton("Clear") { dialogInterface, _ ->
                taskColor = null
                buttonColorPicker.setBackgroundResource(android.R.drawable.btn_default)
                dialogInterface.dismiss()
            }
            .attachAlphaSlideBar(false)
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
            "startDate" to startDate,
            "endDate" to endDate,
            "labels" to emptyList<String>(),
            "lines" to emptyList<String>(),
            "linesChecked" to emptyList<String>(),
            "priority" to priority,
            "state" to -1,
            "status" to "active",
            "createAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("users").document(userId).collection("tasks")
            .add(task)
            .addOnSuccessListener { documentReference ->
                println("Task added with ID: ${documentReference.id}")
                finish()
            }
            .addOnFailureListener { e ->
                println("Error adding task: $e")
                Toast.makeText(this, "Error adding task", Toast.LENGTH_SHORT).show()
            }
    }
}
