package com.example.presentation.ui.nav_graph


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.presentation.ui.note_editor.NoteEditorScreen
import com.example.presentation.ui.note_list.NotesListScreen


sealed class Screen(val route: String) {
    object List : Screen("notes_list")
    object Editor : Screen("note_editor?noteId={noteId}") {
        fun createRoute(noteId: Int?) = "note_editor?noteId=${noteId ?: -1}"
    }
}

@Composable
fun NotesNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.List.route) {
        composable(Screen.List.route) {
            NotesListScreen(onOpenNote = { id -> navController.navigate(Screen.Editor.createRoute(id)) })
        }

        composable(
            route = "note_editor?noteId={noteId}",
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStack ->
            val noteId = backStack.arguments?.getInt("noteId")?.takeIf { it != -1 }
            NoteEditorScreen(
                noteId = noteId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
