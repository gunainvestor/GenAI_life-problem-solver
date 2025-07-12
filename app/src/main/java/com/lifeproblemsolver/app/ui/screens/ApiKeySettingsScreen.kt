package com.lifeproblemsolver.app.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeproblemsolver.app.ui.viewmodel.ApiKeySettingsViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeySettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ApiKeySettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("API Key Settings") },
                navigationIcon = {
                    IconButton(onClick = {
                        Log.d("ApiKeySettingsScreen", "Back button pressed")
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null)
                            Text(
                                text = "OpenAI API Key",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                        
                        Text(
                            text = "Add your own OpenAI API key for unlimited AI requests. " +
                                    "Without a key, you're limited to 5 requests per installation.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // Current API Key Status
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Current Status",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        if (uiState.hasUserApiKey) {
                            Text(
                                text = "✅ Using your API key (unlimited requests)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "⚠️ Using predefined key (${uiState.remainingRequests} requests remaining)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            
            // Saved API Keys List
            if (uiState.apiKeys.isNotEmpty()) {
                item {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Saved API Keys",
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            Text(
                                text = "Tap on a key to make it active, or use the delete button to remove it.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                items(uiState.apiKeys) { apiKey ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (apiKey.isActive) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        if (apiKey.isActive) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = if (apiKey.isActive) "Active" else "Inactive",
                                        tint = if (apiKey.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = apiKey.name,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                                
                                Text(
                                    text = "sk-...${apiKey.apiKey.takeLast(4)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                apiKey.lastUsed?.let { lastUsed ->
                                    Text(
                                        text = "Last used: ${lastUsed.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (!apiKey.isActive) {
                                    Button(
                                        onClick = { viewModel.setActiveApiKey(apiKey.id) },
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text("Activate")
                                    }
                                }
                                
                                IconButton(
                                    onClick = { viewModel.deleteApiKey(apiKey.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Add New API Key
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Add New API Key",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        var apiKey by remember { mutableStateOf("") }
                        var keyName by remember { mutableStateOf("") }
                        var showPassword by remember { mutableStateOf(false) }
                        
                        OutlinedTextField(
                            value = keyName,
                            onValueChange = { keyName = it },
                            label = { Text("Key Name (optional)") },
                            placeholder = { Text("e.g., Work Key, Personal Key") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            label = { Text("OpenAI API Key") },
                            placeholder = { Text("sk-...") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (showPassword) "Hide" else "Show"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Button(
                            onClick = {
                                if (apiKey.isNotBlank()) {
                                    viewModel.saveApiKey(apiKey, keyName.ifBlank { "API Key" })
                                    apiKey = ""
                                    keyName = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = apiKey.isNotBlank() && !uiState.isLoading
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Save API Key")
                            }
                        }
                    }
                }
            }
            
            // Instructions
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "How to get an API key",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Text(
                            text = "1. Go to https://platform.openai.com/api-keys\n" +
                                    "2. Sign in or create an account\n" +
                                    "3. Click 'Create new secret key'\n" +
                                    "4. Copy the key and paste it above\n" +
                                    "5. Your key starts with 'sk-'",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // Error message
            uiState.error?.let { error ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
} 