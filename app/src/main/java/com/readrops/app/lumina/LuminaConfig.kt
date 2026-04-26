package com.readrops.app.lumina

import android.content.SharedPreferences
import com.readrops.app.util.Preferences
import kotlinx.coroutines.flow.first

data class LuminaSettings(
    val apiUrl: String,
    val internalToken: String,
    val skipAiProcessing: Boolean = false
) {
    val isConfigured: Boolean
        get() = apiUrl.isNotBlank() && internalToken.isNotBlank()
}

class LuminaConfig(
    private val preferences: Preferences,
    private val encryptedPreferences: SharedPreferences
) {

    suspend fun getSettings(): LuminaSettings {
        return LuminaSettings(
            apiUrl = preferences.luminaApiUrl.flow.first(),
            internalToken = getInternalToken(),
            skipAiProcessing = preferences.luminaSkipAiProcessing.flow.first()
        )
    }

    fun getInternalToken(): String {
        return encryptedPreferences.getString(INTERNAL_TOKEN_KEY, null).orEmpty()
    }

    suspend fun saveSettings(
        apiUrl: String,
        internalToken: String,
        skipAiProcessing: Boolean
    ) {
        encryptedPreferences.edit()
            .putString(INTERNAL_TOKEN_KEY, internalToken.trim())
            .apply()
        preferences.luminaApiUrl.write(apiUrl.trim())
        preferences.luminaSkipAiProcessing.write(skipAiProcessing)
    }

    companion object {
        const val INTERNAL_TOKEN_KEY = "lumina_internal_token"
    }
}
