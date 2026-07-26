package org.wikipedia.compose.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import org.wikipedia.compose.ComposeColors

@Immutable
data class WikipediaColor(
    val isDarkTheme: Boolean = false,
    val primaryColor: Color,
    val paperColor: Color,
    val backgroundColor: Color,
    val inactiveColor: Color,
    val placeholderColor: Color,
    val secondaryColor: Color,
    val borderColor: Color,
    val progressiveColor: Color,
    val successColor: Color,
    val destructiveColor: Color,
    val warningColor: Color,
    val highlightColor: Color,
    val focusColor: Color,
    val additionColor: Color,
    val overlayColor: Color,

    // Neomorphism tokens — used by AI chat screens for premium soft-UI effect
    val neuBackground: Color = Color.Unspecified,
    val neuLightShadow: Color = Color.Unspecified,
    val neuDarkShadow: Color = Color.Unspecified,
    val neuSurfaceCard: Color = Color.Unspecified,
    val neuAccent: Color = Color.Unspecified
)

val LocalWikipediaColor = staticCompositionLocalOf {
    WikipediaColor(
        primaryColor = Color.Unspecified,
        paperColor = Color.Unspecified,
        backgroundColor = Color.Unspecified,
        inactiveColor = Color.Unspecified,
        placeholderColor = Color.Unspecified,
        secondaryColor = Color.Unspecified,
        borderColor = Color.Unspecified,
        progressiveColor = Color.Unspecified,
        successColor = Color.Unspecified,
        destructiveColor = Color.Unspecified,
        warningColor = Color.Unspecified,
        highlightColor = Color.Unspecified,
        focusColor = Color.Unspecified,
        additionColor = Color.Unspecified,
        overlayColor = Color.Unspecified,
    )
}

val LightColors = WikipediaColor(
    primaryColor = Color(0xFF1A1A1F), // Near-black high contrast text (>12:1 ratio)
    paperColor = Color(0xFFEEF0F5),   // Core Neumorphic rule: surfaces match background
    backgroundColor = Color(0xFFEEF0F5), // Soft cool gray-white
    inactiveColor = ComposeColors.Gray400,
    placeholderColor = Color(0xFF717680),
    secondaryColor = Color(0xFF525866),
    borderColor = Color(0xFFD5D8E1),
    progressiveColor = Color(0xFF255BCA), // Deepened Wikipedia brand blue
    successColor = ComposeColors.Green700,
    destructiveColor = ComposeColors.Red700,
    warningColor = ComposeColors.Yellow700,
    highlightColor = ComposeColors.Yellow500,
    focusColor = ComposeColors.Orange500,
    additionColor = ComposeColors.Blue300_15,
    overlayColor = ComposeColors.Black_30,
    // Neumorphism 2.0: soft cool gray-white clay
    neuBackground = Color(0xFFEEF0F5),
    neuLightShadow = Color(0xFFFFFFFF),
    neuDarkShadow = Color(0xFFB8BCC8),
    neuSurfaceCard = Color(0xFFEEF0F5),
    neuAccent = Color(0xFF255BCA)
)

val DarkColors = WikipediaColor(
    isDarkTheme = true,
    primaryColor = ComposeColors.Gray200,
    paperColor = ComposeColors.Gray700,
    backgroundColor = ComposeColors.Gray675,
    inactiveColor = ComposeColors.Gray500,
    placeholderColor = ComposeColors.Gray400,
    secondaryColor = ComposeColors.Gray300,
    borderColor = ComposeColors.Gray650,
    progressiveColor = ComposeColors.Blue300,
    successColor = ComposeColors.Green600,
    destructiveColor = ComposeColors.Red500,
    warningColor = ComposeColors.Orange500,
    highlightColor = ComposeColors.Yellow500_40,
    focusColor = ComposeColors.Orange500_50,
    additionColor = ComposeColors.Blue600_30,
    overlayColor = ComposeColors.Black_70,
    // Neomorphism: deep charcoal blue — premium dark mode
    neuBackground = Color(0xFF1E1E26),
    neuLightShadow = Color(0xFF2A2A35),
    neuDarkShadow = Color(0xFF111117),
    neuSurfaceCard = Color(0xFF24242E),
    neuAccent = Color(0xFF818CF8)
)

val BlackColors = WikipediaColor(
    isDarkTheme = true,
    primaryColor = ComposeColors.Gray200,
    paperColor = ComposeColors.Black,
    backgroundColor = ComposeColors.Gray700,
    inactiveColor = ComposeColors.Gray500,
    placeholderColor = ComposeColors.Gray500,
    secondaryColor = ComposeColors.Gray300,
    borderColor = ComposeColors.Gray675,
    progressiveColor = ComposeColors.Blue300,
    successColor = ComposeColors.Green600,
    destructiveColor = ComposeColors.Red500,
    warningColor = ComposeColors.Orange500,
    highlightColor = ComposeColors.Yellow500_40,
    focusColor = ComposeColors.Orange500_50,
    additionColor = ComposeColors.Blue600_30,
    overlayColor = ComposeColors.Black_70,
    // Neomorphism: true black abyss — AMOLED premium
    neuBackground = Color(0xFF0A0A0F),
    neuLightShadow = Color(0xFF1A1A22),
    neuDarkShadow = Color(0xFF000000),
    neuSurfaceCard = Color(0xFF121218),
    neuAccent = Color(0xFF818CF8)
)

val SepiaColors = WikipediaColor(
    primaryColor = ComposeColors.Gray700,
    paperColor = ComposeColors.Beige100,
    backgroundColor = ComposeColors.Beige300,
    inactiveColor = ComposeColors.Taupe200,
    placeholderColor = ComposeColors.Taupe600,
    secondaryColor = ComposeColors.Gray600,
    borderColor = ComposeColors.Beige400,
    progressiveColor = ComposeColors.Blue600,
    successColor = ComposeColors.Green700,
    destructiveColor = ComposeColors.Red700,
    warningColor = ComposeColors.Yellow700,
    highlightColor = ComposeColors.Yellow500,
    focusColor = ComposeColors.Orange500,
    additionColor = ComposeColors.Blue300_15,
    overlayColor = ComposeColors.Black_30,
    // Neomorphism: warm parchment — elegant sepia
    neuBackground = Color(0xFFE8DFD0),
    neuLightShadow = Color(0xFFF8F2E8),
    neuDarkShadow = Color(0xFFC8BFAE),
    neuSurfaceCard = Color(0xFFEDE5D6),
    neuAccent = Color(0xFF8B7355)
)

@Composable
fun WikipediaColor.shimmerColors(): List<Color> {
    return if (isDarkTheme) {
        listOf(
            borderColor.copy(alpha = 0.3f),
            inactiveColor.copy(alpha = 0.5f),
            borderColor.copy(alpha = 0.3f)
        )
    } else {
        listOf(
            borderColor.copy(alpha = 0.6f),
            backgroundColor.copy(alpha = 0.8f),
            borderColor.copy(alpha = 0.6f)
        )
    }
}
