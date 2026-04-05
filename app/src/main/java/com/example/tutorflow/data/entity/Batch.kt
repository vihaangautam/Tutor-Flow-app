package com.example.tutorflow.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BatchType {
    COACHING, HOME
}

@Entity(tableName = "batches")
data class Batch(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: BatchType,
    val location: String = "",
    val timing: String = ""
)
