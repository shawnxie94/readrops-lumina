package com.readrops.app.lumina

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LuminaSettingsTest {

    @Test
    fun defaultSkipAiProcessingIsFalse() {
        val settings = LuminaSettings(
            apiUrl = "https://lumina.example/backend",
            internalToken = "secret"
        )

        assertFalse(settings.skipAiProcessing)
    }

    @Test
    fun requiresApiUrlAndToken() {
        assertFalse(
            LuminaSettings(
                apiUrl = "",
                internalToken = "secret"
            ).isConfigured
        )
        assertFalse(
            LuminaSettings(
                apiUrl = "https://lumina.example/backend",
                internalToken = ""
            ).isConfigured
        )
        assertTrue(
            LuminaSettings(
                apiUrl = "https://lumina.example/backend",
                internalToken = "secret"
            ).isConfigured
        )
    }
}
