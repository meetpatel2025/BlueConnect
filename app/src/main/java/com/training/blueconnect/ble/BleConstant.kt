package com.training.blueconnect.ble


import java.util.UUID

object BleConstants {

    const val DEVICE_NAME = "BLE_PERIPHERAL"

    val SERVICE_UUID: UUID =
        UUID.fromString("12345678-1234-1234-1234-123456789000")

    val CHARACTERISTIC_UUID: UUID =
        UUID.fromString("12345678-1234-1234-1234-123456789001")

    val CHAR_WRITE_UUID = UUID.fromString("12345678-1234-1234-1234-123456789001")

    val CHAR_NOTIFY_UUID = UUID.fromString("12345678-1234-1234-1234-123456789002")
}