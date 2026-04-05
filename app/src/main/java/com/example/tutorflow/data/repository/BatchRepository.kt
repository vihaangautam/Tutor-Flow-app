package com.example.tutorflow.data.repository

import com.example.tutorflow.data.dao.BatchDao
import com.example.tutorflow.data.entity.Batch
import kotlinx.coroutines.flow.Flow

class BatchRepository(private val batchDao: BatchDao) {
    val allBatches: Flow<List<Batch>> = batchDao.getAllBatches()
    val batchCount: Flow<Int> = batchDao.getBatchCount()

    suspend fun getBatchById(id: Long): Batch? = batchDao.getBatchById(id)
    suspend fun insert(batch: Batch): Long = batchDao.insert(batch)
    suspend fun update(batch: Batch) = batchDao.update(batch)
    suspend fun delete(batch: Batch) = batchDao.delete(batch)
}
