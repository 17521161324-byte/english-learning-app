package com.personal.englishlearning.data

import com.personal.englishlearning.BuildConfig
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val changelog: String,
    val apkUrl: String,
) {
    val hasUpdate: Boolean get() = versionCode > BuildConfig.VERSION_CODE
}

class UpdateRepository {
    suspend fun check(): Result<UpdateInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val connection = URL(BuildConfig.UPDATE_CHANNEL_URL).openConnection() as HttpURLConnection
            connection.connectTimeout = 8_000
            connection.readTimeout = 8_000
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("Accept", "application/json")
            try {
                check(connection.responseCode in 200..299) { "HTTP ${connection.responseCode}" }
                val json = connection.inputStream.bufferedReader().use { JSONObject(it.readText()) }
                UpdateInfo(
                    versionCode = json.getInt("versionCode"),
                    versionName = json.getString("versionName"),
                    changelog = json.optString("changelog"),
                    apkUrl = json.optString("apkUrl", json.optString("downloadUrl")),
                )
            } finally {
                connection.disconnect()
            }
        }
    }
}
