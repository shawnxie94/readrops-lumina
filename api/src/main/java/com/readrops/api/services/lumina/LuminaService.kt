package com.readrops.api.services.lumina

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface LuminaService {

    @POST("api/articles")
    suspend fun createArticle(
        @Header(INTERNAL_TOKEN_HEADER) internalToken: String,
        @Body request: LuminaCreateArticleRequest
    ): LuminaReportUrlResponse

    @POST("api/articles/report-url")
    suspend fun reportUrl(
        @Header(INTERNAL_TOKEN_HEADER) internalToken: String,
        @Body request: LuminaReportUrlRequest
    ): LuminaReportUrlResponse

    companion object {
        const val INTERNAL_TOKEN_HEADER = "X-Internal-Token"
    }
}
