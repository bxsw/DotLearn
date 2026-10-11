package com.dotlearn.app.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dotlearn.app.R

object Routes {
    const val TODAY = "today"
    const val FOCUS = "focus"
    const val NOTES = "notes"
    const val EXPLAIN = "explain"
    const val ME = "me"

    const val SUBJECTS = "subjects"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val REVIEW = "review"

    const val SUBJECT_DETAIL = "subject/{subjectId}"
    fun subjectDetail(id: Long) = "subject/$id"

    const val CONCEPT_DETAIL = "concept/{conceptId}"
    fun conceptDetail(id: Long) = "concept/$id"

    const val NOTE_EDIT = "note_edit/{noteId}/{conceptId}"
    fun noteEdit(noteId: Long, conceptId: Long) = "note_edit/$noteId/$conceptId"

    const val NOTE_REVIEW = "note_review/{noteId}"
    fun noteReview(noteId: Long) = "note_review/$noteId"

    const val FEYNMAN_EDIT = "feynman_edit/{entryId}/{conceptId}"
    fun feynmanEdit(entryId: Long, conceptId: Long) = "feynman_edit/$entryId/$conceptId"
}

private data class BottomItem(val route: String, val icon: ImageVector, val labelRes: Int)

@Composable
fun AppRoot(vm: AppViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val items = listOf(
        BottomItem(Routes.TODAY, Icons.Outlined.Today, R.string.nav_today),
        BottomItem(Routes.FOCUS, Icons.Outlined.Timer, R.string.nav_focus),
        BottomItem(Routes.NOTES, Icons.Outlined.Description, R.string.nav_notes),
        BottomItem(Routes.EXPLAIN, Icons.Outlined.RecordVoiceOver, R.string.nav_explain),
        BottomItem(Routes.ME, Icons.Outlined.Person, R.string.nav_me)
    )
    val showBar = items.any { it.route == currentRoute }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBar) {
                NavigationBar {
                    items.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(Routes.TODAY) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(item.icon, contentDescription = null)
                            },
                            label = { Text(stringResource(item.labelRes)) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.TODAY,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.TODAY) { TodayScreen(vm, navController) }
            composable(Routes.FOCUS) { FocusScreen(vm, navController) }
            composable(Routes.NOTES) { NotesScreen(vm, navController) }
            composable(Routes.EXPLAIN) { FeynmanScreen(vm, navController) }
            composable(Routes.ME) { ProfileScreen(vm, navController) }

            composable(Routes.SUBJECTS) { SubjectsScreen(vm, navController) }
            composable(Routes.STATS) { StatsScreen(vm, navController) }
            composable(Routes.SETTINGS) { SettingsScreen(vm, navController) }
            composable(Routes.REVIEW) { ReviewScreen(vm, navController) }

            composable(
                route = Routes.SUBJECT_DETAIL,
                arguments = listOf(navArgument("subjectId") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("subjectId") ?: 0L
                SubjectDetailScreen(vm, navController, id)
            }

            composable(
                route = Routes.CONCEPT_DETAIL,
                arguments = listOf(navArgument("conceptId") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("conceptId") ?: 0L
                ConceptDetailScreen(vm, navController, id)
            }

            composable(
                route = Routes.NOTE_EDIT,
                arguments = listOf(
                    navArgument("noteId") { type = NavType.LongType },
                    navArgument("conceptId") { type = NavType.LongType }
                )
            ) { entry ->
                val noteId = entry.arguments?.getLong("noteId") ?: -1L
                val conceptId = entry.arguments?.getLong("conceptId") ?: -1L
                NoteEditScreen(vm, navController, noteId, conceptId)
            }

            composable(
                route = Routes.NOTE_REVIEW,
                arguments = listOf(navArgument("noteId") { type = NavType.LongType })
            ) { entry ->
                val noteId = entry.arguments?.getLong("noteId") ?: -1L
                NoteReviewScreen(vm, navController, noteId)
            }

            composable(
                route = Routes.FEYNMAN_EDIT,
                arguments = listOf(
                    navArgument("entryId") { type = NavType.LongType },
                    navArgument("conceptId") { type = NavType.LongType }
                )
            ) { entry ->
                val entryId = entry.arguments?.getLong("entryId") ?: -1L
                val conceptId = entry.arguments?.getLong("conceptId") ?: -1L
                FeynmanEditScreen(vm, navController, entryId, conceptId)
            }
        }
    }
}

/** 供各屏幕跳转到知识点详情。 */
fun NavHostController.openConcept(id: Long) = navigate(Routes.conceptDetail(id))
