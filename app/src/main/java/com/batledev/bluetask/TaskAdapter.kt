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

        titleTextView.text = task?.title
        descriptionTextView.text = task?.description

        return view
    }
}
