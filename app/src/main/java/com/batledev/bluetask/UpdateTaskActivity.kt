package com.batledev.bluetask

import android.app.DatePickerDialog
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
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Activity to update a task.
 * Launches when the user clicks on a short task in the main activity list.
 * The user can update the task title, description, color, start date, end date, and priority.
 */
class UpdateTaskActivity : AppCompatActivity() {

    // UI elements
    private lateinit var editTextTitle: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var buttonColorPicker: Button
    private lateinit var buttonStartDatePicker: Button
    private lateinit var buttonEndDatePicker: Button
    private lateinit var spinnerPriority: Spinner
    private lateinit var buttonUpdate: Button
    private lateinit var buttonDelete: Button
    private lateinit var buttonArchive: Button

    // Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // Task data
    private lateinit var taskRef: DocumentReference // The reference to the task document
    private var taskDoc: Task? = null // The task data
    private var taskColor: String? = null
    private var startDate: Date? = null
    private var endDate: Date? = null
    private var priority: Int = -1

    /**
     * This function is called when the activity is created.
     * - Get the UI elements and set up the event listeners.
     * - Initialize Firebase.
     * - Set up the priority spinner (With translated values).
     * - Get the task id from the intent and load the task data.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_task)

        // Get UI elements
        editTextTitle = findViewById(R.id.editTextTitle)
        editTextDescription = findViewById(R.id.editTextDescription)
        buttonColorPicker = findViewById(R.id.buttonColorPicker)
        buttonStartDatePicker = findViewById(R.id.buttonStartDatePicker)
        buttonEndDatePicker = findViewById(R.id.buttonEndDatePicker)
        spinnerPriority = findViewById(R.id.spinnerPriority)
        buttonUpdate = findViewById(R.id.buttonUpdate)
        buttonDelete = findViewById(R.id.buttonDelete)
        buttonArchive = findViewById(R.id.buttonArchive)

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Set up click listeners
        buttonColorPicker.setOnClickListener { showColorPicker() }
        buttonStartDatePicker.setOnClickListener { showStartDatePicker() }
        buttonEndDatePicker.setOnClickListener { showEndDatePicker() }
        buttonUpdate.setOnClickListener { updateTask() }
        buttonDelete.setOnClickListener { deleteTask() }
        buttonArchive.setOnClickListener { archiveTask() }

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

        // Get taskId from intent and load task data
        val userId = firebaseAuth.currentUser!!.uid
        val taskId = intent.getStringExtra("TASK_ID") ?: return finish()
        this.taskRef = firestore.collection("users").document(userId)
            .collection("tasks").document(taskId)
        loadTask()
    }

    /**
     * Load task data from Firestore
     */
    private fun loadTask() {
        // Get task data
        taskRef.get().addOnSuccessListener { document ->
            taskDoc = document.toObject<Task>()
            taskDoc?.let {
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
                buttonUpdate.text =
                    if (it.status == "deleted") resources.getString(R.string.restore)
                    else resources.getString(R.string.update)
                buttonArchive.text =
                    if (it.status == "archived") resources.getString(R.string.unarchive)
                    else resources.getString(R.string.archive)
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
     * Update the task in Firestore.
     * - If the task is deleted, change the status to active (restore it).
     */
    private fun updateTask() {
        // Get input data
        val title = editTextTitle.text.toString().trim()
        val description = editTextDescription.text.toString().trim()

        // Validate input data
        if (title.isEmpty()) {
            editTextTitle.error = "Title required"
            editTextTitle.requestFocus()
            return
        }

        // Assemble task data
        val task = hashMapOf<String, Any?>(
            "title" to title,
            "description" to description,
            "color" to taskColor,
            "startDate" to startDate,
            "endDate" to endDate,
            "priority" to priority
        )

        // Change to active if the task is deleted
        if (taskDoc?.status == "deleted") {
            task["status"] = "active"
        }

        // Update task in Firestore database
        taskRef.update(task as Map<String, Any?>)
            .addOnSuccessListener {
                println("Task updated successfully")
                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener { e ->
                println("Error updating task : $e")
                Toast.makeText(this, resources.getString(R.string.update_error), Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Move to the trash or delete the task permanently.
     * - If the task is already deleted, delete it permanently, otherwise move it to the trash.
     */
    private fun deleteTask() {
        if (taskDoc?.status == "deleted") {
            taskRef.delete()
                .addOnSuccessListener {
                    println("Task deleted successfully")
                    Toast.makeText(this, resources.getString(R.string.delete_successful), Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                .addOnFailureListener { e ->
                    println("Error deleting task: $e")
                    Toast.makeText(this, resources.getString(R.string.update_error), Toast.LENGTH_SHORT).show()
                }
        } else {
            taskRef.update("status", "deleted")
                .addOnSuccessListener {
                    println("Task marked as deleted successfully")
                    Toast.makeText(this, resources.getString(R.string.trash_successful), Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                .addOnFailureListener { e ->
                    println("Error updating task : $e")
                    Toast.makeText(this, resources.getString(R.string.update_error), Toast.LENGTH_SHORT).show()
                }
        }
    }

    /**
     * Archive or unarchive the task.
     * - If the task is archived, unarchive it, otherwise archive it.
     */
    private fun archiveTask() {
        val newStatus = if (taskDoc?.status == "archived") "active" else "archived"
        taskRef.update("status", newStatus)
            .addOnSuccessListener {
                val message =
                    if (newStatus == "archived") resources.getString(R.string.archive_successful)
                    else resources.getString(R.string.unarchive_successful)
                println(message)
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener { e ->
                println("Error updating task : $e")
                Toast.makeText(this, resources.getString(R.string.update_error), Toast.LENGTH_SHORT).show()
            }
    }
}
