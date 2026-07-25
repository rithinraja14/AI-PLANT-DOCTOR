package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.DiagnosisResult
import com.example.data.model.PlantDiaryEntry
import com.example.data.model.PlantSpecies
import com.example.data.model.WeatherInfo
import com.example.data.repository.AiDiagnosisRepository
import com.example.data.repository.PlantLibraryRepository
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NavTab {
    HOME, CAMERA, HISTORY, LIBRARY, SETTINGS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val diagnosisDao = db.diagnosisDao()
    private val diaryDao = db.plantDiaryDao()
    private val aiRepository = AiDiagnosisRepository(application)

    init {
        viewModelScope.launch {
            // Check if diary entries need initial seeding for weekly health trends
            val existing = diaryDao.getAllDiaryEntries()
            viewModelScope.launch {
                existing.collect { list ->
                    if (list.isEmpty()) {
                        seedSampleDiaryEntries()
                    }
                }
            }
        }
    }

    private suspend fun seedSampleDiaryEntries() {
        val now = System.currentTimeMillis()
        val dayMs = 86400000L
        val sampleEntries = listOf(
            PlantDiaryEntry(
                plantName = "Monstera Deliciosa",
                dateFormatted = "6 days ago",
                timestamp = now - (6 * dayMs),
                notes = "Initial check: Lower leaves showed light stress. Watered thoroughly.",
                healthRating = 3,
                heightCm = 14.5f
            ),
            PlantDiaryEntry(
                plantName = "Monstera Deliciosa",
                dateFormatted = "4 days ago",
                timestamp = now - (4 * dayMs),
                notes = "New leaf shoot emerging! Added organic seaweed liquid fertilizer.",
                healthRating = 4,
                heightCm = 15.6f
            ),
            PlantDiaryEntry(
                plantName = "Monstera Deliciosa",
                dateFormatted = "2 days ago",
                timestamp = now - (2 * dayMs),
                notes = "Vibrant new leaf unfurled with deep green fenestrations.",
                healthRating = 5,
                heightCm = 16.8f
            ),
            PlantDiaryEntry(
                plantName = "Monstera Deliciosa",
                dateFormatted = "Today",
                timestamp = now,
                notes = "Plant thriving at peak vitality! Height reached 18.2 cm.",
                healthRating = 5,
                heightCm = 18.2f
            ),
            PlantDiaryEntry(
                plantName = "Fiddle Leaf Fig",
                dateFormatted = "5 days ago",
                timestamp = now - (5 * dayMs),
                notes = "Soil moisture balanced. Top growth node activated.",
                healthRating = 4,
                heightCm = 24.0f
            ),
            PlantDiaryEntry(
                plantName = "Fiddle Leaf Fig",
                dateFormatted = "1 day ago",
                timestamp = now - (1 * dayMs),
                notes = "Foliage expanding steadily under indirect sunlight.",
                healthRating = 5,
                heightCm = 25.5f
            )
        )
        sampleEntries.forEach { diaryDao.insertEntry(it) }
    }

    // Saved History & Diary
    val historyList: StateFlow<List<DiagnosisResult>> = diagnosisDao.getAllDiagnoses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val diaryEntries: StateFlow<List<PlantDiaryEntry>> = diaryDao.getAllDiaryEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation & Screen State
    private val _currentTab = MutableStateFlow(NavTab.HOME)
    val currentTab: StateFlow<NavTab> = _currentTab.asStateFlow()

    private val _selectedDiagnosis = MutableStateFlow<DiagnosisResult?>(null)
    val selectedDiagnosis: StateFlow<DiagnosisResult?> = _selectedDiagnosis.asStateFlow()

    private val _selectedLibraryPlant = MutableStateFlow<PlantSpecies?>(null)
    val selectedLibraryPlant: StateFlow<PlantSpecies?> = _selectedLibraryPlant.asStateFlow()

    // Analysis State
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analysisError = MutableStateFlow<String?>(null)
    val analysisError: StateFlow<String?> = _analysisError.asStateFlow()

    // Search Query in Library
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filteredPlants = MutableStateFlow(PlantLibraryRepository.plantList)
    val filteredPlants: StateFlow<List<PlantSpecies>> = _filteredPlants.asStateFlow()

    // Weather
    val weatherInfo = MutableStateFlow(WeatherInfo())

    // Settings
    val isDarkMode = MutableStateFlow(false)
    val selectedLanguage = MutableStateFlow("English")
    val notificationsEnabled = MutableStateFlow(true)
    val isCloudAiMode = MutableStateFlow(true)

    fun selectTab(tab: NavTab) {
        _currentTab.value = tab
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        _filteredPlants.value = PlantLibraryRepository.searchPlants(query)
    }

    fun selectDiagnosisDetail(diagnosis: DiagnosisResult?) {
        _selectedDiagnosis.value = diagnosis
    }

    fun selectLibraryPlant(plant: PlantSpecies?) {
        _selectedLibraryPlant.value = plant
    }

    fun analyzeImage(uri: Uri?, bitmap: Bitmap?, plantHint: String = "") {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _analysisError.value = null
            try {
                val result = aiRepository.analyzePlantImage(uri, bitmap, plantHint)
                val newId = diagnosisDao.insertDiagnosis(result)
                val savedResult = result.copy(id = newId)
                _selectedDiagnosis.value = savedResult
                _isAnalyzing.value = false
            } catch (e: Exception) {
                _isAnalyzing.value = false
                _analysisError.value = "Analysis error: ${e.localizedMessage}"
            }
        }
    }

    fun addDiaryEntry(plantName: String, notes: String, rating: Int, heightCm: Float? = null, imageUri: Uri? = null) {
        viewModelScope.launch {
            val entry = PlantDiaryEntry(
                plantName = plantName,
                dateFormatted = "Today",
                notes = notes,
                healthRating = rating,
                heightCm = heightCm,
                imagePath = imageUri?.toString()
            )
            diaryDao.insertEntry(entry)
        }
    }

    fun deleteDiagnosis(diagnosis: DiagnosisResult) {
        viewModelScope.launch {
            diagnosisDao.deleteDiagnosis(diagnosis)
            if (_selectedDiagnosis.value?.id == diagnosis.id) {
                _selectedDiagnosis.value = null
            }
        }
    }

    fun triggerWateringReminder() {
        if (notificationsEnabled.value) {
            NotificationHelper.sendCareReminder(
                getApplication(),
                "Plant Water Reminder",
                "Don't forget to check soil moisture for your plants today!"
            )
        }
    }
}
