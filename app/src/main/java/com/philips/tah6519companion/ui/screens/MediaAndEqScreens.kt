package com.philips.tah6519companion.ui.screens

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
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
import androidx.core.content.edit
import kotlinx.coroutines.delay

private val bandFrequencies = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")

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
    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }

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
                duration = metadata?.getLong(android.media.MediaMetadata.METADATA_KEY_DURATION) ?: 0L
            }
            override fun onPlaybackStateChanged(state: android.media.session.PlaybackState?) {
                isPlaying = state?.state == android.media.session.PlaybackState.STATE_PLAYING
                position = state?.position ?: 0L
            }
        }
        mc?.registerCallback(callback)
        mc?.let {
            title = it.metadata?.getString(android.media.MediaMetadata.METADATA_KEY_TITLE) ?: "Onbekend"
            artist = it.metadata?.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST) ?: ""
            isPlaying = it.playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING
            albumArt = it.metadata?.getBitmap(android.media.MediaMetadata.METADATA_KEY_ALBUM_ART)
            duration = it.metadata?.getLong(android.media.MediaMetadata.METADATA_KEY_DURATION) ?: 0L
            position = it.playbackState?.position ?: 0L
        }

        onDispose { mc?.unregisterCallback(callback) }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(1000)
            controller?.playbackState?.let { position = it.position }
        }
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
        Spacer(Modifier.height(16.dp))

        if (duration > 0) {
            Slider(
                value = position.toFloat().coerceIn(0f, duration.toFloat()),
                onValueChange = { position = it.toLong() },
                onValueChangeFinished = { controller?.transportControls?.seekTo(position) },
                valueRange = 0f..duration.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )
            Text("${formatTime(position)} / ${formatTime(duration)}", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
        }

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

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

@Composable
fun EqualizerScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("eq_prefs", Context.MODE_PRIVATE) }
    var eq by remember { mutableStateOf<Equalizer?>(null) }
    var bass by remember { mutableStateOf<BassBoost?>(null) }
    var virtualizer by remember { mutableStateOf<Virtualizer?>(null) }
    var bandLevels by remember { mutableStateOf(loadLevels(prefs)) }
    var bassStrength by remember { mutableIntStateOf(prefs.getInt("bass", 0)) }
    var virtualStrength by remember { mutableIntStateOf(prefs.getInt("virtual", 0)) }
    var eqEnabled by remember { mutableStateOf(prefs.getBoolean("eq_enabled", true)) }

    DisposableEffect(Unit) {
        try {
            eq = Equalizer(0, 0).apply { enabled = eqEnabled }
            bass = BassBoost(0, 0).apply { enabled = true }
            virtualizer = Virtualizer(0, 0).apply { enabled = true }
            applyLevels(eq, bass, virtualizer, bandLevels, bassStrength, virtualStrength)
        } catch (_: Exception) {}
        onDispose {
            eq?.release()
            bass?.release()
            virtualizer?.release()
        }
    }

    fun saveAndApply(levels: List<Int>, strength: Int = bassStrength, virt: Int = virtualStrength) {
        bandLevels = levels
        bassStrength = strength
        virtualStrength = virt
        prefs.edit {
            levels.forEachIndexed { i, v -> putInt("band_$i", v) }
            putInt("bass", strength)
            putInt("virtual", virt)
            putBoolean("eq_enabled", eqEnabled)
        }
        applyLevels(eq, bass, virtualizer, levels, strength, virt)
        eq?.enabled = eqEnabled
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("System Equalizer", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("EQ inschakelen")
            Spacer(Modifier.width(8.dp))
            Switch(checked = eqEnabled, onCheckedChange = {
                eqEnabled = it
                eq?.enabled = it
                prefs.edit { putBoolean("eq_enabled", it) }
            })
        }
        Spacer(Modifier.height(16.dp))

        Text("Presets", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { saveAndApply(listOf(0, 0, 0, 0, 0), 0, 0) }, modifier = Modifier.weight(1f)) { Text("Flat") }
            Button(onClick = { saveAndApply(listOf(600, 400, 0, -200, -400), 800, 200) }, modifier = Modifier.weight(1f)) { Text("Bass+") }
            Button(onClick = { saveAndApply(listOf(300, 100, -100, 200, 400), 200, 100) }, modifier = Modifier.weight(1f)) { Text("Treble") }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { saveAndApply(listOf(400, 200, -100, 100, 300), 400, 300) }, modifier = Modifier.weight(1f)) { Text("Rock") }
            Button(onClick = { saveAndApply(listOf(200, 0, 100, 200, 100), 100, 150) }, modifier = Modifier.weight(1f)) { Text("Pop") }
            Button(onClick = { saveAndApply(listOf(100, 0, 0, 100, 200), 0, 250) }, modifier = Modifier.weight(1f)) { Text("Jazz") }
        }
        Spacer(Modifier.height(16.dp))

        Text("Bass Boost: $bassStrength")
        Slider(
            value = bassStrength.toFloat(),
            onValueChange = { v ->
                bassStrength = v.toInt()
                saveAndApply(bandLevels, bassStrength, virtualStrength)
            },
            valueRange = 0f..1000f,
            modifier = Modifier.fillMaxWidth()
        )

        Text("Virtualizer: $virtualStrength")
        Slider(
            value = virtualStrength.toFloat(),
            onValueChange = { v ->
                virtualStrength = v.toInt()
                saveAndApply(bandLevels, bassStrength, virtualStrength)
            },
            valueRange = 0f..1000f,
            modifier = Modifier.fillMaxWidth()
        )

        eq?.let { e ->
            val bands = minOf(5, e.numberOfBands)
            repeat(bands) { i ->
                Text("${bandFrequencies.getOrElse(i) { "Band ${i+1}" }}: ${bandLevels.getOrElse(i) { 0 }} mB")
                Slider(
                    value = bandLevels.getOrElse(i) { 0 }.toFloat(),
                    onValueChange = { v ->
                        val newLevels = bandLevels.toMutableList()
                        while (newLevels.size <= i) newLevels.add(0)
                        newLevels[i] = v.toInt()
                        bandLevels = newLevels
                        e.setBandLevel(i.toShort(), v.toInt().toShort())
                        prefs.edit { putInt("band_$i", v.toInt()) }
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

private fun loadLevels(prefs: SharedPreferences): List<Int> =
    (0 until 5).map { prefs.getInt("band_$it", 0) }

private fun applyLevels(eq: Equalizer?, bass: BassBoost?, virt: Virtualizer?, levels: List<Int>, bassStrength: Int = 0, virtStrength: Int = 0) {
    eq?.let { e ->
        levels.forEachIndexed { i, level ->
            if (i < e.numberOfBands) e.setBandLevel(i.toShort(), level.toShort())
        }
    }
    bass?.setStrength(bassStrength.toShort())
    virt?.setStrength(virtStrength.toShort())
}
