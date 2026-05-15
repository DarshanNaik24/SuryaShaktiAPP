package com.suryashakti.solarmonitor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.suryashakti.solarmonitor.ui.theme.DarkBackground
import com.suryashakti.solarmonitor.ui.theme.SolarYellow
import com.suryashakti.solarmonitor.ui.theme.SurfaceDark
import com.suryashakti.solarmonitor.viewmodel.AuthState
import com.suryashakti.solarmonitor.viewmodel.AuthViewModel
import com.suryashakti.solarmonitor.viewmodel.SolarViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    solarViewModel: SolarViewModel,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val plants by solarViewModel.plants.collectAsState()
    
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    var showAddPlantDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val mainGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF1A1A2E), DarkBackground)
    )

    LaunchedEffect(authViewModel.authState) {
        authViewModel.authState.collect { state ->
            if (state is AuthState.Error) {
                snackbarHostState.showSnackbar("Error: ${state.message}")
                authViewModel.resetState()
            } else if (state is AuthState.Authenticated) {
                if (oldPassword.isNotBlank() && newPassword.isNotBlank()) {
                    snackbarHostState.showSnackbar("Security credentials updated!")
                    oldPassword = ""
                    newPassword = ""
                    confirmNewPassword = ""
                }
                authViewModel.resetState()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        modifier = Modifier.background(mainGradient),
        topBar = {
            TopAppBar(
                title = { Text("User Profile", color = SolarYellow, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SolarYellow)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(SolarYellow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        Column {
                            Text(currentUser?.name ?: "User", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("ID: ${currentUser?.userId ?: "---"}", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }

            item {
                Text("SECURITY", color = SolarYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        ProfileTextField(value = oldPassword, onValueChange = { oldPassword = it }, label = "Current Password", isPassword = true)
                        Spacer(modifier = Modifier.height(16.dp))
                        ProfileTextField(value = newPassword, onValueChange = { newPassword = it }, label = "New Password", isPassword = true)
                        Spacer(modifier = Modifier.height(16.dp))
                        ProfileTextField(value = confirmNewPassword, onValueChange = { confirmNewPassword = it }, label = "Confirm New Password", isPassword = true)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                if (newPassword != confirmNewPassword) {
                                    scope.launch { snackbarHostState.showSnackbar("Passwords do not match!") }
                                } else if (newPassword.isBlank()) {
                                    scope.launch { snackbarHostState.showSnackbar("New password required!") }
                                } else {
                                    authViewModel.changePassword(oldPassword, newPassword)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SolarYellow),
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("UPDATE PASSWORD", color = DarkBackground, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("MANAGED PLANTS", color = SolarYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { showAddPlantDialog = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Add Plant", tint = SolarYellow)
                    }
                }
            }

            items(plants) { plant ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(plant.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(plant.location, color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        authViewModel.logout()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCF6679).copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCF6679))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color(0xFFCF6679))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("TERMINATE SESSION", color = Color(0xFFCF6679), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    if (showAddPlantDialog) {
        ProfileAddPlantDialog(
            viewModel = solarViewModel,
            onDismiss = { showAddPlantDialog = false },
            onConfirm = { name, location, type ->
                solarViewModel.addPlant(name, location, type)
                showAddPlantDialog = false
                scope.launch { snackbarHostState.showSnackbar("New plant synchronized!") }
            }
        )
    }
}

@Composable
fun ProfileTextField(value: String, onValueChange: (String) -> Unit, label: String, isPassword: Boolean = false) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SolarYellow,
            unfocusedBorderColor = Color.DarkGray,
            focusedLabelColor = SolarYellow,
            unfocusedTextColor = Color.White,
            focusedTextColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAddPlantDialog(
    viewModel: SolarViewModel,
    onDismiss: () -> Unit, 
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Home") }
    var expanded by remember { mutableStateOf(false) }
    
    val suggestions by viewModel.locationSuggestions.collectAsState()

    LaunchedEffect(location) {
        viewModel.updateLocationQuery(location)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Initialize New Plant", color = SolarYellow) },
        containerColor = SurfaceDark,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Plant Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SolarYellow, unfocusedTextColor = Color.White, focusedTextColor = Color.White)
                )
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { 
                            location = it
                            expanded = true
                        },
                        label = { Text("Location Lookup") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SolarYellow, unfocusedTextColor = Color.White, focusedTextColor = Color.White)
                    )
                    DropdownMenu(
                        expanded = expanded && suggestions.isNotEmpty(),
                        onDismissRequest = { expanded = false },
                        properties = PopupProperties(focusable = false),
                        modifier = Modifier.fillMaxWidth(0.8f).background(SurfaceDark)
                    ) {
                        suggestions.forEach { suggestion ->
                            DropdownMenuItem(
                                text = { Text(suggestion, color = Color.White) },
                                onClick = {
                                    location = suggestion
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Home", "Factory", "Farmhouse").forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SolarYellow,
                                selectedLabelColor = DarkBackground,
                                containerColor = Color.DarkGray,
                                labelColor = Color.White
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, location, type) }, colors = ButtonDefaults.buttonColors(containerColor = SolarYellow)) {
                Text("SYNCHRONIZE", color = DarkBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = Color.Gray) }
        }
    )
}
