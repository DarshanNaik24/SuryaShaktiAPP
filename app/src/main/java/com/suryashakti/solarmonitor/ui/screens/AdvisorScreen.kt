package com.suryashakti.solarmonitor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.suryashakti.solarmonitor.ui.theme.DarkBackground
import com.suryashakti.solarmonitor.ui.theme.SolarYellow
import com.suryashakti.solarmonitor.viewmodel.SolarViewModel

@Composable
fun AdvisorScreen(viewModel: SolarViewModel = hiltViewModel()) {
    val suggestion by viewModel.advisorSuggestion.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().background(DarkBackground).padding(16.dp)
    ) {
        Text("AI Energy Advisor", style = MaterialTheme.typography.headlineMedium, color = SolarYellow)
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { viewModel.fetchAdvisorSuggestion() },
            colors = ButtonDefaults.buttonColors(containerColor = SolarYellow)
        ) {
            Text("Get Latest Suggestion", color = DarkBackground)
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        suggestion?.let {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Text(it, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
