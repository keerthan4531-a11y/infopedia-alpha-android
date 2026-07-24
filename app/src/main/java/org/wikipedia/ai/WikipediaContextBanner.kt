package org.wikipedia.ai

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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

    Surface(
        color = WikipediaTheme.colors.progressiveColor.copy(alpha = 0.1f),
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_w_transparent),
                contentDescription = null,
                modifier = Modifier
                    .size(18.dp)
                    .alpha(pulseAlpha),
                tint = WikipediaTheme.colors.progressiveColor
            )
            Spacer(modifier = Modifier.width(10.dp))

            when (status) {
                is WikipediaSearchStatus.Searching -> {
                    Text(
                        text = "🔍 Perplexity Search: Querying Wikipedia for \"${status.query}\"…",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = WikipediaTheme.colors.progressiveColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.alpha(pulseAlpha)
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
                    Text(
                        text = "💡 Synthesizing ${status.count} verified articles & citations…",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = WikipediaTheme.colors.progressiveColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.alpha(pulseAlpha)
                    )
                }
                is WikipediaSearchStatus.Done -> {
                    val count = status.context.pagesRead.size
                    Text(
                        text = "✅ Read $count Wikipedia article${if (count != 1) "s" else ""}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = WikipediaTheme.colors.successColor
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
