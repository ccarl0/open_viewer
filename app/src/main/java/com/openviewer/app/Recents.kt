package com.openviewer.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class RecentFile(val uri: String, val name: String, val openedAt: Long)

/** Recently opened files, stored locally on the device only. */
object Recents {
    private const val PREFS = "recents"
    private const val KEY = "items"
    private const val MAX = 50

    fun load(context: Context): List<RecentFile> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "[]")
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map {
                val o = arr.getJSONObject(it)
                RecentFile(o.getString("uri"), o.getString("name"), o.getLong("at"))
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun add(context: Context, uri: String, name: String) {
        val list = load(context).filterNot { it.uri == uri }.toMutableList()
        list.add(0, RecentFile(uri, name, System.currentTimeMillis()))
        save(context, list.take(MAX))
    }

    fun remove(context: Context, uri: String) =
        save(context, load(context).filterNot { it.uri == uri })

    fun clear(context: Context) = save(context, emptyList())

    private fun save(context: Context, list: List<RecentFile>) {
        val arr = JSONArray()
        list.forEach { arr.put(JSONObject().put("uri", it.uri).put("name", it.name).put("at", it.openedAt)) }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, arr.toString()).apply()
    }
}
