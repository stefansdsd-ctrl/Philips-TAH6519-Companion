package com.philips.tah6519companion.ui.screens

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import kotlinx.coroutines.delay

private val bandFrequencies = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")

private data class EqPreset(
    val name: String,
    val bands: List<Int>,
    val bass: Int,
    val virt: Int,
    val loud: Int
)

private val eqPresets = listOf(
    EqPreset("Flat", listOf(0, 0, 0, 0, 0), 0, 0, 0),
    EqPreset("Bass+", listOf(700, 450, 0, -200, -300), 850, 150, 200),
    EqPreset("Treble", listOf(-100, 0, 100, 400, 700), 100, 250, 0),
    EqPreset("Rock", listOf(500, 300, -150, 200, 450), 500, 350, 150),
    EqPreset("Pop", listOf(200, 100, 150, 300, 250), 200, 200, 100),
    EqPreset("Jazz", listOf(150, 0, 50, 200, 300), 100, 300, 0),
    EqPreset("Hip Hop", listOf(800, 500, -100, 100, 200), 900, 100, 250),
    EqPreset("Dance", listOf(600, 300, 0, 200, 500), 600, 250, 200),
    EqPreset("Classical", listOf(0, 0, 0, 200, 400), 0, 400, 0),
    EqPreset("Acoustic", listOf(250, 150, 0, 150, 300), 150, 200, 0),
    EqPreset("Vocal", listOf(-200, -50, 450, 500, 150), 0, 150, 100),
    EqPreset("Podcast", listOf(-300, 150, 500, 250, -100), 0, 0, 200)
)

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
    var loudnessFx by remember { mutableStateOf<LoudnessEnhancer?>(null) }
    var bandLevels by remember { mutableStateOf(loadLevels(prefs)) }
    var bassStrength by remember { mutableIntStateOf(prefs.getInt("bass", 0)) }
    var virtualStrength by remember { mutableIntStateOf(prefs.getInt("virtual", 0)) }
    var loudness by remember { mutableIntStateOf(prefs.getInt("loudness", 0)) }
    var eqEnabled by remember { mutableStateOf(prefs.getBoolean("eq_enabled", true)) }
    var bandMin by remember { mutableFloatStateOf(-1500f) }
    var bandMax by remember { mutableFloatStateOf(1500f) }

    DisposableEffect(Unit) {
        try {
            eq = Equalizer(0, 0).apply {
                enabled = eqEnabled
                bandMin = bandLevelRange[0].toFloat()
                bandMax = bandLevelRange[1].toFloat()
            }
        } catch (_: Exception) {}
        try { bass = BassBoost(0, 0).apply { enabled = true } } catch (_: Exception) {}
        try { virtualizer = Virtualizer(0, 0).apply { enabled = true } } catch (_: Exception) {}
        try { loudnessFx = LoudnessEnhancer(0) } catch (_: Exception) {}
        applyLevels(eq, bass, virtualizer, loudnessFx, bandLevels, bassStrength, virtualStrength, loudness)
        onDispose {
            eq?.release()
            bass?.release()
            virtualizer?.release()
            loudnessFx?.release()
        }
    }

    fun saveAndApply(
        levels: List<Int>,
        strength: Int = bassStrength,
        virt: Int = virtualStrength,
        loud: Int = loudness
    ) {
        val clamped = levels.map { it.coerceIn(bandMin.toInt(), bandMax.toInt()) }
        bandLevels = clamped
        bassStrength = strength.coerceIn(0, 1000)
        virtualStrength = virt.coerceIn(0, 1000)
        loudness = loud.coerceIn(0, 1500)
        prefs.edit {
            clamped.forEachIndexed { i, v -> putInt("band_$i", v) }
            putInt("bass", bassStrength)
            putInt("virtual", virtualStrength)
            putInt("loudness", loudness)
            putBoolean("eq_enabled", eqEnabled)
        }
        applyLevels(eq, bass, virtualizer, loudnessFx, clamped, bassStrength, virtualStrength, loudness)
        eq?.enabled = eqEnabled
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
        Spacer(Modifier.height(8.dp))
        EqCurve(bandLevels)
        Spacer(Modifier.height(8.dp))

        Text("Presets", style = MaterialTheme.typography.titleMedium)
        eqPresets.chunked(3).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            ) {
                row.forEach { preset ->
                    FilledTonalButton(
                        onClick = { saveAndApply(preset.bands, preset.bass, preset.virt, preset.loud) },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Text(preset.name, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        Spacer(Modifier.height(16.dp))

        Text("Bass Boost: $bassStrength")
        Slider(
            value = bassStrength.toFloat(),
            onValueChange = { saveAndApply(bandLevels, it.toInt(), virtualStrength, loudness) },
            valueRange = 0f..1000f,
            modifier = Modifier.fillMaxWidth()
        )

        Text("Virtualizer: $virtualStrength")
        Slider(
            value = virtualStrength.toFloat(),
            onValueChange = { saveAndApply(bandLevels, bassStrength, it.toInt(), loudness) },
            valueRange = 0f..1000f,
            modifier = Modifier.fillMaxWidth()
        )

        Text("Loudness: +${"%.1f".format(loudness / 100f)} dB")
        Slider(
            value = loudness.toFloat(),
            onValueChange = { saveAndApply(bandLevels, bassStrength, virtualStrength, it.toInt()) },
            valueRange = 0f..1500f,
            enabled = loudnessFx != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (loudnessFx == null) {
            Text("Loudness niet ondersteund op dit apparaat.", style = MaterialTheme.typography.bodySmall)
        }

        eq?.let { e ->
            val bands = minOf(5, e.numberOfBands.toInt())
            repeat(bands) { i ->
                Text("${bandFrequencies.getOrElse(i) { "Band ${i + 1}" }}: ${bandLevels.getOrElse(i) { 0 }} mB")
                Slider(
                    value = bandLevels.getOrElse(i) { 0 }.toFloat().coerceIn(bandMin, bandMax),
                    onValueChange = { v ->
                        val newLevels = bandLevels.toMutableList()
                        while (newLevels.size <= i) newLevels.add(0)
                        newLevels[i] = v.toInt()
                        saveAndApply(newLevels, bassStrength, virtualStrength, loudness)
                    },
                    valueRange = bandMin..bandMax,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } ?: Text("Equalizer niet beschikbaar op dit apparaat.", style = MaterialTheme.typography.bodySmall)

        Spacer(Modifier.height(16.dp))
        Text(
            "Systeem-EQ, bass, virtualizer en loudness gelden voor audio-sessie 0. Niet elke app respecteert dat.",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun EqCurve(levels: List<Int>) {
    val line = MaterialTheme.colorScheme.primary
    val fillColor = line.copy(alpha = 0.18f)
    val zeroColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
        if (levels.isEmpty()) return@Canvas
        fun yFor(level: Int): Float {
            val norm = (level.coerceIn(-1500, 1500) + 1500) / 3000f
            return size.height - (norm * size.height)
        }
        val stepX = if (levels.size > 1) size.width / (levels.size - 1) else size.width
        val curve = Path()
        val fill = Path()
        levels.forEachIndexed { i, level ->
            val x = i * stepX
            val y = yFor(level)
            if (i == 0) {
                curve.moveTo(x, y)
                fill.moveTo(x, size.height)
                fill.lineTo(x, y)
            } else {
                curve.lineTo(x, y)
                fill.lineTo(x, y)
            }
        }
        fill.lineTo((levels.size - 1) * stepX, size.height)
        fill.close()
        val zeroY = yFor(0)
        drawLine(zeroColor, Offset(0f, zeroY), Offset(size.width, zeroY), strokeWidth = 2f)
        drawPath(fill, fillColor)
        drawPath(curve, line, style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        levels.forEachIndexed { i, level ->
            drawCircle(line, radius = 6f, center = Offset(i * stepX, yFor(level)))
        }
    }
}

private fun loadLevels(prefs: SharedPreferences): List<Int> =
    (0 until 5).map { prefs.getInt("band_$it", 0) }

private fun applyLevels(
    eq: Equalizer?,
    bass: BassBoost?,
    virt: Virtualizer?,
    loud: LoudnessEnhancer?,
    levels: List<Int>,
    bassStrength: Int,
    virtStrength: Int,
    loudness: Int
) {
    eq?.let { e ->
        val range = e.bandLevelRange
        levels.forEachIndexed { i, level ->
            if (i < e.numberOfBands) {
                val clamped = level.coerceIn(range[0].toInt(), range[1].toInt())
                e.setBandLevel(i.toShort(), clamped.toShort())
            }
        }
    }
    try { bass?.setStrength(bassStrength.coerceIn(0, 1000).toShort()) } catch (_: Exception) {}
    try { virt?.setStrength(virtStrength.coerceIn(0, 1000).toShort()) } catch (_: Exception) {}
    try {
        val gain = loudness.coerceIn(0, 1500)
        loud?.setTargetGain(gain)
        loud?.enabled = gain > 0
    } catch (_: Exception) {}
}
