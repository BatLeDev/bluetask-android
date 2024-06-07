package com.batledev.bluetask

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.batledev.bluetask.authentication.UnloggedActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject

class MainActivity : AppCompatActivity()  {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var taskAdapter: TaskAdapter
    private val tasks = mutableListOf<Task>()

    // Filters
    private var orderBy: String = "createAt"
    private var priority: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is not logged in
        firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser == null) {
            startActivity(Intent(this, UnloggedActivity::class.java))
            finish()
        }

        // --------- The user is logged in ---------
        setContentView(R.layout.activity_main)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Setup ListView
        val listView = findViewById<ListView>(R.id.listViewTasks)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        taskAdapter = TaskAdapter(this, tasks)
        listView.adapter = taskAdapter

        // Load tasks
        loadTasks()

        // --------- Listeners ---------
        swipeRefreshLayout.setOnRefreshListener {
            loadTasks()
        }

        val createTaskButton = findViewById<Button>(R.id.createTaskButton)
        createTaskButton.setOnClickListener {
            startActivity(Intent(this, CreateTaskActivity::class.java))
        }

        val filtersButton = findViewById<Button>(R.id.filtersButton)
        filtersButton.setOnClickListener {
            showFiltersDialog()
        }
    }

    private fun loadTasks() {
        swipeRefreshLayout.isRefreshing = true

        val userId = firebaseAuth.currentUser?.uid ?: return
        var tasksRef = firestore.collection("users").document(userId)
            .collection("tasks")
            .whereEqualTo("status", "active")
            .orderBy(orderBy, Query.Direction.DESCENDING)

        // Apply filters
        if (priority != -1) {
            tasksRef = tasksRef.whereEqualTo("priority", priority)
        }

        tasksRef.get().addOnSuccessListener { documents ->
            tasks.clear()
            taskAdapter.notifyDataSetChanged()
            for (document in documents) {
                val taskId = document.id
                val task = document.toObject<Task>().copy(id = taskId)
                println(task)
                tasks.add(task)
            }
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun showFiltersDialog() {
        // Get the layout inflater
        val dialogView = layoutInflater.inflate(R.layout.dialog_filters, null)
        val orderByGroup = dialogView.findViewById<RadioGroup>(R.id.orderByGroup)
        val priorityGroup = dialogView.findViewById<RadioGroup>(R.id.priorityGroup)

        // Pre-select the current filters
        when (orderBy) {
            "createAt" -> orderByGroup.check(R.id.orderByNewest)
            "startDate" -> orderByGroup.check(R.id.orderByStartDate)
            "endDate" -> orderByGroup.check(R.id.orderByEndDate)
        }
        when (priority) {
            0 -> priorityGroup.check(R.id.priorityLow)
            1 -> priorityGroup.check(R.id.priorityMedium)
            2 -> priorityGroup.check(R.id.priorityHigh)
            -1 -> priorityGroup.check(R.id.priorityAll)
        }

        // Build the dialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle(resources.getString(R.string.filters))
            .create()

        // Set the listener on the apply button
        dialogView.findViewById<Button>(R.id.applyFiltersButton).setOnClickListener {
            // Save the selected filters
            orderBy = when (orderByGroup.checkedRadioButtonId) {
                R.id.orderByStartDate -> "startDate"
                R.id.orderByEndDate -> "endDate"
                else -> "createAt"
            }
            priority = when (priorityGroup.checkedRadioButtonId) {
                R.id.priorityLow -> 0
                R.id.priorityMedium -> 1
                R.id.priorityHigh -> 2
                else -> -1
            }

            loadTasks()
            dialog.dismiss()
        }

        dialog.show()
    }
}
