package com.philips.tah6519companion.viewmodel

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.philips.tah6519companion.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HeadphonesViewModel(private val context: Context) : ViewModel() {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? = bluetoothManager.adapter
    private var headsetProxy: BluetoothHeadset? = null
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val _connectedDevice = MutableStateFlow<BluetoothDevice?>(null)
    val connectedDevice: StateFlow<BluetoothDevice?> = _connectedDevice

    private val _batteryLevel = MutableStateFlow(-1)
    val batteryLevel: StateFlow<Int> = _batteryLevel

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.HEADSET) {
                headsetProxy = proxy as BluetoothHeadset
                refresh()
            }
        }
        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HEADSET) headsetProxy = null
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_ACL_CONNECTED,
                BluetoothDevice.ACTION_ACL_DISCONNECTED,
                BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED,
                BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED,
                "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED",
                BluetoothHeadset.ACTION_HF_INDICATORS_VALUE_CHANGED -> {
                    if (intent.action == BluetoothHeadset.ACTION_HF_INDICATORS_VALUE_CHANGED) {
                        val id = intent.getIntExtra(BluetoothHeadset.EXTRA_HF_INDICATORS_IND_ID, -1)
                        if (id == 2) {
                            val value = intent.getIntExtra(BluetoothHeadset.EXTRA_HF_INDICATORS_IND_VALUE, -1)
                            if (value >= 0) _batteryLevel.value = value
                        }
                    }
                    refresh()
                }
            }
        }
    }

    init {
        createNotificationChannel()
        adapter?.getProfileProxy(context, profileListener, BluetoothProfile.HEADSET)
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
            addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
            addAction("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED")
            addAction(BluetoothHeadset.ACTION_HF_INDICATORS_VALUE_CHANGED)
        }
        context.registerReceiver(receiver, filter)
        startPeriodicRefresh()
        refresh()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("tah6519", "TAH6519 Status", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(battery: Int, connected: Boolean, name: String?) {
        val intent = Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_SINGLE_TOP }
        val pending = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val text = if (connected) "${name ?: "TAH6519"} • ${if (battery >= 0) "$battery%" else "batterij onbekend"}" else "Niet verbonden"
        val notif = NotificationCompat.Builder(context, "tah6519")
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode)
            .setContentTitle("Philips TAH6519")
            .setContentText(text)
            .setContentIntent(pending)
            .setOngoing(connected)
            .setOnlyAlertOnce(true)
            .build()
        notificationManager.notify(1, notif)
    }

    private fun startPeriodicRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(15_000)
                refresh()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try { context.unregisterReceiver(receiver) } catch (_: Exception) {}
        adapter?.closeProfileProxy(BluetoothProfile.HEADSET, headsetProxy)
        notificationManager.cancel(1)
    }

    fun refresh() {
        viewModelScope.launch {
            val bonded = adapter?.bondedDevices ?: emptySet()
            val device = bonded.firstOrNull {
                it.name?.contains("TAH6519", ignoreCase = true) == true ||
                it.name?.contains("Philips", ignoreCase = true) == true
            }
            _connectedDevice.value = device
            val connectedViaProxy = headsetProxy?.getConnectionState(device) == BluetoothProfile.STATE_CONNECTED
            val connected = device != null && (connectedViaProxy || isDeviceConnected(device))
            _isConnected.value = connected

            var battery = -1
            device?.let {
                battery = getBatteryLevel(it)
            }
            if (battery >= 0) _batteryLevel.value = battery
            updateNotification(battery, connected, device?.name)
        }
    }

    private fun getBatteryLevel(device: BluetoothDevice): Int {
        try {
            val method = device.javaClass.getMethod("getBatteryLevel")
            val level = method.invoke(device) as? Int
            if (level != null && level >= 0) return level
        } catch (_: Exception) {}
        return -1
    }

    private fun isDeviceConnected(device: BluetoothDevice): Boolean {
        return try {
            val method = device.javaClass.getMethod("isConnected")
            method.invoke(device) as? Boolean ?: false
        } catch (_: Exception) { false }
    }
}
