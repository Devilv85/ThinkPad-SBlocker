package com.vishal2376.scrollblock.presentation.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vishal2376.scrollblock.domain.model.SessionData
import com.vishal2376.scrollblock.presentation.common.h1style
import com.vishal2376.scrollblock.presentation.common.h3style
import com.vishal2376.scrollblock.presentation.common.smallDescriptionStyle
import com.vishal2376.scrollblock.presentation.viewmodels.AnalyticsViewModel
import com.vishal2376.scrollblock.ui.theme.ScrollBlockTheme
import com.vishal2376.scrollblock.ui.theme.blue
import com.vishal2376.scrollblock.ui.theme.green
import com.vishal2376.scrollblock.ui.theme.orange
import com.vishal2376.scrollblock.ui.theme.red
import com.vishal2376.scrollblock.utils.formatTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onNavigateBack: () -> Unit
) {
    val analyticsData by viewModel.analyticsData.collectAsState()
    val recentSessions by viewModel.recentSessions.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics", style = h1style) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnalyticsCard(
                        modifier = Modifier.weight(1f),
                        title = "Blocked",
                        value = "${analyticsData.totalBlockedScrolls}",
                        subtitle = "scrolls",
                        icon = Icons.Default.Block,
                        color = red
                    )
                    AnalyticsCard(
                        modifier = Modifier.weight(1f),
                        title = "Time Saved",
                        value = formatTime(analyticsData.timeSaved),
                        subtitle = "today",
                        icon = Icons.Default.Schedule,
                        color = green
                    )
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnalyticsCard(
                        modifier = Modifier.weight(1f),
                        title = "Sessions",
                        value = "${analyticsData.totalSessions}",
                        subtitle = "today",
                        icon = Icons.Default.TouchApp,
                        color = blue
                    )
                    AnalyticsCard(
                        modifier = Modifier.weight(1f),
                        title = "Avg Session",
                        value = formatTime(analyticsData.averageSessionDuration),
                        subtitle = "duration",
                        icon = Icons.Default.Schedule,
                        color = orange
                    )
                }
            }
            
            // Recent Sessions
            item {
                Text(
                    text = "Recent Sessions",
                    style = h1style,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(recentSessions) { session ->
                SessionCard(session = session)
            }
        }
    }
}

@Composable
fun AnalyticsCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = value,
                style = h1style,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = title,
                style = h3style,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = subtitle,
                style = smallDescriptionStyle
            )
        }
    }
}

@Composable
fun SessionCard(session: SessionData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getAppDisplayName(session.packageName),
                    style = h3style,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = session.sessionType.replace("_", " ").uppercase(),
                    style = smallDescriptionStyle,
                    color = when (session.sessionType) {
                        "doom_scroll" -> red
                        "productive" -> green
                        else -> orange
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Duration",
                        style = smallDescriptionStyle
                    )
                    Text(
                        text = formatTime(((session.endTime - session.startTime) / 1000).toInt()),
                        style = h3style,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                Column {
                    Text(
                        text = "Scrolls",
                        style = smallDescriptionStyle
                    )
                    Text(
                        text = "${session.totalScrolls}",
                        style = h3style,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                Column {
                    Text(
                        text = "Blocked",
                        style = smallDescriptionStyle
                    )
                    Text(
                        text = "${session.blockedScrolls}",
                        style = h3style,
                        color = red
                    )
                }
            }
        }
    }
}

private fun getAppDisplayName(packageName: String): String {
    return when {
        packageName.contains("instagram") -> "Instagram"
        packageName.contains("youtube") -> "YouTube"
        packageName.contains("linkedin") -> "LinkedIn"
        packageName.contains("snapchat") -> "Snapchat"
        else -> packageName.split(".").lastOrNull()?.capitalize() ?: "Unknown"
    }
}

@Preview
@Composable
private fun AnalyticsScreenPreview() {
    ScrollBlockTheme {
        // Preview would need a mock ViewModel
    }
}