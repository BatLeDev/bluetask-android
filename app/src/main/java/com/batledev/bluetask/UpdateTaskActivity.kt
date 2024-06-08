package com.batledev.bluetask

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import java.text.SimpleDateFormat
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
        buttonColorPicker.setOnClickListener {
            TaskUtils.showColorPicker(this, buttonColorPicker) { color ->
                taskColor = color
            }
        }
        buttonStartDatePicker.setOnClickListener {
            TaskUtils.showStartDatePicker(this, buttonStartDatePicker, endDate) { date ->
                startDate = date
            }
        }
        buttonEndDatePicker.setOnClickListener {
            TaskUtils.showEndDatePicker(this, buttonEndDatePicker, startDate) { date ->
                endDate = date
            }
        }
        buttonUpdate.setOnClickListener { updateTask() }
        buttonDelete.setOnClickListener { deleteTask() }
        buttonArchive.setOnClickListener { archiveTask() }

        // Set up priority spinner
        val priorities = resources.getStringArray(R.array.priorities)
        TaskUtils.setupPrioritySpinner(this, spinnerPriority, priorities) { priority ->
            this.priority = priority
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
