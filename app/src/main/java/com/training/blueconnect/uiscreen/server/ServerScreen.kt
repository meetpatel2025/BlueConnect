package com.training.blueconnect.uiscreen.server

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.training.blueconnect.ble.BleCapabilityChecker
import com.training.blueconnect.uiscreen.components.DeviceInfoCard
import com.training.blueconnect.util.DeviceInfoProvider

@Composable
fun ServerScreen() {

    val context = LocalContext.current

    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    val adapter = bluetoothManager.adapter

    val deviceInfo = remember {
        DeviceInfoProvider.getDeviceInfo(context)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    fun requiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    val vm: ServerViewModel = viewModel(
        factory = ServerViewModelFactory(
            context = context, bluetoothManager = bluetoothManager
        )
    )

    val isServerRunning by vm.isServerRunning.collectAsState()

    val logs by vm.logs.collectAsState()

    val isClientConnected by vm.isClientConnected.collectAsState()

    val connectedDeviceAddress by vm.connectedDeviceAddress.collectAsState()

    val serverLogs by vm.serverLogs.collectAsState()

    val receivedMessages by vm.receivedMessages.collectAsState()

    val isAdvertising by vm.isAdvertising.collectAsState()

    val advertiserAvailable = remember { BleCapabilityChecker.isBleAdvertiserAvailable(adapter) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {

        Text(
            text = "Server Mode", style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        DeviceInfoCard(
            info = deviceInfo
        )

        Spacer(modifier = Modifier.height(16.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "Server State", style = MaterialTheme.typography.titleMedium
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = if (isServerRunning) "GATT Server: Running ✅"
                    else "GATT Server: Stopped ❌"
                )

                Text(
                    text = if (isAdvertising) "Advertising: Active ✅"
                    else "Advertising: Inactive ❌"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "Connection Status", style = MaterialTheme.typography.titleMedium
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = if (isClientConnected) "Connected 🟢"
                    else "Disconnected 🔴"
                )

                Text(
                    text = connectedDeviceAddress ?: "No Device Connected"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "Received Messages", style = MaterialTheme.typography.titleMedium
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                if (receivedMessages.isEmpty()) {

                    Text(
                        text = "No messages yet"
                    )

                } else {

                    receivedMessages.forEach {

                        Text(
                            text = "• $it"
                        )
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = if (advertiserAvailable) "Advertiser: Available ✅"
            else "Advertiser: Not Available ❌"
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isAdvertising) "Status: Advertising 🟢"
            else "Status: Stopped 🔴"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                permissionLauncher.launch(requiredPermissions())
            }, modifier = Modifier.fillMaxWidth()
        ) {
            Text("Grant Permissions")
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                vm.startServer()
            }, modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start GATT Server")
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                vm.stopServer()
            }, modifier = Modifier.fillMaxWidth()
        ) {
            Text("Stop GATT Server")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                vm.sendNotification()
            }) {
            Text("Send Notification")
        }
        Spacer(modifier = Modifier.height(24.dp))

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "Server Logs", style = MaterialTheme.typography.titleLarge
        )

        logs.forEach {

            Text(
                text = "• $it"
            )
        }
    }
}