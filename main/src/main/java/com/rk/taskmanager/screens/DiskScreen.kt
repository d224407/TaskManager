package com.rk.taskmanager.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rk.taskmanager.daemon.daemon_messages
import com.rk.taskmanager.daemon.isConnected
import com.rk.taskmanager.daemon.send_daemon_messages
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import org.json.JSONObject

private data class DiskDevice(val name: String, val readBytes: Long, val writeBytes: Long, val readRate: Double = 0.0, val writeRate: Double = 0.0)
private var previousDiskSample: Map<String, Triple<Long, Long, Long>> = emptyMap()

@Composable
fun DiskScreen(modifier: Modifier = Modifier) {
    var devices by remember { mutableStateOf<List<DiskDevice>>(emptyList()) }

    LaunchedEffect(Unit) {
        daemon_messages.collectLatest { raw ->
            runCatching {
                val j = JSONObject(raw)
                if (j.optString("type") == "DISK_STATS") {
                    val arr = j.optJSONArray("devices")
                    val now = System.currentTimeMillis()
                    val next = buildList {
                        if (arr != null) for (i in 0 until arr.length()) {
                            val d = arr.getJSONObject(i)
                            val name = d.optString("name")
                            val read = d.optLong("readBytes")
                            val write = d.optLong("writeBytes")
                            val prev = previousDiskSample[name]
                            val elapsed = if (prev != null) ((now - prev.third).coerceAtLeast(1) / 1000.0) else 1.0
                            val readRate = if (prev != null && read >= prev.first) (read - prev.first) / elapsed else 0.0
                            val writeRate = if (prev != null && write >= prev.second) (write - prev.second) / elapsed else 0.0
                            add(DiskDevice(name, read, write, readRate, writeRate))
                        }
                    }
                    previousDiskSample = next.associate { it.name to Triple(it.readBytes, it.writeBytes, now) }
                    devices = next
                }
            }
        }
    }

    LaunchedEffect(isConnected) {
        if (!isConnected) return@LaunchedEffect
        while (true) {
            send_daemon_messages.emit(JSONObject().put("cmd", "DISK_PING").toString())
            delay(1500)
        }
    }

    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Storage, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Disk monitor", style = MaterialTheme.typography.titleLarge)
        }
        Text("Read/write totals since boot", color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (devices.isEmpty()) {
            Text("No disk statistics available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(devices) { d ->
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(d.name, style = MaterialTheme.typography.titleMedium)
                            Text("Read: ${formatDiskBytes(d.readBytes)}")
                            Text("Written: ${formatDiskBytes(d.writeBytes)}")
                            Text("Read speed: ${formatDiskBytes(d.readRate.toLong())}/s")
                            Text("Write speed: ${formatDiskBytes(d.writeRate.toLong())}/s")
                        }
                    }
                }
            }
        }
    }
}

private fun formatDiskBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val units = arrayOf("KB", "MB", "GB", "TB", "PB")
    var value = bytes.toDouble()
    var i = -1
    while (value >= 1024 && i < units.lastIndex) { value /= 1024; i++ }
    return "%.1f %s".format(value, units[i])
}
