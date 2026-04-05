package com.example.tutorflow.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutorflow.TutorFlowApp
import com.example.tutorflow.data.entity.Batch
import com.example.tutorflow.data.entity.Payment
import com.example.tutorflow.data.entity.Student
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class FeeStatus { PAID, PARTIAL, PENDING }

data class StudentFeeInfo(
    val student: Student,
    val batchName: String,
    val totalPaid: Double,
    val remaining: Double,
    val status: FeeStatus
)

data class FeeUiState(
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val studentFees: List<StudentFeeInfo> = emptyList(),
    val searchQuery: String = "",
    val filteredStudentFees: List<StudentFeeInfo> = emptyList()
)

class FeeViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as TutorFlowApp
    private val studentRepo = app.studentRepository
    private val paymentRepo = app.paymentRepository
    private val batchRepo = app.batchRepository

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH) + 1)
    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    private val _searchQuery = MutableStateFlow("")

    val selectedMonth: StateFlow<Int> = _selectedMonth
    val selectedYear: StateFlow<Int> = _selectedYear

    val uiState: StateFlow<FeeUiState> = combine(
        _selectedMonth,
        _selectedYear,
        _searchQuery,
        studentRepo.allStudents,
        batchRepo.allBatches
    ) { month, year, query, students, batches ->
        Triple(Triple(month, year, query), students, batches)
    }.flatMapLatest { (triple, students, batches) ->
        val (month, year, query) = triple
        val paymentsFlow = paymentRepo.getPaymentsForMonth(month, year)
        paymentsFlow.map { payments ->
            val studentFees = students.map { student ->
                val batch = batches.find { it.id == student.batchId }
                val totalPaid = payments
                    .filter { it.studentId == student.id }
                    .sumOf { it.amount }
                val remaining = (student.monthlyFee - totalPaid).coerceAtLeast(0.0)
                val status = when {
                    totalPaid >= student.monthlyFee -> FeeStatus.PAID
                    totalPaid > 0 -> FeeStatus.PARTIAL
                    else -> FeeStatus.PENDING
                }
                StudentFeeInfo(student, batch?.name ?: "Unknown", totalPaid, remaining, status)
            }
            val filtered = if (query.isBlank()) studentFees
            else studentFees.filter {
                it.student.fullName.contains(query, ignoreCase = true) ||
                        it.batchName.contains(query, ignoreCase = true)
            }
            FeeUiState(
                selectedMonth = month,
                selectedYear = year,
                studentFees = studentFees,
                searchQuery = query,
                filteredStudentFees = filtered
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FeeUiState())

    fun setMonth(month: Int) { _selectedMonth.value = month }
    fun setYear(year: Int) { _selectedYear.value = year }
    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun logPayment(studentId: Long, amount: Double, datePaid: Long, targetMonth: Int, targetYear: Int, note: String) {
        viewModelScope.launch {
            paymentRepo.insert(
                Payment(
                    studentId = studentId,
                    amount = amount,
                    datePaid = datePaid,
                    targetMonth = targetMonth,
                    targetYear = targetYear,
                    note = note
                )
            )
        }
    }

    fun getPaymentsByStudent(studentId: Long): Flow<List<Payment>> =
        paymentRepo.getPaymentsByStudent(studentId)

    fun deletePayment(paymentId: Long) {
        viewModelScope.launch {
            paymentRepo.deleteById(paymentId)
        }
    }
}
