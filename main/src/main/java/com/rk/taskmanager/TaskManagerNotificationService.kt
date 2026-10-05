package com.rk.taskmanager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.rk.taskmanager.daemon.daemon_messages
import com.rk.taskmanager.daemon.send_daemon_messages
import kotlinx.coroutines.*
import org.json.JSONObject
import kotlin.math.roundToInt

class TaskManagerNotificationService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var cpu = -1
    private var gpu = -1

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        scope.launch {
            daemon_messages.collect { raw ->
                runCatching {
                    val j = JSONObject(raw)
                    when (j.optString("type")) {
                        "CPU_USAGE" -> { cpu = j.optInt("usage", -1); update() }
                        "GPU_USAGE" -> { gpu = j.optInt("usage", -1); update() }
                    }
                }
            }
        }
        scope.launch {
            while (isActive) {
                send_daemon_messages.emit(JSONObject().put("cmd", "CPU_PING").toString())
                send_daemon_messages.emit(JSONObject().put("cmd", "GPU_PING").toString())
                delay(2000)
            }
        }
    }

    private fun update() {
        val text = buildString {
            if (cpu >= 0) append("CPU $cpu%")
            if (gpu >= 0) {
                if (isNotEmpty()) append("  •  ")
                append("GPU $gpu%")
            }
            if (isEmpty()) append("System monitoring active")
        }
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, buildNotification(text))
    }

    private fun buildNotification(text: String = "System monitoring active"): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(com.rk.taskmanager.R.drawable.speed_24px)
            .setContentTitle("Task Manager")
            .setContentText(text)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOnlyAlertOnce(true)
            .build()

    private fun createChannel() {
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Task Manager monitoring", NotificationManager.IMPORTANCE_LOW)
        )
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "task_manager_monitor"
        const val NOTIFICATION_ID = 1001
    }
}
