package com.akhilraghav.hourlymins

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.akhilraghav.hourlymins.data.AppDatabase
import com.akhilraghav.hourlymins.data.repository.DiaryRepository
import com.akhilraghav.hourlymins.data.repository.ProductivityRepository
import com.akhilraghav.hourlymins.data.repository.TaskRepository
import com.akhilraghav.hourlymins.navigation.AppNavigation
import com.akhilraghav.hourlymins.navigation.Screen
import com.akhilraghav.hourlymins.notifications.NotificationHelper
import com.akhilraghav.hourlymins.notifications.NotificationReceiver
import com.akhilraghav.hourlymins.ui.theme.HourlyminsTheme
import com.akhilraghav.hourlymins.viewmodel.DiaryViewModel
import com.akhilraghav.hourlymins.viewmodel.ProductivityViewModel
import com.akhilraghav.hourlymins.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    
    private lateinit var diaryViewModel: DiaryViewModel
    private lateinit var taskViewModel: TaskViewModel
    private lateinit var productivityViewModel: ProductivityViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize ViewModels
        val database = (application as HourlyMinsApp).database
        initializeViewModels(database)
        
        // Schedule hourly notifications
        NotificationReceiver.scheduleHourlyNotifications(this)
        
        // Handle notification intent
        handleNotificationIntent(intent)
        
        setContent {
            HourlyminsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleNotificationIntent(it) }
    }
    
    private fun initializeViewModels(database: AppDatabase) {
        // Create repositories
        val diaryRepository = DiaryRepository(database.diaryEntryDao())
        val taskRepository = TaskRepository(database.taskDao())
        val productivityRepository = ProductivityRepository(database.productivityDao())
        
        // Create ViewModels
        diaryViewModel = ViewModelProvider(
            this,
            DiaryViewModel.DiaryViewModelFactory(diaryRepository)
        )[DiaryViewModel::class.java]
        
        taskViewModel = ViewModelProvider(
            this,
            TaskViewModel.TaskViewModelFactory(taskRepository)
        )[TaskViewModel::class.java]
        
        productivityViewModel = ViewModelProvider(
            this,
            ProductivityViewModel.ProductivityViewModelFactory(
                productivityRepository,
                diaryRepository,
                taskRepository
            )
        )[ProductivityViewModel::class.java]
    }
    
    private fun handleNotificationIntent(intent: Intent) {
        val notificationType = intent.getStringExtra(NotificationHelper.EXTRA_NOTIFICATION_TYPE)
        
        when (notificationType) {
            NotificationHelper.NOTIFICATION_TYPE_HOURLY -> {
                // Handle hourly check-in notification
                // We'll navigate to the diary screen in the UI
            }
            NotificationHelper.NOTIFICATION_TYPE_POMODORO -> {
                // Handle pomodoro notification
                // We'll navigate to the pomodoro screen in the UI
            }
            NotificationHelper.NOTIFICATION_TYPE_TODO -> {
                // Handle todo notification
                val taskId = intent.getLongExtra(NotificationHelper.EXTRA_TASK_ID, -1L)
                if (taskId != -1L) {
                    // We'll navigate to the todo screen and show the task
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    
    // Define navigation items
    val navigationItems = listOf(
        NavigationItem(Screen.Diary, "Diary", Icons.Default.DateRange),
        NavigationItem(Screen.Todo, "Todo", Icons.Default.List),
        NavigationItem(Screen.Pomodoro, "Pomodoro", Icons.Default.Timer),
        NavigationItem(Screen.Dashboard, "Dashboard", Icons.Default.Home)
    )
    
    // Track the current selected item
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    Scaffold(
        // Remove top padding by setting it to 0
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar {
                navigationItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                    
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = selected,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        AppNavigation(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

/**
 * Data class representing a navigation item
 */
data class NavigationItem(
    val screen: Screen,
    val title: String,
    val icon: ImageVector
)