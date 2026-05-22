package com.example.ui.admin

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Student
import com.example.ui.MainViewModel
import com.example.ui.theme.BrightAccent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GrayText
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.White

@Composable
fun AdminStudentsList(
    viewModel: MainViewModel,
    onNavigateToDetail: (String) -> Unit
) {
    val students by viewModel.students.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredStudents = students.filter {
        it.fullName.contains(searchQuery, ignoreCase = true) ||
                it.regNo.contains(searchQuery, ignoreCase = true) ||
                it.branch.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "Registered Students",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = White
            )
            Text(
                text = "Manage registered accounts and profiles on the local register.",
                fontSize = 14.sp,
                color = GrayText,
                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name, ID, or branch", color = GrayText) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = BrightAccent) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_students_search_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = BrightAccent,
                    unfocusedBorderColor = GrayText.copy(alpha = 0.5f)
                ),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Students Scroll list
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filteredStudents.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Text(
                            text = if (students.isEmpty()) "No students registered yet.\nShare the sign up sheet!" else "No students match your query criteria.",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = GrayText,
                            modifier = Modifier.padding(32.dp).fillMaxWidth()
                        )
                    }
                }
            } else {
                items(filteredStudents) { student ->
                    StudentOverviewCard(student = student, onClick = { onNavigateToDetail(student.regNo) })
                }
            }

            item {
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }
}

@Composable
fun StudentOverviewCard(student: Student, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("student_row_card_${student.regNo}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle avatar initials
            val initials = student.fullName.split(" ").filter { it.isNotEmpty() }.take(2).map { it.first() }.joinToString("")
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(BrightAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials.uppercase(),
                    color = BrightAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.fullName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Reg No: ${student.regNo} • Branch: ${student.branch}",
                    fontSize = 13.sp,
                    color = GrayText
                )
            }
            
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit details",
                tint = BrightAccent.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailScreen(
    regNo: String,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val students by viewModel.students.collectAsState()
    val student = students.find { it.regNo == regNo }

    if (student == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Details missing") },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back") }
                    }
                )
            }
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(it), contentAlignment = Alignment.Center) {
                Text("Student profile not found.")
            }
        }
        return
    }

    var fullName by remember { mutableStateOf(student.fullName) }
    var email by remember { mutableStateOf(student.email) }
    var phone by remember { mutableStateOf(student.phone) }
    var branch by remember { mutableStateOf(student.branch) }
    var feedbackSuccess by remember { mutableStateOf("") }
    var feedbackError by remember { mutableStateOf("") }

    val branches = listOf("CSE", "ECE", "EEE", "MECH", "CIVIL", "IT", "OTHER")
    var isDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Student Data", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Modifying Registry File",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = White,
                modifier = Modifier.align(Alignment.Start)
            )

            Text(
                text = "Changing records for register entry: ${student.regNo}",
                fontSize = 13.sp,
                color = GrayText,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 24.dp)
            )

            AnimatedVisibility(visible = feedbackSuccess.isNotEmpty()) {
                Text(text = feedbackSuccess, color = SuccessGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            }
            AnimatedVisibility(visible = feedbackError.isNotEmpty()) {
                Text(text = feedbackError, color = ErrorRed, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            }

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = BrightAccent) },
                modifier = Modifier.fillMaxWidth().testTag("student_detail_fullname_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = BrightAccent, unfocusedBorderColor = GrayText.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = BrightAccent) },
                modifier = Modifier.fillMaxWidth().testTag("student_detail_email_input"),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = BrightAccent, unfocusedBorderColor = GrayText.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { if (it.length <= 10 && it.all { ch -> ch.isDigit() }) phone = it },
                label = { Text("Phone Number") },
                leadingIcon = { Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = BrightAccent) },
                modifier = Modifier.fillMaxWidth().testTag("student_detail_phone_input"),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = BrightAccent, unfocusedBorderColor = GrayText.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = branch,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Branch") },
                    leadingIcon = { Icon(imageVector = Icons.Default.School, contentDescription = null, tint = BrightAccent) },
                    modifier = Modifier.fillMaxWidth().testTag("student_detail_branch_dropdown"),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        IconButton(onClick = { isDropdownExpanded = !isDropdownExpanded }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit branch", tint = BrightAccent)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = BrightAccent, unfocusedBorderColor = GrayText.copy(alpha = 0.5f)
                    )
                )

                DropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.8f).background(DarkSurface)
                ) {
                    branches.forEach { b ->
                        DropdownMenuItem(
                            text = { Text(text = b, color = Color.White) },
                            onClick = {
                                branch = b
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        val res = viewModel.updateStudentByAdmin(
                            originalRegNo = student.regNo,
                            updatedStudent = Student(
                                fullName = fullName.trim(),
                                email = email.trim(),
                                phone = phone.trim(),
                                regNo = student.regNo,
                                branch = branch
                            )
                        )
                        if (res.first) {
                            feedbackSuccess = res.second
                            feedbackError = ""
                        } else {
                            feedbackError = res.second
                            feedbackSuccess = ""
                        }
                    },
                    modifier = Modifier.weight(1f).height(50.dp).testTag("student_detail_save_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Edits")
                }

                Button(
                    onClick = onBack,
                    modifier = Modifier.weight(1f).height(50.dp).testTag("student_detail_cancel_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f), contentColor = GrayText)
                ) {
                    Icon(imageVector = Icons.Default.Cancel, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cancel")
                }
            }
        }
    }
}
