package com.batledev.bluetask

import android.app.DatePickerDialog
import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
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
     * Show the start date picker.
     * @param context The context of the activity.
     * @param button The button to show the date.
     * @param endDate The end date of the task.
     * @param onDateSet The function to call when a date is selected.
     */
    fun showStartDatePicker(context: Context, button: Button, endDate: Date?, onDateSet: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
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
    fun showEndDatePicker(context: Context, button: Button, startDate: Date?, onDateSet: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
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