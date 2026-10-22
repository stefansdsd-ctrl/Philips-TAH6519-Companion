package com.philips.tah6519companion.ui.screens

import android.content.Context
import android.media.AudioManager
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun MediaScreen() {
    val context = LocalContext.current
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    var controller by remember { mutableStateOf<MediaController?>(null) }
    var title by remember { mutableStateOf("Geen media") }
    var artist by remember { mutableStateOf("") }
    var isPlaying by remember { mutableStateOf(false) }
    var volume by remember { mutableFloatStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) }
    var albumArt by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    DisposableEffect(Unit) {
        val msm = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        val sessions = msm.getActiveSessions(null)
        val mc = sessions.firstOrNull()?.let { MediaController(context, it.sessionToken) }
        controller = mc

        val callback = object : MediaController.Callback() {
            override fun onMetadataChanged(metadata: android.media.MediaMetadata?) {
                title = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_TITLE) ?: "Onbekend"
                artist = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST) ?: ""
                albumArt = metadata?.getBitmap(android.media.MediaMetadata.METADATA_KEY_ALBUM_ART)
            }
            override fun onPlaybackStateChanged(state: android.media.session.PlaybackState?) {
                isPlaying = state?.state == android.media.session.PlaybackState.STATE_PLAYING
            }
        }
        mc?.registerCallback(callback)
        mc?.let {
            title = it.metadata?.getString(android.media.MediaMetadata.METADATA_KEY_TITLE) ?: "Onbekend"
            artist = it.metadata?.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST) ?: ""
            isPlaying = it.playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING
            albumArt = it.metadata?.getBitmap(android.media.MediaMetadata.METADATA_KEY_ALBUM_ART)
        }

        onDispose { mc?.unregisterCallback(callback) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Media Controls", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        albumArt?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.size(120.dp))
            Spacer(Modifier.height(8.dp))
        }
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
        Spacer(Modifier.height(24.dp))
        Text("Volume")
        Slider(
            value = volume,
            onValueChange = {
                volume = it
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (it * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)).toInt(), 0)
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun EqualizerScreen() {
    val context = LocalContext.current
    var eq by remember { mutableStateOf<Equalizer?>(null) }
    var bass by remember { mutableStateOf<BassBoost?>(null) }
    var bandLevels by remember { mutableStateOf(listOf(0, 0, 0, 0, 0)) }

    DisposableEffect(Unit) {
        try {
            eq = Equalizer(0, 0).apply { enabled = true }
            bass = BassBoost(0, 0).apply { enabled = true }
            eq?.let { e ->
                bandLevels = (0 until minOf(5, e.numberOfBands)).map { e.getBandLevel(it.toShort()).toInt() }
            }
        } catch (_: Exception) {}
        onDispose {
            eq?.release()
            bass?.release()
        }
    }

    fun applyPreset(levels: List<Int>, bassStrength: Int = 0) {
        eq?.let { e ->
            levels.forEachIndexed { i, level ->
                if (i < e.numberOfBands) e.setBandLevel(i.toShort(), level.toShort())
            }
        }
        bass?.setStrength(bassStrength.toShort())
        bandLevels = levels
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("System Equalizer", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("Geldt voor globale audio sessie", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(16.dp))

        // Presets
        Text("Presets", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { applyPreset(listOf(0, 0, 0, 0, 0)) }, modifier = Modifier.weight(1f)) { Text("Flat") }
            Button(onClick = { applyPreset(listOf(600, 400, 0, -200, -400), 800) }, modifier = Modifier.weight(1f)) { Text("Bass+") }
            Button(onClick = { applyPreset(listOf(300, 100, -100, 200, 400)) }, modifier = Modifier.weight(1f)) { Text("Treble") }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { applyPreset(listOf(400, 200, -100, 100, 300)) }, modifier = Modifier.weight(1f)) { Text("Rock") }
            Button(onClick = { applyPreset(listOf(200, 0, 100, 200, 100)) }, modifier = Modifier.weight(1f)) { Text("Pop") }
            Button(onClick = { applyPreset(listOf(100, 0, 0, 100, 200)) }, modifier = Modifier.weight(1f)) { Text("Jazz") }
        }
        Spacer(Modifier.height(16.dp))

        // Custom sliders
        eq?.let { e ->
            val bands = minOf(5, e.numberOfBands)
            repeat(bands) { i ->
                Text("Band ${i + 1}: ${bandLevels.getOrElse(i) { 0 }} mB")
                Slider(
                    value = bandLevels.getOrElse(i) { 0 }.toFloat(),
                    onValueChange = { v ->
                        val newLevels = bandLevels.toMutableList()
                        if (i < newLevels.size) newLevels[i] = v.toInt()
                        else newLevels.add(v.toInt())
                        bandLevels = newLevels
                        e.setBandLevel(i.toShort(), v.toInt().toShort())
                    },
                    valueRange = -1500f..1500f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Let op: Systeem EQ werkt niet altijd op alle apps/apparaten.")
    }
}
