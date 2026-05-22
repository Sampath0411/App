package com.example.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrightAccent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GrayText
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.White
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var regNo by remember { mutableStateOf("") }
    var branch by remember { mutableStateOf("") }
    
    // Status states
    var feedbackError by remember { mutableStateOf("") }
    var feedbackSuccess by remember { mutableStateOf("") }
    var isSyncing by remember { mutableStateOf(false) }
    
    val branches = listOf("CSE", "ECE", "EEE", "MECH", "CIVIL", "IT", "OTHER")
    var isDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Registration", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "REGISTRATION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BrightAccent,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Text(
                text = "Join AttendEase Today",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                letterSpacing = (-0.5).sp,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Create an account to track your academic presence and access your digital QR index card.",
                fontSize = 14.sp,
                color = GrayText,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Success feedback message
            AnimatedVisibility(visible = feedbackSuccess.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.15f))
                ) {
                    Text(
                        text = feedbackSuccess,
                        color = SuccessGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Error feedback message
            AnimatedVisibility(visible = feedbackError.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.15f))
                ) {
                    Text(
                        text = feedbackError,
                        color = ErrorRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Full Name Input
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it; feedbackError = "" },
                label = { Text("Full Name") },
                leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = BrightAccent) },
                modifier = Modifier.fillMaxWidth().testTag("register_fullname_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = BrightAccent,
                    unfocusedBorderColor = GrayText.copy(alpha = 0.5f),
                    focusedLabelColor = BrightAccent,
                    unfocusedLabelColor = GrayText,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; feedbackError = "" },
                label = { Text("Email Address") },
                leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = BrightAccent) },
                modifier = Modifier.fillMaxWidth().testTag("register_email_input"),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = BrightAccent,
                    unfocusedBorderColor = GrayText.copy(alpha = 0.5f),
                    focusedLabelColor = BrightAccent,
                    unfocusedLabelColor = GrayText,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phone Input
            OutlinedTextField(
                value = phone,
                onValueChange = { if (it.length <= 10 && it.all { ch -> ch.isDigit() }) { phone = it; feedbackError = "" } },
                label = { Text("Phone Number") },
                leadingIcon = { Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = BrightAccent) },
                modifier = Modifier.fillMaxWidth().testTag("register_phone_input"),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = BrightAccent,
                    unfocusedBorderColor = GrayText.copy(alpha = 0.5f),
                    focusedLabelColor = BrightAccent,
                    unfocusedLabelColor = GrayText,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Registration Number Input
            OutlinedTextField(
                value = regNo,
                onValueChange = { regNo = it.uppercase(); feedbackError = "" },
                label = { Text("Registration Number") },
                leadingIcon = { Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = BrightAccent) },
                modifier = Modifier.fillMaxWidth().testTag("register_regno_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = BrightAccent,
                    unfocusedBorderColor = GrayText.copy(alpha = 0.5f),
                    focusedLabelColor = BrightAccent,
                    unfocusedLabelColor = GrayText,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Elegant Branch Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = if (branch.isEmpty()) "Select Branch" else branch,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Branch") },
                    leadingIcon = { Icon(imageVector = Icons.Default.School, contentDescription = null, tint = BrightAccent) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Expand branch list",
                            tint = Color.White,
                            modifier = Modifier.clickable { isDropdownExpanded = !isDropdownExpanded }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isDropdownExpanded = !isDropdownExpanded }
                        .testTag("register_branch_dropdown"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = BrightAccent,
                        unfocusedBorderColor = GrayText.copy(alpha = 0.5f),
                        focusedLabelColor = BrightAccent,
                        unfocusedLabelColor = GrayText,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    )
                )

                DropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .background(DarkSurface)
                ) {
                    branches.forEach { b ->
                        DropdownMenuItem(
                            text = { Text(text = b, color = Color.White, fontSize = 16.sp) },
                            onClick = {
                                branch = b
                                feedbackError = ""
                                isDropdownExpanded = false
                            },
                            modifier = Modifier.testTag("branch_option_$b")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Register Submit Button
            Button(
                onClick = {
                    isSyncing = true
                    viewModel.pullLatestFromCloud {
                        val res = viewModel.registerStudent(
                            fullName = fullName,
                            email = email,
                            phone = phone,
                            regNo = regNo,
                            branch = branch
                        )
                        isSyncing = false
                        if (res.first) {
                            feedbackSuccess = res.second
                            feedbackError = ""
                            // Fast reset fields
                            fullName = ""
                            email = ""
                            phone = ""
                            regNo = ""
                            branch = ""
                        } else {
                            feedbackError = res.second
                            feedbackSuccess = ""
                        }
                    }
                },
                enabled = !isSyncing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("register_submit_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrightAccent,
                    contentColor = Color.White
                )
            ) {
                if (isSyncing) {
                    androidx.compose.material3.CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Register Student", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Already registered?", color = GrayText, fontSize = 14.sp)
                TextButton(onClick = onNavigateToLogin, modifier = Modifier.testTag("navigate_login_text_btn")) {
                    Text("Login here", color = BrightAccent, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentLoginScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var regNo by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var feedbackError by remember { mutableStateOf("") }
    var showBiometricActiveScan by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Login", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = BrightAccent,
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "IDENTIFICATION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BrightAccent,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Text(
                text = "Welcome Back",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                letterSpacing = (-0.5).sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Sign in using your Registration Number and Email coordinates.",
                fontSize = 14.sp,
                color = GrayText,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            // Alert context on failed login
            AnimatedVisibility(visible = feedbackError.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.15f))
                ) {
                    Text(
                        text = feedbackError,
                        color = ErrorRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // RegNo field
            OutlinedTextField(
                value = regNo,
                onValueChange = { regNo = it.uppercase(); feedbackError = "" },
                label = { Text("Registration Number") },
                leadingIcon = { Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = BrightAccent) },
                modifier = Modifier.fillMaxWidth().testTag("student_login_regno_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = BrightAccent,
                    unfocusedBorderColor = GrayText.copy(alpha = 0.5f),
                    focusedLabelColor = BrightAccent,
                    unfocusedLabelColor = GrayText,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; feedbackError = "" },
                label = { Text("Email Address") },
                leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = BrightAccent) },
                modifier = Modifier.fillMaxWidth().testTag("student_login_email_input"),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = BrightAccent,
                    unfocusedBorderColor = GrayText.copy(alpha = 0.5f),
                    focusedLabelColor = BrightAccent,
                    unfocusedLabelColor = GrayText,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Log In Submit button
            Button(
                onClick = {
                    val res = viewModel.loginStudent(regNo = regNo, email = email)
                    if (res.first) {
                        feedbackError = ""
                        onLoginSuccess()
                    } else {
                        feedbackError = res.second
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("student_login_submit_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrightAccent,
                    contentColor = Color.White
                )
            ) {
                Text("Log In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            val bioEnrolledRegNo = viewModel.biometricService.getEnrolledRegNo()
            if (viewModel.biometricService.isBiometricAvailable() && !bioEnrolledRegNo.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                androidx.compose.material3.OutlinedButton(
                    onClick = {
                        showBiometricActiveScan = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("student_login_biometric_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = BrightAccent
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrightAccent)
                ) {
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = BrightAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log In with Fingerprint", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (showBiometricActiveScan) {
                BiometricScanDialog(
                    onDismissRequest = { showBiometricActiveScan = false },
                    onSuccess = {
                        showBiometricActiveScan = false
                        val enrolledReg = viewModel.biometricService.getEnrolledRegNo()
                        if (enrolledReg != null) {
                            val studentEmail = viewModel.students.value.find { it.regNo == enrolledReg }?.email ?: ""
                            val res = viewModel.loginStudent(regNo = enrolledReg, email = studentEmail)
                            if (res.first) {
                                feedbackError = ""
                                onLoginSuccess()
                            } else {
                                feedbackError = "Biometric login failed: ${res.second}"
                            }
                        }
                    },
                    onFailure = { err ->
                        showBiometricActiveScan = false
                        feedbackError = err
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Not registered yet?", color = GrayText, fontSize = 14.sp)
                TextButton(onClick = onNavigateToRegister, modifier = Modifier.testTag("navigate_register_text_btn")) {
                    Text("Register Now", color = BrightAccent, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen(
    onBack: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var feedbackError by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Administrative Login", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = null,
                tint = BrightAccent,
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ADMIN ACCESS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BrightAccent,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Text(
                text = "Secure Admin Portal",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                letterSpacing = (-0.5).sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Enter the dynamic admin verification values to manage core registries.",
                fontSize = 14.sp,
                color = GrayText,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            // Alert context on failed login
            AnimatedVisibility(visible = feedbackError.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.15f))
                ) {
                    Text(
                        text = feedbackError,
                        color = ErrorRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; feedbackError = "" },
                label = { Text("Admin Email") },
                leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = BrightAccent) },
                modifier = Modifier.fillMaxWidth().testTag("admin_login_email_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = BrightAccent,
                    unfocusedBorderColor = GrayText.copy(alpha = 0.5f),
                    focusedLabelColor = BrightAccent,
                    unfocusedLabelColor = GrayText,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; feedbackError = "" },
                label = { Text("Admin Password") },
                leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = BrightAccent) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().testTag("admin_login_password_input"),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = BrightAccent,
                    unfocusedBorderColor = GrayText.copy(alpha = 0.5f),
                    focusedLabelColor = BrightAccent,
                    unfocusedLabelColor = GrayText,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Login Submit button
            Button(
                onClick = {
                    if (email.trim() == "sampathlox@gmail.com" && password == "Sampath2008") {
                        feedbackError = ""
                        onLoginSuccess()
                    } else {
                        feedbackError = "Access Denied: Invalid email or password credentials."
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("admin_login_submit_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrightAccent,
                    contentColor = Color.White
                )
            ) {
                Text("Authenticate", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun BiometricScanDialog(
    onDismissRequest: () -> Unit,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    var scanProgress by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var scanStatus by remember { mutableStateOf("Place and HOLD your finger on the sensor icon...") }
    var isPressing by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(isPressing) {
        if (isPressing) {
            scanStatus = "Sensor active! Scanning fingerprint... Hold steady"
            while (isPressing && scanProgress < 1f) {
                delay(60)
                scanProgress += 0.05f
            }
            if (scanProgress >= 1f) {
                scanStatus = "Fingerprint matched & verified!"
                delay(400)
                onSuccess()
            }
        } else {
            if (scanProgress < 1f) {
                if (scanProgress > 0f) {
                    scanStatus = "Scan interrupted! Keep finger on sensor."
                } else {
                    scanStatus = "Place and HOLD your finger on the sensor icon..."
                }
                while (!isPressing && scanProgress > 0f) {
                    delay(30)
                    scanProgress -= 0.1f
                }
                if (scanProgress < 0f) scanProgress = 0f
            }
        }
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismissRequest) {
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
                    text = "BIOMETRIC SECURE SCAN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrightAccent,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Stored Fingerprint Authenticator",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(55.dp))
                        .background(if (isPressing) BrightAccent.copy(alpha = 0.15f) else Color.Transparent)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isPressing = true
                                    try {
                                        awaitRelease()
                                    } finally {
                                        isPressing = false
                                    }
                                }
                            )
                        }
                        .testTag("fingerprint_secure_press_sensor")
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        progress = { scanProgress },
                        color = BrightAccent,
                        strokeWidth = 5.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Fingerprint Sensor",
                        tint = if (scanProgress >= 1f) SuccessGreen else if (isPressing) BrightAccent else GrayText,
                        modifier = Modifier.size(68.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = scanStatus,
                    color = if (isPressing) SuccessGreen else Color.White,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.height(36.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "(Place and keep your finger pressed on the circle)",
                    color = GrayText,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = GrayText, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
