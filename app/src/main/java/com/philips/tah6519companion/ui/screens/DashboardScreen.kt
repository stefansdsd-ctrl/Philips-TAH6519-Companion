package com.philips.tah6519companion.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
    val connected by vm.isConnected.collectAsState()
    val lastUpdate by vm.lastUpdate.collectAsState()
    val address by vm.deviceAddress.collectAsState()
    val history by vm.batteryHistory.collectAsState()

    LaunchedEffect(Unit) { vm.refresh() }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Headphones, contentDescription = null, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(8.dp))
        Text("Philips TAH6519", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Status: ${if (connected) "Verbonden" else "Niet verbonden"}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("Apparaat: ${device?.name ?: "Niet gevonden"}")
                if (address.isNotEmpty()) Text("MAC: $address", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = if (battery in 0..15) Icons.Default.BatteryAlert else Icons.Default.BatteryFull
                    Icon(icon, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Batterij: ${if (battery >= 0) "$battery%" else "Onbekend"}")
                }
                if (battery >= 0) {
                    LinearProgressIndicator(
                        progress = { battery / 100f },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                }
                if (lastUpdate.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Laatst bijgewerkt: $lastUpdate", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        if (history.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text("Batterij-geschiedenis", style = MaterialTheme.typography.titleMedium)
            BatteryHistoryChart(history)
            Spacer(Modifier.height(8.dp))
            history.take(5).forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
        }

        Spacer(Modifier.height(16.dp))
        Text("ANC / Transparantie: fysieke knop op koptelefoon\n(Officiële Philips-app ondersteunt Awareness + Adaptive)", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))

        Button(onClick = { navController.navigate("media") }, modifier = Modifier.fillMaxWidth()) {
            Text("Media Controls")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { navController.navigate("eq") }, modifier = Modifier.fillMaxWidth()) {
            Text("Equalizer")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { navController.navigate("settings") }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Instellingen")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { vm.refresh() }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Vernieuwen")
        }
    }
}

@Composable
fun BatteryHistoryChart(history: List<String>) {
    val levels = history.mapNotNull { entry ->
        entry.substringAfter(": ").removeSuffix("%").toIntOrNull()
    }.reversed().takeLast(10)
    if (levels.size < 2) return

    Canvas(modifier = Modifier.fillMaxWidth().height(100.dp).padding(vertical = 8.dp)) {
        val path = Path()
        val stepX = size.width / (levels.size - 1).coerceAtLeast(1)
        levels.forEachIndexed { i, level ->
            val x = i * stepX
            val y = size.height - (level / 100f * size.height)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, Color(0xFF1976D2), style = Stroke(width = 4f))
        levels.forEachIndexed { i, level ->
            val x = i * stepX
            val y = size.height - (level / 100f * size.height)
            drawCircle(Color(0xFF1976D2), 6f, Offset(x, y))
        }
    }
}
