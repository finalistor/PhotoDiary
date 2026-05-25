package com.photodiary.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PhotoGrid(
    photoPaths: List<String>,
    showAddButton: Boolean,
    showDeleteButtons: Boolean,
    onAddClick: () -> Unit,
    onPhotoClick: (Int) -> Unit,
    onPhotoDelete: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemCount = if (showAddButton) photoPaths.size + 1 else photoPaths.size
    val rowCount = (itemCount + 2) / 3

    Column(modifier = modifier) {
        for (row in 0 until rowCount) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 3) {
                    val index = row * 3 + col
                    if (index < itemCount) {
                        if (showAddButton && index == photoPaths.size) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp)
                                    .aspectRatio(1f)
                                    .clickable { onAddClick() }
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(2.dp, Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .background(Color.Gray.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add photo",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        } else {
                            PhotoThumbnail(
                                filePath = photoPaths[index],
                                showDeleteButton = showDeleteButtons,
                                onClick = { onPhotoClick(index) },
                                onDelete = { onPhotoDelete(index) },
                                modifier = Modifier.weight(1f).padding(4.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
