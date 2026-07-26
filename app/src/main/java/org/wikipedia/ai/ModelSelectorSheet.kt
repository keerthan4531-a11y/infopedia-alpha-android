package org.wikipedia.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.wikipedia.R
import org.wikipedia.compose.theme.WikipediaTheme

@Composable
fun ModelSelectorSheet(
    models: List<AiModel>,
    selectedModel: AiModel,
    onModelSelected: (AiModel) -> Unit,
    onDismiss: () -> Unit
) {
    val neu = neuColors()

    // Semi-transparent overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable { onDismiss() }
    ) {
        // Bottom sheet — Neumorphic elevated panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clickable(enabled = false) { /* consume click */ }
                .neuElevated(
                    lightShadow = neu.lightShadow,
                    darkShadow = neu.darkShadow,
                    shadowRadius = 14.dp,
                    cornerRadius = 20.dp,
                    lightOffset = (-5).dp,
                    darkOffset = 0.dp,
                    intensity = 0.6f
                )
                .background(
                    WikipediaTheme.colors.neuBackground,
                    RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
        ) {
            Column(
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
            ) {
                // Handle bar — neumorphic pressed pill
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(5.dp)
                        .align(Alignment.CenterHorizontally)
                        .neuPressed(
                            lightShadow = neu.lightShadow,
                            darkShadow = neu.darkShadow,
                            shadowRadius = 2.dp,
                            cornerRadius = 3.dp,
                            intensity = 0.5f
                        )
                        .background(
                            WikipediaTheme.colors.neuBackground,
                            RoundedCornerShape(3.dp)
                        )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Select AI Model",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = WikipediaTheme.colors.primaryColor,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn {
                    items(models) { model ->
                        NeuModelItem(
                            model = model,
                            isSelected = model.id == selectedModel.id,
                            onClick = { onModelSelected(model) },
                            neu = neu
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NeuModelItem(
    model: AiModel,
    isSelected: Boolean,
    onClick: () -> Unit,
    neu: NeuColors
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 5.dp)
            .then(
                if (isSelected) {
                    Modifier
                        .neuPressed(
                            lightShadow = neu.lightShadow,
                            darkShadow = neu.darkShadow,
                            shadowRadius = 5.dp,
                            cornerRadius = 14.dp,
                            intensity = 0.5f
                        )
                        .neuGlow(
                            glowColor = getBadgeColor(model.badgeColor),
                            cornerRadius = 14.dp,
                            glowRadius = 8.dp,
                            intensity = 0.12f
                        )
                } else {
                    Modifier.neuFlat(
                        lightShadow = neu.lightShadow,
                        darkShadow = neu.darkShadow,
                        cornerRadius = 14.dp,
                        intensity = 0.3f
                    )
                }
            )
            .background(WikipediaTheme.colors.neuBackground, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Model icon in a small neumorphic circle
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .neuElevated(
                        lightShadow = neu.lightShadow,
                        darkShadow = neu.darkShadow,
                        shadowRadius = 4.dp,
                        cornerRadius = 16.dp,
                        lightOffset = (-2).dp,
                        darkOffset = 2.dp,
                        intensity = 0.4f
                    )
                    .background(WikipediaTheme.colors.neuBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_ai_sparkles_24dp),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = getBadgeColor(model.badgeColor)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = model.label,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WikipediaTheme.colors.primaryColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Badge
                    Box(
                        modifier = Modifier
                            .background(
                                getBadgeColor(model.badgeColor).copy(alpha = 0.15f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = model.badge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = getBadgeColor(model.badgeColor)
                        )
                    }
                    if (model.enableDeepThink) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    Color(0xFF8B5CF6).copy(alpha = 0.15f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🧠 DeepThink",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8B5CF6)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = model.description,
                    fontSize = 12.sp,
                    color = WikipediaTheme.colors.secondaryColor,
                    fontStyle = FontStyle.Italic
                )
            }

            // Checkmark for selected — inside a neumorphic circle
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .neuPressed(
                            lightShadow = neu.lightShadow,
                            darkShadow = neu.darkShadow,
                            shadowRadius = 3.dp,
                            cornerRadius = 12.dp,
                            intensity = 0.4f
                        )
                        .background(
                            WikipediaTheme.colors.neuAccent.copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check_black_24dp),
                        contentDescription = "Selected",
                        modifier = Modifier.size(16.dp),
                        tint = WikipediaTheme.colors.neuAccent
                    )
                }
            }
        }
    }
}
