package com.training.blueconnect.uiscreen.permissions

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.os.Build

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