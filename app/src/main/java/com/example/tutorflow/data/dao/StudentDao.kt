package com.example.tutorflow.data.dao

import androidx.room.*
import com.example.tutorflow.data.entity.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY fullName ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE batchId = :batchId ORDER BY fullName ASC")
    fun getStudentsByBatch(batchId: Long): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentById(id: Long): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(student: Student): Long

    @Update
    suspend fun update(student: Student)

    @Delete
    suspend fun delete(student: Student)

    @Query("SELECT COUNT(*) FROM students")
    fun getStudentCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM students WHERE batchId = :batchId")
    fun getStudentCountByBatch(batchId: Long): Flow<Int>

    @Query("SELECT * FROM students WHERE fullName LIKE '%' || :query || '%'")
    fun searchStudents(query: String): Flow<List<Student>>
}
