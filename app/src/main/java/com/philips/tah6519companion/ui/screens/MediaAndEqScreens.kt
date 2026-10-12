package com.philips.tah6519companion.ui.screens

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun MediaScreen() {
    val context = LocalContext.current
    var controller by remember { mutableStateOf<MediaController?>(null) }
    var title by remember { mutableStateOf("No media") }
    var isPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val msm = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        val sessions = msm.getActiveSessions(null)
        controller = sessions.firstOrNull()?.let { MediaController(context, it.sessionToken) }
        controller?.let {
            title = it.metadata?.getString(android.media.MediaMetadata.METADATA_KEY_TITLE) ?: "Unknown"
            isPlaying = it.playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Media Controls", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text(title)
        Spacer(Modifier.height(16.dp))
        Row {
            Button(onClick = { controller?.transportControls?.skipToPrevious() }) { Text("Prev") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                if (isPlaying) controller?.transportControls?.pause() else controller?.transportControls?.play()
                isPlaying = !isPlaying
            }) { Text(if (isPlaying) "Pause" else "Play") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { controller?.transportControls?.skipToNext() }) { Text("Next") }
        }
    }
}

@Composable
fun EqualizerScreen() {
    val context = LocalContext.current
    var eq by remember { mutableStateOf<Equalizer?>(null) }
    var bass by remember { mutableStateOf<BassBoost?>(null) }

    DisposableEffect(Unit) {
        try {
            eq = Equalizer(0, 0).apply { enabled = true }
            bass = BassBoost(0, 0).apply { enabled = true }
        } catch (_: Exception) {}
        onDispose {
            eq?.release()
            bass?.release()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("System Equalizer", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text("Applies to global audio session")
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            bass?.setStrength(1000.toShort())
            eq?.let { e ->
                for (i in 0 until e.numberOfBands) e.setBandLevel(i.toShort(), 0)
            }
        }) { Text("Bass Boost") }
        Button(onClick = {
            bass?.setStrength(0)
            eq?.let { e ->
                for (i in 0 until e.numberOfBands) e.setBandLevel(i.toShort(), 0)
            }
        }) { Text("Flat") }
        Button(onClick = {
            eq?.let { e ->
                if (e.numberOfBands > 2) {
                    e.setBandLevel(1.toShort(), 500)
                    e.setBandLevel(2.toShort(), 800)
                }
            }
        }) { Text("Vocal") }
    }
}
