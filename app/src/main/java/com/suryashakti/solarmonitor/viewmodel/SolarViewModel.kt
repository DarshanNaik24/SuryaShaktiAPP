package com.suryashakti.solarmonitor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suryashakti.solarmonitor.data.EnergyLog
import com.suryashakti.solarmonitor.data.SettingsManager
import com.suryashakti.solarmonitor.data.SolarPlant
import com.suryashakti.solarmonitor.repository.AuthRepository
import com.suryashakti.solarmonitor.repository.EnergyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SolarViewModel @Inject constructor(
    private val repository: EnergyRepository,
    private val authRepository: AuthRepository,
    private val settingsManager: SettingsManager
) : ViewModel() {

    data class LifetimeSummary(
        val totalGen: Double = 0.0,
        val totalCons: Double = 0.0,
        val totalSavings: Double = 0.0,
        val totalExport: Double = 0.0,
        val avgIndependence: Int = 0
    )

    val currentUserId: StateFlow<String?> = authRepository.currentUserId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val unitRate = settingsManager.unitRate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 8.0)
    
    val exportRate = settingsManager.exportRate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 4.0)

    private val _plants = MutableStateFlow<List<SolarPlant>>(emptyList())
    val plants: StateFlow<List<SolarPlant>> = _plants.asStateFlow()

    private val _selectedPlant = MutableStateFlow<SolarPlant?>(null)
    val selectedPlant: StateFlow<SolarPlant?> = _selectedPlant.asStateFlow()

    private val _logs = MutableStateFlow<List<EnergyLog>>(emptyList())
    val logs: StateFlow<List<EnergyLog>> = _logs.asStateFlow()

    private val _locationSuggestions = MutableStateFlow<List<String>>(emptyList())
    val locationSuggestions: StateFlow<List<String>> = _locationSuggestions.asStateFlow()

    private val _currentWeather = MutableStateFlow("Clear")
    val currentWeather: StateFlow<String> = _currentWeather.asStateFlow()

    val lifetimeStats = _logs.map { list ->
        if (list.isEmpty()) LifetimeSummary()
        else {
            val totalGen = list.sumOf { it.generationKwh }
            val totalCons = list.sumOf { it.consumptionKwh }
            val totalSavings = list.sumOf { it.costSaved }
            val totalExport = list.sumOf { it.exportedKwh }
            val avgInd = if (list.isEmpty()) 0 else (list.sumOf { it.independenceScore.toLong() } / list.size).toInt()
            LifetimeSummary(totalGen, totalCons, totalSavings, totalExport, avgInd)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LifetimeSummary())

    val todayStats = _logs.map { list ->
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        list.find { it.date == today }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _advisorSuggestion = MutableStateFlow<String?>(null)
    val advisorSuggestion: StateFlow<String?> = _advisorSuggestion.asStateFlow()

    init {
        viewModelScope.launch {
            currentUserId.collectLatest { userId ->
                if (userId != null) {
                    repository.getPlantsByUserId(userId).collect { plantsList ->
                        _plants.value = plantsList
                        if (plantsList.isNotEmpty() && _selectedPlant.value == null) {
                            selectPlant(plantsList.first())
                        }
                    }
                } else {
                    _plants.value = emptyList()
                    _selectedPlant.value = null
                }
            }
        }

        viewModelScope.launch {
            combine(currentUserId, _selectedPlant) { userId, plant ->
                Pair(userId, plant)
            }.collectLatest { (userId, plant) ->
                if (userId != null && plant != null) {
                    repository.getAllLogs(userId, plant.id).collect { list ->
                        _logs.value = list
                    }
                } else {
                    _logs.value = emptyList()
                }
            }
        }
    }

    fun selectPlant(plant: SolarPlant) {
        _selectedPlant.value = plant
        refreshCurrentWeather()
    }

    fun addPlant(name: String, location: String, type: String) {
        viewModelScope.launch {
            val userId = currentUserId.value ?: return@launch
            val newPlant = SolarPlant(userId = userId, name = name, location = location, type = type)
            repository.insertPlant(newPlant)
            if (_plants.value.isEmpty()) {
                _selectedPlant.value = newPlant
            }
        }
    }

    fun saveLog(log: EnergyLog) {
        viewModelScope.launch {
            val userId = currentUserId.value ?: return@launch
            val plantId = _selectedPlant.value?.id ?: return@launch
            
            val uRate = unitRate.value
            val eRate = exportRate.value
            
            val finalLog = log.copy(
                userId = userId,
                plantId = plantId,
                costSaved = log.generationKwh * uRate,
                exportCredit = log.exportedKwh * eRate
            )
            
            val existing = repository.getLogByDate(userId, plantId, log.date)
            if (existing != null) {
                repository.updateLog(finalLog.copy(id = existing.id))
            } else {
                repository.insertLog(finalLog)
            }
        }
    }

    fun fetchAdvisorSuggestion() {
        viewModelScope.launch {
            val plant = _selectedPlant.value ?: return@launch
            _advisorSuggestion.value = "Analyzing data for ${plant.name}..."
            val history = _logs.value.take(5).joinToString { "${it.date}: G:${it.generationKwh} C:${it.consumptionKwh}" }
            val suggestion = repository.getAdvisorSuggestion(
                historyText = if(history.isEmpty()) "No history" else history,
                location = plant.location,
                weather = _currentWeather.value
            )
            _advisorSuggestion.value = suggestion
        }
    }

    fun refreshCurrentWeather() {
        viewModelScope.launch {
            val plant = _selectedPlant.value ?: return@launch
            _currentWeather.value = repository.getAutoWeather(plant.location)
            fetchAdvisorSuggestion()
        }
    }

    fun updateLocationQuery(query: String) {
        viewModelScope.launch {
            if (query.length >= 2) {
                _locationSuggestions.value = repository.getLocationSuggestions(query)
            } else {
                _locationSuggestions.value = emptyList()
            }
        }
    }

    fun saveRates(unit: Double, export: Double) {
        viewModelScope.launch {
            settingsManager.saveRates(unit, export)
        }
    }

    suspend fun getLogForDate(date: String): EnergyLog? {
        val userId = currentUserId.value ?: return null
        val plantId = _selectedPlant.value?.id ?: return null
        return repository.getLogByDate(userId, plantId, date)
    }

    suspend fun fetchWeather(location: String): String {
        return repository.getAutoWeather(location)
    }
}
