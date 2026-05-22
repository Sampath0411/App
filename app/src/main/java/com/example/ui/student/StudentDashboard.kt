package com.example.ui.student

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.SubjectStats
import com.example.ui.theme.BrightAccent
import com.example.ui.theme.LighterAccent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkerAccent
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GrayText
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange
import com.example.ui.theme.White

@Composable
fun StudentDashboard(
    viewModel: MainViewModel
) {
    val currentStudent by viewModel.currentStudent.collectAsState()
    val attendanceRecords by viewModel.attendance.collectAsState()

    val student = currentStudent ?: return

    val stats = viewModel.getStudentAttendanceSummary(student.regNo)
    val totalLecturesMock = stats.values.sumOf { it.total }
    val totalPresentsMock = stats.values.sumOf { it.present }
    val overallPercentage = if (totalLecturesMock > 0) {
        (totalPresentsMock.toFloat() / totalLecturesMock.toFloat() * 100f)
    } else {
        0.0f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                // Welcome Header Card (Immersive style)
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        text = "ATTENDEASE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrightAccent,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Hi, ${student.fullName}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium,
                            color = White,
                            letterSpacing = (-0.5).sp
                        )
                        
                        // Avatar block with custom gradient
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Brush.linearGradient(colors = listOf(BrightAccent, DarkerAccent)))
                                .padding(1.dp) // border effect
                                .clip(RoundedCornerShape(13.dp))
                                .background(DarkSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            val initials = student.fullName.split(" ")
                                .mapNotNull { it.firstOrNull()?.toString() }
                                .take(2)
                                .joinToString("")
                                .uppercase()
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.linearGradient(colors = listOf(BrightAccent, DarkerAccent))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials.ifEmpty { "AR" },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = White
                                )
                            }
                        }
                    }
                }
            }

            item {
                val indicatorColor = getPercentageColor(overallPercentage)
                val statusText = when {
                    overallPercentage >= 75f -> "On Track"
                    overallPercentage >= 60f -> "Low Standings"
                    else -> "Critical Danger"
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    // Underlay soft glow
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Brush.linearGradient(listOf(BrightAccent.copy(alpha = 0.08f), DarkerAccent.copy(alpha = 0.04f))))
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("overall_percentage_card")
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            // Top Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Overall Attendance",
                                    fontSize = 14.sp,
                                    color = White.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Medium
                                )

                                // Status Pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(indicatorColor.copy(alpha = 0.1f))
                                        .border(1.dp, indicatorColor.copy(alpha = 0.2f), RoundedCornerShape(100.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = statusText.uppercase(),
                                        color = indicatorColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Middle Row with Percent & horizontal progress
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Percent Text
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = String.format("%.0f", overallPercentage),
                                        fontSize = 58.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = White,
                                        letterSpacing = (-2).sp
                                    )
                                    Text(
                                        text = "%",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrightAccent,
                                        modifier = Modifier.padding(bottom = 10.dp)
                                    )
                                }

                                // Sleek linear progress bar inside a Box
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(bottom = 14.dp)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(if (overallPercentage > 100f) 1f else if (overallPercentage < 0f) 0f else overallPercentage / 100f)
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(100.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(BrightAccent, LighterAccent)
                                                )
                                            )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Bottom Footer info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Reg: ${student.regNo}",
                                    fontSize = 12.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = White.copy(alpha = 0.4f)
                                )
                                Text(
                                    text = "${student.branch.uppercase()} BRANCH",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp,
                                    color = White.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "COURSE PROGRESS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = White.copy(alpha = 0.4f),
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp, start = 4.dp)
                )
            }

            if (stats.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = BrightAccent,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Attendance Records Marked Yet",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = White
                            )
                            Text(
                                text = "Once the admin registers lectures, stats appear instantly.",
                                fontSize = 12.sp,
                                color = GrayText,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(stats.toList()) { (subject, data) ->
                    SubjectAttendanceCard(subject = subject, data = data)
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun SubjectAttendanceCard(subject: String, data: SubjectStats) {
    val percentageColor = getPercentageColor(data.percentage)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("subject_card_$subject")
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left percentage box matching theme spec
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(percentageColor.copy(alpha = 0.1f))
                    .border(1.dp, percentageColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format("%.0f%%", data.percentage),
                    color = percentageColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Middle details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subject,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${data.present}/${data.total} CLASSES ATTENDED • ${data.total - data.present} ABSENT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = White.copy(alpha = 0.4f),
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Right neon glow dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .drawBehind {
                        drawCircle(
                            color = percentageColor.copy(alpha = 0.3f),
                            radius = 10.dp.toPx()
                        )
                        drawCircle(
                            color = percentageColor
                        )
                    }
            )
        }
    }
}

fun getPercentageColor(percentage: Float): Color {
    return when {
        percentage >= 75f -> SuccessGreen
        percentage >= 60f -> WarningOrange
        else -> ErrorRed
    }
}
