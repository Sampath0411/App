package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.IconButton
import com.example.data.NotificationItem
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.StorageService
import com.example.ui.AdminLoginScreen
import com.example.ui.MainViewModel
import com.example.ui.RegisterScreen
import com.example.ui.StudentLoginScreen
import com.example.ui.WelcomeScreen
import com.example.ui.admin.AdminMarkAttendance
import com.example.ui.admin.AdminOverview
import com.example.ui.admin.AdminStudentsList
import com.example.ui.admin.AdminTimetableManager
import com.example.ui.admin.StudentDetailScreen
import com.example.ui.student.StudentDashboard
import com.example.ui.student.StudentProfile
import com.example.ui.student.StudentTimetable
import com.example.ui.theme.BrightAccent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GrayText
import com.example.ui.theme.MyApplicationTheme

import com.example.ui.student.StudentSettings
import com.example.ui.student.NotificationsScreen
import com.example.ui.admin.AdminNotificationScreen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column

class MainActivity : ComponentActivity() {

    private lateinit var storageService: StorageService
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize local Storage layer & MVVM VM
        storageService = StorageService(applicationContext)
        val factory = MainViewModel.Factory(applicationContext, storageService)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        enableEdgeToEdge()
        
        setContent {
            val isDark by viewModel.isDarkMode.collectAsState()
            MyApplicationTheme(isDark = isDark) {
                AppNavigation(viewModel)
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "welcome",
        modifier = Modifier.fillMaxSize()
    ) {
        // Welcome portal selection screen
        composable("welcome") {
            WelcomeScreen(
                onNavigateToStudentPortal = { navController.navigate("student_login") },
                onNavigateToAdminPortal = { navController.navigate("admin_login") }
            )
        }

        // Student onboarding register screen
        composable("register") {
            RegisterScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToLogin = { navController.navigate("student_login") { popUpTo("welcome") } }
            )
        }

        // Student signing portal screen
        composable("student_login") {
            StudentLoginScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = {
                    navController.navigate("student_home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }

        // Administration signing portal screen
        composable("admin_login") {
            AdminLoginScreen(
                onBack = { navController.popBackStack() },
                onLoginSuccess = {
                    navController.navigate("admin_home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }

        // Student Bottom Tab Navigation panel
        composable("student_home") {
            StudentBottomTabNavigator(
                viewModel = viewModel,
                onNavigateToNotifications = { navController.navigate("notifications") },
                onLogout = {
                    viewModel.logout()
                    navController.navigate("welcome") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Administrative Bottom Tab Navigation panel
        composable("admin_home") {
            AdminBottomTabNavigator(
                viewModel = viewModel,
                onLogout = {
                    navController.navigate("welcome") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToStudentDetail = { regNo ->
                    navController.navigate("student_detail/$regNo")
                }
            )
        }

        // Detail records editor panel
        composable(
            route = "student_detail/{regNo}",
            arguments = listOf(navArgument("regNo") { type = NavType.StringType })
        ) { backStackEntry ->
            val regNo = backStackEntry.arguments?.getString("regNo") ?: ""
            StudentDetailScreen(
                regNo = regNo,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("notifications") {
            NotificationsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

// Student bottom bar
@Composable
fun StudentBottomTabNavigator(
    viewModel: MainViewModel,
    onNavigateToNotifications: () -> Unit,
    onLogout: () -> Unit
) {
    var selectTabIdx by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "AUCSE APP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrightAccent,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = when (selectTabIdx) {
                            0 -> "Dashboard"
                            1 -> "Class Schedule"
                            2 -> "Digital ID Card"
                            else -> "App Settings"
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                val listFlow = viewModel.notifications.collectAsState().value
                val lastRead = viewModel.lastReadNotifAt.collectAsState().value
                val unreadCount = listFlow.count { it.timestamp > lastRead }

                Box(modifier = Modifier.size(44.dp)) {
                    IconButton(
                        onClick = onNavigateToNotifications,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(DarkSurface)
                            .testTag("notifications_bell_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White
                        )
                    }
                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Color.Red),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = unreadCount.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("student_bottom_bar")
            ) {
                NavigationBarItem(
                    selected = selectTabIdx == 0,
                    onClick = { selectTabIdx = 0 },
                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = null) },
                    label = { Text("Dashboard") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = BrightAccent,
                        unselectedIconColor = GrayText,
                        unselectedTextColor = GrayText
                    ),
                    modifier = Modifier.testTag("student_tab_item_dashboard")
                )

                NavigationBarItem(
                    selected = selectTabIdx == 1,
                    onClick = { selectTabIdx = 1 },
                    icon = { Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null) },
                    label = { Text("Timetable") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = BrightAccent,
                        unselectedIconColor = GrayText,
                        unselectedTextColor = GrayText
                    ),
                    modifier = Modifier.testTag("student_tab_item_timetable")
                )

                NavigationBarItem(
                    selected = selectTabIdx == 2,
                    onClick = { selectTabIdx = 2 },
                    icon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profile") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = BrightAccent,
                        unselectedIconColor = GrayText,
                        unselectedTextColor = GrayText
                    ),
                    modifier = Modifier.testTag("student_tab_item_profile")
                )

                NavigationBarItem(
                    selected = selectTabIdx == 3,
                    onClick = { selectTabIdx = 3 },
                    icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = BrightAccent,
                        unselectedIconColor = GrayText,
                        unselectedTextColor = GrayText
                    ),
                    modifier = Modifier.testTag("student_tab_item_settings")
                )
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectTabIdx) {
                0 -> StudentDashboard(viewModel = viewModel)
                1 -> StudentTimetable(viewModel = viewModel)
                2 -> StudentProfile(viewModel = viewModel, onLogout = onLogout)
                3 -> StudentSettings(
                    viewModel = viewModel,
                    onNavigateToProfileTab = { selectTabIdx = 2 },
                    onLogout = onLogout
                )
            }
        }
    }
}

// Admin bottom bar
@Composable
fun AdminBottomTabNavigator(
    viewModel: MainViewModel,
    onLogout: () -> Unit,
    onNavigateToStudentDetail: (String) -> Unit
) {
    var selectTabIdx by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "AUCSE ADMIN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrightAccent,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = when (selectTabIdx) {
                            0 -> "Students Roster"
                            1 -> "Mark Attendance"
                            2 -> "Manage Timetable"
                            3 -> "Analytics Overview"
                            else -> "Broadcast Notice"
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(DarkSurface)
                        .testTag("admin_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Logout",
                        tint = Color.Red
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("admin_bottom_bar")
            ) {
                NavigationBarItem(
                    selected = selectTabIdx == 0,
                    onClick = { selectTabIdx = 0 },
                    icon = { Icon(imageVector = Icons.Default.Group, contentDescription = null) },
                    label = { Text("Students") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = BrightAccent,
                        unselectedIconColor = GrayText,
                        unselectedTextColor = GrayText
                    ),
                    modifier = Modifier.testTag("admin_tab_item_students")
                )

                NavigationBarItem(
                    selected = selectTabIdx == 1,
                    onClick = { selectTabIdx = 1 },
                    icon = { Icon(imageVector = Icons.Default.QrCode, contentDescription = null) },
                    label = { Text("Mark Presence") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = BrightAccent,
                        unselectedIconColor = GrayText,
                        unselectedTextColor = GrayText
                    ),
                    modifier = Modifier.testTag("admin_tab_item_mark")
                )

                NavigationBarItem(
                    selected = selectTabIdx == 2,
                    onClick = { selectTabIdx = 2 },
                    icon = { Icon(imageVector = Icons.Default.Schedule, contentDescription = null) },
                    label = { Text("Timetable") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = BrightAccent,
                        unselectedIconColor = GrayText,
                        unselectedTextColor = GrayText
                    ),
                    modifier = Modifier.testTag("admin_tab_item_timetable")
                )

                NavigationBarItem(
                    selected = selectTabIdx == 3,
                    onClick = { selectTabIdx = 3 },
                    icon = { Icon(imageVector = Icons.Default.Analytics, contentDescription = null) },
                    label = { Text("Overview") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = BrightAccent,
                        unselectedIconColor = GrayText,
                        unselectedTextColor = GrayText
                    ),
                    modifier = Modifier.testTag("admin_tab_item_overview")
                )

                NavigationBarItem(
                    selected = selectTabIdx == 4,
                    onClick = { selectTabIdx = 4 },
                    icon = { Icon(imageVector = Icons.Default.Notifications, contentDescription = null) },
                    label = { Text("Notify") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = BrightAccent,
                        unselectedIconColor = GrayText,
                        unselectedTextColor = GrayText
                    ),
                    modifier = Modifier.testTag("admin_tab_item_notifications")
                )
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectTabIdx) {
                0 -> AdminStudentsList(viewModel = viewModel, onNavigateToDetail = onNavigateToStudentDetail)
                1 -> AdminMarkAttendance(viewModel = viewModel)
                2 -> AdminTimetableManager(viewModel = viewModel)
                3 -> AdminOverview(viewModel = viewModel)
                4 -> AdminNotificationScreen(viewModel = viewModel)
            }
        }
    }
}
