package com.rk.taskmanager.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rk.taskmanager.daemon.daemon_messages
import com.rk.taskmanager.daemon.isConnected
import com.rk.taskmanager.daemon.send_daemon_messages
import kotlinx.coroutines.delay
import org.json.JSONObject

@Composable
fun BatteryScreen(modifier: Modifier = Modifier) {
    var cycles by remember { mutableStateOf(-1L) }

    LaunchedEffect(Unit) {
        daemon_messages.collect { raw ->
            runCatching {
                val j = JSONObject(raw)
                if (j.optString("type") == "CHARGE_CYCLES") cycles = j.optLong("cycles", -1)
            }
        }
    }

    LaunchedEffect(isConnected) {
        while (isConnected) {
            send_daemon_messages.emit(JSONObject().put("cmd", "BAT_CHARGE_CYCLES").toString())
            delay(5000)
        }
    }

    Column(
        modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.BatteryChargingFull, null)
            Spacer(Modifier.width(8.dp))
            Text("Battery statistics", style = MaterialTheme.typography.titleLarge)
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Charge cycles", style = MaterialTheme.typography.labelLarge)
                Text(
                    if (cycles >= 0) cycles.toString() else "Unavailable",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    "Reported by the device battery-cycle counter.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
