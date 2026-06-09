package com.training.blueconnect.util

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import com.training.blueconnect.model.DeviceInfo
import android.content.pm.PackageManager

object DeviceInfoProvider {
    fun getDeviceInfo(context: Context): DeviceInfo {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

        val hasConnectPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val isEnabled = if (hasConnectPermission) bluetoothAdapter?.isEnabled ?: false else false
        val deviceName = if (hasConnectPermission) bluetoothAdapter?.name ?: "Unknown" else "Permission Denied"

        return DeviceInfo(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            sdkVersion = Build.VERSION.SDK_INT,
            bluetoothEnabled = isEnabled,
            bluetoothName = deviceName
        )
    }

}