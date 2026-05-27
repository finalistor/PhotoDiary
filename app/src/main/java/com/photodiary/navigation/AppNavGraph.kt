package com.photodiary.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.photodiary.data.local.UserPreferences
import com.photodiary.domain.repository.DiaryRepository
import com.photodiary.presentation.editentry.CreateEditEntryScreen
import com.photodiary.presentation.entrydetail.EntryDetailScreen
import com.photodiary.presentation.calendar.CalendarScreen
import com.photodiary.presentation.photowall.PhotoWallScreen
import com.photodiary.presentation.photoviewer.PhotoViewerScreen
import com.photodiary.presentation.tagfilter.TagFilterScreen
import com.photodiary.presentation.tagmanagement.TagManagementScreen
import com.photodiary.presentation.timeline.TimelineScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    repository: DiaryRepository,
    userPreferences: UserPreferences
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Timeline.route,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) + fadeIn(tween(200))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) + fadeOut(tween(200))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeIn(tween(200))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeOut(tween(200))
        }
    ) {
        composable(Screen.Timeline.route) {
            TimelineScreen(
                repository = repository,
                userPreferences = userPreferences,
                onNavigateToCreateWithDate = { date ->
                    navController.navigate(Screen.CreateEditEntry.createRoute(date = date))
                },
                onNavigateToDetail = { entryId ->
                    navController.navigate(Screen.EntryDetail.createRoute(entryId))
                },
                onNavigateToPhotoWall = {
                    navController.navigate(Screen.PhotoWall.route)
                },
                onNavigateToTagManagement = {
                    navController.navigate(Screen.TagManagement.route)
                },
                onNavigateToCalendarPicker = {
                    navController.navigate(Screen.Calendar.createRoute(pickerMode = true))
                }
            )
        }

        composable(
            route = Screen.EntryDetail.route,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: return@composable
            EntryDetailScreen(
                entryId = entryId,
                repository = repository,
                userPreferences = userPreferences,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.CreateEditEntry.createRoute(id))
                },
                onNavigateToPhotoViewer = { id, index ->
                    navController.navigate(Screen.PhotoViewer.createRoute(id, index))
                }
            )
        }

        composable(
            route = Screen.CreateEditEntry.route,
            arguments = listOf(
                navArgument("entryId") {
                    type = NavType.LongType
                    defaultValue = -1L
                },
                navArgument("date") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: -1L
            val date = backStackEntry.arguments?.getLong("date") ?: -1L
            val isCreate = entryId == -1L
            CreateEditEntryScreen(
                entryId = if (isCreate) null else entryId,
                initialDate = if (date == -1L) null else date,
                repository = repository,
                userPreferences = userPreferences,
                onNavigateBack = {
                    if (isCreate) navController.popBackStack(Screen.Timeline.route, inclusive = false)
                    else navController.popBackStack()
                },
                onNavigateToEntry = { targetId ->
                    if (isCreate) navController.popBackStack(Screen.Timeline.route, inclusive = false)
                    else navController.popBackStack()
                    navController.navigate(Screen.EntryDetail.createRoute(targetId))
                },
                onNavigateToEditEntry = { targetId ->
                    if (isCreate) navController.popBackStack(Screen.Timeline.route, inclusive = false)
                    else navController.popBackStack()
                    navController.navigate(Screen.CreateEditEntry.createRoute(targetId))
                }
            )
        }

        composable(
            route = Screen.PhotoViewer.route,
            arguments = listOf(
                navArgument("entryId") { type = NavType.LongType },
                navArgument("photoIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: return@composable
            val photoIndex = backStackEntry.arguments?.getInt("photoIndex") ?: 0
            PhotoViewerScreen(
                entryId = entryId,
                initialPhotoIndex = photoIndex,
                repository = repository,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PhotoWall.route) {
            PhotoWallScreen(
                repository = repository,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { entryId ->
                    navController.navigate(Screen.EntryDetail.createRoute(entryId))
                }
            )
        }

        composable(
            route = Screen.Calendar.route,
            arguments = listOf(
                navArgument("pickerMode") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val pickerMode = backStackEntry.arguments?.getBoolean("pickerMode") ?: false
            CalendarScreen(
                repository = repository,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { entryId ->
                    navController.navigate(Screen.EntryDetail.createRoute(entryId))
                },
                onNavigateToCreateWithDate = { date ->
                    navController.navigate(Screen.CreateEditEntry.createRoute(date = date))
                },
                onNavigateToEdit = { entryId ->
                    navController.popBackStack()
                    navController.navigate(Screen.CreateEditEntry.createRoute(entryId))
                },
                pickerMode = pickerMode
            )
        }

        composable(Screen.TagManagement.route) {
            TagManagementScreen(
                repository = repository,
                userPreferences = userPreferences,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTagFilter = { tag ->
                    navController.navigate(Screen.TagFilter.createRoute(tag))
                }
            )
        }

        composable(
            route = Screen.TagFilter.route,
            arguments = listOf(
                navArgument("tag") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tag = backStackEntry.arguments?.getString("tag") ?: return@composable
            val decodedTag = android.net.Uri.decode(tag)
            TagFilterScreen(
                tag = decodedTag,
                repository = repository,
                userPreferences = userPreferences,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { entryId ->
                    navController.navigate(Screen.EntryDetail.createRoute(entryId))
                }
            )
        }
    }
}
