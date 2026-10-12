package com.philips.tah6519companion.viewmodel

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HeadphonesViewModel(private val context: Context) : ViewModel() {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? = bluetoothManager.adapter

    private val _connectedDevice = MutableStateFlow<BluetoothDevice?>(null)
    val connectedDevice: StateFlow<BluetoothDevice?> = _connectedDevice

    private val _batteryLevel = MutableStateFlow(-1)
    val batteryLevel: StateFlow<Int> = _batteryLevel

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_ACL_CONNECTED,
                BluetoothDevice.ACTION_ACL_DISCONNECTED,
                BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED,
                BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED -> refresh()
            }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
            addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
        }
        context.registerReceiver(receiver, filter)
        refresh()
    }

    override fun onCleared() {
        super.onCleared()
        try { context.unregisterReceiver(receiver) } catch (_: Exception) {}
    }

    fun refresh() {
        viewModelScope.launch {
            val bonded = adapter?.bondedDevices ?: emptySet()
            val device = bonded.firstOrNull {
                it.name?.contains("TAH6519", ignoreCase = true) == true ||
                it.name?.contains("Philips", ignoreCase = true) == true
            }
            _connectedDevice.value = device
            _isConnected.value = device != null && isDeviceConnected(device)

            device?.let {
                try {
                    val method = it.javaClass.getMethod("getBatteryLevel")
                    _batteryLevel.value = method.invoke(it) as? Int ?: -1
                } catch (e: Exception) {
                    _batteryLevel.value = -1
                }
            } ?: run { _batteryLevel.value = -1 }
        }
    }

    private fun isDeviceConnected(device: BluetoothDevice): Boolean {
        return try {
            val method = device.javaClass.getMethod("isConnected")
            method.invoke(device) as? Boolean ?: false
        } catch (_: Exception) { false }
    }
}
