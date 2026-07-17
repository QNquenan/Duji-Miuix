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
        "https://gh-proxy.org/https://github.com/QNquenan/Duji-Miuix/raw/main/app/src/main/assets/release_notes.json"

    fun load(context: Context): List<ReleaseNoteEntry> {
        return runCatching {
            val raw = context.assets.open("release_notes.json").bufferedReader().use { it.readText() }
            parse(raw)
        }.getOrDefault(emptyList())
    }

    fun latestVersionName(context: Context): String {
        return latestVersionName(load(context)) ?: "未知版本"
    }

    suspend fun fetchLatestReleaseNote(): ReleaseNoteEntry? = withContext(Dispatchers.IO) {
        val requestUrl = "$REMOTE_RELEASE_NOTES_URL?_=${System.nanoTime()}"
        val connection = (URL(requestUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            instanceFollowRedirects = true
            useCaches = false
            defaultUseCaches = false
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Cache-Control", "no-cache, no-store, max-age=0")
            setRequestProperty("Pragma", "no-cache")
            setRequestProperty("Expires", "0")
            setRequestProperty("User-Agent", "DuJi-Android")
        }

        try {
            if (connection.responseCode !in 200..299) {
                throw IOException("HTTP ${connection.responseCode}")
            }
            val raw = connection.inputStream.bufferedReader().use { it.readText() }
            latestReleaseNote(parse(raw))
        } finally {
            connection.disconnect()
        }
    }

    suspend fun fetchLatestVersionName(): String? = fetchLatestReleaseNote()?.title

    fun isVersionNewer(remoteVersion: String, localVersion: String): Boolean {
        return compareVersionNames(remoteVersion, localVersion) > 0
    }

    fun versionTag(versionName: String): String? {
        val version = Regex("v?\\d+(?:\\.\\d+)*", RegexOption.IGNORE_CASE)
            .find(versionName)
            ?.value
            ?: return null
        return "v${version.removePrefix("v").removePrefix("V")}"
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
        return latestReleaseNote(entries)?.title
    }

    private fun latestReleaseNote(entries: List<ReleaseNoteEntry>): ReleaseNoteEntry? {
        return entries
            .asSequence()
            .filter { it.title.trim().isNotEmpty() }
            .maxWithOrNull(Comparator { first, second -> compareVersionNames(first.title, second.title) })
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
