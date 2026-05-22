package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.AttendanceRecord
import com.example.data.StorageService
import com.example.data.Student
import com.example.data.TimetableEntry
import com.example.data.NotificationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.data.BiometricService
import com.example.data.SyncService
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainViewModel(private val context: Context, private val storageService: StorageService) : ViewModel() {

    private val syncService = SyncService(context, storageService)
    val biometricService = BiometricService(context)

    // Live state triggers
    val editProfileOnNavigate = MutableStateFlow(false)
    val isDarkMode = MutableStateFlow(storageService.getThemeMode())

    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    private val _attendance = MutableStateFlow<List<AttendanceRecord>>(emptyList())
    val attendance: StateFlow<List<AttendanceRecord>> = _attendance.asStateFlow()

    private val _timetable = MutableStateFlow<List<TimetableEntry>>(emptyList())
    val timetable: StateFlow<List<TimetableEntry>> = _timetable.asStateFlow()

    private val _currentStudent = MutableStateFlow<Student?>(null)
    val currentStudent: StateFlow<Student?> = _currentStudent.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    val lastReadNotifAt = MutableStateFlow(storageService.getLastReadTimestamp())

    init {
        refreshAll()

        // Sync with Live Companion Cloud flows in real-time as per Phase 2
        viewModelScope.launch {
            SyncService.cloudStudents.collect { fresh ->
                if (fresh.isNotEmpty()) _students.value = fresh
            }
        }
        viewModelScope.launch {
            SyncService.cloudAttendance.collect { fresh ->
                if (fresh.isNotEmpty()) _attendance.value = fresh
            }
        }
        viewModelScope.launch {
            SyncService.cloudTimetable.collect { fresh ->
                if (fresh.isNotEmpty()) _timetable.value = fresh
            }
        }
        viewModelScope.launch {
            SyncService.cloudNotifications.collect { fresh ->
                _notifications.value = fresh.sortedByDescending { it.timestamp }
            }
        }
    }

    fun toggleThemeMode() {
        val newMode = !isDarkMode.value
        isDarkMode.value = newMode
        storageService.saveThemeMode(newMode)
    }

    fun setThemeMode(isDark: Boolean) {
        isDarkMode.value = isDark
        storageService.saveThemeMode(isDark)
    }

    fun refreshAll() {
        _students.value = storageService.getStudents()
        _attendance.value = storageService.getAttendance()
        _timetable.value = storageService.getTimetable()
        _notifications.value = storageService.getNotifications().sortedByDescending { it.timestamp }
    }

    fun pullLatestFromCloud(onComplete: () -> Unit = {}) {
        syncService.pullLatestFromCloud {
            refreshAll()
            onComplete()
        }
    }

    fun getUnreadCount(): Int {
        val lastRead = lastReadNotifAt.value
        return _notifications.value.count { it.timestamp > lastRead }
    }

    fun markNotificationsAsRead() {
        val now = System.currentTimeMillis()
        lastReadNotifAt.value = now
        storageService.saveLastReadTimestamp(now)
    }

    fun sendNotification(title: String, body: String): Pair<Boolean, String> {
        val cleanTitle = title.trim()
        val cleanBody = body.trim()
        if (cleanTitle.isEmpty() || cleanBody.isEmpty()) {
            return Pair(false, "Title and message are required")
        }
        val notif = com.example.data.NotificationItem(
            title = cleanTitle,
            body = cleanBody
        )
        syncService.syncNotificationToFirestore(notif)
        
        // Feed into local and cloud collections instantly
        val current = _notifications.value.toMutableList()
        current.add(0, notif)
        _notifications.value = current
        storageService.saveNotifications(current)
        
        return Pair(true, "Notification dispatched successfully!")
    }

    // --- Authentication Actions ---
    fun registerStudent(fullName: String, email: String, phone: String, regNo: String, branch: String): Pair<Boolean, String> {
        val cleanFullname = fullName.trim()
        val cleanEmail = email.trim()
        val cleanPhone = phone.trim()
        val cleanRegNo = regNo.trim()
        
        if (cleanFullname.isEmpty() || cleanEmail.isEmpty() || cleanPhone.isEmpty() || cleanRegNo.isEmpty() || branch.isEmpty()) {
            return Pair(false, "All fields are required")
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return Pair(false, "Please enter a valid email format")
        }
        if (cleanPhone.length != 10 || !cleanPhone.all { it.isDigit() }) {
            return Pair(false, "Phone number must be exactly 10 digits")
        }

        val student = Student(
            fullName = cleanFullname,
            email = cleanEmail,
            phone = cleanPhone,
            regNo = cleanRegNo,
            branch = branch
        )

        val success = storageService.registerStudent(student)
        return if (success) {
            syncService.syncStudentToFirestore(student)
            _students.value = storageService.getStudents()
            Pair(true, "Student registered successfully!")
        } else {
            Pair(false, "Registration number already exists!")
        }
    }

    fun loginStudent(regNo: String, email: String): Pair<Boolean, String> {
        val cleanReg = regNo.trim().lowercase()
        val cleanEmail = email.trim().lowercase()

        if (cleanReg.isEmpty() || cleanEmail.isEmpty()) {
            return Pair(false, "Please fill in all fields")
        }

        val match = storageService.getStudents().find {
            it.regNo.trim().lowercase() == cleanReg && 
            it.email.trim().lowercase() == cleanEmail
        }

        return if (match != null) {
            _currentStudent.value = match
            Pair(true, "Login successful")
        } else {
            Pair(false, "Invalid Registration Number or Email combination")
        }
    }

    fun logout() {
        _currentStudent.value = null
    }

    // --- Student Profile Update ---
    fun updateStudentProfile(fullName: String, email: String, phone: String, branch: String): Pair<Boolean, String> {
        val cur = _currentStudent.value ?: return Pair(false, "No active student session")
        val cleanName = fullName.trim()
        val cleanEmail = email.trim()
        val cleanPhone = phone.trim()

        if (cleanName.isEmpty() || cleanEmail.isEmpty() || cleanPhone.isEmpty() || branch.isEmpty()) {
            return Pair(false, "All fields are required")
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            return Pair(false, "Please enter a valid email format")
        }
        if (cleanPhone.length != 10 || !cleanPhone.all { it.isDigit() }) {
            return Pair(false, "Phone number must be exactly 10 digits")
        }

        val updated = cur.copy(
            fullName = cleanName,
            email = cleanEmail,
            phone = cleanPhone,
            branch = branch
        )

        val success = storageService.updateStudent(updated)
        return if (success) {
            syncService.syncStudentToFirestore(updated)
            _currentStudent.value = updated
            _students.value = storageService.getStudents()
            Pair(true, "Profile updated successfully")
        } else {
            Pair(false, "Failed to update profile info")
        }
    }

    fun updateStudentByAdmin(originalRegNo: String, updatedStudent: Student): Pair<Boolean, String> {
        val success = storageService.updateStudent(updatedStudent)
        return if (success) {
            syncService.syncStudentToFirestore(updatedStudent)
            _students.value = storageService.getStudents()
            // Sync current student in case the admin edited the logged in student
            if (_currentStudent.value?.regNo == originalRegNo) {
                _currentStudent.value = updatedStudent
            }
            Pair(true, "Student updated successfully")
        } else {
            Pair(false, "Failed to find/update student info")
        }
    }

    // --- Attendance Operations ---
    fun submitBulkAttendance(records: List<AttendanceRecord>) {
        storageService.markBulkAttendance(records)
        syncService.syncBulkAttendanceToFirestore(records)
        _attendance.value = storageService.getAttendance()
    }

    fun submitSingleAttendance(record: AttendanceRecord) {
        storageService.markSingleAttendance(record)
        syncService.syncAttendanceToFirestore(record)
        _attendance.value = storageService.getAttendance()
    }

    // --- Timetable Management ---
    fun addTimetable(branch: String, day: String, period: Int, subject: String, timeSlot: String): Pair<Boolean, String> {
        if (branch.isEmpty() || day.isEmpty() || subject.trim().isEmpty() || timeSlot.trim().isEmpty()) {
            return Pair(false, "All fields are required")
        }
        val entry = TimetableEntry(
            branch = branch,
            day = day,
            period = period,
            subject = subject.trim(),
            timeSlot = timeSlot.trim()
        )
        storageService.addTimetableEntry(entry)
        syncService.syncTimetableToFirestore(entry)
        _timetable.value = storageService.getTimetable()
        return Pair(true, "Timetable entry added successfully")
    }

    fun updateTimetable(updated: TimetableEntry): Pair<Boolean, String> {
        if (updated.branch.isEmpty() || updated.day.isEmpty() || updated.subject.trim().isEmpty() || updated.timeSlot.trim().isEmpty()) {
            return Pair(false, "All fields are required")
        }
        storageService.updateTimetableEntry(updated)
        syncService.syncTimetableToFirestore(updated)
        _timetable.value = storageService.getTimetable()
        return Pair(true, "Timetable entry updated successfully")
    }

    fun deleteTimetable(id: String) {
        storageService.deleteTimetableEntry(id)
        syncService.deleteTimetableFromFirestore(id)
        _timetable.value = storageService.getTimetable()
    }

    // --- Helper Metrics for Student ---
    fun getStudentAttendanceSummary(regNo: String): Map<String, SubjectStats> {
        val studentRecords = _attendance.value.filter { it.regNo.trim().equals(regNo.trim(), ignoreCase = true) }
        val subjects = studentRecords.map { it.subject }.distinct().toMutableList()
        
        // Let's add standard subjects if they have no records, to guide them
        val student = _students.value.find { it.regNo.trim().equals(regNo.trim(), ignoreCase = true) }
        val branch = student?.branch ?: "CSE"
        val branchTimetableSubjects = _timetable.value
            .filter { it.branch.equals(branch, ignoreCase = true) }
            .map { it.subject }
            .distinct()
        
        for (subj in branchTimetableSubjects) {
            if (!subjects.contains(subj)) {
                subjects.add(subj)
            }
        }

        val map = mutableMapOf<String, SubjectStats>()
        for (sub in subjects) {
            val subRecords = studentRecords.filter { it.subject.equals(sub, ignoreCase = true) }
            val total = subRecords.size
            val present = subRecords.count { it.status.equals("present", ignoreCase = true) }
            val percentage = if (total > 0) (present.toFloat() / total.toFloat() * 100f) else 0.0f
            map[sub] = SubjectStats(present = present, total = total, percentage = percentage)
        }
        return map
    }

    // --- Helper Metrics for Admin ---
    fun getBranchStudentCount(): Map<String, Int> {
        val counts = mutableMapOf<String, Int>()
        val branches = listOf("CSE", "ECE", "EEE", "MECH", "CIVIL", "IT", "OTHER")
        for (b in branches) {
            counts[b] = _students.value.count { it.branch.equals(b, ignoreCase = true) }
        }
        return counts
    }

    fun getTodayAttendanceSummary(): TodayStats {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayRecords = _attendance.value.filter { it.date == today }
        val distinctStudentsToday = todayRecords.map { it.regNo.lowercase() }.distinct().size
        val presentToday = todayRecords.filter { it.status.trim().equals("present", ignoreCase = true) }.map { it.regNo.lowercase() }.distinct().size
        return TodayStats(
            dateString = today,
            totalMarked = distinctStudentsToday,
            presentCount = presentToday
        )
    }

    class Factory(private val context: Context, private val storageService: StorageService) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(context.applicationContext, storageService) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class SubjectStats(
    val present: Int,
    val total: Int,
    val percentage: Float
)

data class TodayStats(
    val dateString: String,
    val totalMarked: Int,
    val presentCount: Int
)
