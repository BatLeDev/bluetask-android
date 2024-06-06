package com.batledev.bluetask

import com.google.firebase.Timestamp

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
    val status: String = "",
    val title: String = ""
)

