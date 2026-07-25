package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PlantDiaryEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDiaryDao {
    @Query("SELECT * FROM plant_diary ORDER BY timestamp DESC")
    fun getAllDiaryEntries(): Flow<List<PlantDiaryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: PlantDiaryEntry): Long

    @Delete
    suspend fun deleteEntry(entry: PlantDiaryEntry)
}
