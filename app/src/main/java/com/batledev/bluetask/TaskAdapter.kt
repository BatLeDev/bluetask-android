package com.batledev.bluetask

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat

class TaskAdapter(context: Context, tasks: List<Task>, private val taskActivityLauncher: ActivityResultLauncher<Intent>) : ArrayAdapter<Task>(context, 0, tasks) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_task, parent, false)
        val task = getItem(position)

        val cardView = view.findViewById<CardView>(R.id.taskItem)
        val titleTextView = view.findViewById<TextView>(R.id.taskTitle)
        val descriptionTextView = view.findViewById<TextView>(R.id.taskDescription)


        // Set the title and description of the task
        // If the title or description is null or empty, hide the text view
        if (task!!.title.isEmpty()) {
            titleTextView.visibility = View.GONE
        } else {
            titleTextView.visibility = View.VISIBLE
            titleTextView.text = task.title
        }

        if (task.description.isEmpty()) {
            descriptionTextView.visibility = View.GONE
        } else {
            descriptionTextView.visibility = View.VISIBLE
            descriptionTextView.text = task.description
        }

        if (!task.color.isNullOrEmpty()) {
            cardView.setCardBackgroundColor(Color.parseColor(task.color))
        } else {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.colorBackgroundFloating, typedValue, true)
            val colorBackgroundFloating = typedValue.data
            cardView.setCardBackgroundColor(colorBackgroundFloating)
        }

        // Set listener to open the task when clicked
        view.setOnClickListener {
            val intent = Intent(context, UpdateTaskActivity::class.java)
            intent.putExtra("TASK_ID", task.id)
            taskActivityLauncher.launch(intent)
        }

        return view
    }
}
