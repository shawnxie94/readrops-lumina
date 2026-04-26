package com.readrops.api.services.lumina.adapters

import com.readrops.api.services.lumina.LuminaCreateArticleRequest
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter

class LuminaCreateArticleRequestAdapter : JsonAdapter<LuminaCreateArticleRequest>() {

    override fun toJson(writer: JsonWriter, value: LuminaCreateArticleRequest?) {
        if (value == null) {
            writer.nullValue()
            return
        }

        writer.beginObject()
        writer.name("title").value(value.title)
        writer.name("content_html").value(value.contentHtml)
        writer.name("content_md").value(value.contentMd)
        writer.name("source_url").value(value.sourceUrl)
        writer.name("top_image").value(value.topImage)
        writer.name("author").value(value.author)
        writer.name("published_at").value(value.publishedAt)
        writer.name("source_domain").value(value.sourceDomain)
        writer.name("skip_ai_processing").value(value.skipAiProcessing)
        writer.endObject()
    }

    override fun fromJson(reader: JsonReader): LuminaCreateArticleRequest {
        throw UnsupportedOperationException("Lumina create article requests are only serialized")
    }
}
