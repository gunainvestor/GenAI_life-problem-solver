package com.lifeproblemsolver.app.ui.screens

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeproblemsolver.app.ui.components.*
import com.lifeproblemsolver.app.ui.theme.*
import com.lifeproblemsolver.app.ui.viewmodel.ApiKeySettingsViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeySettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ApiKeySettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    )
                )
            )
    ) {
        // Premium Top Bar
        PremiumTopBar(
            title = "API Key Settings",
            onBackClick = {
                Log.d("ApiKeySettingsScreen", "Back button pressed")
                onNavigateBack()
            }
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            item {
                PremiumCard(
                    modifier = Modifier.fillMaxWidth(),
                    gradient = PrimaryGradient
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Key,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "OpenAI API Key",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Text(
                            text = "Add your own OpenAI API key for unlimited AI requests. " +
                                    "Without a key, you're limited to 5 requests per installation.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
            
            // Current API Key Status
            item {
                PremiumCard(
                    modifier = Modifier.fillMaxWidth(),
                    gradient = if (uiState.hasUserApiKey) SuccessGradient else AccentGradient
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Current Status",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        if (uiState.hasUserApiKey) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Using your API key (unlimited requests)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Using predefined key (${uiState.remainingRequests} requests remaining)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
            
            // Add New API Key Section
            item {
                PremiumAddApiKeySection(
                    uiState = uiState,
                    onAddApiKey = { name, key -> viewModel.saveApiKey(context, key, name) },
                    onUpdateName = { /* Not implemented in current ViewModel */ },
                    onUpdateKey = { /* Not implemented in current ViewModel */ }
                )
            }
            
            // Saved API Keys List
            if (uiState.apiKeys.isNotEmpty()) {
                item {
                    PremiumCard(
                        modifier = Modifier.fillMaxWidth(),
                        gradient = SecondaryGradient
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Saved API Keys",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            Text(
                                text = "Tap on a key to make it active, or use the delete button to remove it.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                
                items(uiState.apiKeys) { apiKey ->
                    PremiumApiKeyCard(
                        apiKey = apiKey,
                        onSetActive = { viewModel.setActiveApiKey(context, apiKey.id) },
                        onDelete = { viewModel.deleteApiKey(context, apiKey.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumAddApiKeySection(
    uiState: com.lifeproblemsolver.app.ui.viewmodel.ApiKeySettingsUiState,
    onAddApiKey: (String, String) -> Unit,
    onUpdateName: (String) -> Unit,
    onUpdateKey: (String) -> Unit
) {
    var showPassword by remember { mutableStateOf(true) }
    var apiKeyName by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    
    PremiumCard(
        modifier = Modifier.fillMaxWidth(),
        gradient = AccentGradient
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Add New API Key",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            
            PremiumTextFieldWithVoice(
                value = apiKeyName,
                onValueChange = { apiKeyName = it },
                label = "Key Name",
                placeholder = "e.g., Work Account, Personal",
                leadingIcon = Icons.Default.Label
            )
            
            PremiumTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = "API Key",
                placeholder = "sk-...",
                leadingIcon = Icons.Default.Key,
                trailingIcon = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff
            )
            
            PremiumButton(
                onClick = { onAddApiKey(apiKeyName, apiKey) },
                text = "Add API Key",
                icon = Icons.Default.Add,
                enabled = apiKeyName.isNotBlank() && apiKey.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
            
            if (uiState.error != null) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = uiState.error!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumApiKeyCard(
    apiKey: com.lifeproblemsolver.app.data.model.UserApiKey,
    onSetActive: () -> Unit,
    onDelete: () -> Unit
) {
    val animatedElevation by animateFloatAsState(
        targetValue = if (apiKey.isActive) 8f else 4f,
        animationSpec = tween(200),
        label = "api_key_elevation"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = animatedElevation.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = if (apiKey.isActive) PrimaryBlue.copy(alpha = 0.3f) else Color.Transparent
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (apiKey.isActive) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
            else 
                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (apiKey.isActive) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (apiKey.isActive) "Active" else "Inactive",
                            tint = if (apiKey.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = apiKey.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Text(
                        text = "sk-...${apiKey.apiKey.takeLast(4)}",
                        style = MaterialTheme.typography.bodyMedium,
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
                
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!apiKey.isActive) {
                        PremiumButton(
                            onClick = onSetActive,
                            text = "Activate",
                            icon = Icons.Default.Check,
                            modifier = Modifier.height(36.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(8.dp),
                                spotColor = ErrorRed.copy(alpha = 0.3f)
                            )
                            .background(
                                Brush.linearGradient(listOf(ErrorRed, ErrorRedLight)),
                                RoundedCornerShape(8.dp)
                            )
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
} 