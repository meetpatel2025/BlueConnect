package com.training.blueconnect.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.training.blueconnect.model.BleMessage
import com.training.blueconnect.model.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BleClient(
    private val context: Context
) {

    companion object {
        private const val TAG = "BLE_CLIENT"
    }

    private var bluetoothGatt: BluetoothGatt? = null

    private var targetCharacteristic: BluetoothGattCharacteristic? = null

    private val _messages = MutableStateFlow<List<BleMessage>>(emptyList())

    val messages: StateFlow<List<BleMessage>>
        get() = _messages

    private val _isConnected = MutableStateFlow(false)

    val isConnected: StateFlow<Boolean>
        get() = _isConnected

    private val _connectionState = MutableStateFlow(ConnectionState.IDLE)

    val connectionState: StateFlow<ConnectionState>
        get() = _connectionState

    var logger: ((String) -> Unit)? = null

    // ----------------------------------------------------
    // Permission Helper
    // ----------------------------------------------------

    private fun hasBluetoothConnectPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice) {

        _connectionState.value = ConnectionState.CONNECTING

        Log.d(TAG, "Connecting to ${device.address}")
        logger?.invoke("Connected")
        bluetoothGatt = device.connectGatt(
            context, false, gattCallback
        )
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {

        if (!hasBluetoothConnectPermission()) {
            Log.e(TAG, "BLUETOOTH_CONNECT permission missing")
            return
        }

        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()

        bluetoothGatt = null

        _isConnected.value = false

        Log.d(TAG, "Disconnected")
    }

    @SuppressLint("MissingPermission")
    fun readCharacteristic() {

        if (!hasBluetoothConnectPermission()) {
            Log.e(TAG, "BLUETOOTH_CONNECT permission missing")
            return
        }

        val characteristic = targetCharacteristic

        if (characteristic == null) {
            Log.e(TAG, "Characteristic not discovered")
            return
        }

        bluetoothGatt?.readCharacteristic(
            characteristic
        )
    }

    @SuppressLint("MissingPermission")
    fun writeCharacteristic(
        message: String
    ) {

        if (!hasBluetoothConnectPermission()) {
            Log.e(TAG, "BLUETOOTH_CONNECT permission missing")
            return
        }

        val characteristic = targetCharacteristic

        if (characteristic == null) {
            Log.e(TAG, "Characteristic not discovered")
            return
        }

        characteristic.value = message.toByteArray()

        Log.d(
            TAG, "Sending = $message"
        )

        bluetoothGatt?.writeCharacteristic(
            characteristic
        )

        _messages.value += BleMessage(
            source = "Client", message = message
        )

        Log.d(
            TAG, "Write Request: $message"
        )
    }

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(
            gatt: BluetoothGatt, status: Int, newState: Int
        ) {

            when (newState) {

                BluetoothProfile.STATE_CONNECTED -> {

                    Log.d(
                        TAG, "Connected"
                    )

                    _isConnected.value = true

                    if (!hasBluetoothConnectPermission()) {
                        Log.e(
                            TAG, "Missing BLUETOOTH_CONNECT permission"
                        )
                        return
                    }

                    try {

                        gatt.discoverServices()

                    } catch (e: SecurityException) {

                        Log.e(
                            TAG, "discoverServices failed", e
                        )
                    }
                }

                BluetoothProfile.STATE_DISCONNECTED -> {

                    Log.d(
                        TAG, "Disconnected"
                    )

                    _isConnected.value = false
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(
            gatt: BluetoothGatt, status: Int
        ) {

            val descriptor = targetCharacteristic?.getDescriptor(
                java.util.UUID.fromString(
                    "00002902-0000-1000-8000-00805f9b34fb"
                )
            )

            descriptor?.let {

                it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE

                bluetoothGatt?.writeDescriptor(it)
            }

            gatt.services.forEach {

                Log.d(
                    TAG, "Discovered Service: ${it.uuid}"
                )
            }

            val service = gatt.getService(
                BleConstants.SERVICE_UUID
            )

            targetCharacteristic = service?.getCharacteristic(
                BleConstants.CHARACTERISTIC_UUID
            )

            if (targetCharacteristic == null) {

                Log.e(
                    TAG, "Characteristic NOT Found"
                )

            } else {

                Log.d(
                    TAG, "Characteristic Found"
                )
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic
        ) {

            val response = String(characteristic.value)

            Log.d(TAG, "Server Response: $response")

            _messages.value += BleMessage(
                source = "Server", message = response
            )
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int
        ) {

            val response = characteristic.value?.let {
                String(it)
            } ?: ""

            Log.d(
                TAG, "Read Response = $response"
            )

            _messages.value += BleMessage(
                source = "Server", message = response
            )
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int
        ) {

            Log.d(
                TAG, "Write Success"
            )
        }
    }
}