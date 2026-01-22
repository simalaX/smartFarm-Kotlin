package com.example.smartfarm.ui.features.farmpreferences.presentation.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.smartfarm.activity.BuildConfig  // Changed this line
import com.example.smartfarm.ui.features.farmpreferences.domain.models.FarmLocation
import com.example.smartfarm.utils.rememberLocationPermissionState
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapSelectionScreen(
    initialLocation: FarmLocation? = null,
    onLocationSelected: (FarmLocation) -> Unit,
    onNavigateBack: () -> Unit
) {
    val mapsApiKey = BuildConfig.MAP_API_KEY

    var cameraPositionState by remember {
        mutableStateOf(
            CameraPosition.fromLatLngZoom(
                LatLng(
                    initialLocation?.latitude ?: -1.286389, // Default to Nairobi
                    initialLocation?.longitude ?: 36.817223
                ),
                10f
            )
        )
    }

    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var locationName by remember { mutableStateOf("") }

    val locationPermissionState = rememberLocationPermissionState()

    // Check if Maps API key is available
    LaunchedEffect(mapsApiKey) {
        if (mapsApiKey.isBlank()) {
            // Handle missing API key - show error or fallback
            println("Maps API key is missing!")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Select Farm Location",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (locationPermissionState.hasPermission) {
                FloatingActionButton(
                    onClick = {
                        // Center on current location
                        // You can implement this with FusedLocationProviderClient
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.MyLocation, "Current Location")
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                tonalElevation = 8.dp,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    selectedLocation?.let { latLng ->
                        Text(
                            "Selected Location:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Lat: ${latLng.latitude}, Lng: ${latLng.longitude}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = locationName,
                            onValueChange = { locationName = it },
                            label = { Text("Location Name (e.g., My Farm)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (locationName.isNotBlank()) {
                                    onLocationSelected(
                                        FarmLocation(
                                            name = locationName,
                                            latitude = latLng.latitude,
                                            longitude = latLng.longitude,
                                            address = "Selected on map" // You can reverse geocode this
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = locationName.isNotBlank()
                        ) {
                            Text("Confirm Location")
                        }
                    } ?: run {
                        Text(
                            "Tap on the map to select a location",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    ) { paddingValues ->

        if (mapsApiKey.isNotBlank()) {  // Changed this line - removed the custom isNotBlank property
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                cameraPositionState = rememberCameraPositionState {
                    position = cameraPositionState
                },
                properties = MapProperties(
                    isMyLocationEnabled = locationPermissionState.hasPermission,
                ),
                googleMapOptionsFactory = {
                    GoogleMapOptions().apply {
                        // The API key is now handled automatically by the Maps SDK
                    }
                },
                onMapClick = { latLng ->
                    selectedLocation = latLng
                    // You can add reverse geocoding here to get address
                }
            ) {
                selectedLocation?.let { latLng ->
                    Marker(
                        state = MarkerState(position = latLng),
                        title = "Farm Location"
                    )
                }
            }
        } else {
            // Show error state if API key is missing
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Maps Not Available",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Google Maps API key is not configured. Please check your keys.properties file.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = onNavigateBack) {
                        Text("Go Back")
                    }
                }
            }
        }

        // Handle location permission
        if (!locationPermissionState.hasPermission) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(onClick = { locationPermissionState.requestPermission() }) {
                    Text("Enable Location Access")
                }
            }
        }
    }
}