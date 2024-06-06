package com.batledev.bluetask

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UpdateTaskActivity : AppCompatActivity() {

    private lateinit var editTextTitle: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var buttonColorPicker: Button
    private lateinit var buttonUpdate: Button
    private lateinit var buttonStartDatePicker: Button
    private lateinit var buttonEndDatePicker: Button
    private lateinit var spinnerPriority: Spinner

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var taskId: String? = null
    private var taskColor: String? = null
    private var startDate: Date? = null
    private var endDate: Date? = null
    private var priority: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_task)

        // Initialize views
        editTextTitle = findViewById(R.id.editTextTitle)
        editTextDescription = findViewById(R.id.editTextDescription)
        buttonColorPicker = findViewById(R.id.buttonColorPicker)
        buttonUpdate = findViewById(R.id.buttonUpdate)
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
        buttonUpdate.setOnClickListener { updateTask() }

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

        // Get taskId from intent
        taskId = intent.getStringExtra("TASK_ID")
        taskId?.let { loadTask(it) }
    }

    /**
     * Load task data from Firestore
     * @param taskId Task ID
     */
    private fun loadTask(taskId: String) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val taskRef = firestore.collection("users").document(userId).collection("tasks").document(taskId)

        taskRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val task = document.toObject<Task>()
                task?.let {
                    // Set variables
                    taskColor = it.color
                    startDate = it.startDate?.toDate()
                    endDate = it.endDate?.toDate()
                    priority = it.priority

                    // Set elements on the view
                    editTextTitle.setText(it.title)
                    editTextDescription.setText(it.description)
                    taskColor?.let { color ->
                        buttonColorPicker.setBackgroundColor(Color.parseColor(color))
                    }
                    startDate?.let { date ->
                        buttonStartDatePicker.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
                    }
                    endDate?.let { date ->
                        buttonEndDatePicker.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
                    }
                    val priorityIndex = when (priority) {
                        2 -> 1
                        1 -> 2
                        0 -> 3
                        else -> 0
                    }
                    spinnerPriority.setSelection(priorityIndex)
                }
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

    private fun updateTask() {
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
            "priority" to priority
        )

        taskId?.let {
            firestore.collection("users").document(userId).collection("tasks").document(it)
                .update(task as Map<String, Any>)
                .addOnSuccessListener {
                    println("Task updated successfully")
                    Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    println("Error updating task: $e")
                    Toast.makeText(this, "Error updating task", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
