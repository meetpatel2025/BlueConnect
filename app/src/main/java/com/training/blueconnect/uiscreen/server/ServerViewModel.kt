package com.training.blueconnect.uiscreen.server

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.lifecycle.ViewModel
import com.training.blueconnect.ble.BleAdvertiser
import com.training.blueconnect.ble.BleGattServer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ServerViewModel(
    private val context: Context, private val bluetoothManager: BluetoothManager
) : ViewModel() {
    private val gattServer = BleGattServer(context, bluetoothManager)
    private val advertiser = BleAdvertiser(
        bluetoothManager.adapter
    )

    val serverLogs = gattServer.serverLogs

    val receivedMessages = gattServer.receivedMessages
    val isAdvertising = advertiser.isAdvertising

    private val _isServerRunning = MutableStateFlow(false)

    val isServerRunning: StateFlow<Boolean> = _isServerRunning

    val connectedDeviceAddress = gattServer.connectedDeviceAddress

    val isClientConnected = gattServer.isClientConnected

    private val _logs = MutableStateFlow<List<String>>(emptyList())

    val logs: StateFlow<List<String>> = _logs

    val toastMessage =
        gattServer.toastMessage

    fun startServer() {
        gattServer.startServer()
        advertiser.startAdvertising()
        _isServerRunning.value = true
        addLog("GATT Server Started")
        addLog("Advertising Started")
    }

    fun stopServer() {
        advertiser.stopAdvertising()
        gattServer.stopServer()
        _isServerRunning.value = false
        addLog("Advertising Stopped")
        addLog("GATT Server Stopped")
    }

//    fun sendNotification() {
//        gattServer.notifyClient(
//            "Notification from Server"
//        )
//        addLog("Notification Sent")
//    }

    private fun addLog(
        message: String
    ) {
        _logs.value += message
    }

//    fun onSendNotificationClick() {
//        gattServer.sendRandomNotification()
//    }

    fun randomNotification() {

        if (gattServer.isSending()) {
            gattServer.stopRandomNotification()
        } else {
            gattServer.startRandomNotification()
        }
    }

    fun clearToastMessage() {
        gattServer.clearToastMessage()
    }


}