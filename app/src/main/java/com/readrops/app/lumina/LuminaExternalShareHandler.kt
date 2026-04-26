package com.readrops.app.lumina

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.readrops.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class LuminaExternalShareHandler(
    private val luminaShareRepository: LuminaShareRepository
) {

    suspend fun handle(context: Context, sharedText: String) {
        val url = extractFirstUrl(sharedText)
        if (url == null) {
            showToast(context, context.getString(R.string.lumina_no_link_found))
            return
        }

        val intent = Intent(context, LuminaExternalShareConfirmActivity::class.java)
            .putExtra(LuminaExternalShareConfirmActivity.EXTRA_URL, url)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        withContext(Dispatchers.Main) {
            context.startActivity(intent)
        }
    }

    suspend fun syncConfirmedUrl(context: Context, url: String) {
        if (isWeChatArticleUrl(url)) {
            val intent = Intent(context, LuminaWebArticleActivity::class.java)
                .putExtra(LuminaWebArticleActivity.EXTRA_URL, url)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            withContext(Dispatchers.Main) {
                context.startActivity(intent)
            }
            return
        }

        showToast(context, context.getString(R.string.lumina_syncing))
        val result = luminaShareRepository.syncUrl(url)
        showToast(context, result.message(context))
    }

    companion object {
        private val URL_REGEX = Regex("""https?://\S+""")
        private val TRAILING_PUNCTUATION = charArrayOf('.', ',', ';', ')', ']', '}', '>', '"', '\'')

        fun extractFirstUrl(text: String): String? {
            val match = URL_REGEX.find(text) ?: return null
            return match.value.trimEnd(*TRAILING_PUNCTUATION)
        }

        fun isWeChatArticleUrl(url: String): Boolean {
            return url.lowercase(Locale.ROOT).contains("mp.weixin.qq.com")
        }
    }
}

private suspend fun showToast(context: Context, message: String) = withContext(Dispatchers.Main) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
