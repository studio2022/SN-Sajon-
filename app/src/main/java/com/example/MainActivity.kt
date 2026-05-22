package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ui.StreamViewModel
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: StreamViewModel by viewModels {
        StreamViewModel.Factory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        checkRuntimePermissions()

        setContent {
            MyApplicationTheme {
                var currentTab by remember { mutableStateOf("home") }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        Column {
                            HorizontalDivider(
                                color = Color(0xFF00E5FF).copy(alpha = 0.25f),
                                thickness = 1.dp
                            )
                            NavigationBar(
                                containerColor = Color(0xFF0F172A),
                                tonalElevation = 8.dp,
                                modifier = Modifier.testTag("app_navigation_bar")
                            ) {
                                NavigationBarItem(
                                    selected = currentTab == "home",
                                    onClick = { currentTab = "home" },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home", modifier = Modifier.size(24.dp)) },
                                    label = { Text("Home", fontWeight = FontWeight.Bold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF00E5FF),
                                        selectedTextColor = Color(0xFF00E5FF),
                                        unselectedIconColor = Color(0xFF94A3B8),
                                        unselectedTextColor = Color(0xFF94A3B8),
                                        indicatorColor = Color(0xFF00E5FF).copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier.testTag("tab_button_home")
                                )

                                NavigationBarItem(
                                    selected = currentTab == "settings",
                                    onClick = { currentTab = "settings" },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(24.dp)) },
                                    label = { Text("Settings", fontWeight = FontWeight.Bold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF00E5FF),
                                        selectedTextColor = Color(0xFF00E5FF),
                                        unselectedIconColor = Color(0xFF94A3B8),
                                        unselectedTextColor = Color(0xFF94A3B8),
                                        indicatorColor = Color(0xFF00E5FF).copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier.testTag("tab_button_settings")
                                )

                                NavigationBarItem(
                                    selected = currentTab == "analytics",
                                    onClick = { currentTab = "analytics" },
                                    icon = { Icon(Icons.Default.List, contentDescription = "Analytics", modifier = Modifier.size(24.dp)) },
                                    label = { Text("Analytics", fontWeight = FontWeight.Bold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF00E5FF),
                                        selectedTextColor = Color(0xFF00E5FF),
                                        unselectedIconColor = Color(0xFF94A3B8),
                                        unselectedTextColor = Color(0xFF94A3B8),
                                        indicatorColor = Color(0xFF00E5FF).copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier.testTag("tab_button_analytics")
                                )

                                NavigationBarItem(
                                    selected = currentTab == "profile",
                                    onClick = { currentTab = "profile" },
                                    icon = { Icon(Icons.Default.Info, contentDescription = "Profile", modifier = Modifier.size(24.dp)) },
                                    label = { Text("Profile", fontWeight = FontWeight.Bold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF00E5FF),
                                        selectedTextColor = Color(0xFF00E5FF),
                                        unselectedIconColor = Color(0xFF94A3B8),
                                        unselectedTextColor = Color(0xFF94A3B8),
                                        indicatorColor = Color(0xFF00E5FF).copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier.testTag("tab_button_profile")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF030712))
                    ) {
                        when (currentTab) {
                            "home" -> HomeScreen(viewModel, innerPadding)
                            "settings" -> SettingsScreen(viewModel, innerPadding)
                            "analytics" -> AnalyticsScreen(viewModel, innerPadding)
                            "profile" -> ProfileScreen(viewModel, innerPadding)
                        }
                    }
                }
            }
        }
    }

    private fun checkRuntimePermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 101)
        }
    }
}
