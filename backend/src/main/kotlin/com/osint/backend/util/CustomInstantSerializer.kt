package com.osint.backend.util

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.io.IOException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CustomInstantSerializer : JsonSerializer<Instant?>() {
    companion object {
        private val FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault())
    }

    @Throws(IOException::class)
    override fun serialize(value: Instant?, gen: JsonGenerator, serializers: SerializerProvider) {
        if (value != null) {
            gen.writeString(FORMATTER.format(value))
        } else {
            gen.writeNull()
        }
    }
}
