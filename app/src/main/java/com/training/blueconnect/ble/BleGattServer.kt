package com.training.blueconnect.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BleGattServer(
    private val context: Context,
    private val bluetoothManager: BluetoothManager
) {

    private var gattServer: BluetoothGattServer? = null
    private var connectedDevice: BluetoothDevice? = null

    private val _connectedDeviceAddress =
        MutableStateFlow<String?>(null)

    val connectedDeviceAddress:
            StateFlow<String?> =
        _connectedDeviceAddress

    private val _receivedMessages =
        MutableStateFlow<List<String>>(emptyList())

    val receivedMessages: StateFlow<List<String>> = _receivedMessages


    private val _isClientConnected =
        MutableStateFlow(false)

    val isClientConnected:
            StateFlow<Boolean> =
        _isClientConnected

    private val _serverLogs =
        MutableStateFlow<List<String>>(emptyList())

    val serverLogs:
            StateFlow<List<String>> = _serverLogs

    private fun addLog(
        message: String
    ) {
        _serverLogs.value += message
    }

    private fun addReceivedMessage(
        message: String
    ) {
        _receivedMessages.value += message
    }

    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun startServer() {

        if (!hasBluetoothPermission()) {
            Log.e("BLE", "Missing BLUETOOTH_CONNECT permission")
            return
        }

        gattServer =
            bluetoothManager.openGattServer(
                context,
                gattServerCallback
            )

        if (gattServer == null) {

            Log.e(
                "BLE",
                "Failed to create GATT Server"
            )

            return
        }

        val service =
            BluetoothGattService(
                BleConstants.SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY
            )


        val characteristic =
            BluetoothGattCharacteristic(
                BleConstants.CHARACTERISTIC_UUID,

                BluetoothGattCharacteristic.PROPERTY_READ or
                        BluetoothGattCharacteristic.PROPERTY_WRITE or
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY,

                BluetoothGattCharacteristic.PERMISSION_READ or
                        BluetoothGattCharacteristic.PERMISSION_WRITE
            )

        characteristic.value =
            "Server Ready".toByteArray()

        service.addCharacteristic(
            characteristic
        )

        gattServer?.addService(
            service
        )

        Log.d(
            "BLE",
            "Service Added"
        )

        Log.d(
            "BLE",
            "Service Registered"
        )
    }

    @SuppressLint("MissingPermission")
    fun stopServer() {

        if (!hasBluetoothPermission()) return

        gattServer?.close()
        gattServer = null
        connectedDevice = null
    }

    private val gattServerCallback =
        object : BluetoothGattServerCallback() {

            override fun onConnectionStateChange(
                device: BluetoothDevice,
                status: Int,
                newState: Int
            ) {

                when (newState) {

                    BluetoothProfile.STATE_CONNECTED -> {

                        connectedDevice = device

                        _isClientConnected.value = true

                        _connectedDeviceAddress.value =
                            device.address

                        addLog(
                            "Connected: ${device.address}"
                        )
                    }

                    BluetoothProfile.STATE_DISCONNECTED -> {

                        connectedDevice = null

                        _isClientConnected.value = false

                        _connectedDeviceAddress.value = null

                        Log.d(
                            "BLE",
                            "Device Disconnected"
                        )
                        addLog(
                            "Disconnected: ${device.address}"
                        )

                    }
                }

            }

            @SuppressLint("MissingPermission")
            fun handleCharacteristicReadRequest(
                device: BluetoothDevice,
                requestId: Int,
                responseNeeded: Boolean
            ) {
                if (responseNeeded) {
                    gattServer?.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        null
                    )
                }
            }

            @SuppressLint("MissingPermission")
            override fun onCharacteristicWriteRequest(
                device: BluetoothDevice,
                requestId: Int,
                characteristic: BluetoothGattCharacteristic,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                value: ByteArray
            ) {

                val received = String(value)

                Log.d("BLE", "Received from client: $received")

                addReceivedMessage(received)

                // ---- RESPONSE MESSAGE (NEW PART) ----
                val responseMessage =
                    "ACK from Server: $received"

                characteristic.value =
                    responseMessage.toByteArray()

                if (responseNeeded) {

                    gattServer?.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        responseMessage.toByteArray()
                    )
                }

                // Notify client (IMPORTANT for BLE push updates)
                gattServer?.notifyCharacteristicChanged(
                    device,
                    characteristic,
                    false
                )

                Log.d(
                    "BLE",
                    "Response sent: $responseMessage"
                )
            }

            override fun onServiceAdded(
                status: Int,
                service: BluetoothGattService
            ) {

                Log.d(
                    "BLE",
                    "Service Registered: ${service.uuid}"
                )
            }
        }

    @SuppressLint("MissingPermission")
    fun notifyClient(message: String) {

        if (!hasBluetoothPermission()) {
            Log.e("BLE", "Missing BLUETOOTH_CONNECT permission")
            return
        }

        val service =
            gattServer?.getService(BleConstants.SERVICE_UUID)

        val characteristic =
            service?.getCharacteristic(BleConstants.CHARACTERISTIC_UUID)

        if (service == null || characteristic == null) {
            Log.e("BLE", "Service or Characteristic null")
            return
        }

        characteristic.value = message.toByteArray()

        connectedDevice?.let { device ->

            gattServer?.notifyCharacteristicChanged(
                device,
                characteristic,
                false
            )
        }
    }

    private fun hasBluetoothPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }
}