package com.example.smartfarm.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.smartfarm.ui.features.auth.view.LoginScreen
import com.example.smartfarm.ui.features.farmpreferences.domain.models.FarmLocation
import com.example.smartfarm.ui.features.farmpreferences.presentation.view.AddEditFarmScreen
import com.example.smartfarm.ui.features.farmpreferences.presentation.view.FarmPreferencesScreen
import com.example.smartfarm.ui.features.farmpreferences.presentation.view.MapSelectionScreen
import com.example.smartfarm.ui.features.finance.presentation.view.FinanceScreen
import com.example.smartfarm.ui.features.home.presentation.view.HomeScreen
import com.example.smartfarm.ui.features.profile.view.ProfileScreen
import com.example.smartfarm.ui.features.settings.presentation.view.SettingsScreen
import com.example.smartfarm.ui.features.weather.presentation.view.WeatherScreen
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {

    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    var showBottomBar by remember { mutableStateOf(true) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val showComingSoonSnackbar: () -> Unit = {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = "Feature coming soon! ✨",
                actionLabel = "Dismiss" // Optional action label
            )
        }
    }

    // Get AuthViewModel to observe current user
    //val authViewModel: LoginViewModel = hiltViewModel()
    //val currentUser by authViewModel.currentUser.collectAsState()

//    LaunchedEffect(currentUser) {
//        val user = currentUser
//        Timber.d("NavGraph: auth currentUser changed -> ${user?.uid ?: "null"}, currentRoute=$currentRoute")
//
//        if (user != null) {
//            // User is logged in - navigate to Home if not already there
//            if (currentRoute != Routes.Home.route && currentRoute != Routes.Login.route) {
//                navController.navigate(Routes.Home.route) {
//                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
//                    launchSingleTop = true
//                }
//            }
//        } else {
//            // User is not logged in - navigate to Login if not already there
//            if (currentRoute != Routes.Login.route) {
//                navController.navigate(Routes.Login.route) {
//                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
//                    launchSingleTop = true
//                }
//            }
//        }
//    }

    Scaffold (
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },

        bottomBar = {
            // only show bottom bar for bottom-tab routes AND when showBottomBar == true
            val shouldShow = showBottomBar && (currentRoute in BottomRoutes.bottomItem.map { it.route })

            AnimatedVisibility(
                visible = shouldShow,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                BottomBarNav(
                    navController = navController
                )
            }
        }
    ){ paddingValues ->

        NavHost(
            navController = navController,
            startDestination = Routes.Login.route,
            modifier = modifier.padding(paddingValues)
        ) {
            composable (Routes.Login.route){
                LoginScreen(
                    onNavigateToHome = {
                        navController.navigate(Routes.Home.route){
                            popUpTo(Routes.Login.route){
                                inclusive = true
                            }
                        }
                    }
                )

            }

            composable(Routes.Home.route){
                HomeScreen(
                    onNavigate = { route -> navController.navigate(route) },
                )
            }

            composable(Routes.Weather.route){
                WeatherScreen(
                    onNavigateBack = {
                        navController.navigateUp()
                    },
                    onNavigateToFarmPreferences = {
                        navController.navigate(Routes.FarmPreferences.route)
                    },
                )

            }

            composable (Routes.Expenses.route){
                FinanceScreen()

            }

            composable (Routes.Settings.route){
                SettingsScreen(

                    onNavigateToProfile = {
                        navController.navigate(Routes.Profile.route)
                    },
                    onNavigateToFarmPreferences = {
                        navController.navigate(Routes.FarmPreferences.route)
                    },
                    onNavigateToAccountSettings = showComingSoonSnackbar,
                    onNavigateToHelpSupport = showComingSoonSnackbar,
                    onNavigateToNotifications = showComingSoonSnackbar,
                    onNavigateToAbout = showComingSoonSnackbar,
                )

            }

            composable(Routes.Profile.route){
                ProfileScreen(
                    onNavigateBack = {
                        navController.navigateUp()
                    },
                    onLogout = {
                        navController.navigate(Routes.Login.route){
                            popUpTo(Routes.Login.route){
                                inclusive = true
                            }
                        }
                    }
                )

            }

            composable(Routes.FarmPreferences.route){
                FarmPreferencesScreen(
                    onNavigateBack = {
                        navController.navigateUp()
                    },
                    onNavigateToAddFarm = {
                        navController.navigate("${Routes.AddEditFarm.route}?farmId=")
                    },
                    onNavigateToEditFarm = { farmId ->
                        navController.navigate("${Routes.AddEditFarm.route}?farmId=$farmId")
                    },
                )
            }

            composable(
                route = "${Routes.AddEditFarm.route}?farmId={farmId}",
                arguments = listOf(
                    navArgument("farmId") {
                        defaultValue = ""
                        type = androidx.navigation.NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val farmId = backStackEntry.arguments?.getString("farmId") ?: ""

                AddEditFarmScreen(
                    farmId = if (farmId.isBlank()) null else farmId,
                    onNavigateBack = { navController.navigateUp() },
                    navController = navController,
                    onNavigateToMap = { initialLocation ->
                        // Pass initial location to map screen
                        val locationString = initialLocation?.toRouteString() ?: "null"
                        navController.navigate("${Routes.MapSelection.route}/$locationString")
                    }
                )
            }

            composable(
                route = "${Routes.MapSelection.route}/{initialLocation}",
                arguments = listOf(
                    navArgument("initialLocation") {
                        type = androidx.navigation.NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val initialLocationString = backStackEntry.arguments?.getString("initialLocation") ?: "null"
                val initialLocation = initialLocationString.toFarmLocation()

                MapSelectionScreen(
                    initialLocation = initialLocation,
                    onLocationSelected = { location ->

                        // FarmLocation is now Parcelable, so this should work
                        // Pass selected location back to AddEditFarmScreen
                        navController.previousBackStackEntry?.savedStateHandle?.set(
                            "selectedLocation",
                            location
                        )
                        navController.popBackStack()
                    },
                    onNavigateBack = { navController.navigateUp() }
                )
            }















        }
    }

}


// Extension functions for FarmLocation serialization
private fun FarmLocation.toRouteString(): String {
    return "${this.latitude},${this.longitude},${this.name},${this.address}"
}

private fun String.toFarmLocation(): FarmLocation? {
    if (this == "null") return null
    val parts = this.split(",")
    return if (parts.size >= 4) {
        FarmLocation(
            latitude = parts[0].toDoubleOrNull() ?: 0.0,
            longitude = parts[1].toDoubleOrNull() ?: 0.0,
            name = parts[2],
            address = parts[3]
        )
    } else {
        null
    }
}