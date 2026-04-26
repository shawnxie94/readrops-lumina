package com.readrops.app.lumina

import android.content.Context
import com.readrops.api.utils.exceptions.HttpException
import com.readrops.app.R
import com.readrops.app.util.accounterror.AccountError

fun LuminaShareResult.message(context: Context): String {
    return when (this) {
        LuminaShareResult.Success -> context.getString(R.string.synced_to_lumina)
        LuminaShareResult.AlreadyExists -> context.getString(R.string.already_exists_in_lumina)
        LuminaShareResult.MissingConfig -> context.getString(R.string.lumina_not_configured)
        is LuminaShareResult.Error -> {
            val error = exception
            val message = if (error is HttpException) {
                AccountError.Companion.DefaultAccountError(context).genericMessage(error)
            } else {
                error?.message.orEmpty()
            }

            context.getString(
                R.string.lumina_sync_failed,
                message.ifBlank { context.getString(R.string.error_occurred) }
            )
        }
    }
}
