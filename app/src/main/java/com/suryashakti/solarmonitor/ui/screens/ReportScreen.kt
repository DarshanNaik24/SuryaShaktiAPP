package com.suryashakti.solarmonitor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.suryashakti.solarmonitor.ui.theme.DarkBackground
import com.suryashakti.solarmonitor.ui.theme.SolarYellow
import com.suryashakti.solarmonitor.ui.theme.SurfaceDark
import com.suryashakti.solarmonitor.viewmodel.SolarViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(viewModel: SolarViewModel = hiltViewModel()) {
    val logs by viewModel.logs.collectAsState()
    val plants by viewModel.plants.collectAsState()
    val selectedPlant by viewModel.selectedPlant.collectAsState()
    val lifetimeData by viewModel.lifetimeStats.collectAsState()
    
    var selectedDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val selectedLog = selectedPlant?.let { plant ->
        logs.find { it.plantId == plant.id && it.date == selectedDate }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        item {
            Text("Energy Analytics", style = MaterialTheme.typography.headlineMedium, color = SolarYellow, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Plant Selector
        item {
            Text("Select Plant", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(plants) { plant ->
                    FilterChip(
                        selected = selectedPlant?.id == plant.id,
                        onClick = { viewModel.selectPlant(plant) },
                        label = { Text(plant.name) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SolarYellow,
                            selectedLabelColor = DarkBackground,
                            containerColor = SurfaceDark,
                            labelColor = Color.White
                        )
                    )
                }
            }
        }

        if (selectedPlant == null) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("Select a plant to view analytics", color = Color.Gray)
                }
            }
        } else {
            // New Lifetime Report Section
            item {
                Text("Lifetime Performance", color = SolarYellow, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Total Yield", color = Color.Gray, fontSize = 11.sp)
                                Text("${String.format(Locale.getDefault(), "%.1f", lifetimeData.totalGen)} kWh", color = SolarYellow, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Usage", color = Color.Gray, fontSize = 11.sp)
                                Text("${String.format(Locale.getDefault(), "%.1f", lifetimeData.totalCons)} kWh", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Total Exported", color = Color.Gray, fontSize = 11.sp)
                                Text("${String.format(Locale.getDefault(), "%.1f", lifetimeData.totalExport)} kWh", color = Color.Green, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Savings", color = Color.Gray, fontSize = 11.sp)
                                Text("₹${String.format(Locale.getDefault(), "%.0f", lifetimeData.totalSavings)}", color = Color.Cyan, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Reporting Period", color = Color.Gray, fontSize = 12.sp)
                            Text(selectedDate, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        IconButton(onClick = { showDatePicker = true }, modifier = Modifier.background(SolarYellow.copy(alpha = 0.1f), RoundedCornerShape(8.dp))) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date", tint = SolarYellow)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (selectedLog != null) {
                item {
                    Text("Daily Distribution", color = SolarYellow, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth().height(250.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            factory = { context ->
                                PieChart(context).apply {
                                    description.isEnabled = false
                                    setUsePercentValues(true)
                                    holeRadius = 60f
                                    setTransparentCircleAlpha(0)
                                    setHoleColor(android.graphics.Color.TRANSPARENT)
                                    legend.apply {
                                        isEnabled = true
                                        textColor = android.graphics.Color.WHITE
                                        horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
                                    }
                                    setEntryLabelColor(android.graphics.Color.WHITE)
                                }
                            },
                            update = { chart ->
                                val entries = listOf(
                                    PieEntry(selectedLog.generationKwh.toFloat(), "Generated"),
                                    PieEntry(selectedLog.consumptionKwh.toFloat(), "Consumed")
                                )
                                val dataSet = PieDataSet(entries, "").apply {
                                    colors = listOf(SolarYellow.toArgb(), android.graphics.Color.WHITE)
                                    valueTextColor = android.graphics.Color.BLACK
                                    valueTextSize = 12f
                                }
                                chart.data = PieData(dataSet)
                                chart.centerText = "Daily Ratio"
                                chart.setCenterTextColor(android.graphics.Color.WHITE)
                                chart.invalidate()
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            item {
                Text("Weekly Trend", color = SolarYellow, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                val last7Logs = logs.take(7).reversed()
                if (last7Logs.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(280.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            factory = { context ->
                                BarChart(context).apply {
                                    description.isEnabled = false
                                    setDrawGridBackground(false)
                                    xAxis.apply {
                                        position = XAxis.XAxisPosition.BOTTOM
                                        setDrawGridLines(false)
                                        textColor = android.graphics.Color.WHITE
                                        valueFormatter = object : ValueFormatter() {
                                            override fun getFormattedValue(value: Float): String {
                                                val index = value.toInt()
                                                return if (index >= 0 && index < last7Logs.size) {
                                                    last7Logs[index].date.split("-").last()
                                                } else ""
                                            }
                                        }
                                    }
                                    axisLeft.textColor = android.graphics.Color.WHITE
                                    axisRight.isEnabled = false
                                    legend.textColor = android.graphics.Color.WHITE
                                }
                            },
                            update = { chart ->
                                val entriesGen = last7Logs.mapIndexed { index, log -> BarEntry(index.toFloat(), log.generationKwh.toFloat()) }
                                val entriesCons = last7Logs.mapIndexed { index, log -> BarEntry(index.toFloat(), log.consumptionKwh.toFloat()) }

                                val dataSetGen = BarDataSet(entriesGen, "Gen").apply { color = SolarYellow.toArgb() }
                                val dataSetCons = BarDataSet(entriesCons, "Cons").apply { color = android.graphics.Color.WHITE }

                                chart.data = BarData(dataSetGen, dataSetCons).apply { barWidth = 0.3f }
                                chart.groupBars(-0.5f, 0.3f, 0.05f)
                                chart.invalidate()
                            }
                        )
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text("Log data for 7 days to see trends", color = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
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
