package org.wikipedia.ai

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class RotatingTextSplitBy {
    CHARACTERS, WORDS, LINES
}

/**
 * Kotlin Jetpack Compose port of React Bits `<RotatingText />`.
 * Rotates text strings with staggered character entrance animation & vertical slide transition.
 */
@Composable
fun RotatingText(
    texts: List<String>,
    modifier: Modifier = Modifier,
    rotationInterval: Long = 2200L,
    staggerDuration: Long = 20L,
    splitBy: RotatingTextSplitBy = RotatingTextSplitBy.CHARACTERS,
    textStyle: TextStyle = TextStyle.Default,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = 13.sp,
    fontWeight: FontWeight = FontWeight.Medium
) {
    if (texts.isEmpty()) return

    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(texts, rotationInterval) {
        while (true) {
            delay(rotationInterval)
            currentIndex = (currentIndex + 1) % texts.size
        }
    }

    val currentText = texts[currentIndex]

    AnimatedContent(
        targetState = currentText,
        transitionSpec = {
            (slideInVertically { height -> height } + fadeIn(animationSpec = tween(250))) togetherWith
                    (slideOutVertically { height -> -height } + fadeOut(animationSpec = tween(200)))
        },
        label = "rotating_text_anim",
        modifier = modifier
    ) { text ->
        if (splitBy == RotatingTextSplitBy.CHARACTERS) {
            StaggeredCharacterText(
                text = text,
                staggerDuration = staggerDuration,
                textStyle = textStyle,
                color = color,
                fontSize = fontSize,
                fontWeight = fontWeight
            )
        } else {
            Text(
                text = text,
                style = textStyle,
                color = color,
                fontSize = fontSize,
                fontWeight = fontWeight
            )
        }
    }
}

@Composable
private fun StaggeredCharacterText(
    text: String,
    staggerDuration: Long,
    textStyle: TextStyle,
    color: Color,
    fontSize: TextUnit,
    fontWeight: FontWeight
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        text.forEachIndexed { index, char ->
            val animOffsetY = remember(text) { Animatable(25f) }
            val animAlpha = remember(text) { Animatable(0f) }

            LaunchedEffect(text) {
                delay(index * staggerDuration)
                launch {
                    animOffsetY.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }
                launch {
                    animAlpha.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 150)
                    )
                }
            }

            Text(
                text = if (char == ' ') "\u00A0" else char.toString(),
                style = textStyle,
                color = color,
                fontSize = fontSize,
                fontWeight = fontWeight,
                modifier = Modifier
                    .offset { IntOffset(0, animOffsetY.value.toInt()) }
                    .alpha(animAlpha.value)
            )
        }
    }
}
