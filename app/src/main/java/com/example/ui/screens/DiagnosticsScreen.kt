package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RecordingCapability
import com.example.ui.Localization
import com.example.ui.WinstoneViewModel
import com.example.ui.components.CapabilityBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    viewModel: WinstoneViewModel,
    onBack: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    val diagnostics by viewModel.diagnostics.collectAsState()
    val isRunning by viewModel.isRunningDiagnostics.collectAsState()
    val deviceProfile by viewModel.deviceProfile.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Localization.get("diagnostics", language), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_diag_back")) {
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
            // Proton HyperX Hardware Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("card_hyperx_profile"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PROTON HYPERX PROFILE",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            CapabilityBadge(
                                capability = deviceProfile?.capabilityStatus ?: RecordingCapability.UNAVAILABLE,
                                language = language
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        ProfileRow("Model", "Proton HyperX")
                        ProfileRow("Android OS", "Android 16 (API 36 VanillaIceCream)")
                        ProfileRow("Build Number", "HyperX_HW1_V5_12152025")
                        ProfileRow("App Version", "1.0.0 (Winstone CRM Companion)")
                        ProfileRow("Audio HAL Policy", "Android 16 Cellular Stream Sandbox")
                    }
                }
            }

            // Run Diagnostics Button
            item {
                Button(
                    onClick = { viewModel.runFullDiagnostics() },
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("btn_run_diagnostics"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Icon(Icons.Default.Dns, contentDescription = "Run", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Localization.get("run_diagnostics", language), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Diagnostics Health Items
            item {
                Text(
                    text = "SYSTEM HEALTH CHECKS (${diagnostics.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(diagnostics) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (item.isOk) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (item.isOk) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = item.status,
                                tint = if (item.isOk) Color(0xFF15803D) else Color(0xFFB45309),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    item.status,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.isOk) Color(0xFF15803D) else Color(0xFFB45309)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                item.details,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
