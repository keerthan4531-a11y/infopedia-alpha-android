package org.wikipedia.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.wikipedia.R
import org.wikipedia.compose.theme.WikipediaTheme

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalDensity

@Composable
fun InixaAlphaScreen(
    viewModel: InixaAlphaViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val density = LocalDensity.current
    val neu = neuColors()

    // Calculate exact bottom inset to eliminate gap above soft keyboard
    val imeBottom = WindowInsets.ime.getBottom(density)
    val navBottom = WindowInsets.navigationBars.getBottom(density)
    val bottomInsetPx = if (imeBottom > 0) maxOf(0, imeBottom - navBottom) else 0
    val bottomInsetDp = with(density) { bottomInsetPx.toDp() }

    Scaffold(
        containerColor = WikipediaTheme.colors.neuBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WikipediaTheme.colors.neuBackground)
                .padding(paddingValues)
        ) {
            // Header with AI title and model picker trigger button
            NeuTopHeaderBar(
                selectedModel = uiState.selectedModel,
                onModelClick = { viewModel.toggleModelSelector() },
                neu = neu
            )

            // Main chat content area or Welcome screen
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (uiState.messages.isEmpty()) {
                    NeuWelcomeScreen(
                        onSuggestionClick = { viewModel.sendMessage(it) },
                        neu = neu
                    )
                } else {
                    ChatMessagesList(
                        messages = uiState.messages,
                        isStreaming = uiState.isStreaming,
                        thinkingContent = uiState.currentThinkingContent,
                        onFollowUpClick = { viewModel.sendMessage(it) }
                    )
                }
            }

            // Bottom section pinned directly above soft keyboard (zero gap)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = bottomInsetDp)
            ) {
                // Wikipedia context banner / research progress card
                AnimatedVisibility(
                    visible = uiState.wikipediaStatus != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn()
                ) {
                    uiState.wikipediaStatus?.let { status ->
                        WikipediaContextBanner(status = status)
                    }
                }

                // Bottom input area
                NeuChatInputBar(
                    selectedModel = uiState.selectedModel,
                    isWikipediaConnected = uiState.isWikipediaConnected,
                    isStreaming = uiState.isStreaming,
                    onSendMessage = { viewModel.sendMessage(it) },
                    onToggleWikipedia = { viewModel.toggleWikipedia() },
                    onModelClick = { viewModel.toggleModelSelector() },
                    onStopStreaming = { viewModel.stopStreaming() },
                    neu = neu
                )
            }
        }

        // Model selector overlay
        if (uiState.showModelSelector) {
            ModelSelectorSheet(
                models = AiModel.ALL_MODELS,
                selectedModel = uiState.selectedModel,
                onModelSelected = { viewModel.selectModel(it) },
                onDismiss = { viewModel.dismissModelSelector() }
            )
        }
    }
}

// ============================================================================
// NEOMORPHIC TOP HEADER BAR
// ============================================================================

@Composable
private fun NeuTopHeaderBar(
    selectedModel: AiModel,
    onModelClick: () -> Unit,
    neu: NeuColors
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .neuElevated(
                lightShadow = neu.lightShadow,
                darkShadow = neu.darkShadow,
                shadowRadius = 8.dp,
                cornerRadius = 0.dp,
                lightOffset = (-3).dp,
                darkOffset = 4.dp,
                intensity = 0.5f
            )
            .background(WikipediaTheme.colors.neuBackground)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Sparkle icon in a neumorphic circle
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .neuElevated(
                                lightShadow = neu.lightShadow,
                                darkShadow = neu.darkShadow,
                                shadowRadius = 6.dp,
                                cornerRadius = 18.dp,
                                lightOffset = (-2).dp,
                                darkOffset = 2.dp,
                                intensity = 0.5f
                            )
                            .background(WikipediaTheme.colors.neuBackground, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_ai_sparkles_24dp),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = WikipediaTheme.colors.neuAccent
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "INIXA-ALPHA",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = WikipediaTheme.colors.primaryColor,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // "AI ASSISTANT" badge — neumorphic pressed pill
                    Box(
                        modifier = Modifier
                            .neuPressed(
                                lightShadow = neu.lightShadow,
                                darkShadow = neu.darkShadow,
                                shadowRadius = 3.dp,
                                cornerRadius = 6.dp,
                                intensity = 0.3f
                            )
                            .background(WikipediaTheme.colors.neuBackground, RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "AI ASSISTANT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = WikipediaTheme.colors.neuAccent,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Header model selector button — neumorphic elevated pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onModelClick() }
                        .neuElevated(
                            lightShadow = neu.lightShadow,
                            darkShadow = neu.darkShadow,
                            shadowRadius = 5.dp,
                            cornerRadius = 16.dp,
                            lightOffset = (-2).dp,
                            darkOffset = 2.dp,
                            intensity = 0.45f
                        )
                        .background(WikipediaTheme.colors.neuBackground, RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = selectedModel.badge,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = getBadgeColor(selectedModel.badgeColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_down_24),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = WikipediaTheme.colors.secondaryColor
                        )
                    }
                }
            }

            // Subtle accent gradient line at the bottom of the header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                WikipediaTheme.colors.neuAccent.copy(alpha = 0.0f),
                                WikipediaTheme.colors.neuAccent.copy(alpha = 0.3f),
                                WikipediaTheme.colors.neuAccent.copy(alpha = 0.5f),
                                WikipediaTheme.colors.neuAccent.copy(alpha = 0.3f),
                                WikipediaTheme.colors.neuAccent.copy(alpha = 0.0f)
                            )
                        )
                    )
            )
        }
    }
}

