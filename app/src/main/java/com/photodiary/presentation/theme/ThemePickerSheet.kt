package com.photodiary.presentation.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.photodiary.ui.theme.ThemePreset
import com.photodiary.ui.theme.presetColorScheme
import com.photodiary.ui.theme.toHsl

private val presetPalette = listOf(
    Color(0xFFE53935), Color(0xFFD81B60), Color(0xFFC2185B), Color(0xFF8E24AA),
    Color(0xFF7B1FA2), Color(0xFF5E35B1), Color(0xFF3949AB), Color(0xFF1E88E5),
    Color(0xFF1976D2), Color(0xFF039BE5), Color(0xFF0097A7), Color(0xFF00897B),
    Color(0xFF388E3C), Color(0xFF43A047), Color(0xFF689F38), Color(0xFFAFB42B),
    Color(0xFFF9A825), Color(0xFFFFB300), Color(0xFFFB8C00), Color(0xFFF57C00),
    Color(0xFFE64A19), Color(0xFFD84315), Color(0xFFBF360C), Color(0xFF795548),
    Color(0xFF6D4C41), Color(0xFF5D4037), Color(0xFF546E7A), Color(0xFF455A64),
    Color(0xFF607D8B), Color(0xFF9E9E9E), Color(0xFF757575), Color(0xFF616161),
    Color(0xFF424242), Color(0xFF212121), Color(0xFFFFFFFF)
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ThemePickerSheet(
    currentPreset: ThemePreset,
    currentCustomColor: Color,
    onPresetSelected: (ThemePreset) -> Unit,
    onCustomColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedPreset by remember { mutableStateOf(currentPreset) }
    var customColor by remember { mutableStateOf(currentCustomColor) }
    val (initialH, initialS, initialL) = remember(currentCustomColor) { currentCustomColor.toHsl() }
    var hue by remember { mutableFloatStateOf(initialH) }
    var saturation by remember { mutableFloatStateOf(initialS) }
    var lightness by remember { mutableFloatStateOf(initialL) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "选择主题",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "选择一个配色方案",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ThemePreset.entries.forEach { preset ->
                    ThemeSwatch(
                        preset = preset,
                        customColor = customColor,
                        isSelected = preset == selectedPreset,
                        onClick = {
                            selectedPreset = preset
                            if (preset == ThemePreset.CUSTOM) {
                                val (h, s, l) = customColor.toHsl()
                                hue = h; saturation = s; lightness = l
                            }
                        }
                    )
                }
            }

            AnimatedVisibility(
                visible = selectedPreset == ThemePreset.CUSTOM,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(customColor)
                                .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "预览颜色",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                formatHexColor(customColor),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text("色调", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = hue,
                        onValueChange = {
                            hue = it
                            customColor = Color.hsl(hue, saturation, lightness)
                        },
                        valueRange = 0f..360f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("饱和度", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = saturation,
                        onValueChange = {
                            saturation = it
                            customColor = Color.hsl(hue, saturation, lightness)
                        },
                        valueRange = 0f..1f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("亮度", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = lightness,
                        onValueChange = {
                            lightness = it
                            customColor = Color.hsl(hue, saturation, lightness)
                        },
                        valueRange = 0f..1f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("常用颜色", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presetPalette.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                    .clickable {
                                        val (h, s, l) = color.toHsl()
                                        hue = h; saturation = s; lightness = l
                                        customColor = color
                                    }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    if (selectedPreset == ThemePreset.CUSTOM) {
                        onCustomColorSelected(customColor)
                    }
                    onPresetSelected(selectedPreset)
                }) {
                    Text("确定")
                }
            }
        }
    }
}

private fun formatHexColor(color: Color): String {
    val hex = java.lang.Long.toHexString(color.value.toLong() and 0xFFFFFFFFL).uppercase()
    return "#${hex.padStart(8, '0')}"
}

@Composable
private fun ThemeSwatch(
    preset: ThemePreset,
    customColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val primaryColor = if (preset == ThemePreset.CUSTOM) {
        customColor
    } else {
        presetColorScheme(preset, darkTheme = false).primary
    }
    val checkScale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spring(stiffness = 400f, dampingRatio = 0.4f)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp).clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(primaryColor)
                .then(
                    if (isSelected)
                        Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                    else Modifier
                )
        ) {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "已选中",
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size((24f * checkScale).dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = preset.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
