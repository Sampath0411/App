package com.example.ui.admin

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.example.data.AttendanceRecord
import com.example.data.Student
import com.example.ui.MainViewModel
import com.example.ui.theme.BrightAccent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GrayText
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.White
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AdminMarkAttendance(
    viewModel: MainViewModel
) {
    var activeTab by remember { mutableStateOf(0) } // 0 = Manual, 1 = QR Scan
    val tabs = listOf("Manual Roster", "QR Code Scan")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "Record Attendance",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = White
            )
            Text(
                text = "Use structural branch templates or scan QR codes to mark classroom schedules.",
                fontSize = 14.sp,
                color = GrayText,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )
        }

        TabRow(
            selectedTabIndex = activeTab,
            containerColor = DarkBackground,
            contentColor = BrightAccent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = BrightAccent
                )
            },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            tabs.forEachIndexed { idx, label ->
                Tab(
                    selected = activeTab == idx,
                    onClick = { activeTab = idx },
                    text = {
                        Text(
                            text = label,
                            fontSize = 14.sp,
                            fontWeight = if (activeTab == idx) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    modifier = Modifier.testTag("attendance_tab_$idx")
                )
            }
        }

        if (activeTab == 0) {
            ManualAttendanceMode(viewModel = viewModel)
        } else {
            QrScanAttendanceMode(viewModel = viewModel)
        }
    }
}

