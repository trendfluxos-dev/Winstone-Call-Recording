package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppLanguage
import com.example.ui.Localization
import com.example.ui.WinstoneViewModel
import com.example.ui.navigation.NavRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: WinstoneViewModel,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    val session by viewModel.session.collectAsState()
    val crmBaseUrl by viewModel.crmBaseUrl.collectAsState()
    val pendingQueueCount by viewModel.pendingQueueCount.collectAsState()
    val pendingUploadsCount by viewModel.pendingUploadsCount.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    var customUrl by remember(crmBaseUrl) { mutableStateOf(crmBaseUrl) }
    var isEditingUrl by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Localization.get("settings", language), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_settings_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
        ) {
            // Account Info Card (EncryptedSharedPreferences session)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("card_user_account"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AUTHENTICATED AGENT",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Badge(containerColor = Color(0xFF10B981).copy(alpha = 0.2f)) {
                                Text("ENCRYPTED (AES-256)", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                        Text(session.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${session.workEmail} • ${session.employeeId}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Role: ${session.role.name} (Winstone CRM Sales)", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.logout()
                                onNavigate(NavRoutes.LOGIN)
                            },
                            modifier = Modifier.fillMaxWidth().testTag("btn_sign_out"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SIGN OUT FROM PROTON HYPERX", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // CRM Server & Offline Sync Queue
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("card_crm_endpoint"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("WINSTONE CRM SERVER", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            TextButton(onClick = { isEditingUrl = !isEditingUrl }) {
                                Text(if (isEditingUrl) "Done" else "Change", fontSize = 12.sp)
                            }
                        }

                        if (isEditingUrl) {
                            OutlinedTextField(
                                value = customUrl,
                                onValueChange = { customUrl = it },
                                label = { Text("Base URL") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            Button(
                                onClick = {
                                    if (customUrl.isNotBlank()) {
                                        viewModel.setServerUrl(customUrl)
                                        isEditingUrl = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Save Endpoint")
                            }
                        } else {
                            Text(crmBaseUrl, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Offline Sync Queue", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("$pendingQueueCount queue items • $pendingUploadsCount pending uploads", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Button(
                                onClick = { viewModel.syncNow() },
                                enabled = !isSyncing,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_sync_settings")
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Sync, contentDescription = "Sync", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sync", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Language Preference
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("APP LANGUAGE / ভাষা", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChip(
                                selected = language == AppLanguage.EN,
                                onClick = { viewModel.setLanguage(AppLanguage.EN) },
                                label = { Text("English (US)") },
                                modifier = Modifier.weight(1f).testTag("chip_lang_en")
                            )
                            FilterChip(
                                selected = language == AppLanguage.BN,
                                onClick = { viewModel.setLanguage(AppLanguage.BN) },
                                label = { Text("বাংলা (Bangla)") },
                                modifier = Modifier.weight(1f).testTag("chip_lang_bn")
                            )
                        }
                    }
                }
            }

            // Navigation Links
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column {
                        SettingsNavRow("Call Recording Policy & Compliance", Icons.Default.Gavel) {
                            onNavigate(NavRoutes.POLICY)
                        }
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        SettingsNavRow("Proton HyperX Diagnostics", Icons.Default.Dns) {
                            onNavigate(NavRoutes.DIAGNOSTICS)
                        }
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        SettingsNavRow("Test Call Verification", Icons.Default.PlayCircle) {
                            onNavigate(NavRoutes.TEST_CALL)
                        }
                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        SettingsNavRow("Authority Fleet Dashboard", Icons.Default.AdminPanelSettings) {
                            onNavigate(NavRoutes.AUTHORITY)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsNavRow(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
