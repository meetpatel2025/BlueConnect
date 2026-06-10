package com.training.blueconnect.uiscreen.client

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.training.blueconnect.ble.BleClient
import com.training.blueconnect.ble.BleScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ClientViewModel(
    private val bluetoothAdapter: BluetoothAdapter,
    val bleClient: BleClient
) : ViewModel() {

    private val scanner = BleScanner(bluetoothAdapter)

    val devices = scanner.devices

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs

    private fun log(message: String) {
        _logs.value = (_logs.value + message).takeLast(100)
    }

    //    fun startScan() {
//        log("Scan started")
//        scanner.startScan()
//    }
    fun startScan() {

        if (!bluetoothAdapter.isEnabled) {

            log("Bluetooth disabled")
            return
        }

        log("Scan started")

        scanner.startScan()
    }

    fun stopScan() {
        log("Scan stopped")
        scanner.stopScan()
    }

//    fun connect(address: String) {
//        log("Connecting to $address")
//
//        val bluetoothDevice =
//            bluetoothAdapter.getRemoteDevice(address)
//
//        bleClient.connect(bluetoothDevice)
//    }

    fun connect(
        address: String
    ) {

        scanner.stopScan()

        log("Connecting to $address")

        val bluetoothDevice =
            bluetoothAdapter.getRemoteDevice(
                address
            )

        bleClient.connect(
            bluetoothDevice
        )
    }

    fun sendMessage(message: String) {
        bleClient.writeCharacteristic(message)
    }

    fun readMessage() {
        log("Reading characteristic")
        bleClient.readCharacteristic()
    }
}