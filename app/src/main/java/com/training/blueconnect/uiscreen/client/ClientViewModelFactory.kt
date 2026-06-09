package com.training.blueconnect.uiscreen.client

import android.bluetooth.BluetoothAdapter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.training.blueconnect.ble.BleClient

class ClientViewModelFactory(
    private val bluetoothAdapter: BluetoothAdapter,
    private val bleClient: BleClient
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        return ClientViewModel(
            bluetoothAdapter,
            bleClient
        ) as T
    }
}