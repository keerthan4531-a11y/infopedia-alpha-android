package org.wikipedia.compose.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.wikipedia.R
import org.wikipedia.ai.neuColors
import org.wikipedia.ai.neuElevated
import org.wikipedia.ai.neuPressed
import org.wikipedia.compose.theme.WikipediaTheme
import org.wikipedia.navtab.NavTab

// ============================================================================
// NEUMORPHISM 2.0 BOTTOM NAVIGATION BAR
// ============================================================================

/**
 * Neumorphism 2.0 Bottom Navigation Bar supporting Wikipedia's 6 main tabs.
 * Features an elevated soft bar where the active tab icon sits inside a small
 * inset/pressed clay circle paired with crisp accent coloring for WCAG AA compliance.
 */
@Composable
fun NeuBottomNav(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier,
    tabs: List<NavTab> = NavTab.entries
) {
    val neu = neuColors()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .neuElevated(
                lightShadow = neu.lightShadow,
                darkShadow = neu.darkShadow,
                shadowRadius = 14.dp,
                cornerRadius = 0.dp,
                lightOffset = 0.dp,
                darkOffset = (-4).dp,
                intensity = 0.65f
            )
            .background(neu.surface)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEach { tab ->
            val isSelected = tab == selectedTab
            val interactionSource = remember { MutableInteractionSource() }

            val iconScale by animateFloatAsState(
                targetValue = if (isSelected) 1.08f else 1.0f,
                animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                label = "neuTabScale"
            )

            val iconColor by animateColorAsState(
                targetValue = if (isSelected) WikipediaTheme.colors.progressiveColor else WikipediaTheme.colors.secondaryColor,
                animationSpec = tween(durationMillis = 180),
                label = "neuTabColor"
            )

            val textColor by animateColorAsState(
                targetValue = if (isSelected) WikipediaTheme.colors.progressiveColor else WikipediaTheme.colors.secondaryColor,
                animationSpec = tween(durationMillis = 180),
                label = "neuTabTextColor"
            )

            val indicatorBg = if (isSelected) {
                WikipediaTheme.colors.progressiveColor.copy(alpha = 0.15f)
            } else {
                Color.Transparent
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onTabSelected(tab) }
                    )
                    .padding(vertical = 4.dp)
            ) {
                val iconRes = when (tab) {
                    NavTab.AI -> R.drawable.ic_ai_sparkles_24dp
                    NavTab.HOME -> if (isSelected) R.drawable.ic_home_filled_24dp else R.drawable.ic_home_24dp
                    NavTab.READING_LISTS -> if (isSelected) R.drawable.ic_bookmark_white_24dp else R.drawable.ic_bookmark_border_white_24dp
                    NavTab.SEARCH -> if (isSelected) R.drawable.search_bold else R.drawable.ic_search_white_24dp
                    NavTab.EDITS -> R.drawable.outline_activity_24
                    NavTab.MORE -> R.drawable.ic_menu_white_24dp
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .scale(iconScale)
                        .then(
                            if (isSelected) {
                                Modifier.neuPressed(
                                    lightShadow = neu.lightShadow,
                                    darkShadow = neu.darkShadow,
                                    shadowRadius = 5.dp,
                                    cornerRadius = 21.dp,
                                    intensity = 0.7f
                                )
                            } else Modifier
                        )
                        .background(indicatorBg, CircleShape)
                ) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = stringResource(tab.text),
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(tab.text),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = textColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
