package org.wikipedia.ai

import android.graphics.BlurMaskFilter
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.wikipedia.compose.theme.WikipediaTheme

// ============================================================================
// NEUMORPHISM 2.0 COLOR PALETTE & SYSTEM TOKENS
// ============================================================================

/**
 * Neumorphism 2.0 color configuration for a given theme.
 * Light mode uses high-contrast soft gray (#EEF0F5), sharp highlights (#FFFFFF),
 * and deeper depth shadows (#B8BCC8) for strict WCAG AA accessibility compliance.
 */
data class NeuColors(
    val surface: Color,
    val lightShadow: Color,
    val darkShadow: Color,
    val accentGlow: Color
)

@Composable
fun neuColors(): NeuColors {
    val colors = WikipediaTheme.colors
    val isDark = colors.isDarkTheme
    return NeuColors(
        surface = if (colors.neuBackground != Color.Unspecified) colors.neuBackground else if (isDark) Color(0xFF1E1E26) else Color(0xFFEEF0F5),
        lightShadow = if (colors.neuLightShadow != Color.Unspecified) colors.neuLightShadow else if (isDark) Color(0xFF2A2A35) else Color(0xFFFFFFFF),
        darkShadow = if (colors.neuDarkShadow != Color.Unspecified) colors.neuDarkShadow else if (isDark) Color(0xFF111117) else Color(0xFFB8BCC8),
        accentGlow = if (colors.neuAccent != Color.Unspecified) colors.neuAccent else if (isDark) Color(0xFF818CF8) else Color(0xFF255BCA)
    )
}

// ============================================================================
// NEUMORPHIC MODIFIERS — ELEVATED (Raised / Extruded 3D with Cache Optimization)
// ============================================================================

/**
 * Neumorphic 2.0 Elevated modifier. Uses drawWithCache to avoid reallocating
 * Paint and BlurMaskFilter objects during recomposition or scrolling.
 */
fun Modifier.neuElevated(
    lightShadow: Color,
    darkShadow: Color,
    shadowRadius: Dp = 10.dp,
    cornerRadius: Dp = 18.dp,
    lightOffset: Dp = (-5).dp,
    darkOffset: Dp = 5.dp,
    intensity: Float = 0.75f
): Modifier = this.drawWithCache {
    val radiusPx = shadowRadius.toPx()
    val cornerPx = cornerRadius.toPx()
    val lightOffPx = lightOffset.toPx()
    val darkOffPx = darkOffset.toPx()

    val darkPaint = Paint().also { p ->
        val fwPaint = p.asFrameworkPaint()
        fwPaint.isAntiAlias = true
        fwPaint.color = darkShadow.copy(alpha = intensity).toArgb()
        if (radiusPx > 0f) {
            fwPaint.maskFilter = BlurMaskFilter(radiusPx, BlurMaskFilter.Blur.NORMAL)
        }
    }

    val lightPaint = Paint().also { p ->
        val fwPaint = p.asFrameworkPaint()
        fwPaint.isAntiAlias = true
        fwPaint.color = lightShadow.copy(alpha = intensity).toArgb()
        if (radiusPx > 0f) {
            fwPaint.maskFilter = BlurMaskFilter(radiusPx, BlurMaskFilter.Blur.NORMAL)
        }
    }

    onDrawBehind {
        // Dark shadow (bottom-right depth)
        drawIntoCanvas { canvas ->
            canvas.drawRoundRect(
                left = darkOffPx,
                top = darkOffPx,
                right = size.width + darkOffPx,
                bottom = size.height + darkOffPx,
                radiusX = cornerPx,
                radiusY = cornerPx,
                paint = darkPaint
            )
        }
        // Light shadow (top-left highlight)
        drawIntoCanvas { canvas ->
            canvas.drawRoundRect(
                left = lightOffPx,
                top = lightOffPx,
                right = size.width + lightOffPx,
                bottom = size.height + lightOffPx,
                radiusX = cornerPx,
                radiusY = cornerPx,
                paint = lightPaint
            )
        }
    }
}

// ============================================================================
// NEUMORPHIC MODIFIERS — PRESSED (Inset / Scooped with Cache Optimization)
// ============================================================================

/**
 * Neumorphic 2.0 Pressed (inset) modifier. Uses drawWithCache to avoid reallocating
 * Path and Paint objects during recomposition.
 */
fun Modifier.neuPressed(
    lightShadow: Color,
    darkShadow: Color,
    shadowRadius: Dp = 6.dp,
    cornerRadius: Dp = 18.dp,
    intensity: Float = 0.65f
): Modifier = this.drawWithCache {
    val radiusPx = shadowRadius.toPx()
    val cornerPx = cornerRadius.toPx()
    val offsetPx = radiusPx * 0.5f

    val clipPath = Path().apply {
        addRoundRect(
            RoundRect(
                rect = Rect(0f, 0f, size.width, size.height),
                cornerRadius = CornerRadius(cornerPx, cornerPx)
            )
        )
    }

    val darkPaint = Paint().also { p ->
        val fwPaint = p.asFrameworkPaint()
        fwPaint.isAntiAlias = true
        fwPaint.color = darkShadow.copy(alpha = intensity).toArgb()
        if (radiusPx > 0f) {
            fwPaint.maskFilter = BlurMaskFilter(radiusPx, BlurMaskFilter.Blur.NORMAL)
        }
    }

    val lightPaint = Paint().also { p ->
        val fwPaint = p.asFrameworkPaint()
        fwPaint.isAntiAlias = true
        fwPaint.color = lightShadow.copy(alpha = intensity * 0.8f).toArgb()
        if (radiusPx > 0f) {
            fwPaint.maskFilter = BlurMaskFilter(radiusPx, BlurMaskFilter.Blur.NORMAL)
        }
    }

    onDrawWithContent {
        drawContent()

        // Inner dark shadow (top-left edge)
        clipPath(clipPath) {
            drawIntoCanvas { canvas ->
                canvas.drawRoundRect(
                    left = -size.width / 2f,
                    top = -size.height / 2f,
                    right = size.width - offsetPx,
                    bottom = size.height - offsetPx,
                    radiusX = cornerPx,
                    radiusY = cornerPx,
                    paint = darkPaint
                )
            }
        }

        // Inner light shadow (bottom-right edge)
        clipPath(clipPath) {
            drawIntoCanvas { canvas ->
                canvas.drawRoundRect(
                    left = offsetPx,
                    top = offsetPx,
                    right = size.width + size.width / 2f,
                    bottom = size.height + size.height / 2f,
                    radiusX = cornerPx,
                    radiusY = cornerPx,
                    paint = lightPaint
                )
            }
        }
    }
}

