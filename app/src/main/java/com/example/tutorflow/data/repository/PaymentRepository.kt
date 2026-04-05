package com.example.tutorflow.data.repository

import com.example.tutorflow.data.dao.PaymentDao
import com.example.tutorflow.data.entity.Payment
import kotlinx.coroutines.flow.Flow

class PaymentRepository(private val paymentDao: PaymentDao) {
    fun getPaymentsForStudentMonth(studentId: Long, month: Int, year: Int): Flow<List<Payment>> =
        paymentDao.getPaymentsForStudentMonth(studentId, month, year)

    fun getPaymentsForMonth(month: Int, year: Int): Flow<List<Payment>> =
        paymentDao.getPaymentsForMonth(month, year)

    fun getPaymentsByStudent(studentId: Long): Flow<List<Payment>> =
        paymentDao.getPaymentsByStudent(studentId)

    fun getRecentPayments(limit: Int = 10): Flow<List<Payment>> =
        paymentDao.getRecentPayments(limit)

    fun getTotalCollectedForMonth(month: Int, year: Int): Flow<Double?> =
        paymentDao.getTotalCollectedForMonth(month, year)

    fun getTotalPaidByStudentForMonth(studentId: Long, month: Int, year: Int): Flow<Double?> =
        paymentDao.getTotalPaidByStudentForMonth(studentId, month, year)

    fun getPaymentsForYear(year: Int): Flow<List<Payment>> =
        paymentDao.getPaymentsForYear(year)

    suspend fun insert(payment: Payment): Long = paymentDao.insert(payment)
    suspend fun delete(payment: Payment) = paymentDao.delete(payment)
    suspend fun deleteById(paymentId: Long) = paymentDao.deleteById(paymentId)
}
