package org.wikipedia.ai

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Kotlin Jetpack Compose port of React Bits `<ShinyText />`.
 * Renders text with a glowing shiny gradient sweep moving horizontally across the characters.
 */
@Composable
fun ShinyText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF94A3B8),
    shineColor: Color = Color(0xFFFFFFFF),
    durationMillis: Int = 1800,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Medium,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE
) {
    if (text.isEmpty()) return

    val transition = rememberInfiniteTransition(label = "shiny_text_sweep")
    val offsetFraction by transition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset_fraction"
    )

    val shinyBrush = remember(offsetFraction, color, shineColor) {
        val width = 1000f
        val startX = width * offsetFraction
        Brush.linearGradient(
            colors = listOf(
                color,
                color,
                shineColor,
                color,
                color
            ),
            start = Offset(startX, 0f),
            end = Offset(startX + 400f, 0f)
        )
    }

    Text(
        text = text,
        modifier = modifier,
        style = TextStyle(brush = shinyBrush),
        fontSize = fontSize,
        fontWeight = fontWeight,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}
