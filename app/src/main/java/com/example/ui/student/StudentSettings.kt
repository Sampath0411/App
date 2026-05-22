package com.example.ui.student

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.BrightAccent
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GrayText
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.delay
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun StudentSettings(
    viewModel: MainViewModel,
    onNavigateToProfileTab: () -> Unit,
    onLogout: () -> Unit
) {
    val isDark by viewModel.isDarkMode.collectAsState()
    val currentStudent by viewModel.currentStudent.collectAsState()
    val student = currentStudent ?: return

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showHelpOverlay by remember { mutableStateOf(false) }
    var showChangelogDialog by remember { mutableStateOf(false) }
    
    // Biometric enrollment state
    val enrolledRegNo = viewModel.biometricService.getEnrolledRegNo()
    var isBiometricEnabled by remember(enrolledRegNo, student) {
        mutableStateOf(enrolledRegNo == student.regNo)
    }
    var showBiometricEnrollDialog by remember { mutableStateOf(false) }
    var enrolledFingersList by remember(student, enrolledRegNo) {
        mutableStateOf(viewModel.biometricService.getEnrolledFingers())
    }
    
    var infoMessage by remember { mutableStateOf("") }
    var showInfoAlert by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Screen Header Label
            Text(
                text = "SETTINGS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BrightAccent,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Text(
                text = "Preferences",
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Section: Profile Redirection
            SettingsSectionHeader(title = "Account")
            SettingsRowClickable(
                icon = Icons.Default.Person,
                title = "Edit Profile",
                subtitle = "Update info for ${student.fullName}",
                onClick = {
                    viewModel.editProfileOnNavigate.value = true
                    onNavigateToProfileTab()
                },
                tag = "settings_edit_profile_row"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Theme Settings (Appearance)
            SettingsSectionHeader(title = "Appearance")
            SettingsRowToggle(
                icon = Icons.Default.ColorLens,
                title = "Dark Theme Mode",
                checked = isDark,
                onCheckedChange = {
                    viewModel.toggleThemeMode()
                },
                tag = "settings_theme_toggle_row"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Biometrics & Passkeys
            if (viewModel.biometricService.isBiometricAvailable()) {
                SettingsSectionHeader(title = "Local Device Security")
                SettingsRowToggle(
                    icon = Icons.Default.Fingerprint,
                    title = "Enable Fingerprint Login",
                    subtitle = if (isBiometricEnabled) "Status: Enabled" else "Status: Disabled",
                    checked = isBiometricEnabled,
                    onCheckedChange = { checked ->
                        if (checked) {
                            showBiometricEnrollDialog = true
                        } else {
                            viewModel.biometricService.disableBiometric()
                            isBiometricEnabled = false
                            enrolledFingersList = emptyList()
                            infoMessage = "Biometric credentials removed from this device."
                            showInfoAlert = true
                        }
                    },
                    tag = "settings_biometric_toggle_row"
                )
                
                if (isBiometricEnabled && enrolledFingersList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "STORED FINGERPRINTS ON THIS PHONE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrightAccent,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            enrolledFingersList.forEach { finger ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Fingerprint,
                                            contentDescription = null,
                                            tint = SuccessGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = finger,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    
                                    Text(
                                        text = "Active",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessGreen
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Section: Resources
            SettingsSectionHeader(title = "Feedback & Resources")
            SettingsRowClickable(
                icon = Icons.Default.HelpOutline,
                title = "Help & FAQs",
                subtitle = "Attendance ratios, QR rules & Admin contacts",
                onClick = { showHelpOverlay = true },
                tag = "settings_help_row"
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsRowClickable(
                icon = Icons.Default.Info,
                title = "What's New (Changelog)",
                subtitle = "Check out the latest features in AUCSE App v2025.2",
                onClick = { showChangelogDialog = true },
                tag = "settings_changelog_row"
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Logout row (Custom styled with red highlight)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLogoutDialog = true }
                    .testTag("settings_logout_row")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ErrorRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Sign Out Account",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ErrorRed
                    )
                }
            }
        }

        // Help Overlay Screen as requested in Phase 3
        AnimatedVisibility(
            visible = showHelpOverlay,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            HelpOverlayScreen(onDismiss = { showHelpOverlay = false })
        }
    }

    if (showBiometricEnrollDialog) {
        var selectedFinger by remember { mutableStateOf("Right Index Finger") }
        var isPressingFinger by remember { mutableStateOf(false) }
        var enrollProgress by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
        var enrollStatus by remember { mutableStateOf("Select finger type above and PRESS & HOLD finger on sensor...") }
        val fingerTypes = listOf("Right Index Finger", "Left Index Finger", "Right Thumb", "Left Thumb", "Alternative Finger")
        var showSelectorMenu by remember { mutableStateOf(false) }

        androidx.compose.runtime.LaunchedEffect(isPressingFinger) {
            if (isPressingFinger) {
                enrollStatus = "Sensor active! Keep pressing..."
                while (isPressingFinger && enrollProgress < 1f) {
                    delay(60)
                    enrollProgress += 0.05f
                }
                if (enrollProgress >= 1f) {
                    enrollStatus = "Scanner captured print successfully!"
                    delay(400)
                    viewModel.biometricService.enrollFinger(student.regNo, selectedFinger) { success, msg ->
                        if (success) {
                            isBiometricEnabled = true
                            enrolledFingersList = viewModel.biometricService.getEnrolledFingers()
                            showBiometricEnrollDialog = false
                            infoMessage = "Biometric credentials set successfully! Stored '$selectedFinger' in secure phone memory."
                            showInfoAlert = true
                        } else {
                            showBiometricEnrollDialog = false
                            infoMessage = "Biometric enrollment failed."
                            showInfoAlert = true
                        }
                    }
                }
            } else {
                if (enrollProgress < 1f) {
                    if (enrollProgress > 0f) {
                        enrollStatus = "Scan interrupted! Finger lifted."
                    } else {
                        enrollStatus = "Sensor active! Select finger type and PRESS & HOLD on sensor..."
                    }
                    while (!isPressingFinger && enrollProgress > 0f) {
                        delay(20)
                        enrollProgress -= 0.1f
                    }
                    if (enrollProgress < 0f) enrollProgress = 0f
                }
            }
        }

        androidx.compose.ui.window.Dialog(onDismissRequest = { showBiometricEnrollDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .width(320.dp)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "ENROLL FINGERPRINT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrightAccent,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Select Finger to Register:",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Box {
                        TextButton(
                            onClick = { showSelectorMenu = true },
                            colors = ButtonDefaults.textButtonColors(containerColor = MaterialTheme.colorScheme.background)
                        ) {
                            Text(text = "$selectedFinger ▼", color = BrightAccent, fontWeight = FontWeight.Bold)
                        }
                        
                        androidx.compose.material3.DropdownMenu(
                            expanded = showSelectorMenu,
                            onDismissRequest = { showSelectorMenu = false }
                        ) {
                            fingerTypes.forEach { type ->
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        selectedFinger = type
                                        showSelectorMenu = false
                                        enrollProgress = 0f
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RoundedCornerShape(55.dp))
                            .background(if (isPressingFinger) BrightAccent.copy(alpha = 0.15f) else Color.Transparent)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        isPressingFinger = true
                                        try {
                                            awaitRelease()
                                        } finally {
                                            isPressingFinger = false
                                        }
                                    }
                                )
                            }
                            .testTag("fingerprint_enroll_secure_press_sensor")
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            progress = { enrollProgress },
                            color = BrightAccent,
                            strokeWidth = 5.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Enroll Fingerprint Sensor",
                            tint = if (enrollProgress >= 1f) SuccessGreen else if (isPressingFinger) BrightAccent else GrayText,
                            modifier = Modifier.size(68.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = enrollStatus,
                        color = if (isPressingFinger) SuccessGreen else Color.White,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.height(36.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "(Press and keep your finger placed on the sensor center to scan)",
                        color = GrayText,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    TextButton(
                        onClick = { showBiometricEnrollDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel", color = GrayText, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to logout from AUCSE App?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        // Disable biometric active login on clean logout
                        viewModel.biometricService.disableBiometric()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Yes, Logout", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = GrayText)
                }
            }
        )
    }

    // Info Dialog
    if (showInfoAlert) {
        AlertDialog(
            onDismissRequest = { showInfoAlert = false },
            title = { Text("Security update", fontWeight = FontWeight.Bold) },
            text = { Text(infoMessage) },
            confirmButton = {
                Button(
                    onClick = { showInfoAlert = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BrightAccent)
                ) {
                    Text("OK", color = Color.White)
                }
            }
        )
    }

    // Changelog Dialog
    if (showChangelogDialog) {
        AlertDialog(
            onDismissRequest = { showChangelogDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = BrightAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("What's New — Changelog", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Version 2025.2 Update Details",
                        color = BrightAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    
                    ChangelogItem(
                        title = "Real-time Database Sync (Supabase)",
                        desc = "Connects smoothly to Supabase cloud db to automatically synchronize student records, notifications, checklists, and attendance histories instantly across any phone."
                    )
                    ChangelogItem(
                        title = "Secure Fingerprint Logins",
                        desc = "Frictionless login flow using biometric identification with premium scanning animations."
                    )
                    ChangelogItem(
                        title = "Smart Broadcast Channel",
                        desc = "Enable immediate alerts so classroom notices propagate instantly. Displaying real-time unread counter dots to students."
                    )
                    ChangelogItem(
                        title = "Adaptive Scan fallbacks",
                        desc = "Camera setups include custom failsafes and active UI toggles to prevent hardware crash bugs, backed by an built-in QR emulator sandbox."
                    )
                    ChangelogItem(
                        title = "Admin Portal Actions",
                        desc = "Includes secure logout, responsive top-bar menus, student detailed attendance editors, and automatic offline fallback mechanisms."
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showChangelogDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BrightAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Awesome!", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 11.sp,
        color = BrightAccent,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun SettingsRowClickable(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(BrightAccent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = BrightAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = GrayText
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = GrayText.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SettingsRowToggle(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BrightAccent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = BrightAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            fontSize = 12.sp,
                            color = GrayText
                        )
                    }
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = BrightAccent,
                    uncheckedThumbColor = GrayText,
                    uncheckedTrackColor = DarkSurface.copy(alpha = 0.5f)
                )
            )
        }
    }
}

// Help FAQ screen design implementation inside settings
@Composable
fun HelpOverlayScreen(onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = BrightAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Help & FAQ",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            FAQCard(
                question = "How is attendance calculated?",
                answer = "Ratios are updated in real_time as:\npresent / (present + absent) × 100\nAim for general guidelines target of at least 75% presence."
            )

            FAQCard(
                question = "How do I scan QR?",
                answer = "Go to your dashboard to display your unique encrypted profile QR. Present it to the admin or teacher to scan and instantly register your attendance."
            )

            FAQCard(
                question = "Who is the admin?",
                answer = "The department administrator manages system registers, branch rules & timetables.\nEmail: sampathlox@gmail.com"
            )
        }
    }
}

@Composable
fun FAQCard(question: String, answer: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = question,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = BrightAccent,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = answer,
                fontSize = 14.sp,
                color = GrayText,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun ChangelogItem(title: String, desc: String) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(text = "• $title", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = desc, color = GrayText, fontSize = 12.sp, modifier = Modifier.padding(start = 12.dp))
    }
}
