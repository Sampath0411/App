package com.example.ui.student

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.QRCodeImage
import com.example.ui.theme.BrightAccent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GrayText
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.White

import androidx.compose.runtime.LaunchedEffect

@Composable
fun StudentProfile(
    viewModel: MainViewModel,
    onLogout: () -> Unit
) {
    val currentStudent by viewModel.currentStudent.collectAsState()
    val student = currentStudent ?: return

    var isEditMode by remember { mutableStateOf(false) }

    val shouldEdit by viewModel.editProfileOnNavigate.collectAsState()
    LaunchedEffect(shouldEdit) {
        if (shouldEdit) {
            isEditMode = true
            viewModel.editProfileOnNavigate.value = false // Reset navigation state trigger
        }
    }
    
    // Form fields
    var fullName by remember(student) { mutableStateOf(student.fullName) }
    var email by remember(student) { mutableStateOf(student.email) }
    var phone by remember(student) { mutableStateOf(student.phone) }
    var branch by remember(student) { mutableStateOf(student.branch) }

    val branches = listOf("CSE", "ECE", "EEE", "MECH", "CIVIL", "IT", "OTHER")
    var isDropdownExpanded by remember { mutableStateOf(false) }

    var feedbackSuccess by remember { mutableStateOf("") }
    var feedbackError by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Screen Title
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "ID & BIOMETRICS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BrightAccent,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "My Digital Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = White,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Scan or present this QR code to mark your classroom attendance.",
                fontSize = 13.sp,
                color = GrayText,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Center Prominent QR Code (Constraint: size 200x200 background white) - Styled beautifully
        Box(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .testTag("profile_qr_code_container")
                .clip(RoundedCornerShape(24.dp))
                .background(DarkSurface)
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(8.dp)
            ) {
                QRCodeImage(
                    text = student.regNo,
                    sizeDp = 180,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Registration number visual tag - Neon Styled
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(BrightAccent.copy(alpha = 0.1f))
                .border(1.dp, BrightAccent.copy(alpha = 0.2f), RoundedCornerShape(100.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = null,
                    tint = BrightAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ID: ${student.regNo}",
                    color = BrightAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Details list or Edit Form card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .testTag("profile_details_card")
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Inline feedback triggers
                AnimatedVisibility(visible = feedbackSuccess.isNotEmpty()) {
                    Text(
                        text = feedbackSuccess,
                        color = SuccessGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }

                AnimatedVisibility(visible = feedbackError.isNotEmpty()) {
                    Text(
                        text = feedbackError,
                        color = ErrorRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }

                if (!isEditMode) {
                    // DISPLAY MODE
                    ProfileInfoRow(icon = Icons.Default.Person, label = "Full Name", value = student.fullName)
                    Spacer(modifier = Modifier.height(16.dp))
                    ProfileInfoRow(icon = Icons.Default.Email, label = "Email Address", value = student.email)
                    Spacer(modifier = Modifier.height(16.dp))
                    ProfileInfoRow(icon = Icons.Default.Call, label = "Phone Coordinate", value = student.phone)
                    Spacer(modifier = Modifier.height(16.dp))
                    ProfileInfoRow(icon = Icons.Default.School, label = "Enrolled Branch", value = student.branch)
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Edit trigger
                        Button(
                            onClick = { isEditMode = true; feedbackSuccess = ""; feedbackError = "" },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("profile_edit_toggle_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrightAccent)
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Info", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        // Logout button
                        Button(
                            onClick = onLogout,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("profile_logout_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f), contentColor = ErrorRed)
                        ) {
                            Text("Disconnect", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                } else {
                    // EDIT MODE FORM
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it; feedbackError = "" },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = BrightAccent) },
                        modifier = Modifier.fillMaxWidth().testTag("profile_edit_name"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BrightAccent,
                            focusedLabelColor = BrightAccent,
                            unfocusedLabelColor = GrayText,
                            focusedContainerColor = DarkBackground,
                            unfocusedContainerColor = DarkBackground
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; feedbackError = "" },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = BrightAccent) },
                        modifier = Modifier.fillMaxWidth().testTag("profile_edit_email"),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BrightAccent,
                            focusedLabelColor = BrightAccent,
                            unfocusedLabelColor = GrayText,
                            focusedContainerColor = DarkBackground,
                            unfocusedContainerColor = DarkBackground
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { if (it.length <= 10 && it.all { ch -> ch.isDigit() }) { phone = it; feedbackError = "" } },
                        label = { Text("Phone Number") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = BrightAccent) },
                        modifier = Modifier.fillMaxWidth().testTag("profile_edit_phone"),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BrightAccent,
                            focusedLabelColor = BrightAccent,
                            unfocusedLabelColor = GrayText,
                            focusedContainerColor = DarkBackground,
                            unfocusedContainerColor = DarkBackground
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Branch dropdown selectors
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = branch,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Branch") },
                            leadingIcon = { Icon(imageVector = Icons.Default.School, contentDescription = null, tint = BrightAccent) },
                            modifier = Modifier.fillMaxWidth().testTag("profile_edit_branch"),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                TextButton(onClick = { isDropdownExpanded = !isDropdownExpanded }) {
                                    Text("Change", color = BrightAccent)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = BrightAccent,
                                focusedLabelColor = BrightAccent,
                                unfocusedLabelColor = GrayText,
                                focusedContainerColor = DarkBackground,
                                unfocusedContainerColor = DarkBackground
                            )
                        )

                        DropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.75f)
                                .background(DarkSurface)
                        ) {
                            branches.forEach { b ->
                                DropdownMenuItem(
                                    text = { Text(text = b, color = Color.White) },
                                    onClick = {
                                        branch = b
                                        isDropdownExpanded = false
                                        feedbackError = ""
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // SAVE BUTTON
                        Button(
                            onClick = {
                                val res = viewModel.updateStudentProfile(
                                    fullName = fullName,
                                    email = email,
                                    phone = phone,
                                    branch = branch
                                )
                                if (res.first) {
                                    feedbackSuccess = res.second
                                    feedbackError = ""
                                    isEditMode = false
                                } else {
                                    feedbackError = res.second
                                    feedbackSuccess = ""
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("profile_save_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, contentColor = Color.White)
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save", fontWeight = FontWeight.Bold)
                        }

                        // CANCEL BUTTON
                        Button(
                            onClick = {
                                // Restore original values
                                fullName = student.fullName
                                email = student.email
                                phone = student.phone
                                branch = student.branch
                                isEditMode = false
                                feedbackSuccess = ""
                                feedbackError = ""
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("profile_cancel_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f), contentColor = GrayText)
                        ) {
                            Icon(imageVector = Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cancel", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun ProfileInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BrightAccent.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = GrayText
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = White,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
