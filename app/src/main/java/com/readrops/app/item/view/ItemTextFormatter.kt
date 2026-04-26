package com.readrops.app.item.view

import android.text.SpannedString
import androidx.core.text.HtmlCompat
import org.commonmark.Extension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.jsoup.Jsoup
import org.jsoup.nodes.Entities
import org.jsoup.parser.Parser.unescapeEntities

object ItemTextFormatter {

    private val markdownExtensions: List<Extension> = listOf(TablesExtension.create())
    private val markdownParser = Parser.builder()
        .extensions(markdownExtensions)
        .build()
    private val markdownRenderer = HtmlRenderer.builder()
        .extensions(markdownExtensions)
        .escapeHtml(true)
        .sanitizeUrls(true)
        .build()

    fun formatText(text: String?, baseUrl: String?): String {
        if (text == null) return ""

        val unescapedText = unescapeEntities(text, false) ?: text
        val document = if (baseUrl != null) {
            Jsoup.parse(unescapedText, baseUrl)
        } else {
            Jsoup.parse(unescapedText)
        }

        val body = document.body()
        val isPlainText = body.stream().skip(1).allMatch { !it.tag().isKnownTag }
        val isMarkdown = looksLikeMarkdown(unescapedText) &&
                (isPlainText || startsWithMarkdownBlock(unescapedText))

        return if (isPlainText || isMarkdown) {
            if (isMarkdown) {
                markdownRenderer.render(markdownParser.parse(unescapedText))
            } else {
                HtmlCompat.toHtml(
                    SpannedString(unescapedText),
                    HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE
                )
            }
        } else {
            body.select("div,span").forEach { it.clearAttributes() }
            body.html()
        }
    }

    fun formatPreviewText(text: String?): String {
        if (text == null) return ""

        val unescapedText = unescapeEntities(text, false) ?: text
        return if (looksLikeMarkdown(unescapedText)) {
            markdownRenderer.render(markdownParser.parse(unescapedText))
        } else {
            "<p>${Entities.escape(unescapedText).replace("\n", "<br>")}</p>"
        }
    }

    fun looksLikeMarkdown(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return false

        return MARKDOWN_PATTERNS.any { it.containsMatchIn(trimmed) }
    }

    private fun startsWithMarkdownBlock(text: String): Boolean {
        val trimmed = text.trimStart()
        if (trimmed.isBlank()) return false

        return MARKDOWN_BLOCK_START_PATTERNS.any { it.containsMatchIn(trimmed) }
    }

    private val MARKDOWN_PATTERNS = listOf(
        Regex("""(?m)^\s{0,3}#{1,6}\s+\S"""),
        Regex("""(?m)^\s{0,3}>\s+\S"""),
        Regex("""(?m)^\s{0,3}(```|~~~)"""),
        Regex("""(?m)^\s{0,3}[-*+]\s+\S"""),
        Regex("""(?m)^\s{0,3}\d+[.)]\s+\S"""),
        Regex("""(?m)^\s{0,3}[-*_]{3,}\s*$"""),
        Regex("""!\[[^]]*]\([^)]+"""),
        Regex("""\[[^]]+]\([^)]+"""),
        Regex("""(?s)\*\*[^*\n][\s\S]*?\*\*"""),
        Regex("""(?s)__[^_\n][\s\S]*?__"""),
        Regex("""`[^`\n]+`"""),
        Regex("""(?m)^\|.+\|\s*$"""),
        Regex("""(?m)^\s{0,3}[-:| ]{3,}\|\s*[-:| ]{3,}""")
    )

    private val MARKDOWN_BLOCK_START_PATTERNS = listOf(
        Regex("""\A\s{0,3}#{1,6}\s+\S"""),
        Regex("""\A\s{0,3}>\s+\S"""),
        Regex("""\A\s{0,3}(```|~~~)"""),
        Regex("""\A\s{0,3}[-*+]\s+\S"""),
        Regex("""\A\s{0,3}\d+[.)]\s+\S"""),
        Regex("""\A\s{0,3}[-*_]{3,}\s*$"""),
        Regex("""\A\|.+\|\s*$""")
    )
}
