package com.training.blueconnect.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BleGattServer(
    private val context: Context, private val bluetoothManager: BluetoothManager
) {

    private var gattServer: BluetoothGattServer? = null
    private var connectedDevice: BluetoothDevice? = null

    private val _connectedDeviceAddress = MutableStateFlow<String?>(null)

    val connectedDeviceAddress: StateFlow<String?> = _connectedDeviceAddress

    private val _receivedMessages = MutableStateFlow<List<String>>(emptyList())

    val receivedMessages: StateFlow<List<String>> = _receivedMessages


    private val _isClientConnected = MutableStateFlow(false)

    val isClientConnected: StateFlow<Boolean> = _isClientConnected

    private val _serverLogs = MutableStateFlow<List<String>>(emptyList())

    val serverLogs: StateFlow<List<String>> = _serverLogs

    private var streamingJob: kotlinx.coroutines.Job? = null

    private val scope = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.Dispatchers.IO
    )

    private lateinit var writeCharacteristic: BluetoothGattCharacteristic

    private val _toastMessage =
        MutableStateFlow<String?>(null)

    val toastMessage: StateFlow<String?> =
        _toastMessage

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
            context, Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun startServer() {

        if (!hasBluetoothPermission()) {
            Log.e("BLE", "Missing BLUETOOTH_CONNECT permission")
            return
        }
        Log.d(
            "BLE", "Opening GATT Server..."
        )

        gattServer = bluetoothManager.openGattServer(
            context, gattServerCallback
        )

        Log.d(
            "BLE", "GattServer object = $gattServer"
        )

        if (gattServer == null) {

            Log.e(
                "BLE", "Failed to create GATT Server"
            )

            return
        }

        val service = BluetoothGattService(
            BleConstants.SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY
        )


        val characteristic = BluetoothGattCharacteristic(
            BleConstants.CHARACTERISTIC_UUID,

            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_NOTIFY,

            BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        val cccd = BluetoothGattDescriptor(
            java.util.UUID.fromString(
                "00002902-0000-1000-8000-00805f9b34fb"
            ), BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
        )


        characteristic.addDescriptor(
            cccd
        )
        characteristic.value = "Server Ready".toByteArray()

        service.addCharacteristic(
            characteristic
        )

//        writeCharacteristic.addDescriptor(cccd)
//        writeCharacteristic.value =
//            "Server Ready".toByteArray()

//        service.addCharacteristic(
//            characteristic
//        )
        gattServer?.addService(
            service
        )

        Log.d(
            "BLE", "Service Added"
        )

        Log.d(
            "BLE", "Service Registered"
        )
    }

    @SuppressLint("MissingPermission")
    fun stopServer() {

        if (!hasBluetoothPermission()) return

        gattServer?.close()
        gattServer = null
        connectedDevice = null
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(
            device: BluetoothDevice, status: Int, newState: Int
        ) {

            Log.d(
                "BLE_SERVER",
                "Connection State Changed " + "status=$status " + "newState=$newState " + "device=${device.address}"
            )

            when (newState) {

                BluetoothProfile.STATE_CONNECTED -> {

                    val deviceName =
                        device.name ?: "Unknown Device"

                    _toastMessage.value =
                        "$deviceName Connected Successfully!!"

                    connectedDevice = device

                    _isClientConnected.value = true

                    _connectedDeviceAddress.value = device.address

                    addLog(
                        "Connected: ${device.address}"
                    )
                }

                BluetoothProfile.STATE_DISCONNECTED -> {

                    connectedDevice = null

                    _isClientConnected.value = false

                    _connectedDeviceAddress.value = null

                    _receivedMessages.value = emptyList()

                    val deviceName =
                        device.name ?: "Unknown Device"

                    _toastMessage.value =
                        "$deviceName Disconnected & Messages Cleared"

                    Log.d(
                        "BLE", "Device Disconnected"
                    )
                    addLog(
                        "Disconnected: ${device.address}"
                    )

                }
            }


        }

        override fun onMtuChanged(
            device: BluetoothDevice, mtu: Int
        ) {
            Log.d(
                "BLE_SERVER", "MTU changed: $mtu"
            )
        }

//            @SuppressLint("MissingPermission")
//            fun handleCharacteristicReadRequest(
//                device: BluetoothDevice,
//                requestId: Int,
//                responseNeeded: Boolean
//            ) {
//                if (responseNeeded) {
//                    gattServer?.sendResponse(
//                        device,
//                        requestId,
//                        BluetoothGatt.GATT_SUCCESS,
//                        0,
//                        "Ram Ram From Server".toByteArray()
//                    )
//                }
//            }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicReadRequest(
            device: BluetoothDevice,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {

            Log.d(
                "BLE", "Read Request Received"
            )

            gattServer?.sendResponse(
                device,
                requestId,
                BluetoothGatt.GATT_SUCCESS,
                0,
                "Ram Ram From Server".toByteArray()
            )
        }

        @SuppressLint("MissingPermission")
        override fun onDescriptorWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            descriptor: BluetoothGattDescriptor,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray
        ) {
            Log.d("BLE_SERVER", "CCCD written: ${value.contentToString()}")

            if (responseNeeded) {
                gattServer?.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    0,
                    value
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

            Log.d(
                "BLE_SERVER", "Write Request Received Message: $received"
            )

            Log.d(
                "BLE_SERVER", "UUID = ${characteristic.uuid}"
            )

            Log.d(
                "BLE_SERVER", "Bytes = ${value.size}"
            )
            addReceivedMessage(received)

            if (preparedWrite) {
                Log.d("BLE_SERVER", "Prepared write ignored (simple mode)")
                return
            }

            val responseMessage = "ACK from Server: $received"

            characteristic.value = responseMessage.toByteArray()

            if (responseNeeded) {

                gattServer?.sendResponse(
                    device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value
                )
            }

//                gattServer?.notifyCharacteristicChanged(
//                    device,
//                    characteristic,
//                    false
//                )

            Log.d(
                "BLE", "Response sent: $responseMessage"
            )
        }

        override fun onServiceAdded(
            status: Int, service: BluetoothGattService
        ) {
            Log.d(
                "BLE", "Service Registered: ${service.uuid}"
            )

            Log.d(
                "BLE", "Status = $status"
            )

        }
    }

    @SuppressLint("MissingPermission")
    fun notifyClient(message: String) {

        val service = gattServer?.getService(BleConstants.SERVICE_UUID)
        val characteristic = service?.getCharacteristic(BleConstants.CHARACTERISTIC_UUID)

        if (service == null || characteristic == null) {
            Log.e("BLE", "Service/Characteristic null")
            return
        }

        characteristic.value = message.toByteArray()

        connectedDevice?.let { device ->

            Log.d("BLE", "Sending notification: $message")

            gattServer?.notifyCharacteristicChanged(
                device, characteristic, false
            )
        }
    }

    //    @SuppressLint("MissingPermission")
//    fun sendRandomNotification() {
//
//        val service =
//            gattServer?.getService(BleConstants.SERVICE_UUID)
//
//        val characteristic =
//            service?.getCharacteristic(BleConstants.CHARACTERISTIC_UUID)
//
//        if (service == null || characteristic == null) {
//            Log.e("BLE", "Service or Characteristic null")
//            return
//        }
//
//        if (connectedDevice == null) {
//            Log.e("BLE", "No device connected")
//            return
//        }
//
//        val randomData = "DATA_${(1000..9999).random()}"
//
//        characteristic.value = randomData.toByteArray()
//
//        Log.d("BLE_SERVER", "Sending notification: $randomData")
//
//        gattServer?.notifyCharacteristicChanged(
//            connectedDevice,
//            characteristic,
//            false
//        )
//    }
    @SuppressLint("MissingPermission")
    fun startRandomNotificationStream() {

        if (streamingJob != null) {
            Log.d("BLE", "Already streaming")
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

        if (connectedDevice == null) {
            Log.e("BLE", "No device connected")
            return
        }

        streamingJob = scope.launch {

            Log.d("BLE", "Started streaming notifications")

            while (isActive) {

                val value = "TEMP:${(10..45).random()}"

                characteristic.value = value.toByteArray()

                gattServer?.notifyCharacteristicChanged(
                    connectedDevice,
                    characteristic,
                    false
                )

                Log.d("BLE", "Sent: $value")

                delay(2000)
            }
        }
    }

    fun stopRandomNotificationStream() {

        streamingJob?.cancel()
        streamingJob = null

        Log.d("BLE", "Stopped streaming")
    }

    fun isStreaming(): Boolean {
        return streamingJob != null
    }

    private fun hasBluetoothPermission(): Boolean {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            ContextCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED

        } else {

            true
        }
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }
}