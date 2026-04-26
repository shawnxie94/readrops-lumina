package com.readrops.app.lumina

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.readrops.app.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LuminaWebArticleActivity : ComponentActivity(), KoinComponent {

    private val luminaShareRepository: LuminaShareRepository by inject()
    private var extractionJob: Job? = null
    private var uploaded = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent.getStringExtra(EXTRA_URL).orEmpty()
        if (url.isBlank()) {
            Toast.makeText(this, R.string.lumina_no_link_found, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val webView = WebView(this)
        webView.addJavascriptInterface(ArticleBridge(url), BRIDGE_NAME)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, pageUrl: String) {
                scheduleExtraction(view)
            }
        }

        setContentView(webView)
        Toast.makeText(this, R.string.lumina_loading_wechat_article, Toast.LENGTH_SHORT).show()
        webView.loadUrl(url)
    }

    private fun scheduleExtraction(webView: WebView) {
        extractionJob?.cancel()
        extractionJob = lifecycleScope.launch {
            delay(WECHAT_RENDER_DELAY_MS)
            if (!uploaded) {
                webView.evaluateJavascript(EXTRACT_WECHAT_ARTICLE_SCRIPT, null)
            }
        }
    }

    inner class ArticleBridge(private val fallbackUrl: String) {
        @JavascriptInterface
        fun onArticleExtracted(json: String) {
            lifecycleScope.launch {
                val article = runCatching {
                    val payload = JSONObject(json)
                    LuminaArticleContent(
                        title = payload.optString("title"),
                        contentHtml = payload.optString("content_html"),
                        sourceUrl = payload.optString("source_url").ifBlank { fallbackUrl },
                        topImage = payload.optString("top_image").ifBlank { null },
                        author = payload.optString("author").ifBlank { null },
                        publishedAt = payload.optString("published_at").ifBlank { null },
                        sourceDomain = payload.optString("source_domain").ifBlank { null }
                    )
                }.getOrElse {
                    Toast.makeText(
                        this@LuminaWebArticleActivity,
                        R.string.lumina_extract_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                if (article.contentHtml.isBlank()) {
                    Toast.makeText(
                        this@LuminaWebArticleActivity,
                        R.string.lumina_extract_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                uploaded = true
                Toast.makeText(
                    this@LuminaWebArticleActivity,
                    R.string.lumina_uploading_content,
                    Toast.LENGTH_SHORT
                ).show()
                val result = luminaShareRepository.syncArticleContent(article)
                Toast.makeText(
                    this@LuminaWebArticleActivity,
                    result.message(this@LuminaWebArticleActivity),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        const val EXTRA_URL = "lumina_url"
        private const val BRIDGE_NAME = "LuminaArticleBridge"
        private const val WECHAT_RENDER_DELAY_MS = 2500L

        private val EXTRACT_WECHAT_ARTICLE_SCRIPT = """
            (function () {
              function text(selector) {
                var el = document.querySelector(selector);
                return el && el.textContent ? el.textContent.trim() : "";
              }
              function isPlaceholder(src) {
                return !src || src.indexOf("data:image/svg+xml") === 0 || src.indexOf("data:image/gif") === 0;
              }
              var content = document.querySelector("#js_content") || document.body;
              var lazyAttrs = ["data-src", "data-lazy-src", "data-original", "data-lazy", "data-url", "data-croporisrc", "data-actualsrc"];
              var firstImage = "";
              if (content) {
                Array.prototype.forEach.call(content.querySelectorAll("img"), function (img) {
                  var src = img.getAttribute("src") || "";
                  if (isPlaceholder(src)) {
                    for (var i = 0; i < lazyAttrs.length; i++) {
                      var lazySrc = img.getAttribute(lazyAttrs[i]);
                      if (lazySrc && !isPlaceholder(lazySrc)) {
                        img.setAttribute("src", lazySrc);
                        src = lazySrc;
                        break;
                      }
                    }
                  }
                  if (!firstImage && src) {
                    try { firstImage = new URL(src, document.baseURI).href; } catch (e) { firstImage = src; }
                  }
                });
              }
              var publishedAt = text("#publish_time");
              if (!publishedAt) {
                var scripts = document.querySelectorAll("script");
                for (var j = 0; j < scripts.length; j++) {
                  var match = (scripts[j].textContent || "").match(/var\s+ct\s*=\s*"(\d+)"/);
                  if (match) {
                    publishedAt = new Date(parseInt(match[1], 10) * 1000).toISOString();
                    break;
                  }
                }
              }
              var payload = {
                title: text("#activity-name") || document.title || "",
                content_html: content ? content.innerHTML : "",
                source_url: window.location.href,
                top_image: firstImage,
                author: text("#js_author_name") || text("#js_name"),
                published_at: publishedAt,
                source_domain: window.location.hostname
              };
              window.$BRIDGE_NAME.onArticleExtracted(JSON.stringify(payload));
            })();
        """.trimIndent()
    }
}
