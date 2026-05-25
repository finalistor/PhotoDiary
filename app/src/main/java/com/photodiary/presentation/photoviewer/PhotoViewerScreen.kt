package com.photodiary.presentation.photoviewer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.photodiary.domain.repository.DiaryRepository
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoViewerScreen(
    entryId: Long,
    initialPhotoIndex: Int,
    repository: DiaryRepository,
    onNavigateBack: () -> Unit
) {
    val entry by repository.getEntryWithPhotos(entryId)
        .collectAsState(initial = null)

    val photos = entry?.photos ?: return

    val pagerState = rememberPagerState(
        initialPage = initialPhotoIndex.coerceIn(0, (photos.size - 1).coerceAtLeast(0)),
        pageCount = { photos.size }
    )

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        HorizontalPager(state = pagerState) { page ->
            val photo = photos[page]
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(File(photo.filePath))
                    .crossfade(true)
                    .build(),
                contentDescription = "Photo ${page + 1}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "关闭",
                tint = Color.White
            )
        }

        Text(
            text = "${pagerState.currentPage + 1} / ${photos.size}",
            color = Color.White,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        )
    }
}
