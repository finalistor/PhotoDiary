package com.photodiary.presentation.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.toArgb

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
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SaturationLightnessBox(
                            hue = hue,
                            saturation = saturation,
                            lightness = lightness,
                            onColorChange = { s, l ->
                                saturation = s
                                lightness = l
                                customColor = Color.hsl(hue, saturation, lightness)
                            },
                            modifier = Modifier
                                .height(160.dp)
                                .aspectRatio(1f)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        HueBar(
                            hue = hue,
                            onHueChange = { h ->
                                hue = h
                                customColor = Color.hsl(hue, saturation, lightness)
                            },
                            modifier = Modifier
                                .width(24.dp)
                                .height(160.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(customColor)
                                .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            formatHexColor(customColor),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("常用颜色", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        presetPalette.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
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
    val hex = java.lang.Integer.toHexString(color.toArgb()).uppercase()
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

@Composable
private fun SaturationLightnessBox(
    hue: Float,
    saturation: Float,
    lightness: Float,
    onColorChange: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    val outlineColor = MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = modifier
            .clip(shape)
            .border(1.dp, outlineColor, shape)
            .pointerInput(hue) {
                detectTapGestures { offset ->
                    val s = (offset.x / size.width).coerceIn(0f, 1f)
                    val l = 1f - (offset.y / size.height).coerceIn(0f, 1f)
                    onColorChange(s, l)
                }
            }
            .pointerInput(hue) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val s = (change.position.x / size.width).coerceIn(0f, 1f)
                    val l = 1f - (change.position.y / size.height).coerceIn(0f, 1f)
                    onColorChange(s, l)
                }
            }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val step = 2
            for (y in 0 until h.toInt() step step) {
                val light = 1f - (y.toFloat() / h)
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.hsl(hue, 0f, light),
                            Color.hsl(hue, 1f, light)
                        ),
                        startX = 0f,
                        endX = w
                    ),
                    topLeft = Offset(0f, y.toFloat()),
                    size = Size(w, step.toFloat())
                )
            }

            val cx = saturation * size.width
            val cy = (1f - lightness) * size.height
            val r = 7.dp.toPx()
            drawCircle(Color.Black.copy(alpha = 0.3f), r + 2.dp.toPx(), Offset(cx, cy))
            drawCircle(
                Color.White, r, Offset(cx, cy),
                style = Stroke(2.5.dp.toPx())
            )
        }
    }
}

@Composable
private fun HueBar(
    hue: Float,
    onHueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    val outlineColor = MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = modifier
            .clip(shape)
            .border(1.dp, outlineColor, shape)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val h = (offset.y / size.height).coerceIn(0f, 1f) * 360f
                    onHueChange(h)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val h = (change.position.y / size.height).coerceIn(0f, 1f) * 360f
                    onHueChange(h)
                }
            }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFF0000),
                        Color(0xFFFFFF00),
                        Color(0xFF00FF00),
                        Color(0xFF00FFFF),
                        Color(0xFF0000FF),
                        Color(0xFFFF00FF),
                        Color(0xFFFF0000)
                    ),
                    startY = 0f,
                    endY = size.height
                ),
                size = size
            )

            val cy = (hue / 360f) * size.height
            drawLine(
                color = Color.Black.copy(alpha = 0.4f),
                start = Offset(0f, cy),
                end = Offset(size.width, cy),
                strokeWidth = 6.dp.toPx()
            )
            drawLine(
                color = Color.White,
                start = Offset(1.dp.toPx(), cy),
                end = Offset(size.width - 1.dp.toPx(), cy),
                strokeWidth = 4.dp.toPx()
            )
        }
    }
}
