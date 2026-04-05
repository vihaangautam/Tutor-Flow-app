package com.example.tutorflow.ui.screens.batches

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import com.example.tutorflow.data.entity.Student
import com.example.tutorflow.ui.components.EmptyStateMessage
import com.example.tutorflow.ui.components.StatusBadge
import com.example.tutorflow.ui.components.StudentAvatar
import com.example.tutorflow.ui.theme.*
import com.example.tutorflow.viewmodel.BatchViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchDetailScreen(
    batchId: Long,
    viewModel: BatchViewModel,
    onBack: () -> Unit
) {
    val batch by viewModel.getBatchById(batchId).collectAsState(initial = null)
    val students by viewModel.getStudentsByBatch(batchId).collectAsState(initial = emptyList())
    var showAddStudent by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<Student?>(null) }
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = batch?.name ?: "Batch",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        batch?.let {
                            Text(
                                text = if (it.type == BatchType.COACHING) "Coaching Centre" else "Home Tuition",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddStudent = true },
                containerColor = Primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Rounded.PersonAdd, contentDescription = "Add Student")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Batch info card
            item {
                batch?.let { b ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatusBadge(
                                    text = if (b.type == BatchType.COACHING) "Coaching" else "Home",
                                    color = if (b.type == BatchType.COACHING) Primary else Secondary,
                                    backgroundColor = if (b.type == BatchType.COACHING) Primary.copy(alpha = 0.1f) else Secondary.copy(alpha = 0.1f)
                                )
                                Text(
                                    text = "${students.size} students",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (b.location.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.LocationOn, null, Modifier.size(16.dp), tint = Neutral)
                                    Spacer(Modifier.width(4.dp))
                                    Text(b.location, style = MaterialTheme.typography.bodySmall, color = Neutral)
                                }
                            }
                            if (b.timing.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Schedule, null, Modifier.size(16.dp), tint = Neutral)
                                    Spacer(Modifier.width(4.dp))
                                    Text(b.timing, style = MaterialTheme.typography.bodySmall, color = Neutral)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Students",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 8.dp)
                )
            }

            if (students.isEmpty()) {
                item {
                    EmptyStateMessage("No students in this batch yet.\nTap + to add students!")
                }
            }

            items(students, key = { it.id }) { student ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.EndToStart) {
                            viewModel.deleteStudent(student)
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
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .background(color, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(Icons.Default.Delete, "Delete", tint = Color.White, modifier = Modifier.padding(end = 20.dp))
                        }
                    },
                    enableDismissFromStartToEnd = false
                ) {
                    StudentCard(student, currencyFormat) { editingStudent = student }
                }
            }
        }
    }

    if (showAddStudent) {
        StudentFormSheet(
            onDismiss = { showAddStudent = false },
            onSave = { name, phone, fee ->
                viewModel.addStudent(batchId, name, phone, fee)
                showAddStudent = false
            }
        )
    }

    editingStudent?.let { student ->
        StudentFormSheet(
            student = student,
            onDismiss = { editingStudent = null },
            onSave = { name, phone, fee ->
                viewModel.updateStudent(student.copy(fullName = name, phone = phone, monthlyFee = fee))
                editingStudent = null
            }
        )
    }
}

@Composable
private fun StudentCard(
    student: Student,
    currencyFormat: NumberFormat,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StudentAvatar(name = student.fullName, size = 44)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.fullName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (student.phone.isNotBlank()) {
                    Text(
                        text = student.phone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = currencyFormat.format(student.monthlyFee),
                    style = MaterialTheme.typography.titleMedium,
                    color = Primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "/month",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Rounded.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentFormSheet(
    student: Student? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, Double) -> Unit
) {
    var name by remember { mutableStateOf(student?.fullName ?: "") }
    var phone by remember { mutableStateOf(student?.phone ?: "") }
    var feeText by remember { mutableStateOf(student?.monthlyFee?.let { if (it == 0.0) "" else it.toInt().toString() } ?: "") }

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
                text = if (student == null) "Add Student" else "Edit Student",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = feeText,
                onValueChange = { feeText = it },
                label = { Text("Monthly Fee (₹)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val fee = feeText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank()) onSave(name, phone, fee)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = name.isNotBlank()
            ) {
                Text(
                    text = if (student == null) "Add Student" else "Save Changes",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
