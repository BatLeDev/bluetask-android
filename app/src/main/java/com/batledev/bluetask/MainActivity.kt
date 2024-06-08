package com.batledev.bluetask

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.batledev.bluetask.authentication.UnloggedActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject

/**
 * This activity is the main activity of the app. It's shown when the user is logged in.
 * - Show the list of tasks
 * - Show a button to create a new task
 * - Show a button to open the filters dialog
 * - Show a button to open the navigation drawer
 * that allow to navigate to all tasks, archive, trash, labels, logout and about us
 */
class MainActivity : AppCompatActivity() {
    // Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userRef: DocumentReference

    // UI elements
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var taskAdapter: TaskAdapter
    private val tasks = mutableListOf<Task>()

    // Filters
    private var orderBy: String = "createAt"
    private var status: String = "active"
    private var label: String = ""
    private var priority: Int = -1

    // Activity launcher for CreateTaskActivity and UpdateTaskActivity
    private val taskActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            loadTasks()
        }
    }

    /**
     * Called when the activity is starting.
     * - Initialize firebase and return if user is not logged in
     * - Initialize the UI elements
     * - Load tasks and labels
     * - Setup listeners
     */
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
        val userId = firebaseAuth.currentUser?.uid!!
        this.userRef = firestore.collection("users").document(userId)

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
                    this.label = ""
                    this.status = "active"
                    loadTasks()
                }
                R.id.archive -> {
                    this.label = ""
                    this.status = "archived"
                    loadTasks()
                }
                R.id.trash -> {
                    this.label = ""
                    this.status = "deleted"
                    loadTasks()
                }
                R.id.editLabels -> {
                    showEditLabelsDialog()
                }
                R.id.logout -> {
                    firebaseAuth.signOut()
                    startActivity(Intent(this, UnloggedActivity::class.java))
                    finish()
                }
                R.id.aboutUs -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://bluetask.batledev.com/about"))
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    /**
     * Load labels in the navigation view
     * - Get the labels from the user document
     * - Clear existing labels
     * - Add the labels to the navigation view
     */
    private fun loadLabelsInNavigationView() {
        // Get the labels from the user document
        userRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val menu = navigationView.menu
                val labelsMenu = menu.findItem(R.id.labels).subMenu
                labelsMenu?.clear()  // Clear existing labels

                val labels = document.get("labels")
                if (labels is List<*>) {
                    for (labelMap in labels) {
                        if (labelMap is Map<*, *>) {
                            val title = labelMap["title"] as? String ?: ""
                            val menuItem = labelsMenu?.add(Menu.NONE, View.generateViewId(), Menu.NONE, title)
                            menuItem?.setOnMenuItemClickListener {
                                this.label = title
                                drawerLayout.closeDrawer(GravityCompat.START)
                                loadTasks()
                                true
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Load tasks
     * - Show the swipe refresh layout
     * - Get the tasks from Firestore and filter/order them
     * - Clear existing tasks
     * - Add the tasks to the list
     * - Hide the swipe refresh layout
     */
    private fun loadTasks() {
        swipeRefreshLayout.isRefreshing = true

        val userId = firebaseAuth.currentUser?.uid ?: return
        var tasksRef = firestore.collection("users").document(userId)
            .collection("tasks")
            .orderBy(orderBy, Query.Direction.DESCENDING)

        // Apply filters
        if (priority != -1) {
            tasksRef = tasksRef.whereEqualTo("priority", priority)
        }
        if (label.isNotEmpty()) {
            tasksRef = tasksRef.whereEqualTo("status", "active")
            tasksRef = tasksRef.whereArrayContains("labels", label)
        } else {
            tasksRef = tasksRef.whereEqualTo("status", status)
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

    /**
     * Show the filters dialog
     * - Get the layout inflater
     * - Pre-select the current filters
     * - Build the dialog
     * - Set the listener on the apply button
     * -> Save the selected filters
     * -> Refresh the list of tasks
     */
    private fun showFiltersDialog() {
        // Get the layout inflater
        val dialogView = layoutInflater.inflate(R.layout.dialog_filters, null)

        // Get UI elements
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

    /**
     * Show the edit labels dialog
     * - Get the layout inflater
     * - Load existing labels
     * - Add listeners to delete and add labels
     */
    private fun showEditLabelsDialog() {
        // Get the layout inflater
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_labels, null)

        // Get UI elements
        val listViewLabels = dialogView.findViewById<ListView>(R.id.listViewLabels)
        val editTextLabel = dialogView.findViewById<EditText>(R.id.editTextLabel)
        val buttonAddLabel = dialogView.findViewById<Button>(R.id.buttonAddLabel)

        val labelsAdapter = object : ArrayAdapter<String>(this, R.layout.item_label, mutableListOf<String>()) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: layoutInflater.inflate(R.layout.item_label, parent, false)
                val label = getItem(position) ?: return view

                val textViewLabel = view.findViewById<TextView>(R.id.textViewLabel)
                val buttonDeleteLabel = view.findViewById<Button>(R.id.buttonDeleteLabel)

                textViewLabel.text = label
                buttonDeleteLabel.setOnClickListener {
                    deleteLabel(label)
                    remove(label)
                }

                return view
            }
        }
        listViewLabels.adapter = labelsAdapter

        // Load existing labels
        userRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val labels = document.get("labels")
                if (labels is List<*>) {
                    for (labelMap in labels) {
                        if (labelMap is Map<*, *>) {
                            val title = labelMap["title"] as? String ?: ""
                            labelsAdapter.add(title)
                        }
                    }
                }
            }
        }

        // Add new label
        buttonAddLabel.setOnClickListener {
            val newLabel = editTextLabel.text.toString().trim()
            if (newLabel.isNotEmpty()) {
                addLabel(newLabel)
                labelsAdapter.add(newLabel)
                editTextLabel.text.clear()
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Labels")
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok, null)
            .create()

        // call loadLabelsInNavigationView when dialog is dismissed
        dialog.setOnDismissListener {
            loadLabelsInNavigationView()
        }

        dialog.show()
    }

    /**
     * Add a label
     * - Get the user document
     * - Add the label to the user document
     */
    private fun addLabel(newLabel: String) {
        val newLabelMap = mapOf("title" to newLabel, "icon" to "mdi-tag-outline")
        userRef.update("labels", FieldValue.arrayUnion(newLabelMap))
    }

    /**
     * Delete a label
     * - Get the user document
     * - Remove the label from the user label list
     * - Update all tasks that have the label
     */
    private fun deleteLabel(label: String) {
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val labels = snapshot.get("labels")
            if (labels is List<*>) {
                val updatedLabels = labels.filterIsInstance<Map<String, String>>()
                    .filter { it["title"] != label }
                transaction.update(userRef, "labels", updatedLabels)
            }
        }.addOnSuccessListener {
            // Update tasks
            val userId = firebaseAuth.currentUser?.uid!!
            val tasksRef = firestore.collection("users").document(userId).collection("tasks")
            tasksRef.whereArrayContains("labels", label).get().addOnSuccessListener { documents ->
                for (document in documents) {
                    tasksRef.document(document.id).update("labels", FieldValue.arrayRemove(label))
                }
            }
        }
    }

}
