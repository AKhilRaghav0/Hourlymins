package com.akhilraghav.hourlymins.ui.screens.pomodoro

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.akhilraghav.hourlymins.viewmodel.PomodoroViewModel
import com.akhilraghav.hourlymins.viewmodel.StopwatchViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen() {
    // Initialize view models
    val pomodoroViewModel: PomodoroViewModel = viewModel(
        factory = PomodoroViewModel.PomodoroViewModelFactory()
    )
    
    val stopwatchViewModel: StopwatchViewModel = viewModel(
        factory = StopwatchViewModel.StopwatchViewModelFactory()
    )
    
    // Initialize with context
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        pomodoroViewModel.initialize(context)
    }
    
    // Observe pomodoro state
    val timerState by pomodoroViewModel.timerState.collectAsState()
    val timeRemaining by pomodoroViewModel.timeRemaining.collectAsState()
    val pomodoroCount by pomodoroViewModel.pomodoroCount.collectAsState()
    
    // Tab selection state
    var selectedTab by remember { mutableIntStateOf(0) }
    
    // Settings state
    var showSettings by remember { mutableStateOf(false) }
    var workDuration by remember { mutableIntStateOf(25) }
    var shortBreakDuration by remember { mutableIntStateOf(5) }
    var longBreakDuration by remember { mutableIntStateOf(15) }
    var pomodorosBeforeLongBreak by remember { mutableIntStateOf(4) }
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pomodoro Timer") },
                actions = {
                    // Settings button
                    IconButton(onClick = { showSettings = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab row for switching between Pomodoro and Stopwatch
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Pomodoro") },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Pomodoro Timer"
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Stopwatch") },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Timer,
                            contentDescription = "Stopwatch"
                        )
                    }
                )
            }
            
            // Content based on selected tab
            when (selectedTab) {
                0 -> PomodoroContent(pomodoroViewModel)
                1 -> StopwatchContent(stopwatchViewModel)
            }
        }
    }
    
    // Settings bottom sheet
    if (showSettings) {
        ModalBottomSheet(
            onDismissRequest = { showSettings = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Pomodoro Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Work duration slider
                Text(
                    text = "Work Duration: $workDuration minutes",
                    style = MaterialTheme.typography.bodyLarge
                )
                Slider(
                    value = workDuration.toFloat(),
                    onValueChange = { workDuration = it.toInt() },
                    valueRange = 1f..60f,
                    steps = 59,
                    modifier = Modifier.padding(bottom = 16.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                // Short break duration slider
                Text(
                    text = "Short Break: $shortBreakDuration minutes",
                    style = MaterialTheme.typography.bodyLarge
                )
                Slider(
                    value = shortBreakDuration.toFloat(),
                    onValueChange = { shortBreakDuration = it.toInt() },
                    valueRange = 1f..30f,
                    steps = 29,
                    modifier = Modifier.padding(bottom = 16.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.tertiary,
                        activeTrackColor = MaterialTheme.colorScheme.tertiary
                    )
                )
                
                // Long break duration slider
                Text(
                    text = "Long Break: $longBreakDuration minutes",
                    style = MaterialTheme.typography.bodyLarge
                )
                Slider(
                    value = longBreakDuration.toFloat(),
                    onValueChange = { longBreakDuration = it.toInt() },
                    valueRange = 5f..60f,
                    steps = 55,
                    modifier = Modifier.padding(bottom = 16.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary,
                        activeTrackColor = MaterialTheme.colorScheme.secondary
                    )
                )
                
                // Pomodoros before long break slider
                Text(
                    text = "Pomodoros before long break: $pomodorosBeforeLongBreak",
                    style = MaterialTheme.typography.bodyLarge
                )
                Slider(
                    value = pomodorosBeforeLongBreak.toFloat(),
                    onValueChange = { pomodorosBeforeLongBreak = it.toInt() },
                    valueRange = 1f..10f,
                    steps = 9,
                    modifier = Modifier.padding(bottom = 24.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                // Save button
                Button(
                    onClick = {
                        pomodoroViewModel.setWorkDuration(workDuration)
                        pomodoroViewModel.setShortBreakDuration(shortBreakDuration)
                        pomodoroViewModel.setLongBreakDuration(longBreakDuration)
                        pomodoroViewModel.setLongBreakAfterPomodoros(pomodorosBeforeLongBreak)
                        showSettings = false
                        scope.launch {
                            sheetState.hide()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Save Settings")
                }
            }
        }
    }
}

@Composable
fun PomodoroContent(pomodoroViewModel: PomodoroViewModel) {
    // Collect state from the ViewModel
    val timerState by pomodoroViewModel.timerState.collectAsState()
    val timeRemaining by pomodoroViewModel.timeRemaining.collectAsState()
    val pomodoroCount by pomodoroViewModel.pomodoroCount.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Pomodoro count card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pomodoros Completed",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$pomodoroCount",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Timer circle with pulse animation when active
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(300.dp)
                .clip(CircleShape)
                .background(
                    color = when (timerState) {
                        PomodoroViewModel.TimerState.WORK -> MaterialTheme.colorScheme.primary
                        PomodoroViewModel.TimerState.BREAK -> MaterialTheme.colorScheme.tertiary
                        PomodoroViewModel.TimerState.PAUSED -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.outline
                    }.copy(alpha = 0.3f)
                )
        ) {
            TimerCircle(
                timerState = timerState,
                timeRemaining = timeRemaining,
                totalTime = when (timerState) {
                    PomodoroViewModel.TimerState.WORK -> PomodoroViewModel.DEFAULT_WORK_DURATION
                    PomodoroViewModel.TimerState.BREAK -> PomodoroViewModel.DEFAULT_SHORT_BREAK_DURATION
                    else -> PomodoroViewModel.DEFAULT_WORK_DURATION
                }
            )
            
            // Timer text with animation
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formatTime(timeRemaining),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val stateText = when (timerState) {
                    PomodoroViewModel.TimerState.WORK -> "Focus Time"
                    PomodoroViewModel.TimerState.BREAK -> "Break Time"
                    PomodoroViewModel.TimerState.PAUSED -> "Paused"
                    else -> "Ready"
                }
                
                Text(
                    text = stateText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = when (timerState) {
                        PomodoroViewModel.TimerState.WORK -> MaterialTheme.colorScheme.primary
                        PomodoroViewModel.TimerState.BREAK -> MaterialTheme.colorScheme.tertiary
                        PomodoroViewModel.TimerState.PAUSED -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Control buttons
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (timerState == PomodoroViewModel.TimerState.IDLE || timerState == PomodoroViewModel.TimerState.PAUSED) {
                // Start/Resume button
                FloatingActionButton(
                    onClick = {
                        if (timerState == PomodoroViewModel.TimerState.IDLE) {
                            pomodoroViewModel.startWorkTimer()
                        } else {
                            pomodoroViewModel.resumeTimer()
                        }
                    },
                    modifier = Modifier.size(64.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = if (timerState == PomodoroViewModel.TimerState.IDLE) "Start" else "Resume",
                        modifier = Modifier.size(32.dp)
                    )
                }
            } else {
                // Pause button
                FloatingActionButton(
                    onClick = { pomodoroViewModel.pauseTimer() },
                    modifier = Modifier.size(64.dp),
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            // Stop button (only show if timer is running or paused)
            AnimatedVisibility(
                visible = timerState != PomodoroViewModel.TimerState.IDLE,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FloatingActionButton(
                    onClick = { pomodoroViewModel.stopTimer() },
                    modifier = Modifier.size(64.dp),
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            // Skip button (only show if timer is running)
            AnimatedVisibility(
                visible = timerState == PomodoroViewModel.TimerState.WORK || timerState == PomodoroViewModel.TimerState.BREAK,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FloatingActionButton(
                    onClick = { 
                        if (timerState == PomodoroViewModel.TimerState.WORK) {
                            pomodoroViewModel.startBreakTimer()
                        } else {
                            pomodoroViewModel.startWorkTimer()
                        }
                    },
                    modifier = Modifier.size(64.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = "Skip",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StopwatchContent(stopwatchViewModel: StopwatchViewModel) {
    val timeElapsed by stopwatchViewModel.timeElapsed.collectAsState()
    val isRunning by stopwatchViewModel.isRunning.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = formatStopwatchTime(timeElapsed),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Control buttons with enhanced design
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isRunning) {
                // Start button
                FloatingActionButton(
                    onClick = { stopwatchViewModel.start() },
                    modifier = Modifier.size(64.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start",
                        modifier = Modifier.size(32.dp)
                    )
                }
            } else {
                // Pause button
                FloatingActionButton(
                    onClick = { stopwatchViewModel.pause() },
                    modifier = Modifier.size(64.dp),
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            // Stop button (only show if timer is running or paused)
            AnimatedVisibility(
                visible = isRunning,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FloatingActionButton(
                    onClick = { stopwatchViewModel.stop() },
                    modifier = Modifier.size(64.dp),
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TimerCircle(
    timerState: PomodoroViewModel.TimerState,
    timeRemaining: Long,
    totalTime: Long
) {
    val progress = if (totalTime > 0) timeRemaining.toFloat() / totalTime.toFloat() else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress)
    
    val color = when (timerState) {
        PomodoroViewModel.TimerState.WORK -> MaterialTheme.colorScheme.primary
        PomodoroViewModel.TimerState.BREAK -> MaterialTheme.colorScheme.tertiary
        PomodoroViewModel.TimerState.PAUSED -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline
    }
    
    // Capture the surface variant color outside the Canvas scope
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    
    Canvas(modifier = Modifier.size(300.dp)) {
        // Background circle
        drawArc(
            color = surfaceVariantColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            size = Size(size.width, size.height),
            style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Progress arc
        drawArc(
            color = color,
            startAngle = 270f,
            sweepAngle = 360f * animatedProgress,
            useCenter = false,
            size = Size(size.width, size.height),
            style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

/**
 * Formats time in milliseconds to MM:SS format
 */
fun formatTime(timeInMillis: Long): String {
    val totalSeconds = timeInMillis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

/**
 * Formats time in milliseconds to MM:SS.ms format for stopwatch
 */
fun formatStopwatchTime(timeInMillis: Long): String {
    val hours = (timeInMillis / (1000 * 60 * 60)) % 24
    val minutes = (timeInMillis / (1000 * 60)) % 60
    val seconds = (timeInMillis / 1000) % 60
    val millis = (timeInMillis % 1000) / 10
    
    return if (hours > 0) {
        String.format("%02d:%02d:%02d.%02d", hours, minutes, seconds, millis)
    } else {
        String.format("%02d:%02d.%02d", minutes, seconds, millis)
    }
}
