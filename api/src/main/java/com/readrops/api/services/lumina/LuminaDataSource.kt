package com.readrops.api.services.lumina

class LuminaDataSource(private val service: LuminaService) {

    suspend fun createArticle(
        request: LuminaCreateArticleRequest,
        internalToken: String
    ): LuminaReportUrlResponse {
        return service.createArticle(
            internalToken = internalToken,
            request = request
        )
    }

    suspend fun reportUrl(
        url: String,
        internalToken: String,
        skipAiProcessing: Boolean
    ): LuminaReportUrlResponse {
        return service.reportUrl(
            internalToken = internalToken,
            request = LuminaReportUrlRequest(
                url = url,
                skipAiProcessing = skipAiProcessing
            )
        )
    }

    companion object {
        fun normalizeBaseUrl(url: String): String {
            val trimmedUrl = url.trim().trimEnd('/')
            return if (trimmedUrl.endsWith("/backend")) {
                "$trimmedUrl/"
            } else {
                "$trimmedUrl/backend/"
            }
        }
    }
}
