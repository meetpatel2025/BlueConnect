package com.training.blueconnect.model

data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val sdkVersion: Int,
    val bluetoothEnabled: Boolean,
    val bluetoothName: String
)
