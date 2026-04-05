package com.example.tutorflow.viewmodel

import android.app.Application
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutorflow.TutorFlowApp
import com.example.tutorflow.data.entity.Batch
import com.example.tutorflow.data.entity.Payment
import com.example.tutorflow.data.entity.Student
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.Calendar

enum class MonthStatus { PAID, PARTIAL, PENDING, NO_DATA }

data class StudentYearReport(
    val student: Student,
    val batchName: String,
    val monthStatuses: Map<Int, MonthStatus> // month 1-12 -> status
)

data class ReportUiState(
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val selectedBatchId: Long? = null, // null = all batches
    val batches: List<Batch> = emptyList(),
    val reports: List<StudentYearReport> = emptyList(),
    val exportMessage: String? = null
)

class ReportViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as TutorFlowApp
    private val studentRepo = app.studentRepository
    private val paymentRepo = app.paymentRepository
    private val batchRepo = app.batchRepository

    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    private val _selectedBatchId = MutableStateFlow<Long?>(null)
    private val _exportMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ReportUiState> = combine(
        _selectedYear,
        _selectedBatchId,
        studentRepo.allStudents,
        batchRepo.allBatches
    ) { year, batchId, students, batches ->
        Triple(Triple(year, batchId, batches), students, batches)
    }.flatMapLatest { (triple, students, _) ->
        val (year, batchId, batches) = triple
        val filteredStudents = if (batchId != null) students.filter { it.batchId == batchId } else students
        paymentRepo.getPaymentsForYear(year).combine(_exportMessage) { payments, msg ->
            val reports = filteredStudents.map { student ->
                val batch = batches.find { it.id == student.batchId }
                val monthStatuses = (1..12).associateWith { month ->
                    val monthPayments = payments.filter { it.studentId == student.id && it.targetMonth == month }
                    val totalPaid = monthPayments.sumOf { it.amount }
                    when {
                        totalPaid >= student.monthlyFee -> MonthStatus.PAID
                        totalPaid > 0 -> MonthStatus.PARTIAL
                        else -> MonthStatus.PENDING
                    }
                }
                StudentYearReport(student, batch?.name ?: "Unknown", monthStatuses)
            }
            ReportUiState(
                selectedYear = year,
                selectedBatchId = batchId,
                batches = batches,
                reports = reports,
                exportMessage = msg
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportUiState())

    fun setYear(year: Int) { _selectedYear.value = year }
    fun setBatchFilter(batchId: Long?) { _selectedBatchId.value = batchId }
    fun clearExportMessage() { _exportMessage.value = null }

    fun exportToCsv() {
        viewModelScope.launch {
            try {
                val state = uiState.value
                val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
                val sb = StringBuilder()
                sb.appendLine("Student,Batch,Monthly Fee,${months.joinToString(",")}")

                for (report in state.reports) {
                    val statuses = (1..12).joinToString(",") { month ->
                        when (report.monthStatuses[month]) {
                            MonthStatus.PAID -> "Paid"
                            MonthStatus.PARTIAL -> "Partial"
                            MonthStatus.PENDING -> "Pending"
                            else -> "N/A"
                        }
                    }
                    sb.appendLine("\"${report.student.fullName}\",\"${report.batchName}\",${report.student.monthlyFee},$statuses")
                }

                val fileName = "TutorFlow_Report_${state.selectedYear}.csv"
                val context = getApplication<TutorFlowApp>()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                        put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                    val uri = context.contentResolver.insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues
                    )
                    uri?.let {
                        context.contentResolver.openOutputStream(it)?.use { os ->
                            os.write(sb.toString().toByteArray())
                        }
                    }
                } else {
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val file = File(downloadsDir, fileName)
                    FileOutputStream(file).use { it.write(sb.toString().toByteArray()) }
                }

                _exportMessage.value = "Report exported to Downloads/$fileName"
            } catch (e: Exception) {
                _exportMessage.value = "Export failed: ${e.message}"
            }
        }
    }
}
