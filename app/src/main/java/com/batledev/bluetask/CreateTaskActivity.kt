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

/**
 * Activity to create a task.
 * Launches when the user clicks the "Add Task" button in the main activity.
 * The user can input the task title, description, color, start date, end date, and priority.
 */
class CreateTaskActivity : AppCompatActivity() {

    // UI elements
    private lateinit var editTextTitle: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var buttonColorPicker: Button
    private lateinit var buttonAddTask: Button
    private lateinit var buttonStartDatePicker: Button
    private lateinit var buttonEndDatePicker: Button
    private lateinit var spinnerPriority: Spinner

    // Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // Task data (for complex input)
    private var taskColor: String? = null
    private var startDate: Date? = null
    private var endDate: Date? = null
    private var priority: Int = -1

    /**
     * This function is called when the activity is created.
     * - Get the UI elements and set up the event listeners.
     * - Initialize Firebase.
     * - Set up the priority spinner (With translated values).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task)

        // Get UI elements
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

        // Set up click listeners
        buttonColorPicker.setOnClickListener { showColorPicker() }
        buttonStartDatePicker.setOnClickListener { showStartDatePicker() }
        buttonEndDatePicker.setOnClickListener { showEndDatePicker() }
        buttonAddTask.setOnClickListener { addTask() }

        // Set up priority spinner
        val priorities = resources.getStringArray(R.array.priorities)
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

    /**
     * Show the start date picker dialog.
     */
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

    /**
     * Show the end date picker dialog.
     */
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

    /**
     * Show the color picker dialog.
     */
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

    /**
     * Assemble all the task data and add it to the Firestore database.
     * - More data are included to respect the database schema (Task.kt).
     * - The webapp has more features, so the task data is more complex.
     * - On the mobile app, we require a task title that not required on the webapp.
     */
    private fun addTask() {
        // Get the user ID (to store the task in task collection of the user's document)
        val userId = firebaseAuth.currentUser!!.uid

        // Get input data
        val title = editTextTitle.text.toString().trim()
        val description = editTextDescription.text.toString().trim()

        // Validate input data
        if (title.isEmpty()) {
            editTextTitle.error = "Title required"
            editTextTitle.requestFocus()
            return
        }

        // Create task data
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
            "createAt" to FieldValue.serverTimestamp() // Firestore timestamp
        )

        // Add the task to the Firestore database
        firestore.collection("users").document(userId).collection("tasks")
            .add(task)
            .addOnSuccessListener { documentReference ->
                println("Task added with ID: ${documentReference.id}")
                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener { e ->
                println("Error adding task: $e")
                Toast.makeText(this, "Error adding task", Toast.LENGTH_SHORT).show()
            }
    }
}
