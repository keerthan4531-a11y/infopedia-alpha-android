package org.wikipedia.compose.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.wikipedia.R
import org.wikipedia.ai.neuAnimatedPress
import org.wikipedia.ai.neuAnimatedScale
import org.wikipedia.ai.neuColors
import org.wikipedia.ai.neuElevated
import org.wikipedia.ai.neuInputFocused
import org.wikipedia.ai.neuPressed
import org.wikipedia.compose.theme.WikipediaTheme

// ============================================================================
// NEUMORPHISM 2.0 SHARED COMPONENTS
// ============================================================================

/**
 * Neumorphic 2.0 Card Container.
 * Features dual soft shadows and smooth 0.97x scale-down on tap.
 */
@Composable
fun NeuCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    shadowRadius: Dp = 10.dp,
    intensity: Float = 0.75f,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val neu = neuColors()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val borderModifier = if (isPressed && onClick != null) {
        Modifier.border(1.dp, WikipediaTheme.colors.progressiveColor.copy(alpha = 0.5f), RoundedCornerShape(cornerRadius))
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .neuAnimatedScale(isPressed = isPressed && onClick != null)
            .neuElevated(
                lightShadow = neu.lightShadow,
                darkShadow = neu.darkShadow,
                shadowRadius = shadowRadius,
                cornerRadius = cornerRadius,
                intensity = intensity
            )
            .then(borderModifier)
            .background(neu.surface, RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius))
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
    ) {
        content()
    }
}

/**
 * Neumorphic 2.0 Primary Button.
 * At rest: neuElevated(). On tap: animates to inset neuPressed() over 180ms.
 * Features a visible accent-colored fill so it is unambiguously tappable.
 */
@Composable
fun NeuButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    enabled: Boolean = true,
    accentFill: Boolean = true,
    cornerRadius: Dp = 20.dp,
    content: @Composable (() -> Unit)? = null
) {
    val neu = neuColors()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bgColor = if (accentFill) WikipediaTheme.colors.progressiveColor else neu.surface
    val textColor = if (accentFill) WikipediaTheme.colors.paperColor else WikipediaTheme.colors.primaryColor

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .neuAnimatedScale(isPressed = isPressed, targetScale = 0.96f)
            .neuAnimatedPress(
                isPressed = isPressed,
                lightShadow = neu.lightShadow,
                darkShadow = neu.darkShadow,
                shadowRadius = 8.dp,
                cornerRadius = cornerRadius
            )
            .background(bgColor, RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        if (content != null) {
            content()
        } else if (text != null) {
            Text(
                text = text,
                color = textColor,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Neumorphic 2.0 Input Field.
 * Inset/scooped clay container with a crisp 1.5dp accent border on focus for clarity.
 */
@Composable
fun NeuTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    cornerRadius: Dp = 24.dp,
    singleLine: Boolean = true,
    textStyle: TextStyle = TextStyle.Default
) {
    val neu = neuColors()
    var isFocused by remember { mutableStateOf(false) }

    val focusBorderModifier = if (isFocused) {
        Modifier.neuInputFocused(WikipediaTheme.colors.progressiveColor, cornerRadius)
    } else {
        Modifier
    }

    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .neuPressed(
                lightShadow = neu.lightShadow,
                darkShadow = neu.darkShadow,
                shadowRadius = 6.dp,
                cornerRadius = cornerRadius,
                intensity = 0.6f
            )
            .background(neu.surface, RoundedCornerShape(cornerRadius))
            .then(focusBorderModifier)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        if (value.isEmpty() && placeholder.isNotEmpty()) {
            Text(
                text = placeholder,
                color = WikipediaTheme.colors.placeholderColor,
                style = textStyle.copy(fontSize = 15.sp)
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            textStyle = textStyle.copy(
                color = WikipediaTheme.colors.primaryColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            ),
            cursorBrush = SolidColor(WikipediaTheme.colors.progressiveColor),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused }
        )
    }
}

/**
 * Neumorphic 2.0 Toggle Switch.
 * Features an inset clay track with an elevated gradient thumb that animates smoothly.
 * Displays unambiguous color shift (accent blue) when checked.
 */
@Composable
fun NeuToggleSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val neu = neuColors()
    val trackWidth = 52.dp
    val trackHeight = 28.dp
    val thumbSize = 22.dp

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) trackWidth - thumbSize - 3.dp else 3.dp,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "neuThumbOffset"
    )

    val trackColor by animateColorAsState(
        targetValue = if (checked) WikipediaTheme.colors.progressiveColor.copy(alpha = 0.2f) else neu.surface,
        animationSpec = tween(180),
        label = "neuTrackColor"
    )

    val thumbColor = if (checked) WikipediaTheme.colors.progressiveColor else neu.surface

    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .size(width = trackWidth, height = trackHeight)
            .neuPressed(
                lightShadow = neu.lightShadow,
                darkShadow = neu.darkShadow,
                shadowRadius = 4.dp,
                cornerRadius = 14.dp,
                intensity = 0.5f
            )
            .background(trackColor, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = enabled && onCheckedChange != null) {
                onCheckedChange?.invoke(!checked)
            }
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbSize)
                .neuElevated(
                    lightShadow = neu.lightShadow,
                    darkShadow = neu.darkShadow,
                    shadowRadius = 3.dp,
                    cornerRadius = 11.dp,
                    lightOffset = (-1).dp,
                    darkOffset = 1.dp,
                    intensity = 0.7f
                )
                .background(
                    if (checked) {
                        Brush.linearGradient(
                            listOf(
                                WikipediaTheme.colors.progressiveColor,
                                WikipediaTheme.colors.progressiveColor.copy(alpha = 0.85f)
                            )
                        )
                    } else {
                        Brush.linearGradient(listOf(thumbColor, thumbColor))
                    },
                    CircleShape
                )
                .border(
                    width = 0.5.dp,
                    color = if (checked) Color.White.copy(alpha = 0.4f) else WikipediaTheme.colors.borderColor.copy(alpha = 0.5f),
                    shape = CircleShape
                )
        )
    }
}

/**
 * Neumorphic 2.0 Top Application Bar.
 * Elevated bar with clean typography and circular neumorphic buttons for navigation.
 */
@Composable
fun NeuTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigationClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val neu = neuColors()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .neuElevated(
                lightShadow = neu.lightShadow,
                darkShadow = neu.darkShadow,
                shadowRadius = 6.dp,
                cornerRadius = 0.dp,
                lightOffset = 0.dp,
                darkOffset = 3.dp,
                intensity = 0.4f
            )
            .background(neu.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onNavigationClick != null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .neuElevated(
                            lightShadow = neu.lightShadow,
                            darkShadow = neu.darkShadow,
                            shadowRadius = 4.dp,
                            cornerRadius = 20.dp,
                            lightOffset = (-2).dp,
                            darkOffset = 2.dp,
                            intensity = 0.6f
                        )
                        .background(neu.surface, CircleShape)
                        .clip(CircleShape)
                        .clickable { onNavigationClick() }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back_black_24dp),
                        contentDescription = null,
                        tint = WikipediaTheme.colors.primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = WikipediaTheme.colors.primaryColor,
                fontWeight = FontWeight.Bold
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            content = actions
        )
    }
}
