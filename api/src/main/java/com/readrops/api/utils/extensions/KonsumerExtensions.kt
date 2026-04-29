package com.readrops.api.utils.extensions

import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.StaxReader
import com.gitlab.mvysny.konsumexml.Whitespace
import com.gitlab.mvysny.konsumexml.konsumeXml
import com.gitlab.mvysny.konsumexml.stax.StaxEventType
import com.gitlab.mvysny.konsumexml.stax.StaxParser
import com.gitlab.mvysny.konsumexml.textRecursively
import com.readrops.api.utils.exceptions.ParseException
import org.jsoup.nodes.Entities
import java.io.InputStream
import java.util.stream.Stream
import javax.xml.namespace.QName

fun Konsumer.nonNullText(): String {
    val text = text(whitespace = Whitespace.preserve)
    return if (text.isNotEmpty()) text.trim() else throw ParseException("$name text can't be null")
}

fun Konsumer.nullableText(): String? {
    val text = text(whitespace = Whitespace.preserve)
    return if (text.isNotEmpty()) text.trim() else null
}

fun Konsumer.nullableTextRecursively(): String? {
    val text = textRecursively(whitespace = Whitespace.preserve)
    return if (text.isNotEmpty()) text.trim() else null
}

fun Konsumer.nullableInnerXml(): String? {
    val reader = reflectiveReader()
    val stax = reader.stax
    val text = buildString {
        var depth = 0

        while (reader.hasNext()) {
            when (reader.next()) {
                StaxEventType.StartElement -> {
                    depth++
                    appendStartTag(stax)
                }

                StaxEventType.EndElement -> {
                    if (depth == 0) break

                    appendEndTag(stax.elementName)
                    depth--
                }

                StaxEventType.Characters,
                StaxEventType.Space,
                StaxEventType.EntityReference -> appendEscapedTextOrHtml(stax.text.orEmpty())

                StaxEventType.CData -> append(stax.text.orEmpty())

                else -> Unit
            }
        }
    }.trim()

    reflectiveFinish()
    return text.ifEmpty { null }
}

fun Konsumer.checkRoot(name: String): Boolean = try {
    checkCurrent(name)
    true
} catch (e: Exception) {
    false
}

/**
 * Check is the element [name] is already loaded, loads it if it not the case and calls [block]
 */
fun Konsumer.checkElement(name: String, block: (Konsumer) -> Unit) {
    if (checkRoot(name)) block(this)
    else {
        child(name) {
            block(this)
        }
    }
}

public fun instantiateKonsumer(stream: InputStream) = stream.konsumeXml()

private fun Konsumer.reflectiveReader(): StaxReader {
    val field = Konsumer::class.java.getDeclaredField("reader")
    field.isAccessible = true

    return field.get(this) as StaxReader
}

private fun Konsumer.reflectiveFinish() {
    val field = Konsumer::class.java.getDeclaredField("isFinished")
    field.isAccessible = true
    field.set(this, true)
}

private fun StringBuilder.appendStartTag(stax: StaxParser) {
    append('<')
    append(stax.elementName.qualifiedName())

    repeat(stax.attributeCount) { index ->
        val attributeName = stax.getAttributeName(index)

        append(' ')
        append(attributeName.qualifiedName())
        append("=\"")
        append(Entities.escape(stax.getAttributeValue(attributeName.namespaceURI, attributeName.localPart).orEmpty()))
        append('"')
    }

    append('>')
}

private fun StringBuilder.appendEndTag(name: QName) {
    append("</")
    append(name.qualifiedName())
    append('>')
}

private fun QName.qualifiedName() = when {
    prefix.isNullOrBlank() -> localPart
    else -> "$prefix:$localPart"
}

private fun StringBuilder.appendEscapedTextOrHtml(text: String) {
    if (HTML_TAG_PATTERN.containsMatchIn(text)) {
        append(text)
    } else {
        append(Entities.escape(text))
    }
}

private val HTML_TAG_PATTERN = Regex("""</?[A-Za-z][^>]*>""")
