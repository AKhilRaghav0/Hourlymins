package com.akhilraghav.hourlymins.ui.screens.pomodoro

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
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
import com.akhilraghav.hourlymins.viewmodel.StopwatchViewModel
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
    
    // Animation for the timer background (but with no scaling to avoid transitions)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale = 1f  // Fixed scale to avoid animations
    
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
        Box(modifier = Modifier.fillMaxSize()) {
            // Background gradient based on timer state
            val backgroundColor = when (timerState) {
                PomodoroViewModel.TimerState.WORK -> MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                PomodoroViewModel.TimerState.BREAK -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.05f)
                PomodoroViewModel.TimerState.PAUSED -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
                else -> MaterialTheme.colorScheme.background
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                backgroundColor,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Pomodoro count card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
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
                        .size(300.dp * pulseScale)
                        .shadow(
                            elevation = 10.dp,
                            shape = CircleShape,
                            spotColor = when (timerState) {
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
                            PomodoroViewModel.TimerState.WORK, PomodoroViewModel.TimerState.PAUSED -> workDuration * 60 * 1000L
                            PomodoroViewModel.TimerState.BREAK -> shortBreakDuration * 60 * 1000L
                            else -> workDuration * 60 * 1000L
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
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // Control buttons with enhanced design
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (timerState == PomodoroViewModel.TimerState.IDLE || timerState == PomodoroViewModel.TimerState.PAUSED) {
                        // Start/Resume button
                        FloatingActionButton(
                            onClick = {
                                if (timerState == PomodoroViewModel.TimerState.IDLE) {
                                    viewModel.startWorkTimer()
                                } else {
                                    viewModel.resumeTimer()
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
                            onClick = { viewModel.pauseTimer() },
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
    
    // No theme selector
    
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
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Timer Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Work duration
                Text(
                    text = "Work Duration: $workDuration minutes",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Slider(
                    value = workDuration.toFloat(),
                    onValueChange = { workDuration = it.toInt() },
                    valueRange = 5f..60f,
                    steps = 11,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
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
                    steps = 14,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.tertiary,
                        activeTrackColor = MaterialTheme.colorScheme.tertiary,
                        inactiveTrackColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
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
                    steps = 4,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary,
                        activeTrackColor = MaterialTheme.colorScheme.secondary,
                        inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer
                    )
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
                    steps = 4,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { showSettings = false },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            viewModel.setWorkDuration(workDuration)
                            viewModel.setShortBreakDuration(shortBreakDuration)
                            viewModel.setLongBreakDuration(longBreakDuration)
                            viewModel.setLongBreakAfterPomodoros(pomodorosBeforeLongBreak)
                            
                            scope.launch {
                                sheetState.hide()
                                showSettings = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Save")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
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
