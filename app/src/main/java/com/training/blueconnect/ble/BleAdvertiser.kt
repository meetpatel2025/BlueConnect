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
            }

            override fun onStartFailure(
                errorCode: Int
            ) {
                _isAdvertising.value = false
            }
        }

    @SuppressLint("MissingPermission")
    fun startAdvertising() {

        if (advertiser == null) {
            Log.d("ADVERTISE", "Advertiser is null")
            return
        }

        bluetoothAdapter.name =
            BleConstants.DEVICE_NAME

        val settings =
            AdvertiseSettings.Builder()
                .setAdvertiseMode(
                    AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY
                )
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(
                    AdvertiseSettings.ADVERTISE_TX_POWER_HIGH
                )
                .build()

        val data =
            AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(
                    ParcelUuid(
                        BleConstants.SERVICE_UUID
                    )
                )
                .build()
        Log.d("ADVERTISE", "Advertising....")

        advertiser.startAdvertising(
            settings,
            data,
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