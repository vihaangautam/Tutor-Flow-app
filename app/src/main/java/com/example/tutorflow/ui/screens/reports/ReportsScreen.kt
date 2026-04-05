package com.example.tutorflow.ui.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tutorflow.ui.components.EmptyStateMessage
import com.example.tutorflow.ui.components.StudentAvatar
import com.example.tutorflow.ui.theme.*
import com.example.tutorflow.viewmodel.MonthStatus
import com.example.tutorflow.viewmodel.ReportViewModel
import com.example.tutorflow.viewmodel.StudentYearReport

private val monthHeaders = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: ReportViewModel) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.exportMessage) {
        state.exportMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearExportMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Text(
                    text = "Reports",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, top = 48.dp, bottom = 4.dp)
                )
                Text(
                    text = "Annual fee status matrix",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 20.dp, bottom = 16.dp)
                )
            }

            // Controls: Year selector + Batch filter + Export
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Year selector
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.setYear(state.selectedYear - 1) }) {
                            Icon(Icons.Rounded.ChevronLeft, "Previous")
                        }
                        Text(
                            text = state.selectedYear.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { viewModel.setYear(state.selectedYear + 1) }) {
                            Icon(Icons.Rounded.ChevronRight, "Next")
                        }
                    }

                    // Export button
                    FilledTonalButton(
                        onClick = { viewModel.exportToCsv() },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Rounded.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Export CSV")
                    }
                }
            }

            // Batch filter
            item {
                var showDropdown by remember { mutableStateOf(false) }
                val selectedBatch = state.batches.find { it.id == state.selectedBatchId }

                ExposedDropdownMenuBox(
                    expanded = showDropdown,
                    onExpandedChange = { showDropdown = it },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    OutlinedTextField(
                        value = selectedBatch?.name ?: "All Batches",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Filter by Batch") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Batches") },
                            onClick = {
                                viewModel.setBatchFilter(null)
                                showDropdown = false
                            }
                        )
                        state.batches.forEach { batch ->
                            DropdownMenuItem(
                                text = { Text(batch.name) },
                                onClick = {
                                    viewModel.setBatchFilter(batch.id)
                                    showDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Legend
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LegendItem("Paid", Success)
                    LegendItem("Partial", Warning)
                    LegendItem("Pending", Danger)
                    LegendItem("N/A", Neutral)
                }
            }

            if (state.reports.isEmpty()) {
                item {
                    EmptyStateMessage("No data for this view.\nAdd students and batches to see reports.")
                }
            }

            // Matrix Table
            item {
                if (state.reports.isNotEmpty()) {
                    MatrixTable(state.reports)
                }
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MatrixTable(reports: List<StudentYearReport>) {
    val horizontalScrollState = rememberScrollState()

    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        // Header row (sticky student name column + scrollable months)
        Row(modifier = Modifier.fillMaxWidth()) {
            // Fixed student column header
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .padding(8.dp)
            ) {
                Text(
                    text = "Student",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Scrollable month headers
            Row(
                modifier = Modifier.horizontalScroll(horizontalScrollState)
            ) {
                monthHeaders.forEach { month ->
                    Box(
                        modifier = Modifier
                            .width(52.dp)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = month,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

        // Data rows
        reports.forEach { report ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Fixed student column
                Row(
                    modifier = Modifier
                        .width(120.dp)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StudentAvatar(name = report.student.fullName, size = 24)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = report.student.fullName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Scrollable status dots
                Row(
                    modifier = Modifier.horizontalScroll(horizontalScrollState)
                ) {
                    (1..12).forEach { month ->
                        val status = report.monthStatuses[month] ?: MonthStatus.NO_DATA
                        val color = when (status) {
                            MonthStatus.PAID -> Success
                            MonthStatus.PARTIAL -> Warning
                            MonthStatus.PENDING -> Danger
                            MonthStatus.NO_DATA -> Neutral.copy(alpha = 0.3f)
                        }
                        Box(
                            modifier = Modifier.width(52.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
