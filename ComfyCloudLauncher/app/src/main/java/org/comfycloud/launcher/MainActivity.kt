package org.comfycloud.launcher

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import org.comfycloud.launcher.databinding.ActivityMainBinding

/**
 * A small "launcher" for Comfy Cloud App Mode links.
 *
 * IMPORTANT: this deliberately does NOT use a raw android.webkit.WebView.
 * Comfy Cloud supports "Sign in with Google", and Google blocks OAuth logins
 * performed inside embedded WebViews (the "disallowed_useragent" error) for
 * security reasons. Chrome Custom Tabs (androidx.browser) are treated as a
 * real browser by Google, so login works, and the session cookie is shared
 * with the device's normal Chrome profile -- so once you log in once, it
 * stays logged in across app launches, same as opening the site in Chrome.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var store: AppStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        store = AppStore(this)
        binding.addButton.setOnClickListener { showAddAppDialog() }
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun refreshList() {
        binding.appListContainer.removeAllViews()
        val apps = store.getApps()

        if (apps.isEmpty()) {
            val empty = TextView(this)
            empty.text = getString(R.string.empty_state)
            empty.setTextColor(ContextCompat.getColor(this, R.color.comfy_text_secondary))
            empty.setPadding(32, 32, 32, 32)
            binding.appListContainer.addView(empty)
            return
        }

        apps.forEach { entry -> addRow(entry) }
    }

    private fun addRow(entry: AppEntry) {
        val row = LayoutInflater.from(this)
            .inflate(R.layout.item_app, binding.appListContainer, false)

        row.findViewById<TextView>(R.id.appName).text = entry.name
        row.findViewById<TextView>(R.id.appUrl).text = entry.url

        row.setOnClickListener { openInCustomTab(entry.url) }
        row.findViewById<View>(R.id.deleteButton).setOnClickListener {
            store.removeApp(entry)
            refreshList()
        }

        binding.appListContainer.addView(row)
    }

    private fun openInCustomTab(url: String) {
        val colorParams = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(ContextCompat.getColor(this, R.color.comfy_background))
            .build()

        val customTabsIntent = CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(colorParams)
            .setShowTitle(true)
            .build()

        customTabsIntent.launchUrl(this, Uri.parse(url))
    }

    private fun showAddAppDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_app, null)
        val nameField = dialogView.findViewById<EditText>(R.id.nameField)
        val urlField = dialogView.findViewById<EditText>(R.id.urlField)

        AlertDialog.Builder(this)
            .setTitle(R.string.add_app_title)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = nameField.text.toString().trim()
                val url = urlField.text.toString().trim()
                if (name.isNotEmpty() && url.startsWith("http")) {
                    store.addApp(AppEntry(name, url))
                    refreshList()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
