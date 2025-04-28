package com.akhilraghav.hourlymins.ui.screens.diary

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.akhilraghav.hourlymins.HourlyMinsApp
import com.akhilraghav.hourlymins.data.repository.DiaryRepository
import com.akhilraghav.hourlymins.data.entities.DiaryEntry
import com.akhilraghav.hourlymins.viewmodel.DiaryViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen() {
    val context = LocalContext.current
    val database = (context.applicationContext as HourlyMinsApp).database
    val repository = DiaryRepository(database.diaryEntryDao())
    val viewModel: DiaryViewModel = viewModel(
        factory = DiaryViewModel.DiaryViewModelFactory(repository)
    )
    
    val entries by viewModel.entriesForSelectedDate.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    
    // States for UI
    var showDatePicker by remember { mutableStateOf(false) }
    var showEntrySheet by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<DiaryEntry?>(null) }
    
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Diary")
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingEntry = null
                    showEntrySheet = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Entry")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No entries for this day. Tap + to add one.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(entries) { entry ->
                        DiaryEntryItem(
                            entry = entry,
                            onEditClick = {
                                editingEntry = entry
                                showEntrySheet = true
                            }
                        )
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
        
        // Entry Bottom Sheet
        if (showEntrySheet) {
            ModalBottomSheet(
                onDismissRequest = { showEntrySheet = false },
                sheetState = sheetState
            ) {
                DiaryEntryForm(
                    entry = editingEntry,
                    onSave = { content, isProductiveHour, productivityRating, tags ->
                        if (editingEntry != null) {
                            val updatedEntry = editingEntry!!.copy(
                                content = content,
                                isProductiveHour = isProductiveHour,
                                productivityRating = productivityRating,
                                tags = tags
                            )
                            viewModel.updateEntry(updatedEntry)
                        } else {
                            viewModel.createNewEntry(
                                content = content,
                                isProductiveHour = isProductiveHour,
                                productivityRating = productivityRating,
                                tags = tags
                            )
                        }
                        scope.launch {
                            sheetState.hide()
                            showEntrySheet = false
                        }
                    },
                    onCancel = {
                        scope.launch {
                            sheetState.hide()
                            showEntrySheet = false
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DiaryEntryItem(
    entry: DiaryEntry,
    onEditClick: () -> Unit
) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth(),
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
                    text = timeFormat.format(entry.timestamp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Box(
                    modifier = Modifier
                        .background(
                            color = if (entry.isProductiveHour) Color(0xFF4CAF50) else Color(0xFFF44336),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (entry.isProductiveHour) "Productive" else "Unproductive",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Entry"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyLarge
            )
            
            if (!entry.tags.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tags: ${entry.tags}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Rating: ${entry.productivityRating}/5",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun DiaryEntryForm(
    entry: DiaryEntry? = null,
    onSave: (String, Boolean, Int, String?) -> Unit,
    onCancel: () -> Unit
) {
    var content by remember { mutableStateOf(entry?.content ?: "") }
    var isProductiveHour by remember { mutableStateOf(entry?.isProductiveHour ?: true) }
    var productivityRating by remember { mutableStateOf(entry?.productivityRating ?: 3) }
    var tags by remember { mutableStateOf(entry?.tags ?: "") }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = if (entry == null) "What did you accomplish in the last hour?" else "Edit Entry",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Activity Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Was this hour productive?",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                onClick = { isProductiveHour = true },
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (isProductiveHour) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(4.dp)
                    )
            ) {
                Text("Yes")
            }
            
            TextButton(
                onClick = { isProductiveHour = false },
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (!isProductiveHour) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(4.dp)
                    )
            ) {
                Text("No")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Productivity Rating (1-5)",
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
                    onClick = { productivityRating = i },
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (productivityRating == i) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(4.dp)
                        )
                ) {
                    Text("$i")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = tags,
            onValueChange = { tags = it },
            label = { Text("Tags (comma separated)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
            
            TextButton(
                onClick = {
                    if (content.isNotBlank()) {
                        onSave(content, isProductiveHour, productivityRating, tags.ifBlank { null })
                    }
                },
                enabled = content.isNotBlank()
            ) {
                Text("Save")
            }
        }
    }
}
