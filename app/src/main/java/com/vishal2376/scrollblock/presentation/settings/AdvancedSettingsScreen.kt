package com.vishal2376.scrollblock.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vishal2376.scrollblock.presentation.common.h1style
import com.vishal2376.scrollblock.presentation.common.h3style
import com.vishal2376.scrollblock.presentation.common.smallDescriptionStyle
import com.vishal2376.scrollblock.ui.theme.ScrollBlockTheme
import com.vishal2376.scrollblock.ui.theme.blue
import com.vishal2376.scrollblock.ui.theme.white

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSettingsScreen(
    onNavigateBack: () -> Unit
) {
    var sensitivityLevel by remember { mutableFloatStateOf(3f) }
    var adaptiveBlocking by remember { mutableStateOf(true) }
    var gentleNudges by remember { mutableStateOf(true) }
    var smartDetection by remember { mutableStateOf(true) }
    var sessionTimeLimit by remember { mutableFloatStateOf(15f) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Advanced Settings", style = h1style) },
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
            item {
                SettingsSection(title = "Detection Settings") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Sensitivity Level
                        SettingsSlider(
                            title = "Detection Sensitivity",
                            subtitle = "How aggressively to detect doom scrolling",
                            value = sensitivityLevel,
                            onValueChange = { sensitivityLevel = it },
                            valueRange = 1f..5f,
                            steps = 3,
                            valueLabel = when (sensitivityLevel.toInt()) {
                                1 -> "Very Low"
                                2 -> "Low"
                                3 -> "Medium"
                                4 -> "High"
                                5 -> "Very High"
                                else -> "Medium"
                            }
                        )
                        
                        // Session Time Limit
                        SettingsSlider(
                            title = "Session Time Limit",
                            subtitle = "Maximum time before intervention",
                            value = sessionTimeLimit,
                            onValueChange = { sessionTimeLimit = it },
                            valueRange = 5f..60f,
                            steps = 10,
                            valueLabel = "${sessionTimeLimit.toInt()} minutes"
                        )
                    }
                }
            }
            
            item {
                SettingsSection(title = "Blocking Behavior") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SettingsToggle(
                            title = "Adaptive Blocking",
                            subtitle = "Learn from your patterns to improve accuracy",
                            checked = adaptiveBlocking,
                            onCheckedChange = { adaptiveBlocking = it }
                        )
                        
                        SettingsToggle(
                            title = "Gentle Nudges",
                            subtitle = "Show gentle reminders before blocking",
                            checked = gentleNudges,
                            onCheckedChange = { gentleNudges = it }
                        )
                        
                        SettingsToggle(
                            title = "Smart Content Detection",
                            subtitle = "Distinguish between productive and addictive content",
                            checked = smartDetection,
                            onCheckedChange = { smartDetection = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = h1style,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
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
                content()
            }
        }
    }
}

@Composable
fun SettingsToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
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
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = white,
                checkedThumbColor = blue
            )
        )
    }
}

@Composable
fun SettingsSlider(
    title: String,
    subtitle: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    valueLabel: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
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
            
            Text(
                text = valueLabel,
                style = h3style,
                color = blue,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = blue,
                activeTrackColor = blue,
                inactiveTrackColor = MaterialTheme.colorScheme.secondary
            )
        )
    }
}

@Preview
@Composable
private fun AdvancedSettingsScreenPreview() {
    ScrollBlockTheme {
        AdvancedSettingsScreen(onNavigateBack = {})
    }
}