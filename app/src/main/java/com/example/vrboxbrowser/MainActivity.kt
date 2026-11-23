package com.example.vrboxbrowser

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : AppCompatActivity() {

    private lateinit var topBar: LinearLayout
    private lateinit var webLeft: WebView
    private lateinit var webRight: WebView
    private lateinit var etUrl: EditText
    private lateinit var btnBack: Button
    private lateinit var btnGo: Button
    private lateinit var btnVrMode: Button
    private lateinit var btnExitVr: Button

    private var isSyncingScroll = false
    private var isVrFullscreen = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)

        val root = findViewById<View>(R.id.rootLayout)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        topBar = findViewById(R.id.topBar)
        webLeft = findViewById(R.id.webLeft)
        webRight = findViewById(R.id.webRight)
        etUrl = findViewById(R.id.etUrl)
        btnBack = findViewById(R.id.btnBack)
        btnGo = findViewById(R.id.btnGo)
        btnVrMode = findViewById(R.id.btnVrMode)
        btnExitVr = findViewById(R.id.btnExitVr)

        configureWebView(webLeft, isPrimary = true)
        configureWebView(webRight, isPrimary = false)

        webLeft.setOnScrollChangeListener { _, scrollX, scrollY, _, _ ->
            if (!isSyncingScroll) {
                isSyncingScroll = true
                webRight.scrollTo(scrollX, scrollY)
                isSyncingScroll = false
            }
        }

        webRight.setOnScrollChangeListener { _, scrollX, scrollY, _, _ ->
            if (!isSyncingScroll) {
                isSyncingScroll = true
                webLeft.scrollTo(scrollX, scrollY)
                isSyncingScroll = false
            }
        }

        btnGo.setOnClickListener { openUrl(etUrl.text.toString()) }
        etUrl.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE) {
                openUrl(etUrl.text.toString())
                true
            } else {
                false
            }
        }

        btnBack.setOnClickListener {
            if (webLeft.canGoBack()) {
                webLeft.goBack()
                webRight.goBack()
            }
        }

        btnVrMode.setOnClickListener { setVrFullscreen(true) }
        btnExitVr.setOnClickListener { setVrFullscreen(false) }

        val defaultUrl = "https://m.youtube.com"
        etUrl.setText(defaultUrl)
        loadBoth(defaultUrl)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView(webView: WebView, isPrimary: Boolean) {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            cacheMode = WebSettings.LOAD_DEFAULT
        }
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (isPrimary && url != null) {
                    etUrl.setText(url)
                    if (webRight.url != url) {
                        webRight.loadUrl(url)
                    }
                }
            }
        }
    }

    private fun openUrl(rawInput: String) {
        var url = rawInput.trim()
        if (url.isEmpty()) return
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = if (url.contains(".")) "https://$url" else "https://www.google.com/search?q=$url"
        }
        etUrl.setText(url)
        loadBoth(url)
    }

    private fun loadBoth(url: String) {
        webLeft.loadUrl(url)
        webRight.loadUrl(url)
    }

    private fun setVrFullscreen(enable: Boolean) {
        isVrFullscreen = enable
        topBar.visibility = if (enable) View.GONE else View.VISIBLE
        btnExitVr.visibility = if (enable) View.VISIBLE else View.GONE

        val controller = WindowCompat.getInsetsController(window, window.decorView)
        if (enable) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    override fun onDestroy() {
        webLeft.destroy()
        webRight.destroy()
        super.onDestroy()
    }
}
