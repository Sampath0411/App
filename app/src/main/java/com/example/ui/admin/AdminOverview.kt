package com.example.ui.admin

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.BrightAccent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GrayText
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.White

@Composable
fun AdminOverview(
    viewModel: MainViewModel
) {
    val students by viewModel.students.collectAsState()
    val attendanceRecords by viewModel.attendance.collectAsState()

    val totalRegistered = students.size
    val branchBreakdowns = viewModel.getBranchStudentCount()
    val todayStats = viewModel.getTodayAttendanceSummary()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "System Overview",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = White
            )
            Text(
                text = "Live analytics from local attendance indices.",
                fontSize = 14.sp,
                color = GrayText,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Large stats cards block
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Total Students card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("admin_overview_total_students_card"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BrightAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Group, contentDescription = null, tint = BrightAccent, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Registrations", fontSize = 12.sp, color = GrayText)
                    Text(text = "$totalRegistered", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = White, modifier = Modifier.padding(top = 2.dp))
                }
            }

            // Today Marked Present card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("admin_overview_today_presents_card"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.HowToReg, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Present Today", fontSize = 12.sp, color = GrayText)
                    Text(text = "${todayStats.presentCount}", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = SuccessGreen, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Daily summary card details
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Analytics, contentDescription = null, tint = BrightAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Today's Attendance Detail", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Current Calendar Date", color = GrayText, fontSize = 13.sp)
                    Text(todayStats.dateString, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total entries filed today", color = GrayText, fontSize = 13.sp)
                    Text("${todayStats.totalMarked} students", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Per-branch breakdown progress bars
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .testTag("admin_overview_breakdown_card"),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Branch Breakdown Density",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                branchBreakdowns.forEach { (branch, count) ->
                    val fraction = if (totalRegistered > 0) count.toFloat() / totalRegistered.toFloat() else 0f
                    
                    Column(modifier = Modifier.padding(bottom = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.School, contentDescription = null, tint = BrightAccent, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = branch, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Text(text = "$count registry entries", color = GrayText, fontSize = 12.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        LinearProgressIndicator(
                            progress = { fraction },
                            color = BrightAccent,
                            trackColor = Color.White.copy(alpha = 0.08f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}
