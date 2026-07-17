package com.quenan.duji.data

import android.content.Context
import org.json.JSONArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class ReleaseNoteEntry(
    val date: String,
    val title: String,
    val items: List<String>,
)

object ReleaseNotesRepository {
    private const val REMOTE_RELEASE_NOTES_URL =
        "https://gh-proxy.org/https://github.com/QNquenan/Duji-Miuix/blob/main/app/src/main/assets/release_notes.json"

    fun load(context: Context): List<ReleaseNoteEntry> {
        return runCatching {
            val raw = context.assets.open("release_notes.json").bufferedReader().use { it.readText() }
            parse(raw)
        }.getOrDefault(emptyList())
    }

    fun latestVersionName(context: Context): String {
        return latestVersionName(load(context)) ?: "未知版本"
    }

    suspend fun fetchLatestVersionName(): String? = withContext(Dispatchers.IO) {
        val connection = (URL(REMOTE_RELEASE_NOTES_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "DuJi-Android")
        }

        try {
            if (connection.responseCode !in 200..299) {
                throw IOException("HTTP ${connection.responseCode}")
            }
            val raw = connection.inputStream.bufferedReader().use { it.readText() }
            latestVersionName(parse(raw))
        } finally {
            connection.disconnect()
        }
    }

    fun isVersionNewer(remoteVersion: String, localVersion: String): Boolean {
        return compareVersionNames(remoteVersion, localVersion) > 0
    }

    private fun parse(raw: String): List<ReleaseNoteEntry> {
        val array = JSONArray(raw)
        return buildList(array.length()) {
            for (index in 0 until array.length()) {
                val obj = array.getJSONObject(index)
                val itemsArray = obj.optJSONArray("items") ?: JSONArray()
                add(
                    ReleaseNoteEntry(
                        date = obj.optString("date"),
                        title = obj.optString("title"),
                        items = buildList(itemsArray.length()) {
                            for (itemIndex in 0 until itemsArray.length()) {
                                add(itemsArray.optString(itemIndex))
                            }
                        },
                    )
                )
            }
        }
    }

    private fun latestVersionName(entries: List<ReleaseNoteEntry>): String? {
        return entries
            .asSequence()
            .map { it.title.trim() }
            .filter { it.isNotEmpty() }
            .maxWithOrNull(Comparator { first, second -> compareVersionNames(first, second) })
    }

    private fun compareVersionNames(first: String, second: String): Int {
        val firstParts = versionParts(first)
        val secondParts = versionParts(second)
        if (firstParts.isEmpty() || secondParts.isEmpty()) {
            return first.compareTo(second, ignoreCase = true)
        }

        for (index in 0 until maxOf(firstParts.size, secondParts.size)) {
            val firstPart = firstParts.getOrElse(index) { 0 }
            val secondPart = secondParts.getOrElse(index) { 0 }
            if (firstPart != secondPart) return firstPart.compareTo(secondPart)
        }
        return 0
    }

    private fun versionParts(version: String): List<Int> {
        val numericPart = Regex("\\d+(?:\\.\\d+)*").find(version)?.value ?: return emptyList()
        return numericPart.split('.').map { it.toInt() }
    }
}
