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
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.training.blueconnect.model.BleMessage
import com.training.blueconnect.model.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class BleClient(
    private val context: Context
) {

    companion object {
        private const val TAG = "BLE_CLIENT"
    }

    private var bluetoothGatt: BluetoothGatt? = null

    private var bltGattCharateristic: BluetoothGattCharacteristic? = null

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

    private fun hasBluetoothConnectPermission(): Boolean {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED

        } else {

            true
        }
    }

    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice) {

        _connectionState.value = ConnectionState.CONNECTING

        Log.d(TAG, "Connecting device's address :${device.address}")
        Log.d(TAG, "Connecting device's name : ${device.name}")
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

        val characteristic = bltGattCharateristic

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

        val characteristic = bltGattCharateristic

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

            Log.d("BLE_SERVER", "status=$status newState=$newState")

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e("BLE_SERVER", "BAD STATUS → disconnect likely")
            }

            when (newState) {

                BluetoothProfile.STATE_CONNECTED -> {

                    Log.d(
                        TAG, "Connected"
                    )

                    _isConnected.value = true

                    _connectionState.value =
                        ConnectionState.CONNECTED

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

                    _connectionState.value =
                        ConnectionState.DISCONNECTED
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(
            gatt: BluetoothGatt, status: Int
        ) {

            gatt.services.forEach {

                Log.d(
                    TAG, "Discovered Service: ${it.uuid}"
                )
            }

            val service = gatt.getService(
                BleConstants.SERVICE_UUID
            )

            bltGattCharateristic = service?.getCharacteristic(
                BleConstants.CHARACTERISTIC_UUID
            )

            bltGattCharateristic?.let { characteristic ->

                Log.d(TAG, "Enabling notifications")

                val notificationEnabled =
                    gatt.setCharacteristicNotification(
                        characteristic,
                        true
                    )

                Log.d(
                    TAG,
                    "setCharacteristicNotification = $notificationEnabled"
                )

                val descriptor = characteristic.getDescriptor(
                    UUID.fromString(
                        BleConstants.CCCD_UUID
                    )
                )

                if (descriptor == null) {

                    Log.e(
                        TAG,
                        "CCCD descriptor not found"
                    )

                } else {

                    descriptor.value =
                        BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE

                    gatt.writeDescriptor(
                        descriptor
                    )

                    Log.d(
                        TAG,
                        "CCCD write requested"
                    )
                }
            }

            if (bltGattCharateristic == null) {

                Log.e(
                    TAG, "Characteristic NOT Found"
                )

            } else {

                Log.d(
                    TAG, "Characteristic Found"
                )
                logger?.invoke(
                    "Characteristic found"
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

            logger?.invoke(
                "Received: $response"
            )
        }

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

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int
        ) {

            Log.d(
                TAG, "Write Success"
            )
        }
    }
}