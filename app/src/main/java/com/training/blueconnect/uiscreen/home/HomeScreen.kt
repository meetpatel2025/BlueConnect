package com.training.blueconnect.uiscreen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onClientClick: () -> Unit,
    onServerClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),

        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "BLE Practice Lab",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onClientClick,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Client Mode")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onServerClick,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Server Mode")
        }
    }
}