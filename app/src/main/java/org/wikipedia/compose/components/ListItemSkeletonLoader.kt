package org.wikipedia.compose.components

import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.wikipedia.ai.neuColors
import org.wikipedia.ai.neuSkeletonPulse
import org.wikipedia.compose.theme.BaseTheme
import org.wikipedia.search.semanticShimmerColors
import org.wikipedia.theme.Theme

@Composable
fun ListItemSkeletonLoader(
    modifier: Modifier = Modifier,
    shimmerColors: List<Color>,
    transition: InfiniteTransition
) {
    val neu = neuColors()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .neuSkeletonPulse(
                lightShadow = neu.lightShadow,
                darkShadow = neu.darkShadow,
                cornerRadius = 14.dp
            )
            .background(neu.surface, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .fillMaxWidth(0.5f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(neu.darkShadow.copy(alpha = 0.2f))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(neu.darkShadow.copy(alpha = 0.15f))
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(neu.darkShadow.copy(alpha = 0.2f))
            )
        }
    }
}

@Preview
@Composable
private fun ListItemSkeletonLoaderPreview() {
    val shimmerColors = semanticShimmerColors()
    BaseTheme(
        currentTheme = Theme.LIGHT
    ) {
        ListItemSkeletonLoader(
            shimmerColors = shimmerColors,
            transition = rememberInfiniteTransition()
        )
    }
}
