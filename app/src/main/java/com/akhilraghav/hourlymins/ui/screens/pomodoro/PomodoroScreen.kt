package com.akhilraghav.hourlymins.ui.screens.pomodoro

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.akhilraghav.hourlymins.viewmodel.PomodoroViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen() {
    val viewModel: PomodoroViewModel = viewModel(
        factory = PomodoroViewModel.PomodoroViewModelFactory()
    )
    
    val context = LocalContext.current
    
    // Initialize the ViewModel with context for notifications
    LaunchedEffect(key1 = viewModel) {
        viewModel.initialize(context)
    }
    
    val timerState by viewModel.timerState.collectAsState()
    val timeRemaining by viewModel.timeRemaining.collectAsState()
    val pomodoroCount by viewModel.pomodoroCount.collectAsState()
    
    // Settings state
    var showSettings by remember { mutableStateOf(false) }
    var workDuration by remember { mutableIntStateOf(25) }
    var shortBreakDuration by remember { mutableIntStateOf(5) }
    var longBreakDuration by remember { mutableIntStateOf(15) }
    var pomodorosBeforeLongBreak by remember { mutableIntStateOf(4) }
    
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pomodoro Timer") },
                actions = {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Pomodoro count
            Text(
                text = "Pomodoros Completed: $pomodoroCount",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Timer circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(300.dp)
            ) {
                TimerCircle(
                    timerState = timerState,
                    timeRemaining = timeRemaining,
                    totalTime = when (timerState) {
                        PomodoroViewModel.TimerState.WORK, PomodoroViewModel.TimerState.PAUSED -> workDuration * 60 * 1000L
                        PomodoroViewModel.TimerState.BREAK -> shortBreakDuration * 60 * 1000L
                        else -> workDuration * 60 * 1000L
                    }
                )
                
                // Timer text
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
                    
                    Text(
                        text = when (timerState) {
                            PomodoroViewModel.TimerState.WORK -> "Working"
                            PomodoroViewModel.TimerState.BREAK -> "Break Time"
                            PomodoroViewModel.TimerState.PAUSED -> "Paused"
                            else -> "Ready"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                when (timerState) {
                    PomodoroViewModel.TimerState.IDLE -> {
                        FloatingActionButton(
                            onClick = { viewModel.startWorkTimer() },
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Start",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    PomodoroViewModel.TimerState.WORK, PomodoroViewModel.TimerState.BREAK -> {
                        FloatingActionButton(
                            onClick = { viewModel.pauseTimer() },
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Pause",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        
                        FloatingActionButton(
                            onClick = { viewModel.stopTimer() },
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    PomodoroViewModel.TimerState.PAUSED -> {
                        FloatingActionButton(
                            onClick = { viewModel.resumeTimer() },
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Resume",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        
                        FloatingActionButton(
                            onClick = { viewModel.stopTimer() },
                            modifier = Modifier.size(64.dp)
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
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Pomodoro technique explanation
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "The Pomodoro Technique",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "1. Work for 25 minutes\n2. Take a 5-minute break\n3. After 4 pomodoros, take a longer 15-minute break\n4. Repeat",
                        style = MaterialTheme.typography.bodyMedium
                    )
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
                        text = "Timer Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Work duration
                    Text(
                        text = "Work Duration: $workDuration minutes",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Slider(
                        value = workDuration.toFloat(),
                        onValueChange = { workDuration = it.toInt() },
                        valueRange = 5f..60f,
                        steps = 11
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Short break duration
                    Text(
                        text = "Short Break Duration: $shortBreakDuration minutes",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Slider(
                        value = shortBreakDuration.toFloat(),
                        onValueChange = { shortBreakDuration = it.toInt() },
                        valueRange = 1f..15f,
                        steps = 14
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Long break duration
                    Text(
                        text = "Long Break Duration: $longBreakDuration minutes",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Slider(
                        value = longBreakDuration.toFloat(),
                        onValueChange = { longBreakDuration = it.toInt() },
                        valueRange = 10f..30f,
                        steps = 4
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Pomodoros before long break
                    Text(
                        text = "Pomodoros Before Long Break: $pomodorosBeforeLongBreak",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Slider(
                        value = pomodorosBeforeLongBreak.toFloat(),
                        onValueChange = { pomodorosBeforeLongBreak = it.toInt() },
                        valueRange = 2f..6f,
                        steps = 4
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showSettings = false }) {
                            Text("Cancel")
                        }
                        
                        TextButton(
                            onClick = {
                                viewModel.setWorkDuration(workDuration)
                                viewModel.setShortBreakDuration(shortBreakDuration)
                                viewModel.setLongBreakDuration(longBreakDuration)
                                viewModel.setLongBreakAfterPomodoros(pomodorosBeforeLongBreak)
                                
                                scope.launch {
                                    sheetState.hide()
                                    showSettings = false
                                }
                            }
                        ) {
                            Text("Save")
                        }
                    }
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
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "Progress Animation"
    )
    
    val color = when (timerState) {
        PomodoroViewModel.TimerState.WORK -> MaterialTheme.colorScheme.primary
        PomodoroViewModel.TimerState.BREAK -> MaterialTheme.colorScheme.tertiary
        PomodoroViewModel.TimerState.PAUSED -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline
    }
    
    Canvas(modifier = Modifier.size(300.dp)) {
        // Background circle
        drawArc(
            color = MaterialTheme.colorScheme.surfaceVariant,
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
