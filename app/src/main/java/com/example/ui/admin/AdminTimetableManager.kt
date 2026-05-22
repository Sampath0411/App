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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TimetableEntry
import com.example.ui.MainViewModel
import com.example.ui.theme.BrightAccent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GrayText
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.White

@Composable
fun AdminTimetableManager(
    viewModel: MainViewModel
) {
    val timetableEntries by viewModel.timetable.collectAsState()

    // Filter selectors for viewing specific branches
    val branches = listOf("CSE", "ECE", "EEE", "MECH", "CIVIL", "IT", "OTHER")
    var selectedBranchFilter by remember { mutableStateOf("CSE") }
    var selectBranchFilterExpanded by remember { mutableStateOf(false) }

    // Forms
    var formDay by remember { mutableStateOf("Monday") }
    var formDayExpanded by remember { mutableStateOf(false) }
    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    var formPeriod by remember { mutableStateOf("1") }
    var formSubject by remember { mutableStateOf("") }
    var formTimeSlot by remember { mutableStateOf("09:00 AM - 10:00 AM") }

    // Dialog state for Editing
    var editingEntry by remember { mutableStateOf<TimetableEntry?>(null) }
    
    // Dialog state for Deleting
    var entryToDelete by remember { mutableStateOf<TimetableEntry?>(null) }

    var feedbackMsg by remember { mutableStateOf("") }
    var feedbackErrorMsg by remember { mutableStateOf("") }

    // Filter items based on selected branch
    val filteredEntries = timetableEntries
        .filter { it.branch.equals(selectedBranchFilter, ignoreCase = true) }
        .sortedWith(compareBy({ getDayIndex(it.day) }, { it.period }))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "Timetable Manager",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = White
            )
            Text(
                text = "Manage periods, assign subjects, and configure classroom calendar schedules.",
                fontSize = 14.sp,
                color = GrayText,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            // Dynamic Branch Filter list picker
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                OutlinedTextField(
                    value = "Target: $selectedBranchFilter",
                    onValueChange = {},
                    readOnly = true,
                    leadingIcon = { Icon(imageVector = Icons.Default.School, contentDescription = null, tint = BrightAccent) },
                    trailingIcon = {
                        IconButton(onClick = { selectBranchFilterExpanded = !selectBranchFilterExpanded }) {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Pick branch filter", tint = Color.White)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectBranchFilterExpanded = !selectBranchFilterExpanded }
                        .testTag("admin_timetable_branch_filter"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = BrightAccent, unfocusedBorderColor = GrayText.copy(alpha = 0.5f),
                        focusedContainerColor = DarkSurface, unfocusedContainerColor = DarkSurface
                    )
                )

                DropdownMenu(
                    expanded = selectBranchFilterExpanded,
                    onDismissRequest = { selectBranchFilterExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.85f).background(DarkSurface)
                ) {
                    branches.forEach { b ->
                        DropdownMenuItem(
                            text = { Text(b, color = Color.White) },
                            onClick = {
                                selectedBranchFilter = b
                                selectBranchFilterExpanded = false
                            }
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "Weekly slots for $selectedBranchFilter",
                    fontWeight = FontWeight.Bold,
                    color = White,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            if (filteredEntries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Text(
                            text = "No schedules listed for branch $selectedBranchFilter.\nAdd new sessions below!",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = GrayText,
                            modifier = Modifier.padding(32.dp).fillMaxWidth()
                        )
                    }
                }
            } else {
                items(filteredEntries) { entry ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("timetable_manager_slot_${entry.id}"),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(BrightAccent.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = "${entry.day} • Period ${entry.period}", color = BrightAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = entry.subject, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(text = entry.timeSlot, color = GrayText, fontSize = 12.sp)
                            }

                            Row {
                                IconButton(onClick = { editingEntry = entry }) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit entry", tint = SuccessGreen)
                                }
                                IconButton(onClick = { entryToDelete = entry }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete entry", tint = ErrorRed)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Timetable entry Panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .testTag("timetable_manager_add_card"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Schedule New Session", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(10.dp))

                AnimatedVisibility(visible = feedbackMsg.isNotEmpty()) {
                    Text(text = feedbackMsg, color = SuccessGreen, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.padding(bottom = 6.dp))
                }
                AnimatedVisibility(visible = feedbackErrorMsg.isNotEmpty()) {
                    Text(text = feedbackErrorMsg, color = ErrorRed, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.padding(bottom = 6.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Day Picker dropdown
                    Box(modifier = Modifier.weight(1.5f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkBackground)
                                .clickable { formDayExpanded = true }
                                .padding(12.dp)
                                .testTag("timetable_add_day_select"),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(text = formDay, color = Color.White, fontSize = 13.sp)
                            DropdownMenu(
                                expanded = formDayExpanded,
                                onDismissRequest = { formDayExpanded = false },
                                modifier = Modifier.background(DarkSurface)
                            ) {
                                daysOfWeek.forEach { d ->
                                    DropdownMenuItem(
                                        text = { Text(d, color = Color.White) },
                                        onClick = {
                                            formDay = d
                                            formDayExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Period Number field
                    OutlinedTextField(
                        value = formPeriod,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) formPeriod = it },
                        placeholder = { Text("Period") },
                        modifier = Modifier.weight(1f).testTag("timetable_add_period_input"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = BrightAccent, unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = DarkBackground, unfocusedContainerColor = DarkBackground
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Subject name
                OutlinedTextField(
                    value = formSubject,
                    onValueChange = { formSubject = it },
                    placeholder = { Text("Subject Course Name") },
                    modifier = Modifier.fillMaxWidth().testTag("timetable_add_subject_input"),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = BrightAccent, unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = DarkBackground, unfocusedContainerColor = DarkBackground
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Time slot name
                OutlinedTextField(
                    value = formTimeSlot,
                    onValueChange = { formTimeSlot = it },
                    placeholder = { Text("Time Slot (e.g. 09:00 AM - 10:00 AM)") },
                    modifier = Modifier.fillMaxWidth().testTag("timetable_add_timeslot_input"),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = BrightAccent, unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = DarkBackground, unfocusedContainerColor = DarkBackground
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val pNum = formPeriod.toIntOrNull() ?: 1
                        val res = viewModel.addTimetable(
                            branch = selectedBranchFilter,
                            day = formDay,
                            period = pNum,
                            subject = formSubject,
                            timeSlot = formTimeSlot
                        )
                        if (res.first) {
                            feedbackMsg = res.second
                            feedbackErrorMsg = ""
                            // Reset
                            formSubject = ""
                        } else {
                            feedbackErrorMsg = res.second
                            feedbackMsg = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("timetable_add_submit_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = BrightAccent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Entry", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }

    // --- DIALOG: INLINE EDIT ENTRY ---
    if (editingEntry != null) {
        val original = editingEntry!!
        var editSubject by remember { mutableStateOf(original.subject) }
        var editTimeSlot by remember { mutableStateOf(original.timeSlot) }
        var editPeriod by remember { mutableStateOf(original.period.toString()) }

        AlertDialog(
            onDismissRequest = { editingEntry = null },
            title = { Text("Inline Edit Session", color = Color.White, fontWeight = FontWeight.Bold) },
            containerColor = DarkSurface,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Day: ${original.day} | Branch: ${original.branch}", color = GrayText, fontSize = 13.sp)
                    
                    OutlinedTextField(
                        value = editSubject,
                        onValueChange = { editSubject = it },
                        label = { Text("Subject Course") },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = editPeriod,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) editPeriod = it },
                        label = { Text("Period") },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = editTimeSlot,
                        onValueChange = { editTimeSlot = it },
                        label = { Text("Time Slot") },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = editPeriod.toIntOrNull() ?: original.period
                        viewModel.updateTimetable(
                            original.copy(subject = editSubject, timeSlot = editTimeSlot, period = p)
                        )
                        editingEntry = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingEntry = null }) {
                    Text("Cancel", color = GrayText)
                }
            }
        )
    }

    // --- DIALOG: DELETE CONFIRMATION ---
    if (entryToDelete != null) {
        val entry = entryToDelete!!
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Confirm Deletion", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Are you absolutely sure you want to delete '${entry.subject}' (${entry.day}, Period ${entry.period}) schedule?", color = GrayText) },
            containerColor = DarkSurface,
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTimetable(entry.id)
                        entryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Delete schedule")
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text("Keep schedule", color = GrayText)
                }
            }
        )
    }
}

// Helpers
private fun getDayIndex(day: String): Int {
    return when (day) {
        "Monday" -> 0
        "Tuesday" -> 1
        "Wednesday" -> 2
        "Thursday" -> 3
        "Friday" -> 4
        "Saturday" -> 5
        else -> 6
    }
}
