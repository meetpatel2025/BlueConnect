package com.training.blueconnect.uiscreen.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.training.blueconnect.model.BleDevice

@Composable
fun DeviceItem(
    device: BleDevice
) {

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = device.name
            )

            Text(
                text = device.address
            )

            Text(
                text = "RSSI: ${device.rssi}"
            )
        }
    }
}