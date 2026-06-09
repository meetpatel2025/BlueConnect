package com.training.blueconnect.uiscreen.client

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.training.blueconnect.ble.BleCapabilityChecker
import com.training.blueconnect.ble.BleClient
import com.training.blueconnect.model.ConnectionState
import com.training.blueconnect.uiscreen.components.DeviceInfoCard
import com.training.blueconnect.uiscreen.components.DeviceItem
import com.training.blueconnect.util.DeviceInfoProvider

@Composable
fun ClientScreen() {

    val context = LocalContext.current

    val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    val adapter = bluetoothManager.adapter
    val deviceInfo = remember {
        DeviceInfoProvider.getDeviceInfo(context)
    }
    val bleClient = remember {
        BleClient(context)
    }

    var message by remember {
        mutableStateOf("")
    }

    val vm: ClientViewModel = viewModel(
        factory = ClientViewModelFactory(adapter, bleClient)
    )

    val connectionState by vm.bleClient.connectionState.collectAsState()

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { }

    fun requiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    val devices by vm.devices.collectAsState()

    val scannerAvailable =
        remember { BleCapabilityChecker.isBleScannerAvailable(adapter) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Client Mode",
                style = MaterialTheme.typography.headlineMedium
            )
        }
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {

                Column(Modifier.padding(16.dp)) {

                    Text("Connection State")

                    Spacer(Modifier.height(8.dp))

                    Text(
                        when (connectionState) {
                            ConnectionState.IDLE -> "Idle ⚪"
                            ConnectionState.CONNECTING -> "Connecting 🟡"
                            ConnectionState.CONNECTED -> "Connected 🟢"
                            ConnectionState.DISCONNECTED -> "Disconnected 🔴"
                        }
                    )
                }
            }
        }
        item {
            DeviceInfoCard(info = deviceInfo)
        }
        item {
            Text(
                if (scannerAvailable)
                    "Scanner Available ✅"
                else
                    "Scanner Not Available ❌"
            )
        }
        item {
            Button(
                onClick = {
                    permissionLauncher.launch(requiredPermissions())
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant Permissions")
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Button(onClick = { vm.startScan() }) {
                    Text("Start Scan")
                }

                Button(onClick = { vm.stopScan() }) {
                    Text("Stop Scan")
                }
            }
        }
        items(devices) { device ->

            DeviceItem(device)

            Button(
                onClick = {
                    vm.connect(device.address)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Connect")
            }
        }
        item {

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Message") }
            )
        }
        item {

            Button(
                onClick = {
                    vm.sendMessage(message)
                    message = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send To Server")
            }
        }

        item {

            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(Modifier.padding(12.dp)) {

                    Text("BLE Logs")

                    vm.logs.collectAsState().value.forEach {
                        Text("• $it")
                    }
                }
            }
        }

    }
}