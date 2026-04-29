package com.readrops.app.item.view

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ItemTextFormatterTest {

    @Test
    fun rendersMarkdownPlainTextToHtml() {
        val html = ItemTextFormatter.formatText(
            """
            # Title

            This is **important**.

            - One
            - Two
            """.trimIndent(),
            null
        )

        assertTrue(html.contains("<h1>Title</h1>"))
        assertTrue(html.contains("<strong>important</strong>"))
        assertTrue(html.contains("<li>One</li>"))
    }

    @Test
    fun keepsExistingHtmlAsHtml() {
        val html = ItemTextFormatter.formatText(
            """<p>Hello <strong>world</strong></p>""",
            null
        )

        assertTrue(html.contains("<p>Hello <strong>world</strong></p>"))
    }

    @Test
    fun rendersMarkdownInsideExistingHtmlText() {
        val html = ItemTextFormatter.formatText(
            """
            <p>Article **summary**.</p>
            <p><strong>Related articles:</strong></p>
            <ul>
            <li><a href="https://example.org/article">Example article</a> - Example</li>
            </ul>
            """.trimIndent(),
            null
        )

        assertTrue(html.contains("<p>Article <strong>summary</strong>.</p>"))
        assertTrue(html.contains("<p><strong>Related articles:</strong></p>"))
        assertTrue(html.contains("""<a href="https://example.org/article">Example article</a>"""))
        assertTrue(html.contains(" - Example"))
        assertTrue(html.contains("<ul>"))
        assertFalse(html.contains("**summary**"))
    }

    @Test
    fun rendersMarkdownLinksInsideExistingHtmlText() {
        val html = ItemTextFormatter.formatText(
            """<p>Read [docs](https://example.org) today.</p>""",
            null
        )

        assertTrue(html.contains("""href="https://example.org""""))
        assertTrue(html.contains(">docs</a>"))
        assertTrue(html.contains(" today."))
        assertFalse(html.contains("[docs](https://example.org)"))
    }

    @Test
    fun detectsMarkdownSignalsConservatively() {
        assertTrue(ItemTextFormatter.looksLikeMarkdown("## Heading"))
        assertTrue(ItemTextFormatter.looksLikeMarkdown("[Readrops](https://example.org)"))
        assertFalse(ItemTextFormatter.looksLikeMarkdown("This is a regular plain text paragraph."))
    }

    @Test
    fun escapesRawHtmlInMarkdownContent() {
        val html = ItemTextFormatter.formatText(
            """
            # Title

            <script>alert("xss")</script>
            [bad](javascript:alert(1))
            """.trimIndent(),
            null
        )

        assertTrue(html.contains("&lt;script&gt;"))
        assertFalse(html.contains("<script>"))
        assertFalse(html.contains("javascript:alert"))
    }

    @Test
    fun rendersMarkdownPreviewTextToHtml() {
        val html = ItemTextFormatter.formatPreviewText(
            """
            ## Preview

            - **One**
            - Two
            """.trimIndent()
        )

        assertTrue(html.contains("<h2>Preview</h2>"))
        assertTrue(html.contains("<strong>One</strong>"))
        assertTrue(html.contains("<li>Two</li>"))
    }

    @Test
    fun keepsHtmlPreviewTextAsHtml() {
        val html = ItemTextFormatter.formatPreviewText(
            """
            <p>Article **summary**.</p>
            <p><strong>Related articles:</strong></p>
            <ul>
            <li><a href="https://example.org/article">Example article</a> - Example</li>
            </ul>
            """.trimIndent()
        )

        assertTrue(html.contains("<p>Article <strong>summary</strong>.</p>"))
        assertTrue(html.contains("<p><strong>Related articles:</strong></p>"))
        assertTrue(html.contains("• "))
        assertTrue(html.contains("""<a href="https://example.org/article">Example article</a>"""))
        assertTrue(html.contains(" - Example"))
        assertFalse(html.contains("**summary**"))
        assertFalse(html.contains("<ul>"))
        assertFalse(html.contains("&lt;p&gt;"))
    }

    @Test
    fun keepsPlainPreviewTextReadable() {
        val html = ItemTextFormatter.formatPreviewText("This is a regular plain text paragraph.")

        assertTrue(html.contains("This is a regular plain text paragraph."))
        assertFalse(html.contains("<h"))
    }
}
