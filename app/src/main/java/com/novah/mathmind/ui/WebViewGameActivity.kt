package com.novah.mathmind.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.novah.mathmind.NovahMathMindApplication
import com.novah.mathmind.databinding.ActivityWebviewGameBinding
import com.novah.mathmind.ui.developer.AddGameViewModel
import com.novah.mathmind.ui.developer.AddGameViewModelFactory
import kotlinx.coroutines.launch

/**
 * Activity responsible for rendering custom HTML/CSS/JS games in a WebView.
 */
class WebViewGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebviewGameBinding
    private val addGameViewModel: AddGameViewModel by viewModels {
        AddGameViewModelFactory((application as NovahMathMindApplication).repository)
    }

    companion object {
        const val EXTRA_CUSTOM_GAME_ID = "extra_custom_game_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebviewGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val customGameId = intent.getLongExtra(EXTRA_CUSTOM_GAME_ID, -1L)
        if (customGameId == -1L) {
            finish() // Invalid game ID, close activity
            return
        }

        setupWebView()
        loadCustomGame(customGameId)
    }

    /**
     * Configures the WebView settings for optimal game rendering.
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webView.apply {
            webViewClient = WebViewClient() // Handle page navigation within WebView
            webChromeClient = WebChromeClient() // Handle JavaScript dialogs, favicons, titles
            settings.apply {
                javaScriptEnabled = true // Essential for JS games
                domStorageEnabled = true // Enable HTML5 DOM storage (local storage, session storage)
                loadWithOverviewMode = true // Load content entirely zoomed out
                useWideViewPort = true // Allow content to span full width
                allowFileAccess = false // Prevent access to local files for security
                // Ensure no caching for dynamic content
                cacheMode = WebSettings.LOAD_NO_CACHE
            }
        }
    }

    /**
     * Fetches custom game data from the database and loads it into the WebView.
     */
    private fun loadCustomGame(gameId: Long) {
        lifecycleScope.launch {
            val customGame = addGameViewModel.getCustomGameById(gameId)
            if (customGame != null) {
                title = customGame.title // Set activity title to game title
                val combinedHtml = generateCombinedHtml(customGame.htmlCode, customGame.cssCode, customGame.jsCode)
                // Load the HTML content. base URL is null as we're loading raw data.
                binding.webView.loadDataWithBaseURL(null, combinedHtml, "text/html", "UTF-8", null)
            } else {
                finish() // Game not found
            }
        }
    }

    /**
     * Combines HTML, CSS, and JavaScript into a single, renderable HTML string.
     */
    private fun generateCombinedHtml(html: String, css: String, js: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <style>
                    body { margin: 0; padding: 0; overflow: hidden; height: 100vh; width: 100vw; }
                    $css
                </style>
            </head>
            <body>
                $html
                <script>
                    $js
                </script>
            </body>
            </html>
        """.trimIndent()
    }

    /**
     * Allows WebView to navigate back in its history, or the activity to finish.
     */
    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
