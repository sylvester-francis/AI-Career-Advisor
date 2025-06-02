package com.sylvester.careerbot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FormattedMessageContent(
    content: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val blocks = parseMessageBlocks(content)

        blocks.forEach { block ->
            when (block) {
                is MessageBlock.Paragraph -> {
                    Text(
                        text = parseInlineMarkdown(block.text, color),
                        style = MaterialTheme.typography.bodyMedium,
                        color = color
                    )
                }
                is MessageBlock.Header -> {
                    Text(
                        text = block.text,
                        style = when (block.level) {
                            1 -> MaterialTheme.typography.headlineMedium
                            2 -> MaterialTheme.typography.headlineSmall
                            else -> MaterialTheme.typography.titleMedium
                        },
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.padding(top = if (block.level == 1) 8.dp else 4.dp)
                    )
                }
                is MessageBlock.BulletList -> {
                    Column(
                        modifier = Modifier.padding(start = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        block.items.forEach { item ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = color.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = parseInlineMarkdown(item, color),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = color,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                is MessageBlock.NumberedList -> {
                    Column(
                        modifier = Modifier.padding(start = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        block.items.forEachIndexed { index, item ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "${index + 1}.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = color.copy(alpha = 0.7f),
                                    modifier = Modifier.width(24.dp)
                                )
                                Text(
                                    text = parseInlineMarkdown(item, color),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = color,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                is MessageBlock.CodeBlock -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = block.code,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            color = color,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                is MessageBlock.Quote -> {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .fillMaxHeight()
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = parseInlineMarkdown(block.text, color),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = FontStyle.Italic
                            ),
                            color = color.copy(alpha = 0.9f),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is MessageBlock.HorizontalRule -> {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = color.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

sealed class MessageBlock {
    data class Paragraph(val text: String) : MessageBlock()
    data class Header(val level: Int, val text: String) : MessageBlock()
    data class BulletList(val items: List<String>) : MessageBlock()
    data class NumberedList(val items: List<String>) : MessageBlock()
    data class CodeBlock(val code: String, val language: String? = null) : MessageBlock()
    data class Quote(val text: String) : MessageBlock()
    object HorizontalRule : MessageBlock()
}

fun parseMessageBlocks(content: String): List<MessageBlock> {
    val blocks = mutableListOf<MessageBlock>()
    val lines = content.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i].trim()

        when {
            // Headers
            line.startsWith("### ") -> {
                blocks.add(MessageBlock.Header(3, line.removePrefix("### ").trim()))
                i++
            }
            line.startsWith("## ") -> {
                blocks.add(MessageBlock.Header(2, line.removePrefix("## ").trim()))
                i++
            }
            line.startsWith("# ") -> {
                blocks.add(MessageBlock.Header(1, line.removePrefix("# ").trim()))
                i++
            }

            // Horizontal rule
            line == "---" || line == "***" || line == "___" -> {
                blocks.add(MessageBlock.HorizontalRule)
                i++
            }

            // Quote
            line.startsWith("> ") -> {
                val quoteLines = mutableListOf<String>()
                while (i < lines.size && lines[i].trim().startsWith("> ")) {
                    quoteLines.add(lines[i].trim().removePrefix("> ").trim())
                    i++
                }
                blocks.add(MessageBlock.Quote(quoteLines.joinToString(" ")))
            }

            // Code block
            line.startsWith("```") -> {
                val language = line.removePrefix("```").trim().takeIf { it.isNotEmpty() }
                val codeLines = mutableListOf<String>()
                i++
                while (i < lines.size && !lines[i].trim().startsWith("```")) {
                    codeLines.add(lines[i])
                    i++
                }
                blocks.add(MessageBlock.CodeBlock(codeLines.joinToString("\n"), language))
                i++ // Skip closing ```
            }

            // Bullet list
            line.startsWith("- ") || line.startsWith("* ") || line.startsWith("• ") -> {
                val items = mutableListOf<String>()
                while (i < lines.size && (lines[i].trim().startsWith("- ") ||
                            lines[i].trim().startsWith("* ") ||
                            lines[i].trim().startsWith("• "))) {
                    val item = lines[i].trim()
                        .removePrefix("- ")
                        .removePrefix("* ")
                        .removePrefix("• ")
                        .trim()
                    items.add(item)
                    i++
                }
                blocks.add(MessageBlock.BulletList(items))
            }

            // Numbered list
            line.matches(Regex("^\\d+\\.\\s.*")) -> {
                val items = mutableListOf<String>()
                while (i < lines.size && lines[i].trim().matches(Regex("^\\d+\\.\\s.*"))) {
                    val item = lines[i].trim().substringAfter(". ").trim()
                    items.add(item)
                    i++
                }
                blocks.add(MessageBlock.NumberedList(items))
            }

            // Empty line
            line.isEmpty() -> {
                i++
            }

            // Regular paragraph
            else -> {
                val paragraphLines = mutableListOf<String>()
                while (i < lines.size &&
                    lines[i].trim().isNotEmpty() &&
                    !lines[i].trim().startsWith("#") &&
                    !lines[i].trim().startsWith("-") &&
                    !lines[i].trim().startsWith("*") &&
                    !lines[i].trim().startsWith(">") &&
                    !lines[i].trim().startsWith("```") &&
                    !lines[i].trim().matches(Regex("^\\d+\\.\\s.*"))) {
                    paragraphLines.add(lines[i].trim())
                    i++
                }
                if (paragraphLines.isNotEmpty()) {
                    blocks.add(MessageBlock.Paragraph(paragraphLines.joinToString(" ")))
                }
            }
        }
    }

    return blocks
}

fun parseInlineMarkdown(text: String, baseColor: Color): AnnotatedString {
    return buildAnnotatedString {
        var remaining = text

        while (remaining.isNotEmpty()) {
            when {
                // Bold + Italic (must check before bold/italic alone)
                remaining.startsWith("***") -> {
                    val endIndex = remaining.indexOf("***", 3)
                    if (endIndex > 3) {
                        val content = remaining.substring(3, endIndex)
                        withStyle(SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            color = baseColor
                        )) {
                            append(content)
                        }
                        remaining = remaining.substring(endIndex + 3)
                    } else {
                        append("***")
                        remaining = remaining.substring(3)
                    }
                }

                // Bold
                remaining.startsWith("**") -> {
                    val endIndex = remaining.indexOf("**", 2)
                    if (endIndex > 2) {
                        val content = remaining.substring(2, endIndex)
                        withStyle(SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = baseColor
                        )) {
                            append(content)
                        }
                        remaining = remaining.substring(endIndex + 2)
                    } else {
                        append("**")
                        remaining = remaining.substring(2)
                    }
                }

                // Alternative bold
                remaining.startsWith("__") -> {
                    val endIndex = remaining.indexOf("__", 2)
                    if (endIndex > 2) {
                        val content = remaining.substring(2, endIndex)
                        withStyle(SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = baseColor
                        )) {
                            append(content)
                        }
                        remaining = remaining.substring(endIndex + 2)
                    } else {
                        append("__")
                        remaining = remaining.substring(2)
                    }
                }

                // Strikethrough
                remaining.startsWith("~~") -> {
                    val endIndex = remaining.indexOf("~~", 2)
                    if (endIndex > 2) {
                        val content = remaining.substring(2, endIndex)
                        withStyle(SpanStyle(
                            textDecoration = TextDecoration.LineThrough,
                            color = baseColor.copy(alpha = 0.7f)
                        )) {
                            append(content)
                        }
                        remaining = remaining.substring(endIndex + 2)
                    } else {
                        append("~~")
                        remaining = remaining.substring(2)
                    }
                }

                // Italic (check after bold to avoid conflicts)
                remaining.startsWith("*") && !remaining.startsWith("**") -> {
                    val endIndex = remaining.indexOf("*", 1)
                    if (endIndex > 1 && !remaining.substring(1, endIndex).contains("*")) {
                        val content = remaining.substring(1, endIndex)
                        withStyle(SpanStyle(
                            fontStyle = FontStyle.Italic,
                            color = baseColor
                        )) {
                            append(content)
                        }
                        remaining = remaining.substring(endIndex + 1)
                    } else {
                        append("*")
                        remaining = remaining.substring(1)
                    }
                }

                // Alternative italic
                remaining.startsWith("_") && !remaining.startsWith("__") -> {
                    val endIndex = remaining.indexOf("_", 1)
                    if (endIndex > 1 && !remaining.substring(1, endIndex).contains("_")) {
                        val content = remaining.substring(1, endIndex)
                        withStyle(SpanStyle(
                            fontStyle = FontStyle.Italic,
                            color = baseColor
                        )) {
                            append(content)
                        }
                        remaining = remaining.substring(endIndex + 1)
                    } else {
                        append("_")
                        remaining = remaining.substring(1)
                    }
                }

                // Code
                remaining.startsWith("`") -> {
                    val endIndex = remaining.indexOf("`", 1)
                    if (endIndex > 1) {
                        val content = remaining.substring(1, endIndex)
                        withStyle(SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            background = Color.Gray.copy(alpha = 0.2f),
                            color = baseColor
                        )) {
                            append(" $content ")
                        }
                        remaining = remaining.substring(endIndex + 1)
                    } else {
                        append("`")
                        remaining = remaining.substring(1)
                    }
                }

                // Regular character
                else -> {
                    withStyle(SpanStyle(color = baseColor)) {
                        append(remaining.first())
                    }
                    remaining = remaining.drop(1)
                }
            }
        }
    }
}

private fun IntRange.overlaps(other: IntRange): Boolean {
    return first <= other.last && last >= other.first
}