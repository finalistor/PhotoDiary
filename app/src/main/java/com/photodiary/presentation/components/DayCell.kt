package com.photodiary.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.photodiary.domain.model.CalendarDay
import java.io.File
import java.time.LocalDate

@Composable
fun DayCell(
    day: CalendarDay,
    onClick: (CalendarDay) -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(if (compact) 4.dp else 8.dp)
    val paddingPx = if (compact) 2.dp else 3.dp
    val textSize = if (compact) 10.sp else 12.sp
    val dayPaddingH = if (day.isToday) (if (compact) 5.dp else 7.dp) else 0.dp
    val dayPaddingV = if (day.isToday) 1.dp else 0.dp

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(paddingPx)
            .clip(shape)
            .then(
                if (day.isToday) {
                    if (compact)
                        Modifier.background(MaterialTheme.colorScheme.primaryContainer, shape)
                    else
                        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, shape)
                } else if (day.firstEntryId != null && !compact) {
                    Modifier.border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, shape)
                } else {
                    Modifier
                }
            )
            .then(
                if (day.firstEntryId != null || !day.date.isAfter(LocalDate.now()))
                    Modifier.clickable { onClick(day) }
                else Modifier
            )
    ) {
        val alpha = if (day.isCurrentMonth) 1f else 0.25f

        if (day.thumbnailPaths.size == 1) {
            SubcomposeAsyncImage(
                model = File(day.thumbnailPaths[0]),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                alpha = alpha
            )
        } else if (day.thumbnailPaths.size >= 2) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // Top row: first 2 photos
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SubcomposeAsyncImage(
                        model = File(day.thumbnailPaths[0]),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.weight(1f).fillMaxSize(),
                        alpha = alpha
                    )
                    SubcomposeAsyncImage(
                        model = File(day.thumbnailPaths.getOrElse(1) { day.thumbnailPaths[0] }),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.weight(1f).fillMaxSize(),
                        alpha = alpha
                    )
                }
                // Bottom row: next 2 photos
                if (day.thumbnailPaths.size >= 3) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SubcomposeAsyncImage(
                            model = File(day.thumbnailPaths[2]),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.weight(1f).fillMaxSize(),
                            alpha = alpha
                        )
                        if (day.thumbnailPaths.size >= 4) {
                            SubcomposeAsyncImage(
                                model = File(day.thumbnailPaths[3]),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.weight(1f).fillMaxSize(),
                                alpha = alpha
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(if (compact) 3.dp else 4.dp)
                .then(
                    if (day.isToday) {
                        Modifier.background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        )
                    } else Modifier
                )
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                fontSize = textSize,
                color = when {
                    day.isToday -> MaterialTheme.colorScheme.onPrimary
                    day.isCurrentMonth -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                },
                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(
                    horizontal = dayPaddingH,
                    vertical = dayPaddingV
                )
            )
        }
    }
}
