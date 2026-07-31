package org.wikipedia.ai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.wikipedia.R
import org.wikipedia.compose.theme.WikipediaTheme

sealed class MarkdownBlock {
    data class Paragraph(val text: String) : MarkdownBlock()
    data class Heading(val level: Int, val text: String) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
    data class Table(val headers: List<String>, val rows: List<List<String>>) : MarkdownBlock()
    data class BulletList(val items: List<String>) : MarkdownBlock()
}

@Composable
fun StreamingTextRenderer(
    text: String,
    isStreaming: Boolean,
    onCitationClick: ((Int) -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor_blink")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor"
    )

    val neu = neuColors()

    if (text.isEmpty() && isStreaming) {
        // Neumorphic pressed pill for "Thinking..." indicator
        Box(
            modifier = Modifier
                .neuPressed(
                    lightShadow = neu.lightShadow,
                    darkShadow = neu.darkShadow,
                    shadowRadius = 4.dp,
                    cornerRadius = 16.dp,
                    intensity = 0.35f
                )
                .background(WikipediaTheme.colors.neuBackground, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RotatingText(
                    texts = listOf(
                        "✨ Thinking…",
                        "🌐 Connecting to Infopedia…",
                        "🧬 Collecting Context Data…",
                        "⚡ Synthesizing Response…"
                    ),
                    color = WikipediaTheme.colors.neuAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    rotationInterval = 1800L,
                    staggerDuration = 18L
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "▍",
                    fontSize = 15.sp,
                    color = WikipediaTheme.colors.neuAccent,
                    modifier = Modifier.alpha(cursorAlpha)
                )
            }
        }
    } else {
        val blocks = remember(text) { parseMarkdownBlocks(text) }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            blocks.forEachIndexed { index, block ->
                val isLastBlock = index == blocks.lastIndex
                when (block) {
                    is MarkdownBlock.Paragraph -> {
                        RenderParagraph(
                            text = block.text,
                            showCursor = isStreaming && isLastBlock,
                            cursorAlpha = cursorAlpha,
                            onCitationClick = onCitationClick
                        )
                    }
                    is MarkdownBlock.Heading -> {
                        RenderHeading(
                            heading = block,
                            showCursor = isStreaming && isLastBlock,
                            cursorAlpha = cursorAlpha,
                            onCitationClick = onCitationClick
                        )
                    }
                    is MarkdownBlock.CodeBlock -> {
                        NeuRenderCodeBlock(codeBlock = block, neu = neu)
                    }
                    is MarkdownBlock.Table -> {
                        NeuRenderTable(table = block, neu = neu)
                    }
                    is MarkdownBlock.BulletList -> {
                        RenderBulletList(
                            list = block,
                            showCursor = isStreaming && isLastBlock,
                            cursorAlpha = cursorAlpha,
                            onCitationClick = onCitationClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderParagraph(
    text: String,
    showCursor: Boolean,
    cursorAlpha: Float,
    onCitationClick: ((Int) -> Unit)?
) {
    val annotatedString = parseInlineMarkdown(text)
    val fullText = if (showCursor) {
        buildAnnotatedString {
            append(annotatedString)
            withStyle(SpanStyle(color = WikipediaTheme.colors.neuAccent)) {
                append("▍")
            }
        }
    } else {
        annotatedString
    }

    androidx.compose.foundation.text.ClickableText(
        text = fullText,
        onClick = { offset ->
            fullText.getStringAnnotations(tag = "CITATION", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    val citationIndex = annotation.item.toIntOrNull() ?: 1
                    onCitationClick?.invoke(citationIndex)
                }
        },
        style = androidx.compose.ui.text.TextStyle(
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = WikipediaTheme.colors.primaryColor
        )
    )
}

@Composable
private fun RenderHeading(
    heading: MarkdownBlock.Heading,
    showCursor: Boolean,
    cursorAlpha: Float,
    onCitationClick: ((Int) -> Unit)?
) {
    val fontSize = when (heading.level) {
        1 -> 20.sp
        2 -> 18.sp
        else -> 16.sp
    }
    val annotatedString = parseInlineMarkdown(heading.text)
    val fullText = if (showCursor) {
        buildAnnotatedString {
            append(annotatedString)
            withStyle(SpanStyle(color = WikipediaTheme.colors.neuAccent)) {
                append("▍")
            }
        }
    } else {
        annotatedString
    }

    androidx.compose.foundation.text.ClickableText(
        text = fullText,
        onClick = { offset ->
            fullText.getStringAnnotations(tag = "CITATION", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    val citationIndex = annotation.item.toIntOrNull() ?: 1
                    onCitationClick?.invoke(citationIndex)
                }
        },
        style = androidx.compose.ui.text.TextStyle(
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            lineHeight = (fontSize.value * 1.3).sp,
            color = WikipediaTheme.colors.primaryColor
        ),
        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
    )
}

// ============================================================================
// NEOMORPHIC CODE BLOCK — Pressed/inset container
// ============================================================================

@Composable
private fun NeuRenderCodeBlock(
    codeBlock: MarkdownBlock.CodeBlock,
    neu: NeuColors
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .neuPressed(
                lightShadow = neu.lightShadow,
                darkShadow = neu.darkShadow,
                shadowRadius = 6.dp,
                cornerRadius = 10.dp,
                intensity = 0.5f
            )
            .background(Color(0xFF1E1E2E), RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
    ) {
        Column {
            // Header bar with language label and copy button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF181825))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = codeBlock.language.ifEmpty { "CODE" }.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFCBA6F7), // Catppuccin mauve
                    letterSpacing = 1.sp
                )
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("code", codeBlock.code))
                        Toast.makeText(context, "Code copied!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_content_copy_24),
                        contentDescription = "Copy code",
                        tint = Color(0xFFA6ADC8),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFF313244), thickness = 0.5.dp)

            // Monospaced code content with horizontal scroll
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Text(
                    text = codeBlock.code,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = Color(0xFFCDD6F4) // Catppuccin text color
                )
            }
        }
    }
}

// ============================================================================
// NEOMORPHIC TABLE — Elevated container with pressed header
// ============================================================================

@Composable
private fun NeuRenderTable(
    table: MarkdownBlock.Table,
    neu: NeuColors
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .neuElevated(
                lightShadow = neu.lightShadow,
                darkShadow = neu.darkShadow,
                shadowRadius = 6.dp,
                cornerRadius = 10.dp,
                lightOffset = (-2).dp,
                darkOffset = 3.dp,
                intensity = 0.45f
            )
            .background(WikipediaTheme.colors.neuSurfaceCard, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
    ) {
        Column(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(1.dp)
        ) {
            // Header Row — pressed inset effect
            Box(
                modifier = Modifier
                    .neuPressed(
                        lightShadow = neu.lightShadow,
                        darkShadow = neu.darkShadow,
                        shadowRadius = 3.dp,
                        cornerRadius = 0.dp,
                        intensity = 0.25f
                    )
                    .background(WikipediaTheme.colors.neuAccent.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    table.headers.forEach { header ->
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = header,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = WikipediaTheme.colors.primaryColor,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = WikipediaTheme.colors.borderColor, thickness = 1.dp)

            // Data Rows
            table.rows.forEachIndexed { rowIndex, row ->
                val rowBg = if (rowIndex % 2 == 0)
                    WikipediaTheme.colors.neuSurfaceCard
                else
                    WikipediaTheme.colors.neuBackground.copy(alpha = 0.5f)

                Row(
                    modifier = Modifier
                        .background(rowBg)
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    row.forEach { cell ->
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = parseInlineMarkdown(cell),
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = WikipediaTheme.colors.primaryColor
                            )
                        }
                    }
                }
                if (rowIndex < table.rows.lastIndex) {
                    HorizontalDivider(
                        color = WikipediaTheme.colors.borderColor.copy(alpha = 0.3f),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderBulletList(
    list: MarkdownBlock.BulletList,
    showCursor: Boolean,
    cursorAlpha: Float,
    onCitationClick: ((Int) -> Unit)?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        list.items.forEachIndexed { index, item ->
            val isLastItem = index == list.items.lastIndex
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = "• ",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = WikipediaTheme.colors.neuAccent,
                    modifier = Modifier.padding(end = 4.dp)
                )
                val annotatedString = parseInlineMarkdown(item)
                val fullText = if (showCursor && isLastItem) {
                    buildAnnotatedString {
                        append(annotatedString)
                        withStyle(SpanStyle(color = WikipediaTheme.colors.neuAccent)) {
                            append("▍")
                        }
                    }
                } else {
                    annotatedString
                }

                androidx.compose.foundation.text.ClickableText(
                    text = fullText,
                    onClick = { offset ->
                        fullText.getStringAnnotations(tag = "CITATION", start = offset, end = offset)
                            .firstOrNull()?.let { annotation ->
                                val citationIndex = annotation.item.toIntOrNull() ?: 1
                                onCitationClick?.invoke(citationIndex)
                            }
                    },
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = WikipediaTheme.colors.primaryColor
                    )
                )
            }
        }
    }
}

private fun parseInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                // Bold: **text**
                text.startsWith("**", i) -> {
                    val endIndex = text.indexOf("**", i + 2)
                    if (endIndex != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(text.substring(i + 2, endIndex))
                        }
                        i = endIndex + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }
                // Inline code: `text`
                text[i] == '`' && !text.startsWith("```", i) -> {
                    val endIndex = text.indexOf('`', i + 1)
                    if (endIndex != -1) {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                background = Color(0xFF2A2A3C),
                                color = Color(0xFFF5C2E7)
                            )
                        ) {
                            append(" ${text.substring(i + 1, endIndex)} ")
                        }
                        i = endIndex + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                // Inline Citations [1], [2], [3], [4]
                text[i] == '[' && i + 2 < text.length && text[i + 2] == ']' && text[i + 1].isDigit() -> {
                    val num = text[i + 1]
                    pushStringAnnotation(tag = "CITATION", annotation = num.toString())
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF2563EB),
                            background = Color(0xFF2563EB).copy(alpha = 0.15f)
                        )
                    ) {
                        append(" [$num] ")
                    }
                    pop()
                    i += 3
                }
                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
}

