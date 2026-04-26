package com.readrops.app.lumina

import com.readrops.api.services.lumina.LuminaCreateArticleRequest
import com.readrops.api.services.lumina.LuminaDataSource
import com.readrops.api.utils.exceptions.HttpException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

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

    companion object {
        private const val HTTP_CONFLICT = 409
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
