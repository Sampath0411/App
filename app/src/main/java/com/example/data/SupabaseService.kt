package com.example.data

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

object SupabaseService {
    private const val TAG = "SupabaseService"
    private val BASE_URL = com.example.BuildConfig.SUPABASE_URL
    private val API_KEY = com.example.BuildConfig.SUPABASE_KEY

    private val client = OkHttpClient()
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val listStudentAdapter = moshi.adapter<List<Student>>(
        Types.newParameterizedType(List::class.java, Student::class.java)
    )
    private val studentAdapter = moshi.adapter(Student::class.java)

    private val listAttendanceAdapter = moshi.adapter<List<AttendanceRecord>>(
        Types.newParameterizedType(List::class.java, AttendanceRecord::class.java)
    )
    private val attendanceAdapter = moshi.adapter(AttendanceRecord::class.java)

    private val listTimetableAdapter = moshi.adapter<List<TimetableEntry>>(
        Types.newParameterizedType(List::class.java, TimetableEntry::class.java)
    )
    private val timetableAdapter = moshi.adapter(TimetableEntry::class.java)

    private val listNotificationAdapter = moshi.adapter<List<NotificationItem>>(
        Types.newParameterizedType(List::class.java, NotificationItem::class.java)
    )
    private val notificationAdapter = moshi.adapter(NotificationItem::class.java)

    fun pullLatestData(storageService: StorageService, onComplete: () -> Unit = {}) {
        scope.launch {
            try {
                Log.d(TAG, "Pulling latest synchronized database state from Supabase...")
                
                // 1. Fetch Students
                fetchTable("students", listStudentAdapter)?.let { freshStudents ->
                    if (freshStudents.isNotEmpty()) {
                        val currentStudents = storageService.getStudents().toMutableList()
                        freshStudents.forEach { fs ->
                            currentStudents.removeAll { it.regNo.trim().equals(fs.regNo.trim(), ignoreCase = true) }
                            currentStudents.add(fs)
                        }
                        storageService.saveStudents(currentStudents)
                        Log.d(TAG, "Pulled ${freshStudents.size} students successfully.")
                    }
                }

                // 2. Fetch Attendance Records
                fetchTable("attendance", listAttendanceAdapter)?.let { freshAttendance ->
                    if (freshAttendance.isNotEmpty()) {
                        val currentAttendance = storageService.getAttendance().toMutableList()
                        freshAttendance.forEach { fa ->
                            currentAttendance.removeAll {
                                it.regNo.trim().equals(fa.regNo.trim(), ignoreCase = true) &&
                                it.subject.trim().equals(fa.subject.trim(), ignoreCase = true) &&
                                it.date == fa.date
                            }
                            currentAttendance.add(fa)
                        }
                        storageService.saveAttendance(currentAttendance)
                        Log.d(TAG, "Pulled ${freshAttendance.size} attendance records successfully.")
                    }
                }

                // 3. Fetch Timetable
                fetchTable("timetable", listTimetableAdapter)?.let { freshTimetable ->
                    if (freshTimetable.isNotEmpty()) {
                        val currentTimetable = storageService.getTimetable().toMutableList()
                        freshTimetable.forEach { ft ->
                            currentTimetable.removeAll { it.id == ft.id }
                            currentTimetable.add(ft)
                        }
                        storageService.saveTimetable(currentTimetable)
                        Log.d(TAG, "Pulled ${freshTimetable.size} timetable entries successfully.")
                    }
                }

                // 4. Fetch Notifications
                fetchTable("notifications", listNotificationAdapter)?.let { freshNotifications ->
                    if (freshNotifications.isNotEmpty()) {
                        val currentNotifications = storageService.getNotifications().toMutableList()
                        freshNotifications.forEach { fn ->
                            currentNotifications.removeAll { it.id == fn.id }
                            currentNotifications.add(fn)
                        }
                        storageService.saveNotifications(currentNotifications.sortedByDescending { it.timestamp })
                        Log.d(TAG, "Pulled ${freshNotifications.size} notifications successfully.")
                    }
                }

                onComplete()
            } catch (e: Exception) {
                Log.e(TAG, "Supabase connection unavailable, using local offline storage engine.", e)
                onComplete()
            }
        }
    }

    fun pushStudent(student: Student) {
        scope.launch {
            try {
                val jsonBody = studentAdapter.toJson(student)
                postRecord("students", jsonBody)
            } catch (e: Exception) {
                Log.e(TAG, "Error pushing student record: ${e.message}")
            }
        }
    }

    fun pushAttendance(record: AttendanceRecord) {
        scope.launch {
            try {
                val jsonBody = attendanceAdapter.toJson(record)
                postRecord("attendance", jsonBody)
            } catch (e: Exception) {
                Log.e(TAG, "Error pushing attendance: ${e.message}")
            }
        }
    }

    fun pushTimetable(entry: TimetableEntry) {
        scope.launch {
            try {
                val jsonBody = timetableAdapter.toJson(entry)
                postRecord("timetable", jsonBody)
            } catch (e: Exception) {
                Log.e(TAG, "Error pushing timetable: ${e.message}")
            }
        }
    }

    fun pushNotification(item: NotificationItem) {
        scope.launch {
            try {
                val jsonBody = notificationAdapter.toJson(item)
                postRecord("notifications", jsonBody)
            } catch (e: Exception) {
                Log.e(TAG, "Error pushing notification: ${e.message}")
            }
        }
    }

    fun deleteTimetable(id: String) {
        scope.launch {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL/timetable?id=eq.$id")
                    .header("apikey", API_KEY)
                    .header("Authorization", "Bearer $API_KEY")
                    .delete()
                    .build()
                client.newCall(request).execute().close()
            } catch (e: Exception) {
                Log.e(TAG, "Exception during delete: ${e.message}")
            }
        }
    }

    private fun <T> fetchTable(tableName: String, adapter: com.squareup.moshi.JsonAdapter<List<T>>): List<T>? {
        val request = Request.Builder()
            .url("$BASE_URL/$tableName?select=*")
            .header("apikey", API_KEY)
            .header("Authorization", "Bearer $API_KEY")
            .get()
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (body != null) {
                        adapter.fromJson(body)
                    } else null
                } else {
                    Log.e(TAG, "Failed to fetch table $tableName: code=${response.code}")
                    null
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network exception fetching table $tableName (offline fallback model active)", e)
            null
        }
    }

    private fun postRecord(tableName: String, jsonBody: String) {
        val body = jsonBody.toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("$BASE_URL/$tableName")
            .header("apikey", API_KEY)
            .header("Authorization", "Bearer $API_KEY")
            .header("Prefer", "resolution=merge-duplicates")
            .post(body)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Post record to $tableName failed: code=${response.code}")
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network failure posting record to $tableName", e)
        }
    }
}
