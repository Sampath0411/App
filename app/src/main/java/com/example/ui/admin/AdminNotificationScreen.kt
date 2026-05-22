package com.example.ui.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

@Composable
fun AdminNotificationScreen(viewModel: MainViewModel) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    var feedbackSuccess by remember { mutableStateOf("") }
    var feedbackError by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Section Title
        Text(
            text = "COMMUNICATION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = BrightAccent,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Text(
            text = "Dispatches",
            fontSize = 32.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = (-0.5).sp
        )

        Text(
            text = "Broadcast announcements and push alerts instantly to all student profiles in the department.",
            fontSize = 14.sp,
            color = GrayText,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        )

        // Success Alert Box
        AnimatedVisibility(visible = feedbackSuccess.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.15f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Text(
                    text = feedbackSuccess,
                    color = SuccessGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Error Alert Box
        AnimatedVisibility(visible = feedbackError.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.15f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Text(
                    text = feedbackError,
                    color = ErrorRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Input: Announcement Title
        OutlinedTextField(
            value = title,
            onValueChange = { title = it; feedbackSuccess = ""; feedbackError = "" },
            label = { Text("Notification Title") },
            placeholder = { Text("e.g. Timetable Updated") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = BrightAccent
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_notif_title_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = BrightAccent,
                unfocusedBorderColor = GrayText.copy(alpha = 0.5f),
                focusedLabelColor = BrightAccent,
                unfocusedLabelColor = GrayText,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Input: Announcement Message
        OutlinedTextField(
            value = message,
            onValueChange = { message = it; feedbackSuccess = ""; feedbackError = "" },
            label = { Text("Message Body") },
            placeholder = { Text("Enter detailed description...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .testTag("admin_notif_body_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = BrightAccent,
                unfocusedBorderColor = GrayText.copy(alpha = 0.5f),
                focusedLabelColor = BrightAccent,
                unfocusedLabelColor = GrayText,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            maxLines = 6
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Submit Dispatch Button
        Button(
            onClick = {
                val res = viewModel.sendNotification(title = title, body = message)
                if (res.first) {
                    feedbackSuccess = res.second
                    title = ""
                    message = ""
                } else {
                    feedbackError = res.second
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("admin_notif_send_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrightAccent,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Broadcast Notification", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