fun parseMarkdownBlocks(rawText: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = rawText.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        // 1. Code block ```
        if (line.trimStart().startsWith("```")) {
            val lang = line.trimStart().removePrefix("```").trim()
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                codeLines.add(lines[i])
                i++
            }
            if (i < lines.size && lines[i].trimStart().startsWith("```")) {
                i++ // skip closing ```
            }
            blocks.add(MarkdownBlock.CodeBlock(lang, codeLines.joinToString("\n")))
            continue
        }

        // 2. Markdown Table
        if (line.trim().startsWith("|") && line.trim().endsWith("|") && i + 1 < lines.size && lines[i + 1].contains("---")) {
            val headers = line.split("|").map { it.trim() }.filter { it.isNotEmpty() }
            i += 2 // skip header and divider line (|---|---|)
            val rows = mutableListOf<List<String>>()
            while (i < lines.size && lines[i].trim().startsWith("|") && lines[i].trim().endsWith("|")) {
                val rowCells = lines[i].split("|").map { it.trim() }.filter { it.isNotEmpty() }
                if (rowCells.isNotEmpty()) {
                    rows.add(rowCells)
                }
                i++
            }
            blocks.add(MarkdownBlock.Table(headers, rows))
            continue
        }

        // 3. Headings (#, ##, ###)
        if (line.trimStart().startsWith("#")) {
            val level = line.takeWhile { it == '#' }.length
            val text = line.removePrefix("#".repeat(level)).trim()
            blocks.add(MarkdownBlock.Heading(level, text))
            i++
            continue
        }

        // 4. Bullet lists (- or *)
        if (line.trimStart().startsWith("- ") || line.trimStart().startsWith("* ")) {
            val listItems = mutableListOf<String>()
            while (i < lines.size && (lines[i].trimStart().startsWith("- ") || lines[i].trimStart().startsWith("* "))) {
                listItems.add(lines[i].trimStart().substring(2).trim())
                i++
            }
            blocks.add(MarkdownBlock.BulletList(listItems))
            continue
        }

        // 5. Skip blank lines
        if (line.isBlank()) {
            i++
            continue
        }

        // 6. Regular paragraph
        val paragraphLines = mutableListOf<String>()
        while (i < lines.size &&
            lines[i].isNotBlank() &&
            !lines[i].trimStart().startsWith("```") &&
            !lines[i].trimStart().startsWith("#") &&
            !(lines[i].trim().startsWith("|") && lines[i].trim().endsWith("|")) &&
            !lines[i].trimStart().startsWith("- ") &&
            !lines[i].trimStart().startsWith("* ")
        ) {
            paragraphLines.add(lines[i])
            i++
        }
        if (paragraphLines.isNotEmpty()) {
            blocks.add(MarkdownBlock.Paragraph(paragraphLines.joinToString("\n")))
        }
    }

    return if (blocks.isEmpty()) listOf(MarkdownBlock.Paragraph(rawText)) else blocks
}
