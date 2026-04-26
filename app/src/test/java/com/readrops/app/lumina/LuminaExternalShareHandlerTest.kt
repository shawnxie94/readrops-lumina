package com.readrops.app.lumina

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LuminaExternalShareHandlerTest {

    @Test
    fun extractFirstUrlFromSharedText() {
        assertEquals(
            "https://example.org/post?a=1",
            LuminaExternalShareHandler.extractFirstUrl("Title https://example.org/post?a=1.")
        )
    }

    @Test
    fun detectsWeChatArticleUrls() {
        assertTrue(
            LuminaExternalShareHandler.isWeChatArticleUrl(
                "https://mp.weixin.qq.com/s/example"
            )
        )
        assertFalse(
            LuminaExternalShareHandler.isWeChatArticleUrl("https://example.org/post")
        )
    }
}
