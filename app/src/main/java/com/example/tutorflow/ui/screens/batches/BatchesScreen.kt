package com.example.tutorflow.ui.screens.batches

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.tutorflow.data.entity.Batch
import com.example.tutorflow.data.entity.BatchType
import com.example.tutorflow.ui.components.EmptyStateMessage
import com.example.tutorflow.ui.components.OverlappingAvatars
import com.example.tutorflow.ui.components.StatusBadge
import com.example.tutorflow.ui.theme.*
import com.example.tutorflow.viewmodel.BatchViewModel
import com.example.tutorflow.viewmodel.BatchWithCount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchesScreen(
    viewModel: BatchViewModel,
    onBatchClick: (Long) -> Unit
) {
    val batches by viewModel.batches.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var editingBatch by remember { mutableStateOf<Batch?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Text(
                    text = "Batches",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, top = 48.dp, bottom = 4.dp)
                )
                Text(
                    text = "${batches.size} batch${if (batches.size != 1) "es" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 20.dp, bottom = 16.dp)
                )
            }

            if (batches.isEmpty()) {
                item {
                    EmptyStateMessage("No batches yet.\nTap + to create your first batch!")
                }
            }

            items(batches, key = { it.batch.id }) { batchItem ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.EndToStart) {
                            viewModel.deleteBatch(batchItem.batch)
                            true
                        } else false
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        val color by animateColorAsState(
                            if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) Danger else Color.Transparent
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .background(color, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.White,
                                modifier = Modifier.padding(end = 20.dp)
                            )
                        }
                    },
                    enableDismissFromStartToEnd = false
                ) {
                    BatchCard(
                        batchItem = batchItem,
                        onClick = { onBatchClick(batchItem.batch.id) },
                        onEdit = { editingBatch = batchItem.batch }
                    )
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { showAddSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Primary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Add Batch")
        }
    }

    // Add Batch Bottom Sheet
    if (showAddSheet) {
        BatchFormSheet(
            onDismiss = { showAddSheet = false },
            onSave = { name, type, location, timing ->
                viewModel.addBatch(name, type, location, timing)
                showAddSheet = false
            }
        )
    }

    // Edit Batch Bottom Sheet
    editingBatch?.let { batch ->
        BatchFormSheet(
            batch = batch,
            onDismiss = { editingBatch = null },
            onSave = { name, type, location, timing ->
                viewModel.updateBatch(batch.copy(name = name, type = type, location = location, timing = timing))
                editingBatch = null
            }
        )
    }
}

@Composable
private fun BatchCard(
    batchItem: BatchWithCount,
    onClick: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = batchItem.batch.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    StatusBadge(
                        text = if (batchItem.batch.type == BatchType.COACHING) "Coaching" else "Home",
                        color = if (batchItem.batch.type == BatchType.COACHING) Primary else Secondary,
                        backgroundColor = if (batchItem.batch.type == BatchType.COACHING) Primary.copy(alpha = 0.1f) else Secondary.copy(alpha = 0.1f)
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (batchItem.batch.location.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = batchItem.batch.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (batchItem.batch.timing.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = batchItem.batch.timing,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.People,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${batchItem.studentCount} student${if (batchItem.studentCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (batchItem.students.isNotEmpty()) {
                    OverlappingAvatars(
                        names = batchItem.students.map { it.fullName },
                        maxVisible = 4,
                        size = 28
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatchFormSheet(
    batch: Batch? = null,
    onDismiss: () -> Unit,
    onSave: (String, BatchType, String, String) -> Unit
) {
    var name by remember { mutableStateOf(batch?.name ?: "") }
    var selectedType by remember { mutableStateOf(batch?.type ?: BatchType.COACHING) }
    var location by remember { mutableStateOf(batch?.location ?: "") }
    var timing by remember { mutableStateOf(batch?.timing ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = if (batch == null) "New Batch" else "Edit Batch",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Batch Name") },
                placeholder = { Text("e.g., Class 10 - Science") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(14.dp))

            Text("Type", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = selectedType == BatchType.COACHING,
                    onClick = { selectedType = BatchType.COACHING },
                    label = { Text("Coaching Centre") },
                    leadingIcon = if (selectedType == BatchType.COACHING) {
                        { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
                FilterChip(
                    selected = selectedType == BatchType.HOME,
                    onClick = { selectedType = BatchType.HOME },
                    label = { Text("Home Tuition") },
                    leadingIcon = if (selectedType == BatchType.HOME) {
                        { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = timing,
                onValueChange = { timing = it },
                label = { Text("Timing (Optional)") },
                placeholder = { Text("e.g., Mon, Wed, Fri · 5:00 PM") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { if (name.isNotBlank()) onSave(name, selectedType, location, timing) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = name.isNotBlank()
            ) {
                Text(
                    text = if (batch == null) "Create Batch" else "Save Changes",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
