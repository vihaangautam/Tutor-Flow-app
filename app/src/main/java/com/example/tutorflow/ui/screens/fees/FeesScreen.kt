package com.example.tutorflow.ui.screens.fees

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.tutorflow.data.entity.Payment
import com.example.tutorflow.ui.components.EmptyStateMessage
import com.example.tutorflow.ui.components.StudentAvatar
import com.example.tutorflow.ui.theme.*
import com.example.tutorflow.viewmodel.FeeStatus
import com.example.tutorflow.viewmodel.FeeUiState
import com.example.tutorflow.viewmodel.FeeViewModel
import com.example.tutorflow.viewmodel.StudentFeeInfo
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

private val monthNames = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeesScreen(viewModel: FeeViewModel) {
    val state by viewModel.uiState.collectAsState()
    var showPaymentSheet by remember { mutableStateOf<StudentFeeInfo?>(null) }
    var showHistorySheet by remember { mutableStateOf<StudentFeeInfo?>(null) }
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header
            item {
                Text(
                    text = "Fee Tracker",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, top = 48.dp, bottom = 16.dp)
                )
            }

            // Month/Year Selector
            item {
                MonthYearSelector(
                    selectedMonth = state.selectedMonth,
                    selectedYear = state.selectedYear,
                    onMonthChange = { viewModel.setMonth(it) },
                    onYearChange = { viewModel.setYear(it) }
                )
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search by student or batch...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            // Summary bar
            item {
                val paid = state.filteredStudentFees.count { it.status == FeeStatus.PAID }
                val partial = state.filteredStudentFees.count { it.status == FeeStatus.PARTIAL }
                val pending = state.filteredStudentFees.count { it.status == FeeStatus.PENDING }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FeeFilterChip("Paid ($paid)", Success, true, Modifier.weight(1f))
                    FeeFilterChip("Partial ($partial)", Warning, true, Modifier.weight(1f))
                    FeeFilterChip("Pending ($pending)", Danger, true, Modifier.weight(1f))
                }
            }

            if (state.filteredStudentFees.isEmpty()) {
                item {
                    EmptyStateMessage("No students found.\nAdd students in batches first!")
                }
            }

            items(state.filteredStudentFees, key = { it.student.id }) { feeInfo ->
                StudentFeeCard(
                    feeInfo = feeInfo,
                    currencyFormat = currencyFormat,
                    onLogPayment = { showPaymentSheet = feeInfo },
                    onViewHistory = { showHistorySheet = feeInfo }
                )
            }
        }

        // FAB
        FloatingActionButton(
            onClick = {
                if (state.filteredStudentFees.isNotEmpty()) {
                    showPaymentSheet = state.filteredStudentFees.first()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Primary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Log Payment")
        }
    }

    // Payment Bottom Sheet
    showPaymentSheet?.let { feeInfo ->
        PaymentFormSheet(
            feeInfo = feeInfo,
            allStudents = state.filteredStudentFees,
            selectedMonth = state.selectedMonth,
            selectedYear = state.selectedYear,
            onDismiss = { showPaymentSheet = null },
            onSave = { studentId, amount, date, month, year, note ->
                viewModel.logPayment(studentId, amount, date, month, year, note)
                showPaymentSheet = null
            }
        )
    }

    // History Bottom Sheet
    showHistorySheet?.let { feeInfo ->
        PaymentHistorySheet(
            feeInfo = feeInfo,
            viewModel = viewModel,
            onDismiss = { showHistorySheet = null }
        )
    }
}

