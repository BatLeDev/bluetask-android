package com.batledev.bluetask

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

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
        buttonAddTask.setOnClickListener { addTask() }

        // Set up priority spinner
        val priorities = resources.getStringArray(R.array.priorities)
        TaskUtils.setupPrioritySpinner(this, spinnerPriority, priorities) { priority ->
            this.priority = priority
        }
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
