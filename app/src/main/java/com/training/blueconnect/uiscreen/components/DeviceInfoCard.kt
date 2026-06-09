package com.training.blueconnect.uiscreen.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.training.blueconnect.model.DeviceInfo

@Composable
fun DeviceInfoCard(
    info: DeviceInfo
) {

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = "Device Information",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Manufacturer: ${info.manufacturer}")
            Text("Model: ${info.model}")

            Spacer(modifier = Modifier.height(8.dp))

            Text("Android: ${info.androidVersion}")
            Text("SDK: ${info.sdkVersion}")

            Spacer(modifier = Modifier.height(8.dp))

            Text("Bluetooth Name: ${info.bluetoothName}")

            Text(
                text =
                    if (info.bluetoothEnabled)
                        "Bluetooth: Enabled"
                    else
                        "Bluetooth: Disabled"
            )
        }
    }
}