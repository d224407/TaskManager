package com.rk.taskmanager

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data object UpToDate : UpdateState
    data object Failed : UpdateState
    data class Available(val version: String, val url: String) : UpdateState
}

object UpdateChecker {
    suspend fun check(currentVersion: String): UpdateState = withContext(Dispatchers.IO) {
        try {
            fetch(currentVersion)
        } catch (e: Exception) {
            UpdateState.Failed
        }
    }

    private fun fetch(currentVersion: String): UpdateState {
        val conn = URL("https://api.github.com/repos/${BuildConfig.GITHUB_REPO}/releases/latest")
            .openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            if (conn.responseCode != HttpURLConnection.HTTP_OK) return UpdateState.Failed

            val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
            val tag = json.getString("tag_name")
            if (!isNewer(tag, currentVersion)) return UpdateState.UpToDate

            // Prefer the APK asset, fall back to the release page
            var url = json.getString("html_url")
            val assets = json.optJSONArray("assets")
            for (i in 0 until (assets?.length() ?: 0)) {
                val asset = assets!!.getJSONObject(i).optString("browser_download_url")
                if (asset.endsWith(".apk")) {
                    url = asset
                    break
                }
            }
            return UpdateState.Available(tag.removePrefix("v"), url)
        } finally {
            conn.disconnect()
        }
    }

    private fun parts(v: String) =
        v.removePrefix("v").substringBefore('-').split('.').map { it.toIntOrNull() ?: 0 }

    fun isNewer(remote: String, local: String): Boolean {
        val r = parts(remote)
        val l = parts(local)
        for (i in 0 until maxOf(r.size, l.size)) {
            val a = r.getOrElse(i) { 0 }
            val b = l.getOrElse(i) { 0 }
            if (a != b) return a > b
        }
        return false
    }
}
