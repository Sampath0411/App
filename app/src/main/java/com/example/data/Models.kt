package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Student(
    val fullName: String,
    val email: String,
    val phone: String,
    val regNo: String,
    val branch: String
)

@JsonClass(generateAdapter = true)
data class AttendanceRecord(
    val regNo: String,
    val subject: String,
    val date: String, // ISO format YYYY-MM-DD
    val status: String // "present" | "absent"
)

@JsonClass(generateAdapter = true)
data class TimetableEntry(
    val id: String = java.util.UUID.randomUUID().toString(), // Helper unique ID for CRUD
    val branch: String,
    val day: String, // e.g. "Monday"
    val period: Int,
    val subject: String,
    val timeSlot: String // e.g. "9:00 AM - 10:00 AM"
)

@JsonClass(generateAdapter = true)
data class NotificationItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis()
)
