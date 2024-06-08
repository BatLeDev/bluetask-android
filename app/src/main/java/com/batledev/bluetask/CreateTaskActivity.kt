package com.batledev.bluetask

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
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
    private lateinit var buttonLabelPicker: Button
    private lateinit var textViewSelectedLabels: TextView

    // Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // Task data (for complex input)
    private var priority: Int = -1
    private var selectedLabels: List<String> = emptyList()
    private var taskColor: String? = null
    private var startDate: Date? = null
    private var endDate: Date? = null

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
        buttonLabelPicker = findViewById(R.id.buttonLabelPicker)
        textViewSelectedLabels = findViewById(R.id.textViewSelectedLabels)

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        val userRef = firestore.collection("users").document(firebaseAuth.currentUser!!.uid)

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
        buttonLabelPicker.setOnClickListener {
            TaskUtils.showLabelPicker(this, userRef, selectedLabels) { labels ->
                selectedLabels = labels.toMutableList()
                textViewSelectedLabels.text = selectedLabels.joinToString(", ")
            }
        }
        buttonAddTask.setOnClickListener { addTask(userRef) }

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
    private fun addTask(userRef: DocumentReference) {
        // Get input data
        val title = editTextTitle.text.toString().trim()
        val description = editTextDescription.text.toString().trim()

        // Validate input data
        if (title.isEmpty()) {
            editTextTitle.error = resources.getString(R.string.error_empty_title)
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
            "labels" to selectedLabels,
            "lines" to emptyList<String>(),
            "linesChecked" to emptyList<String>(),
            "priority" to priority,
            "state" to -1,
            "status" to "active",
            "createAt" to FieldValue.serverTimestamp() // Firestore timestamp
        )

        // Add the task to the Firestore database
        userRef.collection("tasks")
            .add(task)
            .addOnSuccessListener { documentReference ->
                println("Task added with ID: ${documentReference.id}")
                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener { e ->
                println("Error adding task: $e")
                Toast.makeText(this, resources.getString(R.string.error_create_task),
                    Toast.LENGTH_SHORT).show()
            }
    }
}
