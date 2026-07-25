package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.screens.CameraScreen
import com.example.ui.screens.DiagnosisResultScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PlantLibraryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.NavTab

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            MyApplicationTheme(darkTheme = isDarkMode) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val selectedDiagnosis by viewModel.selectedDiagnosis.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (selectedDiagnosis == null) {
                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = currentTab == NavTab.HOME,
                        onClick = { viewModel.selectTab(NavTab.HOME) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == NavTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home") },
                        modifier = Modifier.testTag("nav_item_home")
                    )

                    NavigationBarItem(
                        selected = currentTab == NavTab.CAMERA,
                        onClick = { viewModel.selectTab(NavTab.CAMERA) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == NavTab.CAMERA) Icons.Filled.PhotoCamera else Icons.Outlined.PhotoCamera,
                                contentDescription = "Camera"
                            )
                        },
                        label = { Text("Camera") },
                        modifier = Modifier.testTag("nav_item_camera")
                    )

                    NavigationBarItem(
                        selected = currentTab == NavTab.HISTORY,
                        onClick = { viewModel.selectTab(NavTab.HISTORY) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == NavTab.HISTORY) Icons.Filled.History else Icons.Outlined.History,
                                contentDescription = "History"
                            )
                        },
                        label = { Text("History") },
                        modifier = Modifier.testTag("nav_item_history")
                    )

                    NavigationBarItem(
                        selected = currentTab == NavTab.LIBRARY,
                        onClick = { viewModel.selectTab(NavTab.LIBRARY) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == NavTab.LIBRARY) Icons.Filled.LocalFlorist else Icons.Outlined.LocalFlorist,
                                contentDescription = "Plant Library"
                            )
                        },
                        label = { Text("Library") },
                        modifier = Modifier.testTag("nav_item_library")
                    )

                    NavigationBarItem(
                        selected = currentTab == NavTab.SETTINGS,
                        onClick = { viewModel.selectTab(NavTab.SETTINGS) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == NavTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = "Settings"
                            )
                        },
                        label = { Text("Settings") },
                        modifier = Modifier.testTag("nav_item_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        if (selectedDiagnosis != null) {
            DiagnosisResultScreen(
                diagnosis = selectedDiagnosis!!,
                onBackClick = { viewModel.selectDiagnosisDetail(null) },
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            when (currentTab) {
                NavTab.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToTab = { viewModel.selectTab(it) },
                    onViewDiagnosis = { viewModel.selectDiagnosisDetail(it) },
                    modifier = Modifier.padding(innerPadding)
                )
                NavTab.CAMERA -> CameraScreen(
                    viewModel = viewModel,
                    onNavigateToTab = { viewModel.selectTab(it) },
                    modifier = Modifier.padding(innerPadding)
                )
                NavTab.HISTORY -> HistoryScreen(
                    viewModel = viewModel,
                    onViewDiagnosis = { viewModel.selectDiagnosisDetail(it) },
                    modifier = Modifier.padding(innerPadding)
                )
                NavTab.LIBRARY -> PlantLibraryScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
                NavTab.SETTINGS -> SettingsScreen(
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
