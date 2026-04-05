package com.example.tutorflow.data.repository

import com.example.tutorflow.data.dao.StudentDao
import com.example.tutorflow.data.entity.Student
import kotlinx.coroutines.flow.Flow

class StudentRepository(private val studentDao: StudentDao) {
    val allStudents: Flow<List<Student>> = studentDao.getAllStudents()
    val studentCount: Flow<Int> = studentDao.getStudentCount()

    fun getStudentsByBatch(batchId: Long): Flow<List<Student>> = studentDao.getStudentsByBatch(batchId)
    fun getStudentCountByBatch(batchId: Long): Flow<Int> = studentDao.getStudentCountByBatch(batchId)
    fun searchStudents(query: String): Flow<List<Student>> = studentDao.searchStudents(query)

    suspend fun getStudentById(id: Long): Student? = studentDao.getStudentById(id)
    suspend fun insert(student: Student): Long = studentDao.insert(student)
    suspend fun update(student: Student) = studentDao.update(student)
    suspend fun delete(student: Student) = studentDao.delete(student)
}
