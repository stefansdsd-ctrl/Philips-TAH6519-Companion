package com.philips.tah6519companion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.philips.tah6519companion.viewmodel.HeadphonesViewModel

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val vm: HeadphonesViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return HeadphonesViewModel(context) as T
        }
    })
    val threshold by vm.batteryThreshold.collectAsState()
    var autoRefresh by remember { mutableStateOf(context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE).getBoolean("auto_refresh", true)) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Instellingen", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        Text("Batterij-alert drempel: $threshold%")
        Slider(
            value = threshold.toFloat(),
            onValueChange = { vm.setBatteryThreshold(it.toInt()) },
            valueRange = 5f..50f,
            steps = 8,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Auto-vernieuwen (15s)", modifier = Modifier.weight(1f))
            Switch(
                checked = autoRefresh,
                onCheckedChange = {
                    autoRefresh = it
                    vm.setAutoRefresh(it)
                }
            )
        }

        Spacer(Modifier.height(24.dp))
        Text("EQ-instellingen worden bewaard in Equalizer-scherm.", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        Text("Versie 1.7.0", style = MaterialTheme.typography.bodySmall)
    }
}
