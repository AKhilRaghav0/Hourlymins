package com.akhilraghav.hourlymins.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.akhilraghav.hourlymins.ui.screens.dashboard.DashboardScreen
import com.akhilraghav.hourlymins.ui.screens.diary.DiaryScreen
import com.akhilraghav.hourlymins.ui.screens.pomodoro.PomodoroScreen
import com.akhilraghav.hourlymins.ui.screens.todo.TodoScreen

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
        startDestination = Screen.Diary.route
    ) {
        composable(Screen.Diary.route) {
            DiaryScreen()
        }
        
        composable(Screen.Todo.route) {
            TodoScreen()
        }
        
        composable(Screen.Pomodoro.route) {
            PomodoroScreen()
        }
        
        composable(Screen.Dashboard.route) {
            DashboardScreen()
        }
    }
}
