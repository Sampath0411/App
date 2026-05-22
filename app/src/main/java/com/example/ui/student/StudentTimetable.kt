package com.example.ui.student

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.BrightAccent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GrayText
import com.example.ui.theme.White

@Composable
fun StudentTimetable(
    viewModel: MainViewModel
) {
    val currentStudent by viewModel.currentStudent.collectAsState()
    val fullTimetable by viewModel.timetable.collectAsState()

    val student = currentStudent ?: return
    val studentBranch = student.branch

    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    var selectedDay by remember { mutableStateOf("Monday") }

    // Filter timetable for this student's branch on the selected day, sorted by period number.
    val filteredEntries = fullTimetable
        .filter { it.branch.trim().equals(studentBranch.trim(), ignoreCase = true) && it.day.trim().equals(selectedDay.trim(), ignoreCase = true) }
        .sortedBy { it.period }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            Text(
                text = "SCHEDULER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BrightAccent,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "My Timetable",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = White,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Branch Scheduler: $studentBranch",
                fontSize = 13.sp,
                color = GrayText,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
        }

        // Days of week selector slider
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("student_timetable_days_row"),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Spacer(modifier = Modifier.width(20.dp)) }
            items(daysOfWeek) { day ->
                val isActive = day == selectedDay
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isActive) BrightAccent else DarkSurface)
                        .border(1.dp, if (isActive) Color.Transparent else Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .clickable { selectedDay = day }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("day_tab_$day"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        color = if (isActive) White else GrayText,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
            item { Spacer(modifier = Modifier.width(20.dp)) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid/List of Periods
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filteredEntries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventNote,
                                contentDescription = null,
                                tint = BrightAccent.copy(alpha = 0.5f),
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Free Session Day",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "No lectures listed for $selectedDay in your curriculum.",
                                fontSize = 13.sp,
                                color = GrayText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredEntries) { entry ->
                    TimetableSlotCard(entry = entry)
                }
            }

            item {
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }
}

@Composable
fun TimetableSlotCard(entry: com.example.data.TimetableEntry) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("timetable_slot_card_${entry.id}")
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Period Number Circular Accent Badge (Immersive style)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BrightAccent.copy(alpha = 0.08f))
                    .border(1.dp, BrightAccent.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "P${entry.period}",
                    color = BrightAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Class details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.subject,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = White
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = GrayText,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = entry.timeSlot,
                        color = GrayText,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
