package org.wikipedia.ai

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (isUser) {
            // User message bubble (Clean right-aligned blue/progressive bubble)
            Surface(
                color = WikipediaTheme.colors.progressiveColor,
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = 18.dp,
                    bottomEnd = 4.dp
                ),
                modifier = Modifier
                    .widthIn(max = 310.dp)
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
            // AI message Card container (Matching Wikipedia's base card aesthetic)
            Surface(
                color = WikipediaTheme.colors.backgroundColor,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, WikipediaTheme.colors.borderColor),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    // AI Model Header Pill
                    if (message.model != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 10.dp)
                        ) {
                            Surface(
                                color = getBadgeColor(message.model.badgeColor).copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
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
                                Surface(
                                    color = getBadgeColor(message.model.badgeColor).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = message.model.badge,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = getBadgeColor(message.model.badgeColor),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 1. TOP: Wikipedia Featured Hero Media Showcase Card (FIRST BEFORE RESPONSE)
                    val primaryArticle = message.wikipediaContext?.primaryArticle
                    val heroImageUrl = primaryArticle?.originalImageUrl ?: primaryArticle?.thumbnailUrl
                    if (!heroImageUrl.isNullOrEmpty()) {
                        HeroMediaCard(
                            article = primaryArticle!!,
                            imageUrl = heroImageUrl,
                            onClick = {
                                val pageTitle = PageTitle(primaryArticle.title, WikipediaApp.instance.wikiSite)
                                val historyEntry = HistoryEntry(pageTitle, HistoryEntry.SOURCE_INTERNAL_LINK)
                                val intent = PageActivity.newIntentForNewTab(context, historyEntry, pageTitle)
                                context.startActivity(intent)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // DeepThink Reasoning Block
                    if (!thinkingContent.isNullOrEmpty()) {
                        Surface(
                            color = WikipediaTheme.colors.paperColor,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .animateContentSize()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🧠", fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "DeepThink Reasoning",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF8B5CF6)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = thinkingContent,
                                    fontSize = 12.sp,
                                    color = WikipediaTheme.colors.secondaryColor,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // 2. MIDDLE: Wikipedia Powered AI Response Content
                    Box(modifier = Modifier.fillMaxWidth()) {
                        StreamingTextRenderer(
                            text = message.content,
                            isStreaming = message.isStreaming
                        )
                    }

                    // 3. BOTTOM: Interactive Wikipedia Article Cards (Source Cards with Image & Action Button)
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
                                ArticleSourceCard(
                                    article = article,
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
                        FollowUpQuestionsSection(
                            questions = followUps,
                            onQuestionClick = onFollowUpClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroMediaCard(
    article: WikipediaArticleItem,
    imageUrl: String,
    onClick: () -> Unit
) {
    Surface(
        color = WikipediaTheme.colors.paperColor,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, WikipediaTheme.colors.borderColor),
        modifier = Modifier
            .fillMaxWidth()
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

@Composable
private fun ArticleSourceCard(
    article: WikipediaArticleItem,
    onClick: () -> Unit
) {
    Surface(
        color = WikipediaTheme.colors.paperColor,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, WikipediaTheme.colors.borderColor),
        modifier = Modifier
            .width(180.dp)
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
                Surface(
                    color = WikipediaTheme.colors.progressiveColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = article.langCode.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = WikipediaTheme.colors.progressiveColor,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
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

            Surface(
                color = WikipediaTheme.colors.progressiveColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 5.dp)
                ) {
                    Text(
                        text = "Read Article",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = WikipediaTheme.colors.progressiveColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_forward_24),
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = WikipediaTheme.colors.progressiveColor
                    )
                }
            }
        }
    }
}

@Composable
private fun FollowUpQuestionsSection(
    questions: List<String>,
    onQuestionClick: (String) -> Unit
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
        Spacer(modifier = Modifier.height(6.dp))
        questions.forEach { question ->
            Surface(
                color = WikipediaTheme.colors.paperColor,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, WikipediaTheme.colors.borderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onQuestionClick(question) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = question,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = WikipediaTheme.colors.progressiveColor,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_forward_24),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = WikipediaTheme.colors.progressiveColor
                    )
                }
            }
        }
    }
}

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
