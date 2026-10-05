package com.rk.taskmanager.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NetworkCheck
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
import kotlinx.coroutines.launch
import org.json.JSONObject

private data class NetInterface(val name: String, val totalBytes: Long)
private data class NetStats(val rx: Double = 0.0, val tx: Double = 0.0, val rxTotal: Long = 0, val txTotal: Long = 0)

@Composable
fun NetworkScreen(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var interfaces by remember { mutableStateOf<List<NetInterface>>(emptyList()) }
    var selected by remember { mutableStateOf<String?>(null) }
    var stats by remember { mutableStateOf(NetStats()) }

    LaunchedEffect(Unit) {
        daemon_messages.collectLatest { raw ->
            runCatching {
                val j = JSONObject(raw)
                when (j.optString("type")) {
                    "NET_INTERFACE_LIST" -> {
                        interfaces = buildList {
                            val a = j.optJSONArray("interfaces") ?: return@buildList
                            for (i in 0 until a.length()) {
                                val x = a.getJSONObject(i)
                                add(NetInterface(x.optString("name"), x.optLong("totalBytes")))
                            }
                        }
                        if (selected == null && interfaces.isNotEmpty()) selected = interfaces.first().name
                    }
                    "NET_STATS" -> stats = NetStats(
                        j.optDouble("rxBytesPerSec"),
                        j.optDouble("txBytesPerSec"),
                        j.optLong("rxBytes"),
                        j.optLong("txBytes")
                    )
                }
            }
        }
    }

    LaunchedEffect(selected, isConnected) {
        if (!isConnected) return@LaunchedEffect
        send_daemon_messages.emit(JSONObject().put("cmd", "LIST_NET_INTERFACES").toString())
        while (true) {
            selected?.let {
                send_daemon_messages.emit(
                    JSONObject().put("cmd", "NET_PING").put("interface", it).toString()
                )
            }
            delay(1000)
        }
    }

    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.NetworkCheck, null)
            Spacer(Modifier.width(8.dp))
            Text("Network monitor", style = MaterialTheme.typography.titleLarge)
        }

        if (interfaces.isEmpty()) {
            Text("No network interfaces available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(interfaces) { iface ->
                    val selectedNow = iface.name == selected
                    ElevatedCard(onClick = { selected = iface.name }, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            Text(iface.name, style = MaterialTheme.typography.titleMedium)
                            Text("Total: ${formatBytes(iface.totalBytes)}")
                            if (selectedNow) {
                                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                                Text("↓ ${formatRate(stats.rx)}")
                                Text("↑ ${formatRate(stats.tx)}")
                                Text("RX ${formatBytes(stats.rxTotal)}  •  TX ${formatBytes(stats.txTotal)}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatRate(bytes: Double): String = "${formatBytes(bytes.toLong())}/s"
private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val units = arrayOf("KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var index = -1
    while (value >= 1024 && index < units.lastIndex) {
        value /= 1024.0
        index++
    }
    return "%.1f %s".format(value, units[index])
}
