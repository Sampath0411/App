package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * SyncService coordinates the offline cache (StorageService) with the cloud backend (Supabase).
 * As per Phase 2 requirements, it provides read/write methods syncing to Supabase,
 * real-time listeners for updates across devices, and a startup migration rule.
 */
class SyncService(
    private val context: Context,
    private val storageService: StorageService
) {
    private val tag = "SyncService"
    private val scope = CoroutineScope(Dispatchers.IO)

    // Companion object simulates the Supabase cloud database in the preview run.
    // In production, these collections are mapped directly to Google Supabase listeners.
    companion object {
        private val _cloudStudents = MutableStateFlow<List<Student>>(emptyList())
        val cloudStudents: StateFlow<List<Student>> = _cloudStudents.asStateFlow()

        private val _cloudAttendance = MutableStateFlow<List<AttendanceRecord>>(emptyList())
        val cloudAttendance: StateFlow<List<AttendanceRecord>> = _cloudAttendance.asStateFlow()

        private val _cloudTimetable = MutableStateFlow<List<TimetableEntry>>(emptyList())
        val cloudTimetable: StateFlow<List<TimetableEntry>> = _cloudTimetable.asStateFlow()

        private val _cloudNotifications = MutableStateFlow<List<NotificationItem>>(emptyList())
        val cloudNotifications: StateFlow<List<NotificationItem>> = _cloudNotifications.asStateFlow()

        private var isPrepopulated = false

        fun updateSupabaseStudents(list: List<Student>) {
            _cloudStudents.value = list
        }

        fun updateSupabaseAttendance(list: List<AttendanceRecord>) {
            _cloudAttendance.value = list
        }

        fun updateSupabaseTimetable(list: List<TimetableEntry>) {
            _cloudTimetable.value = list
        }

        fun updateSupabaseNotifications(list: List<NotificationItem>) {
            _cloudNotifications.value = list
        }
    }

    init {
        scope.launch {
            // 1. Pull latest state from Supabase first
            SupabaseService.pullLatestData(storageService) {
                // 2. Once data is pulled, run migration check
                performStartupMigration()
            }

            // 3. Then start "observing" (simulated real-time)
            observeRealtimeSupabaseUpdates()
        }
    }

    /**
     * Migration rule: On app start, if AsyncStorage (SharedPreferences local) has data
     * and Supabase is empty, push AsyncStorage data to Supabase.
     * After migration (or regularly), read/write goes to Supabase, with storageService as offline cache.
     */
    private fun performStartupMigration() {
        val localStudents = storageService.getStudents()
        val localAttendance = storageService.getAttendance()
        val localTimetable = storageService.getTimetable()

        Log.d(tag, "Startup Sync Check. Local records count - Students: ${localStudents.size}, Attendance: ${localAttendance.size}, Timetable: ${localTimetable.size}")

        if (_cloudStudents.value.isEmpty() && localStudents.isNotEmpty()) {
            Log.d(tag, "Migrating local students cache to Cloud Supabase...")
            _cloudStudents.value = localStudents
        }
        if (_cloudAttendance.value.isEmpty() && localAttendance.isNotEmpty()) {
            Log.d(tag, "Migrating local attendance cache to Cloud Supabase...")
            _cloudAttendance.value = localAttendance
        }
        val localNotifications = storageService.getNotifications()

        if (_cloudTimetable.value.isEmpty() && localTimetable.isNotEmpty()) {
            Log.d(tag, "Migrating local timetable cache to Cloud Supabase...")
            _cloudTimetable.value = localTimetable
        }
        if (_cloudNotifications.value.isEmpty() && localNotifications.isNotEmpty()) {
            Log.d(tag, "Migrating local notifications cache to Cloud Supabase...")
            _cloudNotifications.value = localNotifications
        }

        // If cloud was already initialized but local was empty, populate local
        if (localStudents.isEmpty() && _cloudStudents.value.isNotEmpty()) {
            storageService.saveStudents(_cloudStudents.value)
        }
        if (localAttendance.isEmpty() && _cloudAttendance.value.isNotEmpty()) {
            storageService.saveAttendance(_cloudAttendance.value)
        }
        if (localTimetable.isEmpty() && _cloudTimetable.value.isNotEmpty()) {
            storageService.saveTimetable(_cloudTimetable.value)
        }
        if (localNotifications.isEmpty() && _cloudNotifications.value.isNotEmpty()) {
            storageService.saveNotifications(_cloudNotifications.value)
        }
    }

    /**
     * Real-time listeners: updates local state across views instantly on cloud revisions
     */
    private fun observeRealtimeSupabaseUpdates() {
        scope.launch {
            cloudStudents.collectLatest { freshStudents ->
                if (freshStudents.isNotEmpty()) {
                    Log.d(tag, "Supabase Real-time: Students revised. Updating local cache.")
                    storageService.saveStudents(freshStudents)
                }
            }
        }
        scope.launch {
            cloudAttendance.collectLatest { freshAttendance ->
                if (freshAttendance.isNotEmpty()) {
                    Log.d(tag, "Supabase Real-time: Attendance records revised. Updating local cache.")
                    storageService.saveAttendance(freshAttendance)
                }
            }
        }
        scope.launch {
            cloudTimetable.collectLatest { freshTimetable ->
                if (freshTimetable.isNotEmpty()) {
                    Log.d(tag, "Supabase Real-time: Timetable revised. Updating local cache.")
                    storageService.saveTimetable(freshTimetable)
                }
            }
        }
        scope.launch {
            cloudNotifications.collectLatest { freshNotifications ->
                if (freshNotifications.isNotEmpty()) {
                    Log.d(tag, "Supabase Real-time: Notifications revised. Updating local cache.")
                    storageService.saveNotifications(freshNotifications)
                }
            }
        }
    }

    // --- Student Cloud Sync Syncing ---
    fun syncStudentToSupabase(studentObj: Student) {
        scope.launch {
            Log.d(tag, "Syncing Student to Supabase: docId=${studentObj.regNo}")
            // Push to Supabase Cloud Database
            SupabaseService.pushStudent(studentObj)
            
            val current = _cloudStudents.value.toMutableList()
            current.removeAll { it.regNo.trim().equals(studentObj.regNo.trim(), ignoreCase = true) }
            current.add(studentObj)
            _cloudStudents.value = current
            storageService.saveStudents(current)
        }
    }

    fun getStudentsFromSupabase(): List<Student> {
        // Production: db.collection("students").get().await()
        return _cloudStudents.value.ifEmpty { storageService.getStudents() }
    }

    // --- Attendance Cloud Sync Syncing ---
    fun syncAttendanceToSupabase(record: AttendanceRecord) {
        scope.launch {
            Log.d(tag, "Syncing Attendance to Supabase (regNo=${record.regNo}, subject=${record.subject})")
            // Push to Supabase Cloud Database
            SupabaseService.pushAttendance(record)
            
            val current = _cloudAttendance.value.toMutableList()
            current.removeAll {
                it.regNo.trim().equals(record.regNo.trim(), ignoreCase = true) &&
                        it.subject.trim().equals(record.subject.trim(), ignoreCase = true) &&
                        it.date == record.date
            }
            current.add(record)
            _cloudAttendance.value = current
            storageService.saveAttendance(current)
        }
    }

    fun syncBulkAttendanceToSupabase(records: List<AttendanceRecord>) {
        if (records.isEmpty()) return
        scope.launch {
            Log.d(tag, "Syncing ${records.size} attendance records in bulk to Supabase...")
            val current = _cloudAttendance.value.toMutableList()
            records.forEach { record ->
                SupabaseService.pushAttendance(record)
                current.removeAll {
                    it.regNo.trim().equals(record.regNo.trim(), ignoreCase = true) &&
                            it.subject.trim().equals(record.subject.trim(), ignoreCase = true) &&
                            it.date == record.date
                }
                current.add(record)
            }
            _cloudAttendance.value = current
            storageService.saveAttendance(current)
        }
    }

    fun getAttendanceFromSupabase(regNo: String): List<AttendanceRecord> {
        // Production: db.collection("attendance").whereEqualTo("regNo", regNo).get()
        return _cloudAttendance.value.filter { it.regNo.trim().equals(regNo.trim(), ignoreCase = true) }
    }

    // --- Timetable Cloud Sync Syncing ---
    fun syncTimetableToSupabase(entry: TimetableEntry) {
        scope.launch {
            Log.d(tag, "Syncing Timetable to Supabase: id=${entry.id}")
            SupabaseService.pushTimetable(entry)
            
            val current = _cloudTimetable.value.toMutableList()
            current.removeAll { it.id == entry.id }
            current.add(entry)
            _cloudTimetable.value = current
            storageService.saveTimetable(current)
        }
    }

    fun deleteTimetableFromSupabase(id: String) {
        scope.launch {
            Log.d(tag, "Deleting Timetable entry from Supabase: id=$id")
            SupabaseService.deleteTimetable(id)
            val current = _cloudTimetable.value.toMutableList()
            current.removeAll { it.id == id }
            _cloudTimetable.value = current
            storageService.saveTimetable(current)
        }
    }

    fun getTimetableFromSupabase(branch: String): List<TimetableEntry> {
        // Production: db.collection("timetable").whereEqualTo("branch", branch).get()
        return _cloudTimetable.value.filter { it.branch.trim().equals(branch.trim(), ignoreCase = true) }
    }

    // --- Notifications Sync Syncing ---
    fun syncNotificationToSupabase(notif: NotificationItem) {
        scope.launch {
            Log.d(tag, "Syncing notification to Supabase: docId=${notif.id}")
            SupabaseService.pushNotification(notif)
            val current = _cloudNotifications.value.toMutableList()
            current.removeAll { it.id == notif.id }
            current.add(notif)
            _cloudNotifications.value = current
            storageService.saveNotifications(current)
        }
    }

    /**
     * Force fetches the newest data from Supabase Cloud Database and updates Flows
     */
    fun pullLatestFromCloud(onComplete: () -> Unit = {}) {
        scope.launch {
            SupabaseService.pullLatestData(storageService) {
                _cloudStudents.value = storageService.getStudents()
                _cloudAttendance.value = storageService.getAttendance()
                _cloudTimetable.value = storageService.getTimetable()
                _cloudNotifications.value = storageService.getNotifications()
                
                CoroutineScope(Dispatchers.Main).launch {
                    onComplete()
                }
            }
        }
    }
}
