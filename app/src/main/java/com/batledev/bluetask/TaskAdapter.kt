package com.batledev.bluetask

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class TaskAdapter(context: Context, tasks: List<Task>) : ArrayAdapter<Task>(context, 0, tasks) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_task, parent, false)
        val task = getItem(position)

        val titleTextView = view.findViewById<TextView>(R.id.taskTitle)
        val descriptionTextView = view.findViewById<TextView>(R.id.taskDescription)

        // Set the title and description of the task
        // If the title or description is null or empty, hide the text view
        if (task == null || task.title.isEmpty()) {
            titleTextView.visibility = View.GONE
        } else {
            titleTextView.visibility = View.VISIBLE
            titleTextView.text = task.title
        }

        if (task == null || task.description.isEmpty()) {
            descriptionTextView.visibility = View.GONE
        } else {
            descriptionTextView.visibility = View.VISIBLE
            descriptionTextView.text = task.description
        }

        return view
    }
}
