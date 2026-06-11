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

    //    private val scanner =
//        bluetoothAdapter.bluetoothLeScanner
    private val scanner
        get() = bluetoothAdapter.bluetoothLeScanner

    private val discoveredDevices =
        mutableMapOf<String, BleDevice>()

    private val _isScanning =
        MutableStateFlow(false)

    val isScanning: StateFlow<Boolean>
        get() = _isScanning


    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Log.d(
                "SCAN_DEBUG",
                "onScanResult triggered"
            )
            val device = result.device

            val deviceName = try {
                device.name ?: "Unknown Device"
            } catch (e: SecurityException) {
                "Unknown Device"
            }

            val address = result.device.address

            val bleDevice = BleDevice(
                name = deviceName,
                address = address,
                rssi = result.rssi
            )

            discoveredDevices[address] = bleDevice

//            _devices.value =
//                discoveredDevices.values.toList()

            val existing =
                discoveredDevices[address]

            if (existing == null) {

                discoveredDevices[address] = bleDevice

                _devices.value =
                    discoveredDevices.values
                        .sortedByDescending { it.rssi }

                Log.d(
                    "SCAN",
                    "NEW DEVICE FOUND : $address"
                )

            } else {

                discoveredDevices[address] =
                    bleDevice
            }

            Log.d(
                "SCAN",
                """
            Device Found
            Name = $deviceName
            Address = ${device.address}
            RSSI = ${result.rssi}
            """.trimIndent()
            )

            val hasAdvertisement =
                result.scanRecord != null

            Log.d(
                "SCAN",
                "Advertisement = $hasAdvertisement"
            )

//            val bleDevice = BleDevice(
//                name = deviceName,
//                address = device.address,
//                rssi = result.rssi
//            )
//
//            discoveredDevices[device.address] = bleDevice
//            _devices.value = discoveredDevices.values.toList()

            Log.d(
                "SCAN",
                "Device List Size = ${_devices.value.size}"
            )
        }

        override fun onScanFailed(
            errorCode: Int
        ) {

            Log.e(
                "BLE",
                "Scan Failed: $errorCode"
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {

        if (!bluetoothAdapter.isEnabled) {

            Log.e("SCAN", "Bluetooth disabled")
            return
        }

        if (scanner == null) {

            Log.e(
                "SCAN",
                "bluetoothLeScanner is NULL"
            )

            return
        }

        discoveredDevices.clear()

        _devices.value = emptyList()

        _devices.value = fakeDevices()

        _isScanning.value = true

        scanner?.startScan(scanCallback)

        Log.d("SCAN", "Scanning Started")
    }


    @SuppressLint("MissingPermission")
    fun stopScan() {

        scanner?.stopScan(scanCallback)

        _isScanning.value = false

        Log.d("SCAN", "Scanning Stopped")
    }

    private fun fakeDevices(): List<BleDevice> {

        return listOf(

            BleDevice(
                "Fake Sensor 1",
                "00:11:22:33:44:55",
                -60
            ),

            BleDevice(
                "Fake Sensor 2",
                "AA:BB:CC:DD:EE:FF",
                -70
            )
        )
    }
}