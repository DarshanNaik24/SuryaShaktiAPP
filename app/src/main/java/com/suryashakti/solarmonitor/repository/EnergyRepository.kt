package com.suryashakti.solarmonitor.repository

import android.util.Log
import com.suryashakti.solarmonitor.data.EnergyLog
import com.suryashakti.solarmonitor.data.EnergyLogDao
import com.suryashakti.solarmonitor.data.SolarPlant
import com.suryashakti.solarmonitor.data.SolarPlantDao
import com.suryashakti.solarmonitor.network.Content
import com.suryashakti.solarmonitor.network.GeminiApiService
import com.suryashakti.solarmonitor.network.GeminiRequest
import com.suryashakti.solarmonitor.network.Part
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

class EnergyRepository @Inject constructor(
    private val logDao: EnergyLogDao,
    private val plantDao: SolarPlantDao,
    private val api: GeminiApiService
) {
    fun getAllLogs(userId: String, plantId: Long): Flow<List<EnergyLog>> = logDao.getAllLogs(userId, plantId)

    suspend fun insertLog(log: EnergyLog) = logDao.insertLog(log)
    
    suspend fun updateLog(log: EnergyLog) = logDao.updateLog(log)

    suspend fun deleteLog(log: EnergyLog) = logDao.deleteLog(log)

    suspend fun getLogByDate(userId: String, plantId: Long, date: String) = logDao.getLogByDate(userId, plantId, date)

    fun getPlantsByUserId(userId: String): Flow<List<SolarPlant>> = plantDao.getPlantsByUserId(userId)

    suspend fun insertPlant(plant: SolarPlant) = plantDao.insertPlant(plant)

    suspend fun updatePlant(plant: SolarPlant) = plantDao.updatePlant(plant)

    suspend fun deletePlant(plant: SolarPlant) = plantDao.deletePlant(plant)

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss (EEEE)", Locale.getDefault())
        return sdf.format(Date())
    }

    suspend fun getAdvisorSuggestion(historyText: String, location: String, weather: String): String {
        return try {
            val seed = Random.nextInt(10000)
            val timestamp = getCurrentTimestamp()
            val prompt = """
                You are a professional solar energy advisor. 
                Location: $location. 
                Current Weather: $weather.
                Local Time: $timestamp.
                Energy History: $historyText
                Request ID: $seed
                
                Provide one short, unique, and actionable suggestion based on this data and current weather conditions. 
                If it's Night, focus on reduction or battery prep. If it's Sunny, focus on high-load tasks.
                Be specific. Do not use asterisks. Max 20 words.
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt))))
            )

            val response = api.generateContent(request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            if (!text.isNullOrBlank()) {
                text.replace("*", "").trim()
            } else {
                "Shift high-energy tasks to solar peak hours in $location."
            }
        } catch (e: Exception) {
            Log.e("EnergyRepository", "Gemini API Error: ${e.message}", e)
            "Optimizing your energy independence in $location: Check your usage patterns."
        }
    }

    suspend fun getAutoWeather(location: String): String {
        return try {
            val timestamp = getCurrentTimestamp()
            val prompt = "Location: $location, Local Time: $timestamp. What is the current weather? Respond with ONLY one word from: Sunny, Cloudy, Rainy, Partly Cloudy, Night, Cool. If it is night (after sunset), you MUST say Night."
            val request = GeminiRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt))))
            )
            val response = api.generateContent(request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim() ?: "Sunny"
            
            val valid = listOf("Sunny", "Cloudy", "Rainy", "Partly Cloudy", "Night", "Cool")
            val result = valid.find { text.contains(it, ignoreCase = true) } ?: "Sunny"
            
            // Safety check: override Sunny if it's late night
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            if ((hour >= 19 || hour < 6) && result == "Sunny") "Night" else result
        } catch (e: Exception) {
            "Sunny"
        }
    }

    suspend fun getLocationSuggestions(query: String): List<String> {
        if (query.length < 2) return emptyList()
        return try {
            val prompt = "Suggest 5 Indian cities starting with '$query'. Format each line as: 'City, District, State'. Respond with ONLY the list, one per line. No numbers or bullets."
            val request = GeminiRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt))))
            )
            val response = api.generateContent(request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            text?.lines()?.filter { it.isNotBlank() }?.map { it.replace(Regex("^\\d+\\.\\s*"), "").trim() } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
