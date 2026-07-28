package org.wikipedia.ai

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import coil3.compose.AsyncImage
import org.wikipedia.R
import org.wikipedia.WikipediaApp
import org.wikipedia.compose.theme.WikipediaTheme
import org.wikipedia.history.HistoryEntry
import org.wikipedia.page.PageActivity
import org.wikipedia.page.PageTitle

@Composable
fun ChatBubble(
    message: AiChatMessage,
    thinkingContent: String? = null,
    onFollowUpClick: ((String) -> Unit)? = null
) {
    val isUser = message.role == AiChatMessage.ROLE_USER
    val context = LocalContext.current
    val neu = neuColors()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (isUser) {
            // ============================================================
            // USER MESSAGE — Neumorphic elevated bubble with gradient
            // ============================================================
            Box(
                modifier = Modifier
                    .widthIn(max = 310.dp)
                    .neuElevated(
                        lightShadow = neu.lightShadow,
                        darkShadow = neu.darkShadow,
                        shadowRadius = 8.dp,
                        cornerRadius = 18.dp,
                        lightOffset = (-3).dp,
                        darkOffset = 4.dp,
                        intensity = 0.5f
                    )
                    .neuGlow(
                        glowColor = WikipediaTheme.colors.neuAccent,
                        cornerRadius = 18.dp,
                        glowRadius = 10.dp,
                        intensity = 0.15f
                    )
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                WikipediaTheme.colors.neuAccent,
                                WikipediaTheme.colors.neuAccent.copy(alpha = 0.85f)
                            )
                        ),
                        shape = RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 4.dp
                        )
                    )
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 4.dp
                        )
                    )
                    .animateContentSize()
            ) {
                Text(
                    text = message.content,
                    color = Color.White,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        } else {
            // ============================================================
            // AI RESPONSE — Neumorphic elevated card (no border, same-color)
            // ============================================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neuElevated(
                        lightShadow = neu.lightShadow,
                        darkShadow = neu.darkShadow,
                        shadowRadius = 10.dp,
                        cornerRadius = 16.dp,
                        lightOffset = (-4).dp,
                        darkOffset = 5.dp,
                        intensity = 0.55f
                    )
                    .background(
                        WikipediaTheme.colors.neuBackground,
                        RoundedCornerShape(16.dp)
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .animateContentSize()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    // AI Model Header Pill — neumorphic pressed
                    if (message.model != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .neuPressed(
                                        lightShadow = neu.lightShadow,
                                        darkShadow = neu.darkShadow,
                                        shadowRadius = 3.dp,
                                        cornerRadius = 12.dp,
                                        intensity = 0.35f
                                    )
                                    .background(WikipediaTheme.colors.neuBackground, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_ai_sparkles_24dp),
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = getBadgeColor(message.model.badgeColor)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = message.model.label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = getBadgeColor(message.model.badgeColor)
                                    )
                                }
                            }

                            if (message.model.badge.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(
                                            getBadgeColor(message.model.badgeColor).copy(alpha = 0.12f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = message.model.badge,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = getBadgeColor(message.model.badgeColor)
                                    )
                                }
                            }
                        }
                    }

                    // 1. TOP: Wikipedia Featured Hero Media Showcase Card
                    val primaryArticle = message.wikipediaContext?.primaryArticle
                    val heroImageUrl = primaryArticle?.originalImageUrl ?: primaryArticle?.thumbnailUrl
                    if (!heroImageUrl.isNullOrEmpty()) {
                        NeuHeroMediaCard(
                            article = primaryArticle!!,
                            imageUrl = heroImageUrl,
                            neu = neu,
                            onClick = {
                                val pageTitle = PageTitle(primaryArticle.title, WikipediaApp.instance.wikiSite)
                                val historyEntry = HistoryEntry(pageTitle, HistoryEntry.SOURCE_INTERNAL_LINK)
                                val intent = PageActivity.newIntentForNewTab(context, historyEntry, pageTitle)
                                context.startActivity(intent)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // DeepThink / Qwen Thinking Process Block — Neumorphic pressed container
                    if (!thinkingContent.isNullOrEmpty()) {
                        var isExpanded by remember { mutableStateOf(message.isStreaming) }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .neuPressed(
                                    lightShadow = neu.lightShadow,
                                    darkShadow = neu.darkShadow,
                                    shadowRadius = 5.dp,
                                    cornerRadius = 10.dp,
                                    intensity = 0.4f
                                )
                                .background(WikipediaTheme.colors.neuBackground, RoundedCornerShape(10.dp))
                                .clip(RoundedCornerShape(10.dp))
                                .animateContentSize()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isExpanded = !isExpanded }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "🧠", fontSize = 13.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (message.isStreaming) "Qwen Thinking..." else "Qwen Thought Process",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF8B5CF6)
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (isExpanded) "Hide" else "Show reasoning",
                                            fontSize = 11.sp,
                                            color = Color(0xFF8B5CF6).copy(alpha = 0.8f)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            painter = painterResource(
                                                id = if (isExpanded) R.drawable.ic_arrow_drop_up_24 else R.drawable.ic_arrow_down_24
                                            ),
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color(0xFF8B5CF6)
                                        )
                                    }
                                }

                                if (isExpanded) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(
                                        color = Color(0xFF8B5CF6).copy(alpha = 0.15f),
                                        thickness = 0.5.dp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = thinkingContent,
                                        fontSize = 12.sp,
                                        color = WikipediaTheme.colors.secondaryColor,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    // 2. MIDDLE: AI Response Content with Clickable Inline Citations [1], [2], [3]
                    Box(modifier = Modifier.fillMaxWidth()) {
                        StreamingTextRenderer(
                            text = message.content,
                            isStreaming = message.isStreaming,
                            onCitationClick = { citationIndex ->
                                val articles = message.wikipediaContext?.articles.orEmpty()
                                val targetArticle = articles.getOrNull(citationIndex - 1) ?: articles.firstOrNull()
                                if (targetArticle != null) {
                                    val pageTitle = PageTitle(
                                        targetArticle.title,
                                        org.wikipedia.dataclient.WikiSite.forLanguageCode(targetArticle.langCode)
                                    )
                                    val historyEntry = HistoryEntry(pageTitle, HistoryEntry.SOURCE_INTERNAL_LINK)
                                    context.startActivity(PageActivity.newIntentForNewTab(context, historyEntry, pageTitle))
                                } else {
                                    Toast.makeText(context, "Opening Wikipedia Citation [$citationIndex]…", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                    // 3. BOTTOM: Interactive Wikipedia Article Cards
                    val articles = message.wikipediaContext?.articles.orEmpty()
                    if (articles.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "📚 Wikipedia Sources (${articles.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = WikipediaTheme.colors.secondaryColor,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(end = 4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(articles, key = { it.title }) { article ->
                                NeuArticleSourceCard(
                                    article = article,
                                    neu = neu,
                                    onClick = {
                                        val pageTitle = PageTitle(article.title, WikipediaApp.instance.wikiSite)
                                        val historyEntry = HistoryEntry(pageTitle, HistoryEntry.SOURCE_INTERNAL_LINK)
                                        val intent = PageActivity.newIntentForNewTab(context, historyEntry, pageTitle)
                                        context.startActivity(intent)
                                    }
                                )
                            }
                        }
                    }

                    // 4. PERPLEXITY FOLLOW-UP QUESTIONS CHIPS
                    val followUps = remember(message.content) { extractFollowUpQuestions(message.content) }
                    if (followUps.isNotEmpty() && !message.isStreaming && onFollowUpClick != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        NeuFollowUpQuestionsSection(
                            questions = followUps,
                            onQuestionClick = onFollowUpClick,
                            neu = neu
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// NEOMORPHIC HERO MEDIA CARD
// ============================================================================

@Composable
private fun NeuHeroMediaCard(
    article: WikipediaArticleItem,
    imageUrl: String,
    neu: NeuColors,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevated(
                lightShadow = neu.lightShadow,
                darkShadow = neu.darkShadow,
                shadowRadius = 8.dp,
                cornerRadius = 14.dp,
                lightOffset = (-3).dp,
                darkOffset = 4.dp,
                intensity = 0.5f
            )
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = article.displayTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                            )
                        )
                )
                Text(
                    text = "🌐 Wikipedia Lead Article",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                )
                Text(
                    text = article.displayTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                )
            }
        }
    }
}

