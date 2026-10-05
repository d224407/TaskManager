package com.rk.taskmanager.settings

import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProVersion(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = context as? Activity
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pro features") },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Text("‹", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Testing build", style = MaterialTheme.typography.titleLarge)
            Text(
                "Pro functionality is enabled locally for testing. Payment and purchase verification are intentionally disabled.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Feature(Icons.Outlined.BatteryChargingFull, "Battery statistics", "Read the device battery charge-cycle counter.")
            Feature(Icons.Outlined.NetworkCheck, "Network monitor", "Monitor RX/TX totals and per-interface transfer rates.")
            Feature(Icons.Outlined.PushPin, "Process pinning", "Keep selected processes at the top of the process list.")
            Feature(Icons.Outlined.Notifications, "Usage notification", "Show live CPU/GPU usage in an ongoing notification.")
        }
    }
}

@Composable
private fun Feature(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
