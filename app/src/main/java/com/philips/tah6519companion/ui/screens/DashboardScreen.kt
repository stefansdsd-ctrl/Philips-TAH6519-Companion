package com.philips.tah6519companion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.philips.tah6519companion.viewmodel.HeadphonesViewModel

@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    val vm: HeadphonesViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return HeadphonesViewModel(context) as T
        }
    })
    val device by vm.connectedDevice.collectAsState()
    val battery by vm.batteryLevel.collectAsState()

    LaunchedEffect(Unit) { vm.refresh() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Philips TAH6519 Companion", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        Text("Status: ${if (device != null) "Connected" else "Not found"}")
        Text("Device: ${device?.name ?: "-"}")
        Text("Battery: ${if (battery >= 0) "$battery%" else "Unknown"}")
        Spacer(Modifier.height(16.dp))
        Text("ANC: Use the physical button on headphones to cycle ANC / Normal / Transparency")
        Spacer(Modifier.height(24.dp))
        Button(onClick = { navController.navigate("media") }) { Text("Media Controls") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { navController.navigate("eq") }) { Text("Equalizer") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { vm.refresh() }) { Text("Refresh") }
    }
}
