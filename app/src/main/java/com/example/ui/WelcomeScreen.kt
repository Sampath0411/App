package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.clickable
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.BrightAccent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkerAccent
import com.example.ui.theme.GrayText

@Composable
fun WelcomeScreen(
    onNavigateToStudentPortal: () -> Unit,
    onNavigateToAdminPortal: () -> Unit
) {
    var showInfoModal by remember { mutableStateOf(false) }

    if (showInfoModal) {
        Dialog(onDismissRequest = { showInfoModal = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(DarkSurface)
                    .clickable { /* dismiss if clicked outside */ }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AU",
                            color = BrightAccent,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "AUCSE App",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Version 2025.2",
                        fontSize = 14.sp,
                        color = BrightAccent,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Andhra University CSE Department",
                        fontSize = 14.sp,
                        color = GrayText,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { showInfoModal = false },
                        colors = ButtonDefaults.buttonColors(containerColor = BrightAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Dismiss", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp)
    ) {
        // Aesthetic Top Radial Gradient Glow Effect
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            BrightAccent.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Clickable White Circular Logo representing Andhra University CSE (128x128 placeholder)
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { showInfoModal = true }
                    .testTag("app_logo_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AU",
                    color = BrightAccent,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-2).sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "SMART CAMPUS COMPLIANCE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BrightAccent,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Text(
                text = "AUCSE App",
                fontSize = 36.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Your complete smart campus presence tracker with high precision QR codes & offline verification.",
                fontSize = 15.sp,
                color = GrayText,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Student Register / Login Portal Action Button (Round 16dp corners)
            Button(
                onClick = onNavigateToStudentPortal,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("student_portal_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrightAccent,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AssignmentInd,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Student Portal",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Admin Login Portal Button
            OutlinedButton(
                onClick = onNavigateToAdminPortal,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("admin_portal_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = BrightAccent
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(
                        colors = listOf(BrightAccent, LighterAccent)
                    )
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = BrightAccent
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Admin Portal",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrightAccent
                )
            }
        }

        // Subtitle Footer
        Text(
            text = "v2025.2",
            color = GrayText.copy(alpha = 0.5f),
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        )
    }
}

// Light theme mapping
private val LighterAccent = Color(0xFF8B83FF)