// ============================================================================
// NEOMORPHIC WELCOME SCREEN
// ============================================================================

@Composable
private fun NeuWelcomeScreen(
    onSuggestionClick: (String) -> Unit,
    neu: NeuColors
) {
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800),
        label = "welcome_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Large sparkle icon in a neumorphic raised orb
        Box(
            modifier = Modifier
                .size(80.dp)
                .neuElevated(
                    lightShadow = neu.lightShadow,
                    darkShadow = neu.darkShadow,
                    shadowRadius = 14.dp,
                    cornerRadius = 40.dp,
                    lightOffset = (-6).dp,
                    darkOffset = 6.dp,
                    intensity = 0.6f
                )
                .background(WikipediaTheme.colors.neuBackground, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_ai_sparkles_24dp),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = WikipediaTheme.colors.neuAccent
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.inixa_alpha_tagline),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = WikipediaTheme.colors.primaryColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(36.dp))

        NeuSuggestionChips(onSuggestionClick = onSuggestionClick, neu = neu)
    }
}

// ============================================================================
// NEOMORPHIC SUGGESTION CHIPS
// ============================================================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NeuSuggestionChips(
    onSuggestionClick: (String) -> Unit,
    neu: NeuColors
) {
    val suggestions = listOf(
        "🌍" to "How was the UN formed?",
        "🖼️" to "Impressionism and expressionism",
        "🍄" to "Are all mushrooms edible?",
        "🔭" to "What does the SETI Institute do?",
        "🧬" to "How does DNA evolve?",
        "⏳" to "Timeline of the Roman Empire"
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        suggestions.forEach { (emoji, text) ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onSuggestionClick(text) }
                    .neuElevated(
                        lightShadow = neu.lightShadow,
                        darkShadow = neu.darkShadow,
                        shadowRadius = 5.dp,
                        cornerRadius = 20.dp,
                        lightOffset = (-2).dp,
                        darkOffset = 3.dp,
                        intensity = 0.5f
                    )
                    .background(WikipediaTheme.colors.neuBackground, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                Text(
                    text = "$emoji $text",
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = WikipediaTheme.colors.primaryColor
                )
            }
        }
    }
}

// ============================================================================
// CHAT MESSAGES LIST (delegates to ChatBubble for rendering)
// ============================================================================

@Composable
private fun ChatMessagesList(
    messages: List<AiChatMessage>,
    isStreaming: Boolean,
    thinkingContent: String,
    onFollowUpClick: (String) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, messages.lastOrNull()?.content?.length) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(messages, key = { it.id }) { message ->
            ChatBubble(
                message = message,
                thinkingContent = message.thinkingContent ?: if (message == messages.lastOrNull() && message.role == AiChatMessage.ROLE_ASSISTANT)
                    thinkingContent.ifEmpty { null } else null,
                onFollowUpClick = onFollowUpClick
            )
        }
    }
}

// ============================================================================
// NEOMORPHIC CHAT INPUT BAR
// ============================================================================

