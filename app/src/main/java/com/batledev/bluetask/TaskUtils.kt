package com.batledev.bluetask

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import com.google.firebase.firestore.DocumentReference
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Utilities functions for tasks.
 */
object TaskUtils {

    /**
     * Set up the priority spinner.
     * @param context The context of the activity.
     * @param spinner The spinner to set up.
     * @param priorities The priority values to display.
     * @param onPrioritySelected The function to call when a priority is selected.
     */
    fun setupPrioritySpinner(context: Context, spinner: Spinner, priorities: Array<String>, onPrioritySelected: (Int) -> Unit) {
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, priorities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(0)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val priority = when (position) {
                    1 -> 2
                    2 -> 1
                    3 -> 0
                    else -> -1
                }
                onPrioritySelected(priority)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                onPrioritySelected(-1)
            }
        }
    }

    /**
     * Show the label picker.
     * - Get the labels from the user reference.
     * - Show the label picker dialog.
     * @param context The context of the activity.
     * @param userRef The user reference to get the labels.
     * @param selectedLabels The labels already selected.
     * @param onLabelsSelected The function to call when labels are selected.
     */
    fun showLabelPicker(context: Context, userRef: DocumentReference, selectedLabels: List<String>, onLabelsSelected: (List<String>) -> Unit) {
        userRef.get().addOnSuccessListener { document ->
            val labelsList = mutableListOf<String>() // Mutable list to store available labels
            if (document != null && document.exists()) {
                val labels = document.get("labels")
                if (labels is List<*>) {
                    for (labelMap in labels) {
                        if (labelMap is Map<*, *>) {
                            val title = labelMap["title"] as? String ?: ""
                            labelsList.add(title)
                        }
                    }
                }
            }
            // Show label picker dialog
            showLabelPickerDialog(context, labelsList, selectedLabels, onLabelsSelected)
        }
    }

    /**
     * Show the label picker dialog.
     * @param context The context of the activity.
     * @param labelsList The list of labels to display.
     * @param selectedLabels The labels already selected.
     * @param onLabelsSelected The function to call when labels are selected.
     */
    private fun showLabelPickerDialog(context: Context, labelsList: List<String>, selectedLabels: List<String>, onLabelsSelected: (List<String>) -> Unit) {
        val labelsArray = labelsList.toTypedArray() // All labels
        val selectedItems = BooleanArray(labelsArray.size) // Array of booleans, true if selected
        val selectedLabelsTemp = selectedLabels.toMutableList() // Selected labels

        // Pre-select already selected labels
        for ((index, label) in labelsArray.withIndex()) {
            if (selectedLabels.contains(label)) {
                selectedItems[index] = true
            }
        }

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Select Labels")
        builder.setMultiChoiceItems(labelsArray, selectedItems) { _, which, isChecked ->
            if (isChecked) {
                selectedLabelsTemp.add(labelsArray[which])
            } else {
                selectedLabelsTemp.remove(labelsArray[which])
            }
        }
        builder.setPositiveButton("OK") { _, _ ->
            onLabelsSelected(selectedLabelsTemp)
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    /**
     * Show the start date picker.
     * @param context The context of the activity.
     * @param button The button to show the date.
     * @param endDate The end date of the task.
     * @param onDateSet The function to call when a date is selected.
     */
    fun showStartDatePicker(context: Context, button: Button, initialDate: Date?, endDate: Date?, onDateSet: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        initialDate?.let { calendar.time = it }
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, monthOfYear, dayOfMonth ->
                calendar.set(year, monthOfYear, dayOfMonth)
                val date = calendar.time
                onDateSet(date)
                button.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        if (endDate != null) {
            datePickerDialog.datePicker.maxDate = endDate.time
        }
        datePickerDialog.show()
    }

    /**
     * Show the end date picker.
     * @param context The context of the activity.
     * @param button The button to show the date.
     * @param startDate The start date of the task.
     * @param onDateSet The function to call when a date is selected.
     */
    fun showEndDatePicker(context: Context, button: Button, initialDate: Date?, startDate: Date?, onDateSet: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        initialDate?.let { calendar.time = it }
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, monthOfYear, dayOfMonth ->
                calendar.set(year, monthOfYear, dayOfMonth)
                val date = calendar.time
                onDateSet(date)
                button.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        if (startDate != null) {
            datePickerDialog.datePicker.minDate = startDate.time
        }
        datePickerDialog.show()
    }

    /**
     * Show the color picker.
     * @param context The context of the activity.
     * @param button The button to show the color.
     * @param onColorSelected The function to call when a color is selected.
     */
    fun showColorPicker(context: Context, button: Button, onColorSelected: (String) -> Unit) {
        ColorPickerDialog.Builder(context)
            .setTitle("Pick a color")
            .setPreferenceName("MyColorPickerDialog")
            .setPositiveButton("Confirm", ColorEnvelopeListener { envelope, _ ->
                val selectedColor = envelope.color
                val colorHex = "#" + envelope.hexCode.substring(2, 8)
                onColorSelected(colorHex)
                button.setBackgroundColor(selectedColor)
            })
            .setNegativeButton("Clear") { dialogInterface, _ ->
                onColorSelected("")
                button.setBackgroundResource(android.R.drawable.btn_default)
                dialogInterface.dismiss()
            }
            .attachAlphaSlideBar(false)
            .attachBrightnessSlideBar(true)
            .setBottomSpace(12)
            .show()
    }
}
