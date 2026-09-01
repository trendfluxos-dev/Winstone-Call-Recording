package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.model.CallDirection
import com.example.model.CallRecord
import com.example.model.CrmLead
import com.example.telephony.PhoneNumberUtils
import com.example.ui.Localization
import com.example.ui.WinstoneViewModel
import com.example.ui.components.RecordingStatusBadge
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadsScreen(
    viewModel: WinstoneViewModel,
    onBack: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    val allLeads by viewModel.allLeads.collectAsState()
    val allCalls by viewModel.allCalls.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var selectedLeadForTimeline by remember { mutableStateOf<CrmLead?>(null) }

    val filteredLeads = remember(allLeads, searchQuery) {
        if (searchQuery.isBlank()) {
            allLeads
        } else {
            allLeads.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.phoneNumber.contains(searchQuery, ignoreCase = true) ||
                        it.company.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Localization.get("my_leads", language), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_leads_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_search_leads"),
                placeholder = { Text(Localization.get("search_leads_hint", language), fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${filteredLeads.size} Assigned Leads (Bangladesh Territory)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredLeads, key = { it.leadId }) { lead ->
                    LeadCard(
                        lead = lead,
                        language = language,
                        onCall = { viewModel.dialLead(lead.phoneNumber) },
                        onTimeline = { selectedLeadForTimeline = lead }
                    )
                }
            }
        }
    }

    // Lead Timeline Dialog
    selectedLeadForTimeline?.let { lead ->
        val leadCalls = remember(allCalls, lead) {
            allCalls.filter { it.leadId == lead.leadId || PhoneNumberUtils.isMatch(it.phoneNumber, lead.phoneNumber) }
        }
        LeadTimelineDialog(
            lead = lead,
            calls = leadCalls,
            language = language,
            onDismiss = { selectedLeadForTimeline = null },
            onCall = { viewModel.dialLead(lead.phoneNumber) }
        )
    }
}

@Composable
fun LeadCard(
    lead: CrmLead,
    language: com.example.ui.AppLanguage,
    onCall: () -> Unit,
    onTimeline: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("lead_card_${lead.leadId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lead.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = lead.company,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = PhoneNumberUtils.formatDisplay(lead.phoneNumber),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = lead.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCall,
                    modifier = Modifier.weight(1f).height(44.dp).testTag("btn_call_lead_${lead.leadId}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(Localization.get("call_lead", language), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onTimeline,
                    modifier = Modifier.weight(1f).height(44.dp).testTag("btn_timeline_lead_${lead.leadId}"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Timeline, contentDescription = "Timeline", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(Localization.get("view_timeline", language), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadTimelineDialog(
    lead: CrmLead,
    calls: List<CallRecord>,
    language: com.example.ui.AppLanguage,
    onDismiss: () -> Unit,
    onCall: () -> Unit
) {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = lead.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = "${lead.company} • ${lead.phoneNumber}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onCall) {
                    Icon(Icons.Default.Call, contentDescription = "Call", tint = Color(0xFF10B981))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "CRM CALL TIMELINE & OUTCOMES (${calls.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (calls.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No call history logged yet for this lead.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(calls) { call ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(
                                            if (call.direction == CallDirection.INCOMING) Icons.Default.CallReceived else Icons.Default.CallMade,
                                            contentDescription = call.direction.name,
                                            tint = if (call.direction == CallDirection.INCOMING) Color(0xFF3B82F6) else Color(0xFF10B981),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = if (call.direction == CallDirection.INCOMING) Localization.get("incoming", language) else Localization.get("outgoing", language),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                    RecordingStatusBadge(status = call.recordingStatus, language = language)
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = sdf.format(Date(call.startedAt)),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${Localization.get("duration", language)}: ${call.durationSeconds}s",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                if (call.outcome.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${Localization.get("outcome", language)}: ${call.outcome}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                if (call.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${Localization.get("notes", language)}: ${call.notes}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                if (!call.followUpDate.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${Localization.get("follow_up", language)}: ${call.followUpDate}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD97706)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