// ============================================================================
// NEOMORPHIC ARTICLE SOURCE CARD
// ============================================================================

@Composable
private fun NeuArticleSourceCard(
    article: WikipediaArticleItem,
    neu: NeuColors,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(180.dp)
            .neuElevated(
                lightShadow = neu.lightShadow,
                darkShadow = neu.darkShadow,
                shadowRadius = 6.dp,
                cornerRadius = 12.dp,
                lightOffset = (-2).dp,
                darkOffset = 3.dp,
                intensity = 0.5f
            )
            .background(WikipediaTheme.colors.neuSurfaceCard, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            val thumbUrl = article.thumbnailUrl ?: article.originalImageUrl
            if (!thumbUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = thumbUrl,
                    contentDescription = article.displayTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(85.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = article.displayTitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = WikipediaTheme.colors.primaryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .background(
                            WikipediaTheme.colors.neuAccent.copy(alpha = 0.12f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = article.langCode.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = WikipediaTheme.colors.neuAccent
                    )
                }
            }

            if (!article.description.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = article.description,
                    fontSize = 11.sp,
                    color = WikipediaTheme.colors.secondaryColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // "Read Article" button — neumorphic elevated mini button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neuElevated(
                        lightShadow = neu.lightShadow,
                        darkShadow = neu.darkShadow,
                        shadowRadius = 3.dp,
                        cornerRadius = 8.dp,
                        lightOffset = (-1).dp,
                        darkOffset = 2.dp,
                        intensity = 0.4f
                    )
                    .background(WikipediaTheme.colors.neuBackground, RoundedCornerShape(8.dp))
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Read Article",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = WikipediaTheme.colors.neuAccent
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_forward_24),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = WikipediaTheme.colors.neuAccent
                    )
                }
            }
        }
    }
}

