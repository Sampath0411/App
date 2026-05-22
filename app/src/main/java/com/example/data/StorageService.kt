package com.example.data

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class StorageService(context: Context) {
    private val prefs = context.getSharedPreferences("attendease_prefs", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    private val studentListAdapter = moshi.adapter<List<Student>>(
        Types.newParameterizedType(List::class.java, Student::class.java)
    )
    private val attendanceListAdapter = moshi.adapter<List<AttendanceRecord>>(
        Types.newParameterizedType(List::class.java, AttendanceRecord::class.java)
    )
    private val timetableListAdapter = moshi.adapter<List<TimetableEntry>>(
        Types.newParameterizedType(List::class.java, TimetableEntry::class.java)
    )

    init {
        // Pre-populate timetable if complete empty, to make the screens beautiful on first load
        if (getTimetable().isEmpty()) {
            val initialTimetable = ArrayList<TimetableEntry>()
            // CSE timetable
            initialTimetable.add(TimetableEntry(branch = "CSE", day = "Monday", period = 1, subject = "Software Eng", timeSlot = "09:00 AM - 10:00 AM"))
            initialTimetable.add(TimetableEntry(branch = "CSE", day = "Monday", period = 2, subject = "Database Systems", timeSlot = "10:15 AM - 11:15 AM"))
            initialTimetable.add(TimetableEntry(branch = "CSE", day = "Monday", period = 3, subject = "Compiler Design", timeSlot = "11:30 AM - 12:30 PM"))
            initialTimetable.add(TimetableEntry(branch = "CSE", day = "Tuesday", period = 1, subject = "Compiler Design", timeSlot = "09:00 AM - 10:00 AM"))
            initialTimetable.add(TimetableEntry(branch = "CSE", day = "Tuesday", period = 2, subject = "Web Dev Lab", timeSlot = "10:15 AM - 11:15 AM"))
            initialTimetable.add(TimetableEntry(branch = "CSE", day = "Wednesday", period = 1, subject = "AI & ML", timeSlot = "09:00 AM - 10:00 AM"))
            initialTimetable.add(TimetableEntry(branch = "CSE", day = "Wednesday", period = 2, subject = "Computer Networks", timeSlot = "10:15 AM - 11:15 AM"))
            initialTimetable.add(TimetableEntry(branch = "CSE", day = "Thursday", period = 2, subject = "AI & ML Lab", timeSlot = "10:15 AM - 11:15 AM"))
            initialTimetable.add(TimetableEntry(branch = "CSE", day = "Friday", period = 1, subject = "Software Eng", timeSlot = "09:00 AM - 10:00 AM"))
            initialTimetable.add(TimetableEntry(branch = "CSE", day = "Friday", period = 3, subject = "Computer Networks", timeSlot = "11:30 AM - 12:30 PM"))
            initialTimetable.add(TimetableEntry(branch = "CSE", day = "Saturday", period = 1, subject = "Project Review", timeSlot = "09:00 AM - 10:00 AM"))

            // ECE timetable
            initialTimetable.add(TimetableEntry(branch = "ECE", day = "Monday", period = 1, subject = "VLSI Circuits", timeSlot = "09:00 AM - 10:00 AM"))
            initialTimetable.add(TimetableEntry(branch = "ECE", day = "Monday", period = 2, subject = "Digital Signal Proc", timeSlot = "10:15 AM - 11:15 AM"))
            initialTimetable.add(TimetableEntry(branch = "ECE", day = "Tuesday", period = 1, subject = "Microcontrollers", timeSlot = "09:00 AM - 10:00 AM"))
            initialTimetable.add(TimetableEntry(branch = "ECE", day = "Wednesday", period = 2, subject = "Embedded Systems", timeSlot = "10:15 AM - 11:15 AM"))

            saveTimetable(initialTimetable)
        }
    }

    private fun <T> list(): ArrayList<T> = ArrayList()

    // --- Student ---
    fun getStudents(): List<Student> {
        val json = prefs.getString("students", null) ?: return emptyList()
        return try {
            studentListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            Log.e("StorageService", "Error reading students", e)
            emptyList()
        }
    }

    fun saveStudents(students: List<Student>) {
        val json = studentListAdapter.toJson(students)
        prefs.edit().putString("students", json).apply()
    }

    fun registerStudent(student: Student): Boolean {
        val current = getStudents().toMutableList()
        if (current.any { it.regNo.trim().equals(student.regNo.trim(), ignoreCase = true) }) {
            return false // duplicate registration number
        }
        current.add(student)
        saveStudents(current)
        return true
    }

    fun updateStudent(updatedStudent: Student): Boolean {
        val current = getStudents().toMutableList()
        val index = current.indexOfFirst { it.regNo.trim().equals(updatedStudent.regNo.trim(), ignoreCase = true) }
        if (index == -1) return false
        current[index] = updatedStudent
        saveStudents(current)
        return true
    }

    // --- Attendance ---
    fun getAttendance(): List<AttendanceRecord> {
        val json = prefs.getString("attendance", null) ?: return emptyList()
        return try {
            attendanceListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            Log.e("StorageService", "Error reading attendance", e)
            emptyList()
        }
    }

    fun saveAttendance(records: List<AttendanceRecord>) {
        val json = attendanceListAdapter.toJson(records)
        prefs.edit().putString("attendance", json).apply()
    }

    fun markSingleAttendance(record: AttendanceRecord) {
        val current = getAttendance().toMutableList()
        // Remove existing record for the same student on the same date for the same subject
        current.removeAll {
            it.regNo.equals(record.regNo, ignoreCase = true) &&
                    it.subject.equals(record.subject, ignoreCase = true) &&
                    it.date == record.date
        }
        current.add(record)
        saveAttendance(current)
    }

    fun markBulkAttendance(records: List<AttendanceRecord>) {
        if (records.isEmpty()) return
        val current = getAttendance().toMutableList()
        val removeMap = records.associateBy { "${it.regNo.lowercase()}|${it.subject.lowercase()}|${it.date}" }
        
        current.removeAll {
            val key = "${it.regNo.lowercase()}|${it.subject.lowercase()}|${it.date}"
            removeMap.containsKey(key)
        }
        current.addAll(records)
        saveAttendance(current)
    }

    // --- Timetable ---
    fun getTimetable(): List<TimetableEntry> {
        val json = prefs.getString("timetable", null) ?: return emptyList()
        return try {
            timetableListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            Log.e("StorageService", "Error reading timetable", e)
            emptyList()
        }
    }

    fun saveTimetable(entries: List<TimetableEntry>) {
        val json = timetableListAdapter.toJson(entries)
        prefs.edit().putString("timetable", json).apply()
    }

    fun addTimetableEntry(entry: TimetableEntry) {
        val current = getTimetable().toMutableList()
        current.add(entry)
        saveTimetable(current)
    }

    fun updateTimetableEntry(updated: TimetableEntry) {
        val current = getTimetable().toMutableList()
        val idx = current.indexOfFirst { it.id == updated.id }
        if (idx != -1) {
            current[idx] = updated
            saveTimetable(current)
        }
    }

    fun deleteTimetableEntry(id: String) {
        val current = getTimetable().toMutableList()
        current.removeAll { it.id == id }
        saveTimetable(current)
    }

    // --- Theme Settings ---
    fun getThemeMode(): Boolean {
        // Defaults to true (dark mode) as requested by premium style
        return prefs.getBoolean("theme", true)
    }

    fun saveThemeMode(isDark: Boolean) {
        prefs.edit().putBoolean("theme", isDark).apply()
    }

    // --- Dynamic Notifications Settings ---
    fun getNotifications(): List<NotificationItem> {
        val json = prefs.getString("notifications", null) ?: return emptyList()
        val type = Types.newParameterizedType(List::class.java, NotificationItem::class.java)
        val adapter = moshi.adapter<List<NotificationItem>>(type)
        return try {
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveNotifications(list: List<NotificationItem>) {
        val type = Types.newParameterizedType(List::class.java, NotificationItem::class.java)
        val adapter = moshi.adapter<List<NotificationItem>>(type)
        val json = adapter.toJson(list)
        prefs.edit().putString("notifications", json).apply()
    }

    fun getLastReadTimestamp(): Long {
        return prefs.getLong("last_read_timestamp", 0L)
    }

    fun saveLastReadTimestamp(timestamp: Long) {
        prefs.edit().putLong("last_read_timestamp", timestamp).apply()
    }
}
