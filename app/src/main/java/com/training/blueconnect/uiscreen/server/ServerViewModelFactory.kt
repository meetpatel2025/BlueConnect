package com.training.blueconnect.uiscreen.server

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
class ServerViewModelFactory(
    private val context: Context,
    private val bluetoothManager: BluetoothManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ServerViewModel(context, bluetoothManager) as T
    }
}