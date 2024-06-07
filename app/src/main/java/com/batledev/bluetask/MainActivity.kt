package com.batledev.bluetask

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.RadioGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.batledev.bluetask.authentication.UnloggedActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject

class MainActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var taskAdapter: TaskAdapter
    private val tasks = mutableListOf<Task>()

    // Filters
    private var orderBy: String = "createAt"
    private var priority: Int = -1
    private var label: String = ""

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    // Activity launcher for CreateTaskActivity and UpdateTaskActivity
    private val taskActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            loadTasks()
        }
    }

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
        taskAdapter = TaskAdapter(this, tasks, taskActivityLauncher)
        listView.adapter = taskAdapter

        // Load tasks
        loadTasks()

        // Setup drawer layout and navigation view
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        loadLabelsInNavigationView()

        // --------- Listeners ---------
        swipeRefreshLayout.setOnRefreshListener {
            loadTasks()
        }

        val createTaskButton = findViewById<Button>(R.id.createTaskButton)
        createTaskButton.setOnClickListener {
            taskActivityLauncher.launch(Intent(this, CreateTaskActivity::class.java))
        }

        val filtersButton = findViewById<Button>(R.id.filtersButton)
        filtersButton.setOnClickListener {
            showFiltersDialog()
        }

        val menuButton = findViewById<Button>(R.id.menuButton)
        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.allTasks -> {
                    // Handle All Tasks action
                }
                R.id.archive -> {
                    // Handle Archive action
                }
                R.id.trash -> {
                    // Handle Trash action
                }
                R.id.editLabels -> {
                    // Handle Edit Labels action
                }
                R.id.logout -> {
                    firebaseAuth.signOut()
                    startActivity(Intent(this, UnloggedActivity::class.java))
                    finish()
                }
                R.id.aboutUs -> {
                    // Handle About Us action
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun loadLabelsInNavigationView() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val userRef = firestore.collection("users").document(userId)

        // Get the labels from the user document
        userRef.get().addOnSuccessListener { document ->
            val menu = navigationView.menu
            val labelsMenu = menu.findItem(R.id.labels).subMenu
            @Suppress("UNCHECKED_CAST") // It's safe to cast to List<Map<String, String>>
            val labels = document["labels"] as List<Map<String, String>>
            labelsMenu?.clear()  // Clear existing items

            // Add labels to the navigation view
            for (labelMap in labels) {
                val title = labelMap["title"] ?: ""
                val menuItem = labelsMenu?.add(Menu.NONE, View.generateViewId(), Menu.NONE, title)
                menuItem?.setOnMenuItemClickListener {
                    this.label = title
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
            }
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
