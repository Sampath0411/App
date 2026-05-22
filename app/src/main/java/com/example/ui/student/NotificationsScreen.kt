package com.example.ui.student

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.NotificationItem
import com.example.ui.MainViewModel
import com.example.ui.theme.BrightAccent
import com.example.ui.theme.GrayText
import com.example.ui.theme.SuccessGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val list by viewModel.notifications.collectAsState()
    val lastReadTime by viewModel.lastReadNotifAt.collectAsState()

    var selectedNotification by remember { mutableStateOf<NotificationItem?>(null) }

    // Automatically mark all as read upon opening this screen
    LaunchedEffect(Unit) {
        viewModel.refreshAll()
        viewModel.markNotificationsAsRead()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Screen Top header with back redirection
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .testTag("notifications_back_button")
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
                text = "Notifications",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (list.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = GrayText.copy(alpha = 0.4f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Everything is quiet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Broadcast dispatches will appear here.",
                        fontSize = 14.sp,
                        color = GrayText,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("notifications_list"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(list) { notif ->
                    val isUnread = notif.timestamp > lastReadTime
                    NotificationRow(
                        notification = notif,
                        isUnread = isUnread,
                        onClick = {
                            selectedNotification = notif
                        }
                    )
                }
            }
        }
    }

    // Modal view details dialog trigger
    if (selectedNotification != null) {
        val curr = selectedNotification!!
        val dateStr = SimpleDateFormat("MMM d, yyyy - hh:mm a", Locale.getDefault()).format(Date(curr.timestamp))
        AlertDialog(
            onDismissRequest = { selectedNotification = null },
            title = {
                Text(
                    text = curr.title,
                    fontWeight = FontWeight.Bold,
                    color = BrightAccent,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
                        color = GrayText,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        text = curr.body,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 22.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedNotification = null },
                    colors = ButtonDefaults.buttonColors(containerColor = BrightAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun NotificationRow(
    notification: NotificationItem,
    isUnread: Boolean,
    onClick: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault()).format(Date(notification.timestamp))
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("notification_item_${notification.id}")
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
                    .background(
                        if (isUnread) BrightAccent.copy(alpha = 0.15f)
                        else GrayText.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = if (isUnread) BrightAccent else GrayText,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = notification.title,
                        fontSize = 15.sp,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
                        color = GrayText
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.body,
                    fontSize = 13.sp,
                    color = GrayText,
                    maxLines = 2,
                    lineHeight = 18.sp
                )
            }
            if (isUnread) {
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Default.Circle,
                    contentDescription = "Unread",
                    tint = BrightAccent,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}
