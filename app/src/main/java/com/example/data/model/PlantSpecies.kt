package com.example.data.model

data class PlantSpecies(
    val id: String,
    val name: String,
    val scientificName: String,
    val category: String, // Crop, Fruit, Ornamental, Medicinal, Indoor
    val description: String,
    val careGuide: String,
    val commonDiseases: List<String>,
    val commonPests: List<String>,
    val idealSoil: String,
    val idealTemperature: String,
    val idealHumidity: String,
    val waterRequirement: String,
    val sunlightRequirement: String,
    val iconResName: String = "ic_plant_placeholder"
)
