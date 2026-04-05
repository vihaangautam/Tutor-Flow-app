package com.example.tutorflow.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.tutorflow.data.dao.BatchDao
import com.example.tutorflow.data.dao.PaymentDao
import com.example.tutorflow.data.dao.StudentDao
import com.example.tutorflow.data.entity.Batch
import com.example.tutorflow.data.entity.Payment
import com.example.tutorflow.data.entity.Student

@Database(
    entities = [Batch::class, Student::class, Payment::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun batchDao(): BatchDao
    abstract fun studentDao(): StudentDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tutorflow_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
