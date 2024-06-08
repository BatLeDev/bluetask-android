package com.batledev.bluetask

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.cardview.widget.CardView

/**
 * Task adapter for the task list.
 * @param context The context of the adapter.
 * @param tasks The list of tasks.
 * @param taskActivityLauncher The activity result launcher for the task activity
 *                             (To refresh the task list).
 */
class TaskAdapter(context: Context, tasks: List<Task>, private val taskActivityLauncher: ActivityResultLauncher<Intent>) : ArrayAdapter<Task>(context, 0, tasks) {

    /**
     * Get the view of the task item.
     * @param position The position of the task item.
     * @param convertView The view of the task item.
     * @param parent The parent view of the task item.
     * @return The view of the task item.
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_task, parent, false)
        val task = getItem(position)!!

        // Get UI elements
        val cardView = view.findViewById<CardView>(R.id.taskItem)
        val titleTextView = view.findViewById<TextView>(R.id.taskTitle)
        val descriptionTextView = view.findViewById<TextView>(R.id.taskDescription)

        // Set the title and description of the task
        // If the title and the description are empty,
        // show a message to announce that the task is empty
        if (task.title.isEmpty() && task.description.isEmpty()) {
            titleTextView.visibility = View.GONE
            descriptionTextView.visibility = View.VISIBLE
            descriptionTextView.text = context.getString(R.string.empty_task_description)
            descriptionTextView.setTypeface(null, Typeface.ITALIC)
        }
        // If the title is empty, hide the title, same for the description
        else {
            if (task.title.isEmpty()) {
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
                descriptionTextView.setTypeface(null, Typeface.NORMAL)
            }
        }

        // Set the color of the task (if defined)
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
