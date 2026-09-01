package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RecordingPolicy
import com.example.ui.Localization
import com.example.ui.WinstoneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolicyScreen(
    viewModel: WinstoneViewModel,
    onBack: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    val policy by viewModel.policy.collectAsState()

    var autoRec by remember(policy) { mutableStateOf(policy.autoRecordingEnabled) }
    var announcement by remember(policy) { mutableStateOf(policy.announcementEnabled) }
    var wifiOnly by remember(policy) { mutableStateOf(policy.wifiOnlyUpload) }
    var retentionDays by remember(policy) { mutableStateOf(policy.retentionDays.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Localization.get("policy_title", language), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_policy_back")) {
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
            // Legal Disclaimer Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("card_legal_disclaimer"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Gavel, contentDescription = "Legal", tint = Color(0xFFB45309), modifier = Modifier.size(24.dp))
                        Column {
                            Text("Jurisdiction & Privacy Compliance", fontWeight = FontWeight.Bold, color = Color(0xFF92400E), fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = Localization.get("policy_disclaimer", language),
                                color = Color(0xFFB45309),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Policy Settings Form
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "RECORDING & RETENTION POLICY",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Auto Call Recording", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Attempt recording when hardware/OS permits", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = autoRec,
                                onCheckedChange = { autoRec = it },
                                modifier = Modifier.testTag("switch_auto_rec")
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pre-call Consent Reminder", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Inform caller that call is recorded for quality assurance", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = announcement,
                                onCheckedChange = { announcement = it },
                                modifier = Modifier.testTag("switch_announcement")
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Wi-Fi Only Sync", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Save cellular mobile data when uploading audio", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = wifiOnly,
                                onCheckedChange = { wifiOnly = it },
                                modifier = Modifier.testTag("switch_wifi_only")
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        OutlinedTextField(
                            value = retentionDays,
                            onValueChange = { retentionDays = it },
                            label = { Text("Cloud Retention Period (Days)") },
                            modifier = Modifier.fillMaxWidth().testTag("input_retention_days"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Button(
                            onClick = {
                                val updated = policy.copy(
                                    autoRecordingEnabled = autoRec,
                                    announcementEnabled = announcement,
                                    wifiOnlyUpload = wifiOnly,
                                    retentionDays = retentionDays.toIntOrNull() ?: 90
                                )
                                viewModel.updatePolicy(updated)
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_save_policy"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("SAVE POLICY SETTINGS", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