// --- MODE A: MANUAL ATTENDANCE ---
@Composable
fun ManualAttendanceMode(viewModel: MainViewModel) {
    val students by viewModel.students.collectAsState()
    val fullTimetable by viewModel.timetable.collectAsState()

    val branches = listOf("CSE", "ECE", "EEE", "MECH", "CIVIL", "IT", "OTHER")
    var selectedBranch by remember { mutableStateOf("CSE") }
    var selectBranchExpanded by remember { mutableStateOf(false) }

    // Dynamic subjects based on branch timetable, or standard defaults
    val dynamicSubjects = fullTimetable
        .filter { it.branch.equals(selectedBranch, ignoreCase = true) }
        .map { it.subject }
        .distinct()
        .toMutableList()
    
    if (dynamicSubjects.isEmpty()) {
        dynamicSubjects.addAll(listOf("Software Eng", "Compiler Design", "Database Systems", "Computer Networks", "AI & ML"))
    }

    var selectedSubject by remember { mutableStateOf(dynamicSubjects.firstOrNull() ?: "Software Eng") }
    // Sync subject when branch changes
    remember(selectedBranch) {
        selectedSubject = dynamicSubjects.firstOrNull() ?: "Software Eng"
    }
    var selectSubjectExpanded by remember { mutableStateOf(false) }

    // Pre-populate with today's date formatted as YYYY-MM-DD
    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    var selectedDate by remember { mutableStateOf(todayDate) }

    // Students in the selected branch
    val branchStudents = students.filter { it.branch.equals(selectedBranch, ignoreCase = true) }

    // Present map (Map of Student Reg No -> Boolean representing present/absent)
    val presentStates = remember { mutableStateMapOf<String, Boolean>() }
    // Initialize present states to True for newly filtered students if they are map-empty
    remember(selectedBranch) {
        presentStates.clear()
        branchStudents.forEach { presentStates[it.regNo] = true }
    }

    var transactionResult by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        // Dropdown Pickers Grid
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Branch Selector
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Target Branch", color = GrayText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkBackground)
                                .clickable { selectBranchExpanded = true }
                                .padding(12.dp)
                                .testTag("manual_branch_select"),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(text = selectedBranch, color = Color.White, fontWeight = FontWeight.Bold)
                            DropdownMenu(
                                expanded = selectBranchExpanded,
                                onDismissRequest = { selectBranchExpanded = false },
                                modifier = Modifier.background(DarkSurface)
                            ) {
                                branches.forEach { b ->
                                    DropdownMenuItem(
                                        text = { Text(b, color = Color.White) },
                                        onClick = {
                                            selectedBranch = b
                                            selectBranchExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Subject Selector
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text("Subject Course", color = GrayText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkBackground)
                                .clickable { selectSubjectExpanded = true }
                                .padding(12.dp)
                                .testTag("manual_subject_select"),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(text = selectedSubject, color = Color.White, fontWeight = FontWeight.Bold)
                            DropdownMenu(
                                expanded = selectSubjectExpanded,
                                onDismissRequest = { selectSubjectExpanded = false },
                                modifier = Modifier.background(DarkSurface)
                            ) {
                                dynamicSubjects.forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text(s, color = Color.White) },
                                        onClick = {
                                            selectedSubject = s
                                            selectSubjectExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Date Picker Input field
                Text("Select Date (YYYY-MM-DD)", color = GrayText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = { selectedDate = it },
                    singleLine = true,
                    leadingIcon = { Icon(imageVector = Icons.Default.DateRange, contentDescription = null, tint = BrightAccent) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manual_date_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = BrightAccent, unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = DarkBackground, unfocusedContainerColor = DarkBackground
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transaction notification feedback
        AnimatedVisibility(visible = transactionResult.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.15f))
            ) {
                Text(
                    text = transactionResult,
                    color = SuccessGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Student Roster
        Text(
            text = "Students List • Select Status",
            color = White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (branchStudents.isEmpty()) {
                item {
                    Text(
                        text = "No registered students found in branch $selectedBranch.\nRegister students to mark rosters.",
                        color = GrayText,
                        modifier = Modifier.padding(vertical = 32.dp).fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                items(branchStudents) { student ->
                    val isChecked = presentStates[student.regNo] ?: true
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurface)
                            .clickable { presentStates[student.regNo] = !isChecked }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = student.fullName, color = White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = "ID: ${student.regNo}", color = GrayText, fontSize = 12.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isChecked) "PRESENT" else "ABSENT",
                                color = if (isChecked) SuccessGreen else ErrorRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { presentStates[student.regNo] = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = SuccessGreen,
                                    uncheckedColor = ErrorRed,
                                    checkmarkColor = Color.White
                                ),
                                modifier = Modifier.testTag("attendance_checkbox_${student.regNo}")
                            )
                        }
                    }
                }
            }
        }

        // Submit Block Button
        if (branchStudents.isNotEmpty()) {
            Button(
                onClick = {
                    val listRecords = branchStudents.map { s ->
                        AttendanceRecord(
                            regNo = s.regNo,
                            subject = selectedSubject,
                            date = selectedDate.trim(),
                            status = if (presentStates[s.regNo] == true) "present" else "absent"
                        )
                    }
                    viewModel.submitBulkAttendance(listRecords)
                    transactionResult = "Success: Marked attendance list for ${listRecords.size} students!"
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .height(52.dp)
                    .testTag("manual_attendance_submit"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrightAccent, contentColor = Color.White)
            ) {
                Icon(imageVector = Icons.Default.AssignmentTurnedIn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Process Attendance", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

// --- MODE B: AUTOMATIC QR SCROLL SCANNING ---
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QrScanAttendanceMode(viewModel: MainViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    val students by viewModel.students.collectAsState()
    val fullTimetable by viewModel.timetable.collectAsState()

    var scannedRegNo by remember { mutableStateOf("") }
    var confirmedStudent by remember { mutableStateOf<Student?>(null) }

    // Prepopulate subjects and date
    val defaultSubjectList = fullTimetable.map { it.subject }.distinct().toMutableList()
    if (defaultSubjectList.isEmpty()) {
        defaultSubjectList.addAll(listOf("Software Eng", "Compiler Design", "Database Systems", "Computer Networks", "AI & ML"))
    }
    
    var selectedSubject by remember { mutableStateOf(defaultSubjectList.firstOrNull() ?: "Software Eng") }
    var selectSubjectExpanded by remember { mutableStateOf(false) }

    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    var selectedDate by remember { mutableStateOf(todayStr) }

    var scanResultNotification by remember { mutableStateOf("") }

    var isCameraActive by remember { mutableStateOf(false) }
    var cameraErrorMsg by remember { mutableStateOf("") }

    // Resolve details once scannedRegNo updates
    remember(scannedRegNo) {
        if (scannedRegNo.isNotEmpty()) {
            val s = students.find { it.regNo.trim().equals(scannedRegNo.trim(), ignoreCase = true) }
            confirmedStudent = s
            if (s == null) {
                scanResultNotification = "Error: Scanned registration barcode '$scannedRegNo' is not found in Student records!"
            } else {
                scanResultNotification = ""
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Permission Gate
        if (!cameraPermissionState.status.isGranted) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = BrightAccent, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Classroom Scanning Mode", fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Camera access permission is required to analyze QR Codes on the fly.",
                        color = GrayText,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { cameraPermissionState.launchPermissionRequest() },
                        colors = ButtonDefaults.buttonColors(containerColor = BrightAccent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Grant Permission", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Camera scanner frame
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("admin_camera_camera_card"),
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (cameraErrorMsg.isNotEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cameraErrorMsg,
                                color = ErrorRed,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else if (!isCameraActive) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Camera Info",
                                    tint = BrightAccent.copy(alpha = 0.6f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Camera scanner ready",
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        cameraErrorMsg = ""
                                        isCameraActive = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrightAccent),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(36.dp).padding(horizontal = 16.dp)
                                ) {
                                    Text("Start Live Camera Feed", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // Render CameraPreviewView
                        AndroidView(
                            factory = { ctx ->
                                val pView = PreviewView(ctx)
                                try {
                                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                    cameraProviderFuture.addListener({
                                        try {
                                            val cameraProvider = cameraProviderFuture.get()
                                            val cameraPreview = Preview.Builder().build().also {
                                                it.surfaceProvider = pView.surfaceProvider
                                            }
                                            val imageAnalysis = ImageAnalysis.Builder()
                                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                                .build()
                                            
                                            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(ctx), QrCodeDecoder { qrText ->
                                                if (qrText.isNotEmpty() && scannedRegNo != qrText) {
                                                    scannedRegNo = qrText
                                                }
                                            })

                                            cameraProvider.unbindAll()
                                            cameraProvider.bindToLifecycle(
                                                lifecycleOwner,
                                                CameraSelector.DEFAULT_BACK_CAMERA,
                                                cameraPreview,
                                                imageAnalysis
                                            )
                                        } catch (exc: Exception) {
                                            Log.e("AdminMarkAttendance", "Camera binding or lifecycle failed", exc)
                                            cameraErrorMsg = "Camera initialization failed: ${exc.localizedMessage ?: "hardware block"}.\nPlease use the Emulator Sandbox generator below."
                                            isCameraActive = false
                                        }
                                    }, ContextCompat.getMainExecutor(ctx))
                                } catch (e: Exception) {
                                    Log.e("AdminMarkAttendance", "ProcessCameraProvider failed", e)
                                    cameraErrorMsg = "Camera provider failure: ${e.localizedMessage}.\nPlease use the Emulator Sandbox generator below."
                                    isCameraActive = false
                                }
                                pView
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Target finder border overlay
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .border(width = 3.dp, color = BrightAccent, shape = RoundedCornerShape(16.dp))
                                .align(Alignment.Center)
                        )

                        // Finder Label
                        Text(
                            text = "Center the Student's QR index card here",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sandbox/Emulator QR Simulation Simulator tool - Ultra high quality UX inclusion
        Card(
            modifier = Modifier.fillMaxWidth().testTag("simulator_card_container"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, tint = BrightAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("QR Scanner Emulator Sandbox", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "If you're testing on a web controller or virtual sandbox without a physical barcode scanner card, simulate scans instantly:",
                    fontSize = 12.sp,
                    color = GrayText
                )
                Spacer(modifier = Modifier.height(12.dp))

                var simulateMenuExpanded by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkBackground)
                            .clickable { simulateMenuExpanded = true }
                            .padding(12.dp)
                            .testTag("simulator_pick_student_dropdown"),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = if (students.isEmpty()) "No students registered to simulate" else "Choose a registered student",
                            color = BrightAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        DropdownMenu(
                            expanded = simulateMenuExpanded,
                            onDismissRequest = { simulateMenuExpanded = false },
                            modifier = Modifier.background(DarkSurface)
                        ) {
                            students.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text("${s.fullName} (${s.regNo})", color = Color.White) },
                                    onClick = {
                                        scannedRegNo = s.regNo
                                        simulateMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transaction/Scan Feedback
        AnimatedVisibility(visible = scanResultNotification.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.15f))
            ) {
                Text(
                    text = scanResultNotification,
                    color = ErrorRed,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // SCANNED ENTRY PANEL CONFIRMATION
        if (scannedRegNo.isNotEmpty() && confirmedStudent != null) {
            val studentDetails = confirmedStudent!!
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_scanner_confirmation_panel"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Scanned Core Credentials",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(text = "Name: ${studentDetails.fullName}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Reg No: ${studentDetails.regNo}", color = GrayText, fontSize = 13.sp)
                    Text(text = "Branch: ${studentDetails.branch}", color = GrayText, fontSize = 13.sp)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Subject Selector
                    Text("Select Subject for Attendance", color = GrayText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkBackground)
                            .clickable { selectSubjectExpanded = true }
                            .padding(12.dp)
                            .testTag("qr_scan_course_dropdown"),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(text = selectedSubject, color = Color.White, fontWeight = FontWeight.Bold)
                        DropdownMenu(
                            expanded = selectSubjectExpanded,
                            onDismissRequest = { selectSubjectExpanded = false },
                            modifier = Modifier.background(DarkSurface)
                        ) {
                            defaultSubjectList.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s, color = Color.White) },
                                    onClick = {
                                        selectedSubject = s
                                        selectSubjectExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Date selector input field
                    Text("Select Date (YYYY-MM-DD)", color = GrayText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = selectedDate,
                        onValueChange = { selectedDate = it },
                        singleLine = true,
                        leadingIcon = { Icon(imageVector = Icons.Default.DateRange, contentDescription = null, tint = BrightAccent) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("qr_scan_date_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = BrightAccent, unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = DarkBackground, unfocusedContainerColor = DarkBackground
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // MARK BUTTONS ROW
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Mark Present
                        Button(
                            onClick = {
                                val record = AttendanceRecord(
                                    regNo = studentDetails.regNo,
                                    subject = selectedSubject,
                                    date = selectedDate.trim(),
                                    status = "present"
                                )
                                viewModel.submitSingleAttendance(record)
                                scannedRegNo = "" // Clear scanning
                                scanResultNotification = "Success: Marked ${studentDetails.fullName} as PRESENT!"
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("qr_scan_mark_present_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Present")
                        }

                        // Mark Absent
                        Button(
                            onClick = {
                                val record = AttendanceRecord(
                                    regNo = studentDetails.regNo,
                                    subject = selectedSubject,
                                    date = selectedDate.trim(),
                                    status = "absent"
                                )
                                viewModel.submitSingleAttendance(record)
                                scannedRegNo = "" // Clear scanning
                                scanResultNotification = "Success: Marked ${studentDetails.fullName} as ABSENT"
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("qr_scan_mark_absent_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                        ) {
                            Text("Absent")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { scannedRegNo = ""; confirmedStudent = null },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Reset / Scan Next", color = GrayText)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(130.dp))
    }
}

// --- ZXING EMBEDDED CAMERA ANALYZER IN KOTLIN ---
class QrCodeDecoder(private val onBarcodeDecoded: (String) -> Unit) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader()

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        val yBuffer = image.planes[0].buffer
        val ySize = yBuffer.remaining()
        val yArray = ByteArray(ySize)
        yBuffer.get(yArray)

        val width = image.width
        val height = image.height

        val source = PlanarYUVLuminanceSource(
            yArray, width, height, 0, 0, width, height, false
        )
        val bitmap = BinaryBitmap(HybridBinarizer(source))

        try {
            val result = reader.decode(bitmap)
            val codeText = result.text ?: ""
            if (codeText.isNotEmpty()) {
                onBarcodeDecoded(codeText)
            }
        } catch (e: Exception) {
            // ZXing throws NotFoundException if barcode is missing in frame, silent retry
        } finally {
            image.close()
        }
    }
}
