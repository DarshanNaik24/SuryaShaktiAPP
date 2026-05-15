package com.suryashakti.solarmonitor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.suryashakti.solarmonitor.data.EnergyLog
import com.suryashakti.solarmonitor.ui.theme.DarkBackground
import com.suryashakti.solarmonitor.ui.theme.SolarYellow
import com.suryashakti.solarmonitor.ui.theme.SurfaceDark
import com.suryashakti.solarmonitor.viewmodel.SolarViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogEnergyScreen(viewModel: SolarViewModel = hiltViewModel()) {
    val logs by viewModel.logs.collectAsState()
    val selectedPlant by viewModel.selectedPlant.collectAsState()
    val scope = rememberCoroutineScope()
    
    var selectedDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var genKwh by remember { mutableStateOf("") }
    var meterReading by remember { mutableStateOf("") }
    var weather by remember { mutableStateOf("Fetching...") }
    var showDatePicker by remember { mutableStateOf(false) }

    // Pre-fill fields when date or selected plant changes
    LaunchedEffect(selectedDate, selectedPlant) {
        if (selectedPlant == null) {
            genKwh = ""
            meterReading = ""
            weather = "Select Plant"
            return@LaunchedEffect
        }
        
        val existingLog = viewModel.getLogForDate(selectedDate)
        if (existingLog != null) {
            genKwh = existingLog.generationKwh.toString()
            meterReading = existingLog.consumptionKwh.toString()
            weather = existingLog.weatherCondition
        } else {
            genKwh = ""
            meterReading = ""
            weather = "Fetching..."
            // Auto-fetch weather
            scope.launch {
                weather = viewModel.fetchWeather(selectedPlant!!.location)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        item {
            Text("Log Energy Data", style = MaterialTheme.typography.headlineMedium, color = SolarYellow)
            
            selectedPlant?.let {
                Text(
                    text = "Active Plant: ${it.name} (${it.location})",
                    color = SolarYellow,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } ?: run {
                Text(
                    text = "Please select or add a plant on the Home screen first",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Date Picker Field
            OutlinedTextField(
                value = selectedDate,
                onValueChange = { },
                label = { Text("Select Date") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = SolarYellow)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SolarYellow,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = genKwh,
                onValueChange = { genKwh = it },
                label = { Text("Generated (kWh)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SolarYellow,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = meterReading,
                onValueChange = { meterReading = it },
                label = { Text("Consumption (kWh)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SolarYellow,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Cloud, contentDescription = null, tint = SolarYellow)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Weather Condition", color = Color.Gray, fontSize = 12.sp)
                        Text(weather, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (weather == "Fetching...") {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = SolarYellow, strokeWidth = 2.dp)
                    } else {
                        IconButton(onClick = {
                            scope.launch {
                                weather = "Fetching..."
                                weather = viewModel.fetchWeather(selectedPlant?.location ?: "")
                            }
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Manual Refresh", tint = SolarYellow, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val gen = genKwh.toDoubleOrNull() ?: 0.0
                    val con = meterReading.toDoubleOrNull() ?: 0.0
                    val net = gen - con
                    
                    val log = EnergyLog(
                        date = selectedDate,
                        generationKwh = gen,
                        consumptionKwh = con,
                        netEnergy = net,
                        exportedKwh = if (net > 0) net else 0.0,
                        gridImportKwh = if (net < 0) -net else 0.0,
                        weatherCondition = weather,
                        independenceScore = if (con > 0) minOf(((gen / con) * 100).toInt(), 100) else 100,
                        costSaved = 0.0,
                        exportCredit = 0.0
                    )
                    viewModel.saveLog(log)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SolarYellow),
                enabled = selectedPlant != null && weather != "Fetching..."
            ) {
                val isUpdate = logs.any { it.date == selectedDate }
                Text(
                    text = if (selectedPlant == null) "Select a Plant first"
                           else if (isUpdate) "Update Entry"
                           else "Save Entry",
                    color = DarkBackground
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Text("Recent Logs (Tap to Edit)", style = MaterialTheme.typography.titleMedium, color = SolarYellow)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(logs) { log ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        selectedDate = log.date
                    },
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(log.date, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Gen: ${log.generationKwh} kWh | Cons: ${log.consumptionKwh} kWh | ${log.weatherCondition}", fontSize = 12.sp, color = Color.Gray)
                    }
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = SolarYellow, modifier = Modifier.size(20.dp))
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    return utcTimeMillis <= calendar.timeInMillis
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        cal.timeInMillis = it
                        selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
