package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diagnoses")
data class DiagnosisResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plantName: String,
    val scientificName: String,
    val confidence: Int, // e.g. 96 (%)
    val diseaseName: String, // e.g. "Early Blight" or "Healthy Leaf"
    val isHealthy: Boolean = false,
    val diseaseSeverity: String, // Low, Moderate, High, Severe
    val healthScore: Int, // 0 - 100
    val riskLevel: String, // Low, Medium, High
    val nutrientStatus: String, // Optimal, Nitrogen Deficiency, etc.
    val waterStatus: String, // Under-watered, Optimal, Over-watered
    val sunlightRequirement: String, // Full Sun, Partial Shade, etc.
    val humidityRequirement: String, // 50-70%
    val temperatureRequirement: String, // 20-30°C
    val growthStage: String, // Vegetative, Flowering, Fruiting
    val expectedRecoveryTime: String, // 7-14 Days
    val organicFertilizer: String,
    val chemicalFertilizer: String,
    val organicPesticide: String,
    val chemicalPesticide: String,
    val pruningRecommendation: String,
    val recoveryTimeline: String,
    val preventiveMeasures: String,
    val dailyCareRoutine: String,
    val weeklyCareRoutine: String,
    val monthlyMaintenance: String,
    val imagePath: String? = null,
    val dateFormatted: String,
    val timestamp: Long = System.currentTimeMillis(),
    val location: String = "Garden Plot #1"
)
