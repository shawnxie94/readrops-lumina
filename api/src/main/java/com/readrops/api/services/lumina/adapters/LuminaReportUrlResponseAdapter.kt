package com.readrops.api.services.lumina.adapters

import com.readrops.api.services.lumina.LuminaReportUrlResponse
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter

class LuminaReportUrlResponseAdapter : JsonAdapter<LuminaReportUrlResponse>() {

    override fun toJson(writer: JsonWriter, value: LuminaReportUrlResponse?) {
        if (value == null) {
            writer.nullValue()
            return
        }

        writer.beginObject()
        writer.name("id").value(value.id)
        writer.name("slug").value(value.slug)
        writer.name("status").value(value.status)
        writer.name("source_url").value(value.sourceUrl)
        writer.endObject()
    }

    override fun fromJson(reader: JsonReader): LuminaReportUrlResponse {
        var id: String? = null
        var slug: String? = null
        var status: String? = null
        var sourceUrl: String? = null

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "id" -> id = reader.nextStringOrNull()
                "slug" -> slug = reader.nextStringOrNull()
                "status" -> status = reader.nextStringOrNull()
                "source_url" -> sourceUrl = reader.nextStringOrNull()
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return LuminaReportUrlResponse(
            id = id,
            slug = slug,
            status = status,
            sourceUrl = sourceUrl
        )
    }

    private fun JsonReader.nextStringOrNull(): String? {
        return if (peek() == JsonReader.Token.NULL) nextNull<String>() else nextString()
    }
}
