package org.comfycloud.launcher

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persists the user's saved Comfy Cloud App Mode links (name + URL) in
 * SharedPreferences as a small JSON array. No backend of our own is needed:
 * Comfy Cloud keeps the actual workflow/app state, we just remember the links.
 */
class AppStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getApps(): List<AppEntry> {
        val raw = prefs.getString(KEY_APPS, null) ?: return seedDefaults()
        val array = JSONArray(raw)
        val result = mutableListOf<AppEntry>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            result.add(AppEntry(obj.getString("name"), obj.getString("url")))
        }
        return result
    }

    fun addApp(entry: AppEntry) {
        val apps = getApps().toMutableList()
        apps.add(entry)
        saveApps(apps)
    }

    fun removeApp(entry: AppEntry) {
        val apps = getApps().toMutableList()
        apps.removeAll { it.name == entry.name && it.url == entry.url }
        saveApps(apps)
    }

    private fun saveApps(apps: List<AppEntry>) {
        val array = JSONArray()
        apps.forEach { entry ->
            val obj = JSONObject()
            obj.put("name", entry.name)
            obj.put("url", entry.url)
            array.put(obj)
        }
        prefs.edit().putString(KEY_APPS, array.toString()).apply()
    }

    private fun seedDefaults(): List<AppEntry> {
        val defaults = listOf(
            AppEntry("Comfy Cloud (inicio)", "https://cloud.comfy.org")
        )
        saveApps(defaults)
        return defaults
    }

    companion object {
        private const val PREFS_NAME = "comfy_cloud_launcher"
        private const val KEY_APPS = "apps"
    }
}
