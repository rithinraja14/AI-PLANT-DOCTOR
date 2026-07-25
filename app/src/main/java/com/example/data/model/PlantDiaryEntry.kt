package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plant_diary")
data class PlantDiaryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plantName: String,
    val dateFormatted: String,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String,
    val healthRating: Int, // 1 to 5 stars or percentage 0-100
    val heightCm: Float? = null,
    val imagePath: String? = null,
    val isBeforeAfterPair: Boolean = false,
    val comparisonTag: String? = null // e.g., "Day 1 vs Day 14"
)
