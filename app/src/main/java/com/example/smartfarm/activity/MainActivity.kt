package com.example.smartfarm.activity

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.smartfarm.navigation.NavGraph
import com.example.smartfarm.ui.features.settings.presentation.viewModel.AppTheme
import com.example.smartfarm.ui.features.settings.presentation.viewModel.SettingsViewModel
import com.example.smartfarm.ui.theme.SmartFarmTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val navController = rememberNavController()
            val settingsViewModel: SettingsViewModel = hiltViewModel()

            val uiState by settingsViewModel.uiState.collectAsState()
            val themePreference = uiState.theme

            val useDarkTheme = when (themePreference) {
                AppTheme.DARK -> true
                AppTheme.LIGHT -> false
                AppTheme.SYSTEM -> isSystemInDarkTheme() // Check the system setting
            }




            SmartFarmTheme (
                darkTheme = useDarkTheme,
                dynamicColor = false
            ){
                NavGraph(navController = navController)
            }
        }
    }
}