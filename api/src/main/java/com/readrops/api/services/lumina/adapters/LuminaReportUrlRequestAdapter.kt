package com.readrops.api.services.lumina.adapters

import com.readrops.api.services.lumina.LuminaReportUrlRequest
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter

class LuminaReportUrlRequestAdapter : JsonAdapter<LuminaReportUrlRequest>() {

    override fun toJson(writer: JsonWriter, value: LuminaReportUrlRequest?) {
        if (value == null) {
            writer.nullValue()
            return
        }

        writer.beginObject()
        writer.name("url").value(value.url)
        writer.name("skip_ai_processing").value(value.skipAiProcessing)
        writer.endObject()
    }

    override fun fromJson(reader: JsonReader): LuminaReportUrlRequest {
        var url: String? = null
        var skipAiProcessing = false

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "url" -> url = reader.nextString()
                "skip_ai_processing" -> skipAiProcessing = reader.nextBoolean()
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return LuminaReportUrlRequest(
            url = url.orEmpty(),
            skipAiProcessing = skipAiProcessing
        )
    }
}
