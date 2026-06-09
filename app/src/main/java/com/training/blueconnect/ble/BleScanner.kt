package com.training.blueconnect.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import com.training.blueconnect.model.BleDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BleScanner(
    private val bluetoothAdapter: BluetoothAdapter
) {

    private val _devices =
        MutableStateFlow<List<BleDevice>>(emptyList())

    val devices: StateFlow<List<BleDevice>> = _devices

    private val scanner =
        bluetoothAdapter.bluetoothLeScanner

    private val discoveredDevices =
        mutableMapOf<String, BleDevice>()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device

            val deviceName = try {
                device.name ?: "Unknown Device"
            } catch (e: SecurityException) {
                "Unknown Device"
            }

            val bleDevice = BleDevice(
                name = deviceName,
                address = device.address,
                rssi = result.rssi
            )

            discoveredDevices[device.address] = bleDevice
            _devices.value = discoveredDevices.values.toList()
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {

        if (scanner == null) {
            Log.e(
                "BLE",
                "Scanner unavailable on this device"
            )
            return
        }

        discoveredDevices.clear()

        _devices.value = emptyList()

        scanner.startScan(scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {

        scanner?.stopScan(scanCallback)
    }
}