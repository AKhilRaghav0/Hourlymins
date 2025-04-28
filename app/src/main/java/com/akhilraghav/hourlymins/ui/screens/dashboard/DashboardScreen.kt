package com.akhilraghav.hourlymins.ui.screens.dashboard

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.akhilraghav.hourlymins.HourlyMinsApp
import com.akhilraghav.hourlymins.data.entities.ProductivityRecord
import com.akhilraghav.hourlymins.data.repository.DiaryRepository
import com.akhilraghav.hourlymins.data.repository.ProductivityRepository
import com.akhilraghav.hourlymins.data.repository.TaskRepository
import com.akhilraghav.hourlymins.viewmodel.ProductivityViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val database = (context.applicationContext as HourlyMinsApp).database
    
    // Create repositories
    val diaryRepository = DiaryRepository(database.diaryEntryDao())
    val taskRepository = TaskRepository(database.taskDao())
    val productivityRepository = ProductivityRepository(database.productivityDao())
    
    // Create ViewModel
    val viewModel: ProductivityViewModel = viewModel(
        factory = ProductivityViewModel.ProductivityViewModelFactory(
            productivityRepository,
            diaryRepository,
            taskRepository
        )
    )
    
    val selectedDate by viewModel.selectedDate.collectAsState()
    val recordForSelectedDate by viewModel.recordForSelectedDate.collectAsState()
    val weeklyStats by viewModel.weeklyStats.collectAsState()
    val monthlyStats by viewModel.monthlyStats.collectAsState()
    
    val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    
    // States for UI
    var showDatePicker by remember { mutableStateOf(false) }
    var showRatingSheet by remember { mutableStateOf(false) }
    var selectedRating by remember { mutableIntStateOf(recordForSelectedDate?.overallRating ?: 3) }
    var notes by remember { mutableStateOf(recordForSelectedDate?.notes ?: "") }
    
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    
    // Calculate today's productive hours
    var productiveHours by remember { mutableIntStateOf(0) }
    var wastedHours by remember { mutableIntStateOf(0) }
    var completedTasks by remember { mutableIntStateOf(0) }
    var totalTasks by remember { mutableIntStateOf(0) }
    
    // Load data for the current date if no record exists
    LaunchedEffect(selectedDate, recordForSelectedDate) {
        if (recordForSelectedDate == null) {
            productiveHours = diaryRepository.getProductiveHoursForDate(selectedDate)
            wastedHours = diaryRepository.getWastedHoursForDate(selectedDate)
            completedTasks = taskRepository.getCompletedTasksForDate(selectedDate)
            totalTasks = taskRepository.getTotalTaskCount()
        } else {
            productiveHours = recordForSelectedDate!!.productiveHours
            wastedHours = recordForSelectedDate!!.wastedHours
            completedTasks = recordForSelectedDate!!.completedTasks
            totalTasks = recordForSelectedDate!!.totalTasks
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Productivity Dashboard")
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = dateFormat.format(selectedDate),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Select Date"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Daily summary card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (recordForSelectedDate == null) {
                            TextButton(
                                onClick = { showRatingSheet = true }
                            ) {
                                Text("Rate Your Day")
                            }
                        } else {
                            IconButton(onClick = { showRatingSheet = true }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Rating"
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Productivity pie chart
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProductivityPieChart(
                            productiveHours = productiveHours,
                            wastedHours = wastedHours
                        )
                        
                        Column(
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Productive: $productiveHours hrs",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF4CAF50)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Wasted: $wastedHours hrs",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFF44336)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Tasks: $completedTasks/$totalTasks",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Overall rating
                    if (recordForSelectedDate != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Overall Rating: ${recordForSelectedDate!!.overallRating}/5",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        
                        if (!recordForSelectedDate!!.notes.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Notes: ${recordForSelectedDate!!.notes}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Weekly stats
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Weekly Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (weeklyStats.isEmpty()) {
                        Text(
                            text = "No data for this week yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        WeeklyBarChart(records = weeklyStats)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Weekly averages
                        val avgProductiveHours = weeklyStats.map { it.productiveHours }.average()
                        val avgRating = weeklyStats.map { it.overallRating }.average()
                        
                        Text(
                            text = "Avg. Productive Hours: %.1f".format(avgProductiveHours),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Avg. Rating: %.1f".format(avgRating),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Monthly stats
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Monthly Trends",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (monthlyStats.isEmpty()) {
                        Text(
                            text = "No data for this month yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        // Monthly averages
                        val avgProductiveHours = monthlyStats.map { it.productiveHours }.average()
                        val avgRating = monthlyStats.map { it.overallRating }.average()
                        val totalProductiveHours = monthlyStats.sumOf { it.productiveHours }
                        val totalWastedHours = monthlyStats.sumOf { it.wastedHours }
                        
                        Text(
                            text = "Total Productive Hours: $totalProductiveHours",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Total Wasted Hours: $totalWastedHours",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Avg. Productive Hours/Day: %.1f".format(avgProductiveHours),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "Avg. Rating: %.1f".format(avgRating),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Productivity ratio
                        val productivityRatio = if (totalProductiveHours + totalWastedHours > 0) {
                            totalProductiveHours.toFloat() / (totalProductiveHours + totalWastedHours)
                        } else {
                            0f
                        }
                        
                        Text(
                            text = "Productivity Ratio: ${(productivityRatio * 100).toInt()}%",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Progress bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE0E0E0))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(productivityRatio)
                                    .height(16.dp)
                                    .background(Color(0xFF4CAF50))
                            )
                        }
                    }
                }
            }
        }
        
        // Date Picker Dialog
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDate.time
            )
            
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            viewModel.setSelectedDate(Date(millis))
                        }
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
        
        // Rating Bottom Sheet
        if (showRatingSheet) {
            ModalBottomSheet(
                onDismissRequest = { showRatingSheet = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = if (recordForSelectedDate == null) "Rate Your Day" else "Edit Rating",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Overall Rating (1-5)",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (i in 1..5) {
                            TextButton(
                                onClick = { selectedRating = i },
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        color = if (selectedRating == i) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            ) {
                                Text("$i")
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            scope.launch {
                                sheetState.hide()
                                showRatingSheet = false
                            }
                        }) {
                            Text("Cancel")
                        }
                        
                        TextButton(
                            onClick = {
                                scope.launch {
                                    if (recordForSelectedDate == null) {
                                        viewModel.createDailyProductivityRecord(
                                            date = selectedDate,
                                            notes = notes.ifBlank { null },
                                            overallRating = selectedRating
                                        )
                                    } else {
                                        val updatedRecord = recordForSelectedDate!!.copy(
                                            overallRating = selectedRating,
                                            notes = notes.ifBlank { null }
                                        )
                                        viewModel.updateProductivityRecord(updatedRecord)
                                    }
                                    
                                    sheetState.hide()
                                    showRatingSheet = false
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
fun ProductivityPieChart(
    productiveHours: Int,
    wastedHours: Int,
    modifier: Modifier = Modifier
) {
    val total = productiveHours + wastedHours
    
    if (total == 0) {
        Box(
            modifier = modifier
                .size(120.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No Data",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }
        return
    }
    
    val productiveAngle = 360f * (productiveHours.toFloat() / total.toFloat())
    
    Canvas(
        modifier = modifier.size(120.dp)
    ) {
        val canvasSize = size.minDimension
        val radius = canvasSize / 2
        val center = Offset(size.width / 2, size.height / 2)
        
        // Draw productive hours slice
        drawArc(
            color = Color(0xFF4CAF50), // Green
            startAngle = 0f,
            sweepAngle = productiveAngle,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(canvasSize, canvasSize)
        )
        
        // Draw wasted hours slice
        drawArc(
            color = Color(0xFFF44336), // Red
            startAngle = productiveAngle,
            sweepAngle = 360f - productiveAngle,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(canvasSize, canvasSize)
        )
        
        // Draw a white circle in the middle for a donut chart effect
        val innerRadius = radius * 0.6f
        drawCircle(
            color = MaterialTheme.colorScheme.surface,
            radius = innerRadius,
            center = center
        )
        
        // Draw text in the middle
        drawContext.canvas.nativeCanvas.apply {
            val text = "$productiveHours/$total"
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 14.sp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
            }
            drawText(
                text,
                center.x,
                center.y + textPaint.textSize / 3,
                textPaint
            )
        }
    }
}

@Composable
fun WeeklyBarChart(
    records: List<ProductivityRecord>,
    modifier: Modifier = Modifier
) {
    val calendar = Calendar.getInstance()
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday, 6 = Saturday
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    
    // Group records by day of week
    val recordsByDay = records.groupBy { record ->
        calendar.time = record.date
        calendar.get(Calendar.DAY_OF_WEEK) - 1
    }
    
    // Find max productive hours for scaling
    val maxHours = records.maxOfOrNull { it.productiveHours } ?: 0
    val scale = if (maxHours > 0) 1f / maxHours else 0f
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(8.dp)
    ) {
        // Chart
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            for (day in 0..6) {
                val dayRecords = recordsByDay[day] ?: emptyList()
                val productiveHours = dayRecords.sumOf { it.productiveHours }
                val wastedHours = dayRecords.sumOf { it.wastedHours }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Productive hours bar
                    if (productiveHours > 0) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .weight(scale * productiveHours)
                                .background(
                                    color = Color(0xFF4CAF50),
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )
                    }
                    
                    // Wasted hours bar
                    if (wastedHours > 0) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .weight(scale * wastedHours)
                                .background(
                                    color = Color(0xFFF44336),
                                    shape = if (productiveHours == 0) {
                                        RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    } else {
                                        RoundedCornerShape(0.dp)
                                    }
                                )
                        )
                    }
                    
                    // Empty space if no data
                    if (productiveHours == 0 && wastedHours == 0) {
                        Spacer(modifier = Modifier.weight(0.1f))
                    }
                    
                    // Day label
                    Text(
                        text = daysOfWeek[day],
                        style = MaterialTheme.typography.bodySmall,
                        color = if (day == dayOfWeek) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
