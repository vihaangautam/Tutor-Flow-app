package com.example.tutorflow

import android.app.Application
import com.example.tutorflow.data.AppDatabase
import com.example.tutorflow.data.repository.BatchRepository
import com.example.tutorflow.data.repository.PaymentRepository
import com.example.tutorflow.data.repository.StudentRepository

class TutorFlowApp : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val batchRepository by lazy { BatchRepository(database.batchDao()) }
    val studentRepository by lazy { StudentRepository(database.studentDao()) }
    val paymentRepository by lazy { PaymentRepository(database.paymentDao()) }
}
