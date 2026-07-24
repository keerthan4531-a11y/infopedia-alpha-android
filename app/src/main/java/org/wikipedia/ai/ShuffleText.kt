package org.wikipedia.ai

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Kotlin Jetpack Compose port of React Bits `<Shuffle />` component.
 * Performs a matrix-style character scramble / shuffle animation before settling into final text.
 */
@Composable
fun ShuffleText(
    text: String,
    modifier: Modifier = Modifier,
    scrambleCharset: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*",
    shuffleTimes: Int = 3,
    staggerMs: Long = 20L,
    stepDurationMs: Long = 35L,
    textStyle: TextStyle = TextStyle.Default,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    textAlign: TextAlign? = null,
    onShuffleComplete: (() -> Unit)? = null
) {
    if (text.isEmpty()) return

    var displayedText by remember(text) { mutableStateOf("") }

    LaunchedEffect(text) {
        val charCount = text.length
        val currentArray = CharArray(charCount)

        // Initialize with random scramble chars (spaces and punctuation preserved)
        for (i in 0 until charCount) {
            val originalChar = text[i]
            if (originalChar.isWhitespace()) {
                currentArray[i] = originalChar
            } else {
                currentArray[i] = scrambleCharset[Random.nextInt(scrambleCharset.length)]
            }
        }
        displayedText = String(currentArray)

        // Staggered reveal per character position
        for (i in 0 until charCount) {
            val targetChar = text[i]
            if (targetChar.isWhitespace()) {
                currentArray[i] = targetChar
                displayedText = String(currentArray)
                continue
            }

            // Shuffle scrambleTimes before settling into final target character
            for (roll in 0 until shuffleTimes) {
                currentArray[i] = scrambleCharset[Random.nextInt(scrambleCharset.length)]
                displayedText = String(currentArray)
                delay(stepDurationMs)
            }

            // Lock in target character
            currentArray[i] = targetChar
            displayedText = String(currentArray)
            delay(staggerMs)
        }

        onShuffleComplete?.invoke()
    }

    Text(
        text = displayedText.ifEmpty { text },
        modifier = modifier,
        style = textStyle,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        textAlign = textAlign
    )
}
