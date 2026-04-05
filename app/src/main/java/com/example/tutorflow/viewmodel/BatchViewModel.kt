package com.example.tutorflow.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tutorflow.TutorFlowApp
import com.example.tutorflow.data.entity.Batch
import com.example.tutorflow.data.entity.BatchType
import com.example.tutorflow.data.entity.Student
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BatchWithCount(
    val batch: Batch,
    val studentCount: Int,
    val students: List<Student>
)

class BatchViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as TutorFlowApp
    private val batchRepo = app.batchRepository
    private val studentRepo = app.studentRepository

    val batches: StateFlow<List<BatchWithCount>> = combine(
        batchRepo.allBatches,
        studentRepo.allStudents
    ) { batches, students ->
        batches.map { batch ->
            val batchStudents = students.filter { it.batchId == batch.id }
            BatchWithCount(batch, batchStudents.size, batchStudents)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getStudentsByBatch(batchId: Long): Flow<List<Student>> =
        studentRepo.getStudentsByBatch(batchId)

    fun getBatchById(batchId: Long): Flow<Batch?> = flow {
        emit(batchRepo.getBatchById(batchId))
    }

    fun addBatch(name: String, type: BatchType, location: String, timing: String) {
        viewModelScope.launch {
            batchRepo.insert(Batch(name = name, type = type, location = location, timing = timing))
        }
    }

    fun updateBatch(batch: Batch) {
        viewModelScope.launch {
            batchRepo.update(batch)
        }
    }

    fun deleteBatch(batch: Batch) {
        viewModelScope.launch {
            batchRepo.delete(batch)
        }
    }

    fun addStudent(batchId: Long, fullName: String, phone: String, monthlyFee: Double) {
        viewModelScope.launch {
            studentRepo.insert(
                Student(batchId = batchId, fullName = fullName, phone = phone, monthlyFee = monthlyFee)
            )
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch {
            studentRepo.update(student)
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            studentRepo.delete(student)
        }
    }
}
