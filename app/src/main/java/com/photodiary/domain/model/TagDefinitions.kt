package com.photodiary.domain.model

import androidx.compose.ui.graphics.Color

data class TagDefinition(val name: String, val color: Color)

val presetTagDefs = listOf(
    TagDefinition("旅行", Color(0xFF4CAF50)),
    TagDefinition("美食", Color(0xFFFF9800)),
    TagDefinition("日常", Color(0xFF2196F3)),
    TagDefinition("工作", Color(0xFF9C27B0)),
)

private val customPalette = listOf(
    Color(0xFFE91E63), Color(0xFF00BCD4), Color(0xFF8BC34A), Color(0xFFFF5722),
    Color(0xFF3F51B5), Color(0xFFCDDC39), Color(0xFF795548), Color(0xFF607D8B),
)

private val presetTagNames = presetTagDefs.map { it.name }.toSet()

fun allTagDefs(customNames: List<String>): List<TagDefinition> {
    val custom = customNames
        .filter { it !in presetTagNames }
        .mapIndexed { i, name -> TagDefinition(name, customPalette[i % customPalette.size]) }
    return presetTagDefs + custom
}

fun buildTagColorMap(customNames: List<String>): Map<String, Color> {
    val map = mutableMapOf<String, Color>()
    presetTagDefs.forEach { map[it.name] = it.color }
    customNames.filter { it !in presetTagNames }
        .forEachIndexed { i, name -> map[name] = customPalette[i % customPalette.size] }
    return map
}

fun tagColor(name: String, colorMap: Map<String, Color>): Color =
    colorMap[name] ?: customPalette[Math.abs(name.hashCode()) % customPalette.size]
