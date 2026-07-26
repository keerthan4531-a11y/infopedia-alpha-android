package org.wikipedia.ai

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.wikipedia.R
import org.wikipedia.compose.theme.WikipediaTheme

@Composable
fun WikipediaContextBanner(status: WikipediaSearchStatus) {
    val infiniteTransition = rememberInfiniteTransition(label = "wiki_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val neu = neuColors()

    // Neumorphic elevated slim banner
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .neuElevated(
                lightShadow = neu.lightShadow,
                darkShadow = neu.darkShadow,
                shadowRadius = 6.dp,
                cornerRadius = 12.dp,
                lightOffset = (-2).dp,
                darkOffset = 3.dp,
                intensity = 0.45f
            )
            .background(WikipediaTheme.colors.neuBackground, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Wikipedia icon in a small neumorphic circle
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .neuElevated(
                        lightShadow = neu.lightShadow,
                        darkShadow = neu.darkShadow,
                        shadowRadius = 3.dp,
                        cornerRadius = 14.dp,
                        lightOffset = (-1).dp,
                        darkOffset = 2.dp,
                        intensity = 0.4f
                    )
                    .background(WikipediaTheme.colors.neuBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_w_transparent),
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .alpha(pulseAlpha),
                    tint = WikipediaTheme.colors.progressiveColor
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            when (status) {
                is WikipediaSearchStatus.Searching -> {
                    RotatingText(
                        texts = listOf(
                            "🌐 Multi-Lang Cross-RAG: Querying Wikipedia for \"${status.query}\"…",
                            "🔍 Deep Sections: Extracting article chunks & indexes…",
                            "🏛️ Wikidata Facts: Pulling structured infobox entity facts…"
                        ),
                        color = WikipediaTheme.colors.progressiveColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        rotationInterval = 2000L,
                        staggerDuration = 18L
                    )
                }
                is WikipediaSearchStatus.ReadingPage -> {
                    Text(
                        text = "📖 Reading ${status.current}/${status.total}: ${status.title}…",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = WikipediaTheme.colors.progressiveColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.alpha(pulseAlpha)
                    )
                }
                is WikipediaSearchStatus.Synthesizing -> {
                    RotatingText(
                        texts = listOf(
                            "💡 Synthesizing ${status.count} articles & section chunks…",
                            "⚡ Keyword Relevance Scoring: Ranking top section context…",
                            "🎯 Linking verified inline citations [1], [2], [3]…"
                        ),
                        color = WikipediaTheme.colors.progressiveColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        rotationInterval = 2000L,
                        staggerDuration = 18L
                    )
                }
                is WikipediaSearchStatus.Done -> {
                    val count = status.context.pagesRead.size
                    val chunkCount = status.context.rankedChunks.size
                    val factCount = status.context.wikidataFacts.size
                    ShinyText(
                        text = "⚡ Full-Power RAG Active: $count source${if (count != 1) "s" else ""} • $chunkCount section chunks${if (factCount > 0) " • $factCount Wikidata facts" else ""}",
                        color = WikipediaTheme.colors.successColor,
                        shineColor = androidx.compose.ui.graphics.Color(0xFF86EFAC),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                is WikipediaSearchStatus.Error -> {
                    Text(
                        text = "⚠️ ${status.message}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = WikipediaTheme.colors.destructiveColor
                    )
                }
            }
        }
    }
}
