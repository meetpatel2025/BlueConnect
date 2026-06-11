package com.training.blueconnect.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.os.ParcelUuid
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BleAdvertiser(
    private val bluetoothAdapter: BluetoothAdapter
) {
//    private var tempIsAdvertising = false

    private val advertiser: BluetoothLeAdvertiser?
            = bluetoothAdapter.bluetoothLeAdvertiser

    private val _isAdvertising =
        MutableStateFlow(false)

    val isAdvertising: StateFlow<Boolean>
            = _isAdvertising

    private val advertiseCallback =
        object : AdvertiseCallback() {

            override fun onStartSuccess(
                settingsInEffect: AdvertiseSettings?
            ) {
                _isAdvertising.value = true
                Log.d(
                    "ADVERTISE",
                    "Advertising started successfullyyy...."
                )
            }

            override fun onStartFailure(errorCode: Int) {

                val reason = when(errorCode) {

                    ADVERTISE_FAILED_ALREADY_STARTED ->
                        "ALREADY_STARTED"

                    ADVERTISE_FAILED_DATA_TOO_LARGE ->
                        "DATA_TOO_LARGE"

                    ADVERTISE_FAILED_FEATURE_UNSUPPORTED ->
                        "FEATURE_UNSUPPORTED"

                    ADVERTISE_FAILED_INTERNAL_ERROR ->
                        "INTERNAL_ERROR"

                    ADVERTISE_FAILED_TOO_MANY_ADVERTISERS ->
                        "TOO_MANY_ADVERTISERS"

                    else ->
                        "UNKNOWN_ERRORR"
                }

                Log.e(
                    "ADVERTISE",
                    "Advertising failed: $errorCode ($reason)"
                )
            }
        }



    @SuppressLint("MissingPermission")
    fun startAdvertising() {

        if (advertiser == null) {
            Log.d("ADVERTISE", "Advertiser is null")
            return
        }

        if (_isAdvertising.value) {
            Log.d("ADVERTISE", "Already advertising")
            return
        }

        // set device name
        bluetoothAdapter.name = BleConstants.DEVICE_NAME

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()

        val advertiseData = AdvertiseData.Builder()
            .setIncludeTxPowerLevel(true)
            .addServiceUuid(ParcelUuid(BleConstants.SERVICE_UUID))
            .build()

        val scanResponse = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .build()

        Log.d("ADVERTISE", "Device Name = ${bluetoothAdapter.name}")
        Log.d("ADVERTISE", "Service UUID = ${BleConstants.SERVICE_UUID}")
        Log.d("ADVERTISE", "Advertisinggg....")

        advertiser.startAdvertising(
            settings,
            advertiseData,
            scanResponse,
            advertiseCallback
        )
    }

    @SuppressLint("MissingPermission")
    fun stopAdvertising() {

        advertiser?.stopAdvertising(
            advertiseCallback
        )

        _isAdvertising.value = false
    }
}