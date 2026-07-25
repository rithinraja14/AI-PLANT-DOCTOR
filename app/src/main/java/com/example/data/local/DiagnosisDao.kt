package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.DiagnosisResult
import kotlinx.coroutines.flow.Flow

@Dao
interface DiagnosisDao {
    @Query("SELECT * FROM diagnoses ORDER BY timestamp DESC")
    fun getAllDiagnoses(): Flow<List<DiagnosisResult>>

    @Query("SELECT * FROM diagnoses WHERE id = :id LIMIT 1")
    suspend fun getDiagnosisById(id: Long): DiagnosisResult?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiagnosis(diagnosis: DiagnosisResult): Long

    @Delete
    suspend fun deleteDiagnosis(diagnosis: DiagnosisResult)

    @Query("DELETE FROM diagnoses WHERE id = :id")
    suspend fun deleteDiagnosisById(id: Long)
}
