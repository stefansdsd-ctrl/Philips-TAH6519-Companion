package com.philips.tah6519companion.ui.screens

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
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
    var title by remember { mutableStateOf("Geen media") }
    var artist by remember { mutableStateOf("") }
    var isPlaying by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val msm = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        val sessions = msm.getActiveSessions(null)
        val mc = sessions.firstOrNull()?.let { MediaController(context, it.sessionToken) }
        controller = mc

        val callback = object : MediaController.Callback() {
            override fun onMetadataChanged(metadata: android.media.MediaMetadata?) {
                title = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_TITLE) ?: "Onbekend"
                artist = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST) ?: ""
            }
            override fun onPlaybackStateChanged(state: android.media.session.PlaybackState?) {
                isPlaying = state?.state == android.media.session.PlaybackState.STATE_PLAYING
            }
        }
        mc?.registerCallback(callback)
        // Initial load
        mc?.let {
            title = it.metadata?.getString(android.media.MediaMetadata.METADATA_KEY_TITLE) ?: "Onbekend"
            artist = it.metadata?.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST) ?: ""
            isPlaying = it.playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING
        }

        onDispose {
            mc?.unregisterCallback(callback)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Media Controls", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (artist.isNotEmpty()) Text(artist, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = { controller?.transportControls?.skipToPrevious() }) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Vorige")
            }
            FilledIconButton(onClick = {
                if (isPlaying) controller?.transportControls?.pause() else controller?.transportControls?.play()
            }) {
                Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
            }
            IconButton(onClick = { controller?.transportControls?.skipToNext() }) {
                Icon(Icons.Default.SkipNext, contentDescription = "Volgende")
            }
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
        Spacer(Modifier.height(8.dp))
        Text("Geldt voor globale audio sessie", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                bass?.setStrength(1000.toShort())
                eq?.let { e -> (0 until e.numberOfBands).forEach { e.setBandLevel(it.toShort(), 0) } }
            }) { Text("Bass+") }
            Button(onClick = {
                bass?.setStrength(0)
                eq?.let { e -> (0 until e.numberOfBands).forEach { e.setBandLevel(it.toShort(), 0) } }
            }) { Text("Flat") }
            Button(onClick = {
                eq?.let { e ->
                    if (e.numberOfBands > 3) {
                        e.setBandLevel(1.toShort(), 600)
                        e.setBandLevel(2.toShort(), 900)
                        e.setBandLevel(3.toShort(), 300)
                    }
                }
            }) { Text("Vocal") }
        }
        Spacer(Modifier.height(16.dp))
        Text("Let op: Systeem EQ werkt niet altijd op alle apps/apparaten.")
    }
}
