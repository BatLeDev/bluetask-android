package com.batledev.bluetask

import com.google.firebase.Timestamp

/**
 * Task data class.
 * @param id The task ID.
 * @param color The task color.
 * @param createAt The task creation date.
 * @param description The task description.
 * @param endDate The task end date.
 * @param labels The task labels.
 * @param lines The task lines.
 * @param linesChecked The task checked lines.
 * @param priority The task priority.
 * @param startDate The task start date.
 * @param state The task state.
 * @param status The task status.
 * @param title The task title.
 */
data class Task(
    val id: String = "",
    val color: String? = null,
    val createAt: Timestamp? = null,
    val description: String = "",
    val endDate: Timestamp? = null,
    val labels: List<String> = emptyList(),
    val lines: List<String> = emptyList(),
    val linesChecked: List<String> = emptyList(),
    val priority: Int = -1,
    val startDate: Timestamp? = null,
    val state: Int = -1,
    val status: String = "active",
    val title: String = ""
)
