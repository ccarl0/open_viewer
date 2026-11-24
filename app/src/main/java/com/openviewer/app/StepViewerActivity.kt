package com.openviewer.app

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewAssetLoader

/**
 * Renders a STEP file fully offline: the file is parsed by OpenCascade
 * (occt-import-js, WebAssembly) and drawn with three.js, both bundled in assets.
 */
class StepViewerActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var fileUri: Uri

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri = intent.data ?: run { finish(); return }
        fileUri = uri
        val name = intent.getStringExtra(FileTypes.EXTRA_NAME) ?: queryName(uri)
        title = name

        if (intent.action == Intent.ACTION_VIEW) {
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) { }
        }
        Recents.add(this, uri.toString(), name)

        val loader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .addPathHandler("/model/") { _ ->
                try {
                    val input = contentResolver.openInputStream(fileUri)
                    WebResourceResponse("application/octet-stream", null, input)
                } catch (e: Exception) {
                    WebResourceResponse("text/plain", null, 404, "Not Found", null, null)
                }
            }
            .build()

        webView = WebView(this)
        setContentView(webView)
        webView.settings.apply {
            javaScriptEnabled = true
            allowFileAccess = false
            allowContentAccess = false
        }
        webView.addJavascriptInterface(Bridge(name), "OpenViewer")
        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? =
                loader.shouldInterceptRequest(request.url)

            // Never navigate away from the bundled viewer.
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = true
        }
        webView.loadUrl("https://appassets.androidplatform.net/assets/viewer/index.html")
    }

    override fun onDestroy() {
        if (::webView.isInitialized) webView.destroy()
        super.onDestroy()
    }

    private fun queryName(uri: Uri): String {
        try {
            contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
                if (it.moveToFirst()) it.getString(0)?.let { n -> return n }
            }
        } catch (_: Exception) { }
        return uri.lastPathSegment?.substringAfterLast('/') ?: "model.step"
    }

    private class Bridge(private val name: String) {
        @JavascriptInterface
        fun fileName(): String = name
    }
}
