package org.wikipedia.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.wikipedia.R
import org.wikipedia.ai.neuColors
import org.wikipedia.ai.neuElevated
import org.wikipedia.analytics.eventplatform.BreadCrumbLogEvent
import org.wikipedia.compose.theme.WikipediaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WikiTopAppBar(
    modifier: Modifier = Modifier,
    title: String,
    onNavigationClick: (() -> Unit),
    titleStyle: TextStyle = MaterialTheme.typography.headlineSmall.copy(
        fontWeight = FontWeight.Bold,
        lineHeight = 24.sp
    ),
    elevation: Dp = 0.dp,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val context = LocalContext.current
    val neu = neuColors()

    TopAppBar(
        title = {
            Text(
                text = title,
                style = titleStyle,
                color = WikipediaTheme.colors.primaryColor
            )
        },
        navigationIcon = {
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
                        intensity = 0.65f
                    )
                    .background(neu.surface, CircleShape)
                    .clip(CircleShape)
                    .clickable {
                        BreadCrumbLogEvent.logClick(context, "navigationButton")
                        onNavigationClick()
                    }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back_black_24dp),
                    tint = WikipediaTheme.colors.primaryColor,
                    contentDescription = stringResource(R.string.search_back_button_content_description),
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = neu.surface,
            titleContentColor = WikipediaTheme.colors.primaryColor
        ),
        actions = actions,
        modifier = modifier
            .neuElevated(
                lightShadow = neu.lightShadow,
                darkShadow = neu.darkShadow,
                shadowRadius = 8.dp,
                cornerRadius = 0.dp,
                lightOffset = 0.dp,
                darkOffset = 3.dp,
                intensity = 0.45f
            )
            .shadow(elevation = elevation)
    )
}
