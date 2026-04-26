package com.readrops.app

import com.readrops.app.util.Utils
import com.readrops.db.entities.Item
import com.readrops.db.pojo.ItemWithFeed
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UtilsTest {

    @Test
    fun truncateStringTest() {
        val result =
            Utils.truncateString("This is a very very long string with more than 30 characters", 30)

        assertEquals(33, result.length)
        assertTrue(result.contains("This is a very very long strin"))
        assertTrue(result.endsWith("..."))
    }

    @Test
    fun shareIntentTextUsesUrlByDefault() {
        val itemWithFeed = ItemWithFeed(
            item = Item(
                title = "Title",
                link = "https://example.org/post"
            ),
            feedName = "Feed",
            feedId = 1,
            color = 0,
            feedIconUrl = null,
            websiteUrl = null,
            folder = null,
            openIn = null
        )

        val result = Utils.getShareIntentText(
            itemWithFeed = itemWithFeed,
            useCustomShareIntentTpl = false,
            customShareIntentTpl = ""
        )

        assertEquals("https://example.org/post", result)
    }
}