// ============================================================================
// NEOMORPHIC FOLLOW-UP QUESTIONS SECTION
// ============================================================================

@Composable
private fun NeuFollowUpQuestionsSection(
    questions: List<String>,
    onQuestionClick: (String) -> Unit,
    neu: NeuColors
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "💡", fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Related Follow-Up Questions",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = WikipediaTheme.colors.secondaryColor
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        questions.forEach { question ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .neuFlat(
                        lightShadow = neu.lightShadow,
                        darkShadow = neu.darkShadow,
                        cornerRadius = 16.dp,
                        intensity = 0.4f
                    )
                    .background(WikipediaTheme.colors.neuBackground, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onQuestionClick(question) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = question,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = WikipediaTheme.colors.neuAccent,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_forward_24),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = WikipediaTheme.colors.neuAccent
                    )
                }
            }
        }
    }
}

// ============================================================================
// UTILITY
// ============================================================================

private fun extractFollowUpQuestions(text: String): List<String> {
    if (!text.contains("Related Questions", ignoreCase = true)) return emptyList()
    val section = text.substringAfter("Related Questions", "")
    return section.lines().mapNotNull { line ->
        val trimmed = line.trim().removePrefix("-").removePrefix("*").removePrefix("1.").removePrefix("2.").removePrefix("3.").trim()
        if (trimmed.length > 5 && (trimmed.endsWith("?") || trimmed.startsWith("What") || trimmed.startsWith("How") || trimmed.startsWith("Why"))) {
            trimmed
        } else null
    }.take(3)
}