@Composable
private fun MonthYearSelector(
    selectedMonth: Int,
    selectedYear: Int,
    onMonthChange: (Int) -> Unit,
    onYearChange: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // Year selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onYearChange(selectedYear - 1) }) {
                Icon(Icons.Rounded.ChevronLeft, "Previous year")
            }
            Text(
                text = selectedYear.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { onYearChange(selectedYear + 1) }) {
                Icon(Icons.Rounded.ChevronRight, "Next year")
            }
        }

        // Month chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            monthNames.forEachIndexed { index, name ->
                val month = index + 1
                val isSelected = month == selectedMonth
                Surface(
                    modifier = Modifier.clickable { onMonthChange(month) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) Primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FeeFilterChip(
    text: String,
    color: Color,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StudentFeeCard(
    feeInfo: StudentFeeInfo,
    currencyFormat: NumberFormat,
    onLogPayment: () -> Unit,
    onViewHistory: () -> Unit
) {
    val statusColor = when (feeInfo.status) {
        FeeStatus.PAID -> Success
        FeeStatus.PARTIAL -> Warning
        FeeStatus.PENDING -> Danger
    }
    val statusBg = when (feeInfo.status) {
        FeeStatus.PAID -> SuccessLight
        FeeStatus.PARTIAL -> WarningLight
        FeeStatus.PENDING -> DangerLight
    }
    val statusText = when (feeInfo.status) {
        FeeStatus.PAID -> "Paid"
        FeeStatus.PARTIAL -> "Partial"
        FeeStatus.PENDING -> "Pending"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudentAvatar(name = feeInfo.student.fullName, size = 44)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = feeInfo.student.fullName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = feeInfo.batchName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                com.example.tutorflow.ui.components.StatusBadge(
                    text = statusText,
                    color = statusColor,
                    backgroundColor = statusBg
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            val progress = if (feeInfo.student.monthlyFee > 0)
                (feeInfo.totalPaid / feeInfo.student.monthlyFee).toFloat().coerceIn(0f, 1f)
            else 0f

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = statusColor,
                trackColor = statusColor.copy(alpha = 0.12f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Paid: ${currencyFormat.format(feeInfo.totalPaid)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Success
                )
                Text(
                    text = "Due: ${currencyFormat.format(feeInfo.remaining)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (feeInfo.remaining > 0) Danger else Neutral
                )
                Text(
                    text = "Fee: ${currencyFormat.format(feeInfo.student.monthlyFee)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onViewHistory) {
                    Icon(Icons.Rounded.History, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("History")
                }
                Spacer(Modifier.width(4.dp))
                if (feeInfo.status != FeeStatus.PAID) {
                    FilledTonalButton(
                        onClick = onLogPayment,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Pay")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentFormSheet(
    feeInfo: StudentFeeInfo,
    allStudents: List<StudentFeeInfo>,
    selectedMonth: Int,
    selectedYear: Int,
    onDismiss: () -> Unit,
    onSave: (Long, Double, Long, Int, Int, String) -> Unit
) {
    var selectedStudentId by remember { mutableStateOf(feeInfo.student.id) }
    val selectedStudent = allStudents.find { it.student.id == selectedStudentId } ?: feeInfo
    var amountText by remember { mutableStateOf(selectedStudent.remaining.toInt().toString()) }
    var note by remember { mutableStateOf("") }
    var targetMonth by remember { mutableIntStateOf(selectedMonth) }
    var targetYear by remember { mutableIntStateOf(selectedYear) }
    var showStudentDropdown by remember { mutableStateOf(false) }

    // Update amount when student changes
    LaunchedEffect(selectedStudentId) {
        val student = allStudents.find { it.student.id == selectedStudentId }
        if (student != null) {
            amountText = student.remaining.toInt().toString()
        }
    }

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
                text = "Log Payment",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Student selector
            ExposedDropdownMenuBox(
                expanded = showStudentDropdown,
                onExpandedChange = { showStudentDropdown = it }
            ) {
                OutlinedTextField(
                    value = selectedStudent.student.fullName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Student") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showStudentDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = showStudentDropdown,
                    onDismissRequest = { showStudentDropdown = false }
                ) {
                    allStudents.forEach { info ->
                        DropdownMenuItem(
                            text = { Text(info.student.fullName) },
                            onClick = {
                                selectedStudentId = info.student.id
                                showStudentDropdown = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount (₹)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Month dropdown
                var showMonthDropdown by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = showMonthDropdown,
                    onExpandedChange = { showMonthDropdown = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = monthNames[targetMonth - 1],
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Month") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showMonthDropdown) },
                        modifier = Modifier.menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = showMonthDropdown,
                        onDismissRequest = { showMonthDropdown = false }
                    ) {
                        monthNames.forEachIndexed { index, name ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    targetMonth = index + 1
                                    showMonthDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = targetYear.toString(),
                    onValueChange = { targetYear = it.toIntOrNull() ?: targetYear },
                    label = { Text("Year") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onSave(selectedStudentId, amount, System.currentTimeMillis(), targetMonth, targetYear, note)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Success),
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Record Payment", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentHistorySheet(
    feeInfo: StudentFeeInfo,
    viewModel: FeeViewModel,
    onDismiss: () -> Unit
) {
    val payments by viewModel.getPaymentsByStudent(feeInfo.student.id).collectAsState(initial = emptyList())
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

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
                text = "Payment History",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = feeInfo.student.fullName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (payments.isEmpty()) {
                Text(
                    text = "No payments recorded yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Neutral,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                payments.forEach { payment ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.deletePayment(payment.id)
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
                                    .background(color, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(Icons.Default.Delete, "Delete", tint = Color.White, modifier = Modifier.padding(end = 16.dp))
                            }
                        },
                        enableDismissFromStartToEnd = false
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${monthNames[payment.targetMonth - 1]} ${payment.targetYear}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = dateFormat.format(Date(payment.datePaid)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (payment.note.isNotBlank()) {
                                        Text(
                                            text = payment.note,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Neutral
                                        )
                                    }
                                }
                                Text(
                                    text = currencyFormat.format(payment.amount),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Success,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
