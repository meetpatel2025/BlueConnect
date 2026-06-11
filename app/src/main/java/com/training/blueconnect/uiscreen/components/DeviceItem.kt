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
                text =
                    if (device.name.isBlank() ||
                        device.name == "Unknown Device")
                        "Unknown BLE Device"
                    else
                        "${device.name}"
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = device.address
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = "Signal: ${device.rssi} dBm"
            )
        }
    }
}