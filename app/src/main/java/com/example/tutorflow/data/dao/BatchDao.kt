package com.example.tutorflow.data.dao

import androidx.room.*
import com.example.tutorflow.data.entity.Batch
import kotlinx.coroutines.flow.Flow

@Dao
interface BatchDao {
    @Query("SELECT * FROM batches ORDER BY name ASC")
    fun getAllBatches(): Flow<List<Batch>>

    @Query("SELECT * FROM batches WHERE id = :id")
    suspend fun getBatchById(id: Long): Batch?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(batch: Batch): Long

    @Update
    suspend fun update(batch: Batch)

    @Delete
    suspend fun delete(batch: Batch)

    @Query("SELECT COUNT(*) FROM batches")
    fun getBatchCount(): Flow<Int>
}
