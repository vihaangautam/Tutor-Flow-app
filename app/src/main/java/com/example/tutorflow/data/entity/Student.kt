package com.example.tutorflow.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "students",
    foreignKeys = [
        ForeignKey(
            entity = Batch::class,
            parentColumns = ["id"],
            childColumns = ["batchId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("batchId")]
)
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchId: Long,
    val fullName: String,
    val phone: String,
    val monthlyFee: Double
)
