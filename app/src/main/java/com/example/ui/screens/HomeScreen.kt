package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RecordingCapability
import com.example.ui.AppLanguage
import com.example.ui.Localization
import com.example.ui.WinstoneViewModel
import com.example.ui.components.CapabilityBadge
import com.example.ui.navigation.NavRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: WinstoneViewModel,
    onNavigate: (String) -> Unit
) {
    val language by viewModel.language.collectAsState()
    val session by viewModel.session.collectAsState()
    val deviceProfile by viewModel.deviceProfile.collectAsState()
    val callsCount by viewModel.callsCount.collectAsState()
    val recordedCallsCount by viewModel.recordedCallsCount.collectAsState()
    val pendingUploadsCount by viewModel.pendingUploadsCount.collectAsState()
    val failedUploadsCount by viewModel.failedUploadsCount.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "WINSTONE",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = Localization.get("app_title", language),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                },
                actions = {
                    // Language Switcher Toggle
                    IconButton(
                        onClick = { viewModel.toggleLanguage() },
                        modifier = Modifier.testTag("btn_toggle_lang")
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Text(
                                text = if (language == AppLanguage.EN) "বাং" else "EN",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    IconButton(
                        onClick = { onNavigate(NavRoutes.SETTINGS) },
                        modifier = Modifier.testTag("btn_home_settings")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
        ) {
            // Status Bar Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("card_status_bar"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (session.isLoggedIn) Color(0xFF10B981) else Color.Red)
                                )
                                Text(
                                    text = Localization.get("crm_connected", language),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = session.fullName,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = Localization.get("device_registered", language),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Proton HyperX (Android 16)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            CapabilityBadge(
                                capability = deviceProfile?.capabilityStatus ?: RecordingCapability.UNAVAILABLE,
                                language = language
                            )
                        }
                    }
                }
            }

            // Android 16 Honest Capability Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("banner_capability_info"),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFEF3C7)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Info",
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = Localization.get("rec_unavailable_banner", language),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E),
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = Localization.get("rec_unavailable_detail", language),
                                color = Color(0xFFB45309),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Metrics Cards Grid
            item {
                Text(
                    text = "ACTIVITY DASHBOARD",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = Localization.get("todays_calls", language),
                        value = callsCount.toString(),
                        icon = Icons.Default.Call,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = Localization.get("recorded_calls", language),
                        value = recordedCallsCount.toString(),
                        icon = Icons.Default.Mic,
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = Localization.get("pending_upload", language),
                        value = pendingUploadsCount.toString(),
                        icon = Icons.Default.CloudUpload,
                        color = Color(0xFF3B82F6),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = Localization.get("failed_uploads", language),
                        value = failedUploadsCount.toString(),
                        icon = Icons.Default.ErrorOutline,
                        color = Color(0xFFEF4444),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Primary Navigation Touch Actions
            item {
                Text(
                    text = "CALL OPERATIONS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LargeActionButton(
                        title = Localization.get("my_leads", language),
                        subtitle = "Assigned Bangladesh enterprise CRM leads",
                        icon = Icons.Default.PersonSearch,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        testTag = "btn_nav_my_leads",
                        onClick = { onNavigate(NavRoutes.LEADS) }
                    )

                    LargeActionButton(
                        title = Localization.get("recent_calls", language),
                        subtitle = "Call logs, outcome selection & notes",
                        icon = Icons.Default.History,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        testTag = "btn_nav_recent_calls",
                        onClick = { onNavigate(NavRoutes.RECENT_CALLS) }
                    )

                    LargeActionButton(
                        title = Localization.get("recordings", language),
                        subtitle = "Audio playback & manual authorized uploads",
                        icon = Icons.Default.GraphicEq,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        testTag = "btn_nav_recordings",
                        onClick = { onNavigate(NavRoutes.RECORDINGS) }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.syncNow() },
                            modifier = Modifier.weight(1f).height(52.dp).testTag("btn_sync_now"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = "Sync", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(Localization.get("sync_now", language), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Button(
                            onClick = { onNavigate(NavRoutes.TEST_CALL) },
                            modifier = Modifier.weight(1f).height(52.dp).testTag("btn_nav_test_call"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(Icons.Default.PlayCircle, contentDescription = "Test", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(Localization.get("test_call", language), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    // Secondary Tools
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onNavigate(NavRoutes.DIAGNOSTICS) },
                            modifier = Modifier.weight(1f).height(48.dp).testTag("btn_nav_diagnostics"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Dns, contentDescription = "Diagnostics", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(Localization.get("diagnostics", language), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = { onNavigate(NavRoutes.AUTHORITY) },
                            modifier = Modifier.weight(1f).height(48.dp).testTag("btn_nav_authority"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = "Authority", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ADMIN / FLEET", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun LargeActionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(contentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = title, tint = contentColor, modifier = Modifier.size(24.dp))
                }
                Column {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = contentColor)
        }
    }
}
