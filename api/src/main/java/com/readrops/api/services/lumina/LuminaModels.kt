package com.readrops.api.services.lumina

import com.squareup.moshi.Json

data class LuminaReportUrlRequest(
    val url: String,
    @Json(name = "skip_ai_processing") val skipAiProcessing: Boolean
)

data class LuminaReportUrlResponse(
    val id: String?,
    val slug: String?,
    val status: String?,
    @Json(name = "source_url") val sourceUrl: String?
)

data class LuminaCreateArticleRequest(
    val title: String,
    @Json(name = "content_html") val contentHtml: String?,
    @Json(name = "content_md") val contentMd: String?,
    @Json(name = "source_url") val sourceUrl: String?,
    @Json(name = "top_image") val topImage: String?,
    val author: String?,
    @Json(name = "published_at") val publishedAt: String?,
    @Json(name = "source_domain") val sourceDomain: String?,
    @Json(name = "skip_ai_processing") val skipAiProcessing: Boolean
)
