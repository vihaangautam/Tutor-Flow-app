package com.example.tutorflow.data.dao

import androidx.room.*
import com.example.tutorflow.data.entity.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE studentId = :studentId AND targetMonth = :month AND targetYear = :year")
    fun getPaymentsForStudentMonth(studentId: Long, month: Int, year: Int): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE targetMonth = :month AND targetYear = :year")
    fun getPaymentsForMonth(month: Int, year: Int): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE studentId = :studentId ORDER BY datePaid DESC")
    fun getPaymentsByStudent(studentId: Long): Flow<List<Payment>>

    @Query("SELECT * FROM payments ORDER BY datePaid DESC LIMIT :limit")
    fun getRecentPayments(limit: Int = 10): Flow<List<Payment>>

    @Query("SELECT SUM(amount) FROM payments WHERE targetMonth = :month AND targetYear = :year")
    fun getTotalCollectedForMonth(month: Int, year: Int): Flow<Double?>

    @Query("SELECT SUM(amount) FROM payments WHERE studentId = :studentId AND targetMonth = :month AND targetYear = :year")
    fun getTotalPaidByStudentForMonth(studentId: Long, month: Int, year: Int): Flow<Double?>

    @Query("SELECT * FROM payments WHERE targetYear = :year")
    fun getPaymentsForYear(year: Int): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: Payment): Long

    @Delete
    suspend fun delete(payment: Payment)

    @Query("DELETE FROM payments WHERE id = :paymentId")
    suspend fun deleteById(paymentId: Long)
}
