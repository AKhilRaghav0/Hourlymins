package com.akhilraghav.hourlymins.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.akhilraghav.hourlymins.ui.screens.dashboard.DashboardScreen
import com.akhilraghav.hourlymins.ui.screens.diary.DiaryScreen
import com.akhilraghav.hourlymins.ui.screens.pomodoro.PomodoroScreen
import com.akhilraghav.hourlymins.ui.screens.todo.TodoScreen
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    object Diary : Screen("diary")
    object Todo : Screen("todo")
    object Pomodoro : Screen("pomodoro")
    object Dashboard : Screen("dashboard")
}

/**
 * Main navigation component for the app
 */
@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Diary.route,
        modifier = modifier,
        // Disable animations between destinations
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable(
            route = Screen.Diary.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            DiaryScreen()
        }
        
        composable(
            route = Screen.Todo.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            TodoScreen()
        }
        
        composable(
            route = Screen.Pomodoro.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            PomodoroScreen()
        }
        
        composable(
            route = Screen.Dashboard.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            DashboardScreen()
        }
    }
}
