package com.philips.tah6519companion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MediaScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Now Playing / Media Controls", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text("Use system media controls or integrate MediaSession in next version.")
        Spacer(Modifier.height(16.dp))
        Row {
            Button(onClick = {}) { Text("Prev") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = {}) { Text("Play/Pause") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = {}) { Text("Next") }
        }
    }
}

@Composable
fun EqualizerScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("System Equalizer", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text("Android Equalizer API will be wired here for bass/treble boost and custom presets.")
        Spacer(Modifier.height(16.dp))
        Button(onClick = {}) { Text("Bass Boost") }
        Button(onClick = {}) { Text("Flat") }
        Button(onClick = {}) { Text("Vocal") }
    }
}
