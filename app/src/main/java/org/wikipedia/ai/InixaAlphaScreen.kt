package org.wikipedia.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity

@Composable
fun InixaAlphaScreen(
    viewModel: InixaAlphaViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val density = LocalDensity.current

    // Calculate exact bottom inset to eliminate gap above soft keyboard
    val imeBottom = WindowInsets.ime.getBottom(density)
    val navBottom = WindowInsets.navigationBars.getBottom(density)
    val bottomInsetPx = if (imeBottom > 0) maxOf(0, imeBottom - navBottom) else 0
    val bottomInsetDp = with(density) { bottomInsetPx.toDp() }

    Scaffold(
        containerColor = WikipediaTheme.colors.backgroundColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header with AI title and model picker trigger button
            TopHeaderBar(
                selectedModel = uiState.selectedModel,
                onModelClick = { viewModel.toggleModelSelector() }
            )

            // Main chat content area or Welcome screen
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (uiState.messages.isEmpty()) {
                    WelcomeScreen(
                        onSuggestionClick = { viewModel.sendMessage(it) }
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
                ChatInputBar(
                    selectedModel = uiState.selectedModel,
                    isWikipediaConnected = uiState.isWikipediaConnected,
                    isStreaming = uiState.isStreaming,
                    onSendMessage = { viewModel.sendMessage(it) },
                    onToggleWikipedia = { viewModel.toggleWikipedia() },
                    onModelClick = { viewModel.toggleModelSelector() },
                    onStopStreaming = { viewModel.stopStreaming() }
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

@Composable
private fun TopHeaderBar(
    selectedModel: AiModel,
    onModelClick: () -> Unit
) {
    Surface(
        color = WikipediaTheme.colors.paperColor,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_ai_sparkles_24dp),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = WikipediaTheme.colors.progressiveColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "INIXA-ALPHA",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = WikipediaTheme.colors.primaryColor,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = WikipediaTheme.colors.progressiveColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "AI ASSISTANT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = WikipediaTheme.colors.progressiveColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Header model selector button
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onModelClick() },
                    color = WikipediaTheme.colors.backgroundColor,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
            HorizontalDivider(color = WikipediaTheme.colors.borderColor, thickness = 0.5.dp)
        }
    }
}

@Composable
private fun WelcomeScreen(onSuggestionClick: (String) -> Unit) {
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
        Icon(
            painter = painterResource(id = R.drawable.ic_ai_sparkles_24dp),
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = WikipediaTheme.colors.progressiveColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.inixa_alpha_tagline),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = WikipediaTheme.colors.primaryColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(36.dp))

        SuggestionChips(onSuggestionClick = onSuggestionClick)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SuggestionChips(onSuggestionClick: (String) -> Unit) {
    val suggestions = listOf(
        "🌍" to "How was the UN formed?",
        "🖼️" to "Impressionism and expressionism",
        "🍄" to "Are all mushrooms edible?",
        "🔭" to "What does the SETI Institute do?",
        "🧬" to "How does DNA evolve?",
        "⏳" to "Timeline of the Roman Empire"
    )

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        suggestions.forEach { (emoji, text) ->
            AssistChip(
                onClick = { onSuggestionClick(text) },
                label = {
                    Text(
                        text = "$emoji $text",
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = WikipediaTheme.colors.primaryColor
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = WikipediaTheme.colors.backgroundColor
                ),
                border = AssistChipDefaults.assistChipBorder(
                    enabled = true,
                    borderColor = WikipediaTheme.colors.borderColor
                )
            )
        }
    }
}

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



@Composable
private fun ChatInputBar(
    selectedModel: AiModel,
    isWikipediaConnected: Boolean,
    isStreaming: Boolean,
    onSendMessage: (String) -> Unit,
    onToggleWikipedia: () -> Unit,
    onModelClick: () -> Unit,
    onStopStreaming: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    Surface(
        color = WikipediaTheme.colors.paperColor,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Model selector + Wikipedia toggle row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Model badge pill
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onModelClick() },
                    color = getBadgeColor(selectedModel.badgeColor).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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

                // Wikipedia toggle pill
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onToggleWikipedia() },
                    color = if (isWikipediaConnected)
                        WikipediaTheme.colors.progressiveColor.copy(alpha = 0.15f)
                    else
                        WikipediaTheme.colors.backgroundColor,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_w_transparent),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isWikipediaConnected)
                                WikipediaTheme.colors.progressiveColor
                            else
                                WikipediaTheme.colors.secondaryColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Wikipedia",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isWikipediaConnected)
                                WikipediaTheme.colors.progressiveColor
                            else
                                WikipediaTheme.colors.secondaryColor
                        )
                    }
                }
            }

            // Input text field + Send action button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.inixa_alpha_ask_anything),
                            color = WikipediaTheme.colors.placeholderColor
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = WikipediaTheme.colors.backgroundColor,
                        unfocusedContainerColor = WikipediaTheme.colors.backgroundColor,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = WikipediaTheme.colors.progressiveColor,
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

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (isStreaming) {
                            onStopStreaming()
                        } else if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = if (isStreaming) WikipediaTheme.colors.destructiveColor
                            else WikipediaTheme.colors.progressiveColor,
                            shape = CircleShape
                        )
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
