package com.training.blueconnect.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
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

            Log.d(
                "SCAN",
                "Device Found = ${result.device.address} and Name = $deviceName"
            )

            val bleDevice = BleDevice(
                name = deviceName,
                address = device.address,
                rssi = result.rssi
            )

            discoveredDevices[device.address] = bleDevice
            _devices.value = discoveredDevices.values.toList()

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

        if (scanner == null) {
            Log.e(
                "BLE",
                "Scanner unavailable on this device"
            )
            return
        }

        discoveredDevices.clear()

        _devices.value = emptyList()

        val filter =
            ScanFilter.Builder()
                .setServiceUuid(
                    ParcelUuid(
                        BleConstants.SERVICE_UUID
                    )
                )
                .build()

        val settings =
            ScanSettings.Builder()
                .setScanMode(
                    ScanSettings.SCAN_MODE_LOW_LATENCY
                )
                .build()

        Log.d(
            "SCAN",
            "startScan called"
        )

//        scanner.startScan(
//            listOf(filter),
//            settings,
//            scanCallback
//        )
        scanner.startScan(
            scanCallback
        )


    }

    @SuppressLint("MissingPermission")
    fun stopScan() {

        scanner?.stopScan(scanCallback)
    }
}