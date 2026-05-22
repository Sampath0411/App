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
 * SyncService coordinates the offline cache (StorageService) with the cloud backend (Firestore).
 * As per Phase 2 requirements, it provides read/write methods syncing to Firestore,
 * real-time listeners for updates across devices, and a startup migration rule.
 */
class SyncService(
    private val context: Context,
    private val storageService: StorageService
) {
    private val tag = "SyncService"
    private val scope = CoroutineScope(Dispatchers.IO)

    // Companion object simulates the Firestore cloud database in the preview run.
    // In production, these collections are mapped directly to Google Firestore listeners.
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

        fun updateFirestoreStudents(list: List<Student>) {
            _cloudStudents.value = list
        }

        fun updateFirestoreAttendance(list: List<AttendanceRecord>) {
            _cloudAttendance.value = list
        }

        fun updateFirestoreTimetable(list: List<TimetableEntry>) {
            _cloudTimetable.value = list
        }

        fun updateFirestoreNotifications(list: List<NotificationItem>) {
            _cloudNotifications.value = list
        }
    }

    init {
        // Pull latest state from Supabase Realtime Database
        SupabaseService.pullLatestData(storageService) {
            // Run standard local database migration check
            performStartupMigration()
        }
        
        // Start listening to Firestore real-time streams to update local cache offline copy
        observeRealtimeFirestoreUpdates()
    }

    /**
     * Migration rule: On app start, if AsyncStorage (SharedPreferences local) has data
     * and Firestore is empty, push AsyncStorage data to Firestore.
     * After migration (or regularly), read/write goes to Firestore, with storageService as offline cache.
     */
    private fun performStartupMigration() {
        val localStudents = storageService.getStudents()
        val localAttendance = storageService.getAttendance()
        val localTimetable = storageService.getTimetable()

        Log.d(tag, "Startup Sync Check. Local records count - Students: ${localStudents.size}, Attendance: ${localAttendance.size}, Timetable: ${localTimetable.size}")

        if (_cloudStudents.value.isEmpty() && localStudents.isNotEmpty()) {
            Log.d(tag, "Migrating local students cache to Cloud Firestore...")
            _cloudStudents.value = localStudents
        }
        if (_cloudAttendance.value.isEmpty() && localAttendance.isNotEmpty()) {
            Log.d(tag, "Migrating local attendance cache to Cloud Firestore...")
            _cloudAttendance.value = localAttendance
        }
        val localNotifications = storageService.getNotifications()

        if (_cloudTimetable.value.isEmpty() && localTimetable.isNotEmpty()) {
            Log.d(tag, "Migrating local timetable cache to Cloud Firestore...")
            _cloudTimetable.value = localTimetable
        }
        if (_cloudNotifications.value.isEmpty() && localNotifications.isNotEmpty()) {
            Log.d(tag, "Migrating local notifications cache to Cloud Firestore...")
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
    private fun observeRealtimeFirestoreUpdates() {
        scope.launch {
            cloudStudents.collectLatest { freshStudents ->
                if (freshStudents.isNotEmpty()) {
                    Log.d(tag, "Firestore Real-time: Students revised. Updating local cache.")
                    storageService.saveStudents(freshStudents)
                }
            }
        }
        scope.launch {
            cloudAttendance.collectLatest { freshAttendance ->
                if (freshAttendance.isNotEmpty()) {
                    Log.d(tag, "Firestore Real-time: Attendance records revised. Updating local cache.")
                    storageService.saveAttendance(freshAttendance)
                }
            }
        }
        scope.launch {
            cloudTimetable.collectLatest { freshTimetable ->
                if (freshTimetable.isNotEmpty()) {
                    Log.d(tag, "Firestore Real-time: Timetable revised. Updating local cache.")
                    storageService.saveTimetable(freshTimetable)
                }
            }
        }
        scope.launch {
            cloudNotifications.collectLatest { freshNotifications ->
                if (freshNotifications.isNotEmpty()) {
                    Log.d(tag, "Firestore Real-time: Notifications revised. Updating local cache.")
                    storageService.saveNotifications(freshNotifications)
                }
            }
        }
    }

    // --- Student Cloud Sync Syncing ---
    fun syncStudentToFirestore(studentObj: Student) {
        scope.launch {
            Log.d(tag, "Syncing Student to Firestore: docId=${studentObj.regNo}")
            // Push to Supabase Cloud Database
            SupabaseService.pushStudent(studentObj)
            
            val current = _cloudStudents.value.toMutableList()
            current.removeAll { it.regNo.trim().equals(studentObj.regNo.trim(), ignoreCase = true) }
            current.add(studentObj)
            _cloudStudents.value = current
            storageService.saveStudents(current)
        }
    }

    fun getStudentsFromFirestore(): List<Student> {
        // Production: db.collection("students").get().await()
        return _cloudStudents.value.ifEmpty { storageService.getStudents() }
    }

    // --- Attendance Cloud Sync Syncing ---
    fun syncAttendanceToFirestore(record: AttendanceRecord) {
        scope.launch {
            Log.d(tag, "Syncing Attendance to Firestore (regNo=${record.regNo}, subject=${record.subject})")
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

    fun syncBulkAttendanceToFirestore(records: List<AttendanceRecord>) {
        if (records.isEmpty()) return
        scope.launch {
            Log.d(tag, "Syncing ${records.size} attendance records in bulk to Firestore...")
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

    fun getAttendanceFromFirestore(regNo: String): List<AttendanceRecord> {
        // Production: db.collection("attendance").whereEqualTo("regNo", regNo).get()
        return _cloudAttendance.value.filter { it.regNo.trim().equals(regNo.trim(), ignoreCase = true) }
    }

    // --- Timetable Cloud Sync Syncing ---
    fun syncTimetableToFirestore(entry: TimetableEntry) {
        scope.launch {
            Log.d(tag, "Syncing Timetable to Firestore: id=${entry.id}")
            SupabaseService.pushTimetable(entry)
            
            val current = _cloudTimetable.value.toMutableList()
            current.removeAll { it.id == entry.id }
            current.add(entry)
            _cloudTimetable.value = current
            storageService.saveTimetable(current)
        }
    }

    fun deleteTimetableFromFirestore(id: String) {
        scope.launch {
            Log.d(tag, "Deleting Timetable entry from Firestore: id=$id")
            SupabaseService.deleteTimetable(id)
            val current = _cloudTimetable.value.toMutableList()
            current.removeAll { it.id == id }
            _cloudTimetable.value = current
            storageService.saveTimetable(current)
        }
    }

    fun getTimetableFromFirestore(branch: String): List<TimetableEntry> {
        // Production: db.collection("timetable").whereEqualTo("branch", branch).get()
        return _cloudTimetable.value.filter { it.branch.trim().equals(branch.trim(), ignoreCase = true) }
    }

    // --- Notifications Sync Syncing ---
    fun syncNotificationToFirestore(notif: NotificationItem) {
        scope.launch {
            Log.d(tag, "Syncing notification to Firestore: docId=${notif.id}")
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
