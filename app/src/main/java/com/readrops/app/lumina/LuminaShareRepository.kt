package com.readrops.app.lumina

import com.readrops.api.services.lumina.LuminaCreateArticleRequest
import com.readrops.api.services.lumina.LuminaDataSource
import com.readrops.api.utils.exceptions.HttpException
import com.readrops.db.pojo.ItemWithFeed
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import java.net.URI
import java.util.Locale

class LuminaShareRepository(
    private val luminaConfig: LuminaConfig,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : KoinComponent {

    suspend fun syncArticleContent(article: LuminaArticleContent): LuminaShareResult =
        withContext(dispatcher) {
            if (article.contentHtml.isBlank()) {
                return@withContext LuminaShareResult.Error()
            }

            val settings = luminaConfig.getSettings()
            if (!settings.isConfigured) {
                return@withContext LuminaShareResult.MissingConfig
            }

            try {
                val dataSource = get<LuminaDataSource> { parametersOf(settings.apiUrl) }
                dataSource.createArticle(
                    request = LuminaCreateArticleRequest(
                        title = article.title.ifBlank { article.sourceUrl },
                        contentHtml = article.contentHtml,
                        contentMd = null,
                        sourceUrl = article.sourceUrl,
                        topImage = article.topImage,
                        author = article.author,
                        publishedAt = article.publishedAt,
                        sourceDomain = article.sourceDomain,
                        skipAiProcessing = settings.skipAiProcessing
                    ),
                    internalToken = settings.internalToken
                )
                LuminaShareResult.Success
            } catch (exception: Exception) {
                if (exception is HttpException && exception.code == HTTP_CONFLICT) {
                    LuminaShareResult.AlreadyExists
                } else {
                    LuminaShareResult.Error(exception)
                }
            }
        }

    suspend fun syncUrl(url: String?): LuminaShareResult = withContext(dispatcher) {
        if (url.isNullOrBlank()) {
            return@withContext LuminaShareResult.Error()
        }

        val settings = luminaConfig.getSettings()
        if (!settings.isConfigured) {
            return@withContext LuminaShareResult.MissingConfig
        }

        try {
            val dataSource = get<LuminaDataSource> { parametersOf(settings.apiUrl) }
            dataSource.reportUrl(
                url = url,
                internalToken = settings.internalToken,
                skipAiProcessing = settings.skipAiProcessing
            )
            LuminaShareResult.Success
        } catch (exception: Exception) {
            if (exception is HttpException && exception.code == HTTP_CONFLICT) {
                LuminaShareResult.AlreadyExists
            } else {
                LuminaShareResult.Error(exception)
            }
        }
    }

    suspend fun syncItemContent(itemWithFeed: ItemWithFeed): LuminaShareResult {
        val item = itemWithFeed.item
        val contentHtml = item.text?.trim().orEmpty()
        if (contentHtml.isBlank()) {
            return LuminaShareResult.Error()
        }

        return syncArticleContent(
            LuminaArticleContent(
                title = item.title.orEmpty(),
                contentHtml = normalizeContentHtml(contentHtml),
                sourceUrl = item.link.orEmpty(),
                topImage = item.imageLink,
                author = item.author,
                publishedAt = item.pubDate?.toString(),
                sourceDomain = sourceDomain(item.link) ?: itemWithFeed.websiteUrl?.let(::sourceDomain)
            )
        )
    }

    companion object {
        private const val HTTP_CONFLICT = 409

        private val HTML_TAG_PATTERN = Regex("<[a-zA-Z][\\s\\S]*>")

        private fun normalizeContentHtml(content: String): String {
            if (HTML_TAG_PATTERN.containsMatchIn(content)) {
                return content
            }

            return content
                .split(Regex("\\n{2,}"))
                .joinToString(separator = "") { paragraph ->
                    val escaped = paragraph.trim().escapeHtml().replace("\n", "<br>")
                    if (escaped.isBlank()) "" else "<p>$escaped</p>"
                }
        }

        private fun String.escapeHtml(): String = buildString(length) {
            this@escapeHtml.forEach { char ->
                when (char) {
                    '&' -> append("&amp;")
                    '<' -> append("&lt;")
                    '>' -> append("&gt;")
                    '"' -> append("&quot;")
                    '\'' -> append("&#39;")
                    else -> append(char)
                }
            }
        }

        private fun sourceDomain(url: String?): String? {
            if (url.isNullOrBlank()) {
                return null
            }

            return runCatching {
                URI(url).host?.lowercase(Locale.ROOT)
            }.getOrNull()
        }
    }
}

data class LuminaArticleContent(
    val title: String,
    val contentHtml: String,
    val sourceUrl: String,
    val topImage: String?,
    val author: String?,
    val publishedAt: String?,
    val sourceDomain: String?
)

sealed class LuminaShareResult {
    data object Success : LuminaShareResult()
    data object AlreadyExists : LuminaShareResult()
    data object MissingConfig : LuminaShareResult()
    data class Error(val exception: Exception? = null) : LuminaShareResult()
}
