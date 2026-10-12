package com.philips.tah6519companion.viewmodel

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
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

    fun refresh() {
        viewModelScope.launch {
            val bonded = adapter?.bondedDevices ?: emptySet()
            val device = bonded.firstOrNull { it.name?.contains("TAH6519", ignoreCase = true) == true || it.name?.contains("Philips", ignoreCase = true) == true }
            _connectedDevice.value = device
            device?.let {
                try {
                    val method = it.javaClass.getMethod("getBatteryLevel")
                    _batteryLevel.value = method.invoke(it) as? Int ?: -1
                } catch (e: Exception) {
                    _batteryLevel.value = -1
                }
            }
        }
    }
}