// ============================================================================
// NEUMORPHIC MODIFIERS — FLAT (Subtle passive container)
// ============================================================================

fun Modifier.neuFlat(
    lightShadow: Color,
    darkShadow: Color,
    cornerRadius: Dp = 16.dp,
    intensity: Float = 0.4f
): Modifier = this.neuElevated(
    lightShadow = lightShadow,
    darkShadow = darkShadow,
    shadowRadius = 6.dp,
    cornerRadius = cornerRadius,
    lightOffset = (-3).dp,
    darkOffset = 3.dp,
    intensity = intensity
)

// ============================================================================
// NEUMORPHIC MODIFIERS — GLOW & FOCUSED BORDER
// ============================================================================

fun Modifier.neuGlow(
    glowColor: Color,
    cornerRadius: Dp = 18.dp,
    glowRadius: Dp = 12.dp,
    intensity: Float = 0.3f
): Modifier = this.drawWithCache {
    val radiusPx = glowRadius.toPx()
    val cornerPx = cornerRadius.toPx()

    val glowPaint = Paint().also { p ->
        val fwPaint = p.asFrameworkPaint()
        fwPaint.isAntiAlias = true
        fwPaint.color = glowColor.copy(alpha = intensity).toArgb()
        if (radiusPx > 0f) {
            fwPaint.maskFilter = BlurMaskFilter(radiusPx, BlurMaskFilter.Blur.NORMAL)
        }
    }

    onDrawBehind {
        drawIntoCanvas { canvas ->
            canvas.drawRoundRect(
                left = 0f,
                top = 0f,
                right = size.width,
                bottom = size.height,
                radiusX = cornerPx,
                radiusY = cornerPx,
                paint = glowPaint
            )
        }
    }
}

/**
 * 1.5dp crisp accent border for input field focus states in Neumorphism 2.0.
 */
fun Modifier.neuInputFocused(
    accentColor: Color,
    cornerRadius: Dp = 24.dp
): Modifier = this.border(
    width = 1.5.dp,
    color = accentColor,
    shape = RoundedCornerShape(cornerRadius)
)

// ============================================================================
// NEUMORPHIC MODIFIERS — MICRO-INTERACTIONS & ANIMATIONS
// ============================================================================

/**
 * Micro-interaction: Smooth scale-down (0.97x) animation when tapped or pressed.
 */
fun Modifier.neuAnimatedScale(
    isPressed: Boolean,
    targetScale: Float = 0.97f,
    animationDuration: Int = 160
): Modifier = composed {
    val scale by animateFloatAsState(
        targetValue = if (isPressed) targetScale else 1.0f,
        animationSpec = tween(durationMillis = animationDuration, easing = FastOutSlowInEasing),
        label = "neuScale"
    )
    this.scale(scale)
}

/**
 * Micro-interaction: Smooth transition between elevated (at rest) and pressed (on tap).
 */
fun Modifier.neuAnimatedPress(
    isPressed: Boolean,
    lightShadow: Color,
    darkShadow: Color,
    shadowRadius: Dp = 8.dp,
    cornerRadius: Dp = 18.dp,
    intensity: Float = 0.75f
): Modifier = composed {
    val currentIntensity by animateFloatAsState(
        targetValue = if (isPressed) intensity * 0.9f else intensity,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "neuIntensity"
    )
    if (isPressed) {
        this.neuPressed(
            lightShadow = lightShadow,
            darkShadow = darkShadow,
            shadowRadius = shadowRadius * 0.75f,
            cornerRadius = cornerRadius,
            intensity = currentIntensity
        )
    } else {
        this.neuElevated(
            lightShadow = lightShadow,
            darkShadow = darkShadow,
            shadowRadius = shadowRadius,
            cornerRadius = cornerRadius,
            intensity = currentIntensity
        )
    }
}

/**
 * Neumorphic Pulsing Skeleton Loader — replaces generic flat shimmer with a soft breathing clay effect.
 */
fun Modifier.neuSkeletonPulse(
    lightShadow: Color,
    darkShadow: Color,
    cornerRadius: Dp = 16.dp
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "neuSkeleton")
    val pulseIntensity by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neuPulseIntensity"
    )
    this.neuElevated(
        lightShadow = lightShadow,
        darkShadow = darkShadow,
        shadowRadius = 8.dp,
        cornerRadius = cornerRadius,
        intensity = pulseIntensity
    )
}
