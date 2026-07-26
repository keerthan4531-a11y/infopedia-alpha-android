package org.wikipedia.compose.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.wikipedia.R
import org.wikipedia.ai.neuColors
import org.wikipedia.compose.theme.BaseTheme
import org.wikipedia.compose.theme.WikipediaTheme
import org.wikipedia.settings.homefeed.CommunityModuleType
import org.wikipedia.settings.homefeed.ForYouModuleType
import org.wikipedia.theme.Theme

/**
 * Neumorphic 2.0 Preference Row.
 * Wrapped in a subtle NeuCard with minimal elevation to avoid visual clutter.
 */
@Composable
fun SettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    isNavigation: Boolean = false,
    onClick: (() -> Unit)? = null,
    onSubtitleLinkClick: ((String) -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    NeuCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        cornerRadius = 14.dp,
        shadowRadius = 4.dp,
        intensity = 0.45f,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = WikipediaTheme.colors.primaryColor,
                    fontWeight = FontWeight.SemiBold
                )
                if (subtitle != null) {
                    HtmlText(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = WikipediaTheme.colors.secondaryColor,
                        modifier = Modifier.padding(top = 4.dp),
                        linkInteractionListener = { link ->
                            val url = (link as LinkAnnotation.Url).url
                            onSubtitleLinkClick?.invoke(url)
                        },
                        linkStyle = TextLinkStyles(
                            style = SpanStyle(
                                color = WikipediaTheme.colors.progressiveColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.25.sp
                            )
                        )
                    )
                }
            }

            if (isNavigation) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_subdirectory_arrow_right_black_24dp),
                    contentDescription = null,
                    tint = WikipediaTheme.colors.progressiveColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            if (trailingContent != null) {
                Spacer(modifier = Modifier.width(12.dp))
                trailingContent()
            }
        }
    }
}

/**
 * Section Header — Styled flat per accessibility guidelines (labels are non-interactive).
 */
@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold
            ),
            color = WikipediaTheme.colors.progressiveColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        content()
    }
}

/**
 * Neumorphic Theme Multi-Choice Selector Pills.
 */
@Composable
fun SettingsThemeSelectorRow(
    currentTheme: Theme,
    onThemeSelected: (Theme) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Theme.entries.forEach { theme ->
            val isSelected = (theme == currentTheme)
            val themeName = when (theme) {
                Theme.LIGHT -> "Light"
                Theme.DARK -> "Dark"
                Theme.BLACK -> "Black"
                Theme.SEPIA -> "Sepia"
            }
            NeuButton(
                onClick = { onThemeSelected(theme) },
                accentFill = isSelected,
                cornerRadius = 16.dp,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = themeName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ToggleListScreen(
    title: String,
    description: String,
    modules: List<ToggleSettingItem>,
    hiddenModules: Set<String>,
    onToggle: (key: String, isVisible: Boolean) -> Unit,
    onSubtitleLinkClick: ((href: String) -> Unit)? = null,
    onBack: () -> Unit,
) {
    val neu = neuColors()

    Scaffold(
        topBar = {
            WikiTopAppBar(
                title = title,
                onNavigationClick = onBack,
            )
        },
        containerColor = neu.surface,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp, bottom = 4.dp),
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = WikipediaTheme.colors.secondaryColor
            )

            modules.forEach { module ->
                val isVisible = module.key !in hiddenModules
                SettingsRow(
                    title = stringResource(module.title),
                    subtitle = stringResource(module.subtitle),
                    trailingContent = {
                        NeuToggleSwitch(
                            checked = isVisible,
                            onCheckedChange = { newChecked ->
                                onToggle(module.key, newChecked)
                            }
                        )
                    },
                    onSubtitleLinkClick = { href ->
                        onSubtitleLinkClick?.invoke(href)
                    }
                )
            }
        }
    }
}

data class ToggleSettingItem(
    @param:StringRes val title: Int,
    @param:StringRes val subtitle: Int,
    val key: String
)

@Preview
@Composable
private fun SettingsSectionPreview() {
    BaseTheme(
        currentTheme = Theme.LIGHT
    ) {
        SettingsSection(
            title = "For you"
        ) {
            Column {
                SettingsRow(
                    title = "Modules",
                    subtitle = "Turn on or off For You sections",
                    isNavigation = true
                )
                SettingsRow(
                    title = "Notifications",
                    subtitle = "Manage your notification preferences for home feed updates",
                    trailingContent = {
                        NeuToggleSwitch(
                            checked = true,
                            onCheckedChange = {}
                        )
                    }
                )
            }
        }
    }
}
