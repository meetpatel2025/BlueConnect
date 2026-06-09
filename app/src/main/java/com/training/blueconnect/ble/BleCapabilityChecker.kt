package com.training.blueconnect.ble

import android.bluetooth.BluetoothAdapter

object BleCapabilityChecker {

    fun isBleScannerAvailable(
        bluetoothAdapter: BluetoothAdapter
    ): Boolean {

        return bluetoothAdapter.bluetoothLeScanner != null
    }

    fun isBleAdvertiserAvailable(
        bluetoothAdapter: BluetoothAdapter
    ): Boolean {

        return bluetoothAdapter.bluetoothLeAdvertiser != null
    }
}