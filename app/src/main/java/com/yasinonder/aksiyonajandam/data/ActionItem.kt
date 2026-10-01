package com.yasinonder.aksiyonajandam.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

data class ActionItem(
    val id: Long = 0,
    val title: String,
    val subject: String = "",
    val notes: String = "",
    val type: String = "Genel",
    val priority: String = "Normal",
    val date: String = LocalDate.now().toString(),
    val time: String = LocalTime.now().withSecond(0).withNano(0).toString(),
    val alarmEnabled: Boolean = true,
    val completed: Boolean = false,
    val imageUris: List<String> = emptyList(),
    val extra1: String = "",
    val extra2: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun scheduledAtMillis(): Long {
        val local = LocalDateTime.of(LocalDate.parse(date), LocalTime.parse(time))
        return local.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