@Composable
private fun NeuChatInputBar(
    selectedModel: AiModel,
    isWikipediaConnected: Boolean,
    isStreaming: Boolean,
    onSendMessage: (String) -> Unit,
    onToggleWikipedia: () -> Unit,
    onModelClick: () -> Unit,
    onStopStreaming: () -> Unit,
    neu: NeuColors
) {
    var inputText by remember { mutableStateOf("") }

    // Elevated neumorphic bottom bar
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neuElevated(
                lightShadow = neu.lightShadow,
                darkShadow = neu.darkShadow,
                shadowRadius = 10.dp,
                cornerRadius = 0.dp,
                lightOffset = (-4).dp,
                darkOffset = 0.dp,
                intensity = 0.5f
            )
            .background(WikipediaTheme.colors.neuBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            // Model selector + Wikipedia toggle row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Model badge pill — neumorphic elevated
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onModelClick() }
                        .neuElevated(
                            lightShadow = neu.lightShadow,
                            darkShadow = neu.darkShadow,
                            shadowRadius = 4.dp,
                            cornerRadius = 16.dp,
                            lightOffset = (-2).dp,
                            darkOffset = 2.dp,
                            intensity = 0.45f
                        )
                        .background(WikipediaTheme.colors.neuBackground, RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_ai_sparkles_24dp),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = getBadgeColor(selectedModel.badgeColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = selectedModel.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = getBadgeColor(selectedModel.badgeColor),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_down_24),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = getBadgeColor(selectedModel.badgeColor)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Wikipedia toggle pill — neumorphic pressed when active, elevated when inactive
                if (isWikipediaConnected) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onToggleWikipedia() }
                            .neuPressed(
                                lightShadow = neu.lightShadow,
                                darkShadow = neu.darkShadow,
                                shadowRadius = 4.dp,
                                cornerRadius = 16.dp,
                                intensity = 0.5f
                            )
                            .background(
                                WikipediaTheme.colors.progressiveColor.copy(alpha = 0.12f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_w_transparent),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = WikipediaTheme.colors.progressiveColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Wikipedia",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = WikipediaTheme.colors.progressiveColor
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onToggleWikipedia() }
                            .neuElevated(
                                lightShadow = neu.lightShadow,
                                darkShadow = neu.darkShadow,
                                shadowRadius = 4.dp,
                                cornerRadius = 16.dp,
                                lightOffset = (-2).dp,
                                darkOffset = 2.dp,
                                intensity = 0.4f
                            )
                            .background(WikipediaTheme.colors.neuBackground, RoundedCornerShape(16.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_w_transparent),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = WikipediaTheme.colors.secondaryColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Wikipedia",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = WikipediaTheme.colors.secondaryColor
                            )
                        }
                    }
                }
            }

            // Input text field + Send action button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Neumorphic pressed/inset text field — "scooped" into the surface
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .neuPressed(
                            lightShadow = neu.lightShadow,
                            darkShadow = neu.darkShadow,
                            shadowRadius = 5.dp,
                            cornerRadius = 24.dp,
                            intensity = 0.45f
                        )
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.inixa_alpha_ask_anything),
                                color = WikipediaTheme.colors.placeholderColor
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = WikipediaTheme.colors.neuBackground,
                            unfocusedContainerColor = WikipediaTheme.colors.neuBackground,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = WikipediaTheme.colors.neuAccent,
                            focusedTextColor = WikipediaTheme.colors.primaryColor,
                            unfocusedTextColor = WikipediaTheme.colors.primaryColor
                        ),
                        shape = RoundedCornerShape(24.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank() && !isStreaming) {
                                    onSendMessage(inputText)
                                    inputText = ""
                                }
                            }
                        ),
                        singleLine = false,
                        maxLines = 4
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Send / Stop button — neumorphic elevated circle with gradient
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .then(
                            if (isStreaming) {
                                Modifier.neuGlow(
                                    glowColor = WikipediaTheme.colors.destructiveColor,
                                    cornerRadius = 23.dp,
                                    glowRadius = 10.dp,
                                    intensity = 0.35f
                                )
                            } else {
                                Modifier.neuElevated(
                                    lightShadow = neu.lightShadow,
                                    darkShadow = neu.darkShadow,
                                    shadowRadius = 6.dp,
                                    cornerRadius = 23.dp,
                                    lightOffset = (-3).dp,
                                    darkOffset = 3.dp,
                                    intensity = 0.6f
                                )
                            }
                        )
                        .background(
                            brush = if (isStreaming) {
                                Brush.linearGradient(
                                    colors = listOf(
                                        WikipediaTheme.colors.destructiveColor,
                                        WikipediaTheme.colors.destructiveColor.copy(alpha = 0.8f)
                                    )
                                )
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(
                                        WikipediaTheme.colors.neuAccent,
                                        WikipediaTheme.colors.neuAccent.copy(alpha = 0.8f)
                                    )
                                )
                            },
                            shape = CircleShape
                        )
                        .clip(CircleShape)
                        .clickable {
                            if (isStreaming) {
                                onStopStreaming()
                            } else if (inputText.isNotBlank()) {
                                onSendMessage(inputText)
                                inputText = ""
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isStreaming) R.drawable.ic_close_black_24dp
                            else R.drawable.ic_arrow_forward_24
                        ),
                        contentDescription = if (isStreaming) "Stop" else "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
