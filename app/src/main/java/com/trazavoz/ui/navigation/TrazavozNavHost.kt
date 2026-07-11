package com.trazavoz.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.trazavoz.ui.audio.TrazavozTtsManager
import com.trazavoz.ui.game.GameScreen
import com.trazavoz.ui.game.GameViewModel
import com.trazavoz.ui.menu.MenuScreen
import com.trazavoz.ui.menu.MenuViewModel
import com.trazavoz.ui.tutor.AddWordScreen
import com.trazavoz.ui.tutor.TutorDashboardScreen
import com.trazavoz.ui.tutor.TutorViewModel
import com.trazavoz.ui.words.WordListScreen
import com.trazavoz.ui.words.WordListViewModel

@Composable
fun TrazavozNavHost(
    ttsManager: TrazavozTtsManager
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "menu"
    ) {
        composable("menu") {
            val viewModel: MenuViewModel = hiltViewModel()
            MenuScreen(
                viewModel = viewModel,
                onLetterClick = { letter ->
                    navController.navigate("words_list/letter/$letter")
                },
                onBoardClick = { boardId ->
                    navController.navigate("words_list/board/$boardId")
                },
                onTutorAuthenticated = {
                    navController.navigate("tutor_dashboard")
                }
            )
        }

        composable(
            route = "words_list/{filterType}/{filterValue}",
            arguments = listOf(
                navArgument("filterType") { type = NavType.StringType },
                navArgument("filterValue") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val filterType = backStackEntry.arguments?.getString("filterType") ?: "letter"
            val filterValue = backStackEntry.arguments?.getString("filterValue") ?: ""
            val viewModel: WordListViewModel = hiltViewModel()

            WordListScreen(
                filterType = filterType,
                filterValue = filterValue,
                viewModel = viewModel,
                onWordClick = { wordId ->
                    navController.navigate("game/$wordId")
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "game/{wordId}",
            arguments = listOf(
                navArgument("wordId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val wordId = backStackEntry.arguments?.getInt("wordId") ?: 0
            val viewModel: GameViewModel = hiltViewModel()

            GameScreen(
                wordId = wordId,
                viewModel = viewModel,
                ttsManager = ttsManager,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("tutor_dashboard") {
            val viewModel: TutorViewModel = hiltViewModel()
            TutorDashboardScreen(
                viewModel = viewModel,
                onAddWordClick = {
                    navController.navigate("add_word")
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("add_word") {
            val viewModel: TutorViewModel = hiltViewModel()
            AddWordScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
