package com.training.blueconnect.model

data class BleDevice(
    val name: String,
    val address: String,
    val rssi: Int
)