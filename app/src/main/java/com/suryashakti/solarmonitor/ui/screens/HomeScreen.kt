package com.suryashakti.solarmonitor.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suryashakti.solarmonitor.R
import com.suryashakti.solarmonitor.data.SolarPlant
import com.suryashakti.solarmonitor.ui.theme.DarkBackground
import com.suryashakti.solarmonitor.ui.theme.SolarYellow
import com.suryashakti.solarmonitor.ui.theme.SurfaceDark
import com.suryashakti.solarmonitor.viewmodel.SolarViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: SolarViewModel,
    onProfileClick: () -> Unit
) {
    val lifetimeStats by viewModel.lifetimeStats.collectAsState()
    val todayLog by viewModel.todayStats.collectAsState()
    val suggestion by viewModel.advisorSuggestion.collectAsState()
    val plants by viewModel.plants.collectAsState()
    val selectedPlant by viewModel.selectedPlant.collectAsState()

    LaunchedEffect(selectedPlant) {
        if (selectedPlant != null) {
            viewModel.fetchAdvisorSuggestion()
            viewModel.refreshCurrentWeather()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // App Icon at the top left beside the name
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground), // Fallback: User should place ic_app_logo here
                            contentDescription = "App Logo",
                            modifier = Modifier.fillMaxSize().padding(4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Surya-Shakti", 
                        fontSize = 26.sp, 
                        fontWeight = FontWeight.Black, 
                        color = SolarYellow,
                        letterSpacing = (-1).sp
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.fetchAdvisorSuggestion() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Advice", tint = SolarYellow)
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = SolarYellow)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Plant Selector
        item {
            Text(
                text = "Your Solar Assets", 
                color = Color.Gray, 
                fontSize = 12.sp, 
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(plants) { plant ->
                    val isSelected = selectedPlant?.id == plant.id
                    FilterChip(
                        selected = isSelected,
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
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (selectedPlant != null) {
            val currentPlant = selectedPlant!!
            
            // Location Info
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = SolarYellow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(currentPlant.location, color = Color.Gray, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Overall Savings Card (Lifetime)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B3320)) // Deep Green
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(52.dp),
                            shape = CircleShape,
                            color = Color(0xFF2E7D32).copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF4CAF50))
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Total Savings (Till Now)", color = Color.LightGray, fontSize = 13.sp)
                            Text(
                                "₹${String.format(Locale.getDefault(), "%,.0f", lifetimeStats.totalSavings)}", 
                                color = Color(0xFF4CAF50), 
                                fontSize = 30.sp, 
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Independence Score (Average)
            item {
                IndependenceRing(score = lifetimeStats.avgIndependence)
                Spacer(modifier = Modifier.height(32.dp))
            }

            // AI Insight
            item {
                suggestion?.let {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SolarYellow, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AI Dynamic Suggestion", color = SolarYellow, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(it, color = Color.White, fontSize = 14.sp, lineHeight = 20.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Today's Stats
            item {
                Text("Today's Performance", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard("Generation", "${todayLog?.generationKwh ?: 0.0} kWh", Icons.Default.WbSunny, SolarYellow, modifier = Modifier.weight(1f))
                    MetricCard("Consumption", "${todayLog?.consumptionKwh ?: 0.0} kWh", Icons.Default.Bolt, Color.White, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Lifetime Overview
            item {
                Text("Lifetime Stats Overview", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricCard("Total Yield", "${String.format(Locale.getDefault(), "%.1f", lifetimeStats.totalGen)} kWh", Icons.Default.TrendingUp, SolarYellow, modifier = Modifier.weight(1f))
                    MetricCard("Total Cons.", "${String.format(Locale.getDefault(), "%.1f", lifetimeStats.totalCons)} kWh", Icons.Default.DataUsage, Color.White, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SolarPower, contentDescription = null, tint = Color.Gray.copy(0.3f), modifier = Modifier.size(80.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Welcome to Surya-Shakti!", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Add your first solar plant in Profile to start.", color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onProfileClick) {
                            Text("Go to Profile", color = SolarYellow)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IndependenceRing(score: Int) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
        Canvas(modifier = Modifier.size(180.dp)) {
            drawArc(
                color = SurfaceDark,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = SolarYellow,
                startAngle = -90f,
                sweepAngle = (score / 100f) * 360f,
                useCenter = false,
                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$score%", fontSize = 44.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text("LIFETIME AVG IND.", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, icon: ImageVector, valueColor: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.Start) {
            Icon(icon, contentDescription = null, tint = valueColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(value, color = valueColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
