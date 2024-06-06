package com.batledev.bluetask

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.batledev.bluetask.authentication.UnloggedActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

class MainActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var taskAdapter: TaskAdapter
    private val tasks = mutableListOf<Task>()

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

        // Swipe to refresh
        swipeRefreshLayout.setOnRefreshListener {
            loadTasks()
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun loadTasks() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val tasksRef = firestore.collection("users").document(userId).collection("tasks")

        tasksRef.get().addOnSuccessListener { documents ->
            tasks.clear()
            for (document in documents) {
                val task = document.toObject<Task>()
                tasks.add(task)
            }
            taskAdapter.notifyDataSetChanged()
        }
    }
}
