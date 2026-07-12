package com.quenan.duji.data

import android.content.Context
import org.json.JSONArray

data class ReleaseNoteEntry(
    val date: String,
    val title: String,
    val items: List<String>,
)

object ReleaseNotesRepository {
    fun load(context: Context): List<ReleaseNoteEntry> {
        val raw = context.assets.open("release_notes.json").bufferedReader().use { it.readText() }
        return runCatching {
            val array = JSONArray(raw)
            buildList(array.length()) {
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
        }.getOrDefault(emptyList())
    }

    fun latestVersionName(context: Context): String {
        return load(context).firstOrNull()?.title?.takeUnless { it.isBlank() } ?: "未知版本"
    }
}