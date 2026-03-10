package com.ita24.yumly

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Patterns
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object RecipeWebsite {

    fun getName(id: Int): String {
        return "https://theodor-litt-schule-neumunster.github.io/yumly/recipes/" + id + ".html"
    }

    fun sendToWebsite(context: Context, id: Int) {
        val url = getName(id)
        openUrlInWebView(context, url)
    }

    private fun openUrlInWebView(context: Context, url: String) {
        val intent = Intent(context, RecipeWebViewActivity::class.java).apply {
            putExtra("RECIPE_URL", url)
        }
        context.startActivity(intent)
    }

    fun openSource(context: Context, source: String) {
        val trimmed = source.trim()
        if (trimmed.isEmpty()) return

        val isWebUrl = trimmed.startsWith("http://", ignoreCase = true) ||
                       trimmed.startsWith("https://", ignoreCase = true) ||
                       Patterns.WEB_URL.matcher(trimmed).matches()

        if (isWebUrl && !trimmed.startsWith("/") && !trimmed.startsWith("content://") && !trimmed.startsWith("file://")) {
            var url = trimmed
            if (!url.startsWith("http://", ignoreCase = true) && !url.startsWith("https://", ignoreCase = true)) {
                url = "https://$url"
            }
            openUrlInWebView(context, url)
        } else {
            // Es ist eine Datei
            try {
                val file = if (trimmed.startsWith("/")) {
                    File(trimmed) // Absoluter Pfad
                } else {
                    File(context.filesDir, trimmed) // Nur Dateiname
                }

                if (file.exists()) {
                    // Erstelle eine sichere URI über den FileProvider
                    val contentUri = FileProvider.getUriForFile(
                        context,
                        "com.ita24.yumly.fileprovider",
                        file
                    )

                    // MIME-Typ anhand der Dateiendung bestimmen
                    val extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString())
                    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase()) 
                                   ?: "application/octet-stream"

                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(contentUri, mimeType)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    
                    val chooser = Intent.createChooser(intent, context.getString(R.string.open_recipe_with))
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(chooser)
                } else {
                    // Fallback für alte content:// URIs
                    val uri = Uri.parse(trimmed)
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, context.contentResolver.getType(uri) ?: "application/*")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.open_recipe_with)))
                }
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.error_open_file), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
