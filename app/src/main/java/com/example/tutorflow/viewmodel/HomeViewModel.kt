package com.example.tutorflow.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutorflow.TutorFlowApp
import com.example.tutorflow.data.entity.Payment
import com.example.tutorflow.data.entity.Student
import kotlinx.coroutines.flow.*
import java.util.Calendar

data class RecentPaymentItem(
    val payment: Payment,
    val studentName: String
)

data class HomeUiState(
    val totalExpected: Double = 0.0,
    val totalCollected: Double = 0.0,
    val totalPending: Double = 0.0,
    val activeStudents: Int = 0,
    val paidCount: Int = 0,
    val pendingCount: Int = 0,
    val recentPayments: List<RecentPaymentItem> = emptyList()
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as TutorFlowApp
    private val studentRepo = app.studentRepository
    private val paymentRepo = app.paymentRepository

    private val calendar = Calendar.getInstance()
    private val currentMonth = calendar.get(Calendar.MONTH) + 1
    private val currentYear = calendar.get(Calendar.YEAR)

    val uiState: StateFlow<HomeUiState> = combine(
        studentRepo.allStudents,
        paymentRepo.getPaymentsForMonth(currentMonth, currentYear),
        paymentRepo.getRecentPayments(10)
    ) { students, monthPayments, recentPayments ->
        val totalExpected = students.sumOf { it.monthlyFee }
        val totalCollected = monthPayments.sumOf { it.amount }

        // Calculate paid/pending counts
        var paidCount = 0
        var pendingCount = 0
        for (student in students) {
            val studentPaid = monthPayments
                .filter { it.studentId == student.id }
                .sumOf { it.amount }
            if (studentPaid >= student.monthlyFee) paidCount++
            else pendingCount++
        }

        val recentItems = recentPayments.map { payment ->
            val student = students.find { it.id == payment.studentId }
            RecentPaymentItem(payment, student?.fullName ?: "Unknown")
        }

        HomeUiState(
            totalExpected = totalExpected,
            totalCollected = totalCollected,
            totalPending = (totalExpected - totalCollected).coerceAtLeast(0.0),
            activeStudents = students.size,
            paidCount = paidCount,
            pendingCount = pendingCount,
            recentPayments = recentItems
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())
}
