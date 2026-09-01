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
import com.example.model.RecordingStatus
import com.example.telephony.PhoneNumberUtils
import com.example.ui.Localization
import com.example.ui.WinstoneViewModel
import com.example.ui.components.RecordingStatusBadge
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentCallsScreen(
    viewModel: WinstoneViewModel,
    onBack: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    val allCalls by viewModel.allCalls.collectAsState()
    val allLeads by viewModel.allLeads.collectAsState()
    var selectedCallForEdit by remember { mutableStateOf<CallRecord?>(null) }
    var selectedCallForLeadPicker by remember { mutableStateOf<CallRecord?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Localization.get("recent_calls", language), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_recent_calls_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (allCalls.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No call records logged yet.\nIncoming and outgoing calls will appear here automatically.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
            ) {
                items(allCalls, key = { it.callId }) { call ->
                    CallRecordCard(
                        call = call,
                        language = language,
                        onEdit = { selectedCallForEdit = call },
                        onPickLead = { selectedCallForLeadPicker = call },
                        onCall = { viewModel.dialLead(call.phoneNumber) }
                    )
                }
            }
        }
    }

    // Edit Call Details Dialog
    selectedCallForEdit?.let { call ->
        EditCallDetailsDialog(
            call = call,
            language = language,
            onDismiss = { selectedCallForEdit = null },
            onSave = { outcome, notes, followUp ->
                viewModel.updateCallDetails(call.callId, outcome, notes, followUp)
                selectedCallForEdit = null
            }
        )
    }

    // Multiple Leads / Lead Picker Dialog
    selectedCallForLeadPicker?.let { call ->
        LeadPickerDialog(
            call = call,
            leads = allLeads,
            language = language,
            onDismiss = { selectedCallForLeadPicker = null },
            onSelectLead = { lead ->
                viewModel.associateLeadToCall(call.callId, lead)
                selectedCallForLeadPicker = null
            }
        )
    }
}

@Composable
fun CallRecordCard(
    call: CallRecord,
    language: com.example.ui.AppLanguage,
    onEdit: () -> Unit,
    onPickLead: () -> Unit,
    onCall: () -> Unit
) {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("call_card_${call.callId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Direction, Number, Time, Recording Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (call.direction == CallDirection.INCOMING) Color(0xFFDBEAFE) else Color(0xFFDCFCE7)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (call.direction == CallDirection.INCOMING) Icons.Default.CallReceived else Icons.Default.CallMade,
                            contentDescription = call.direction.name,
                            tint = if (call.direction == CallDirection.INCOMING) Color(0xFF1D4ED8) else Color(0xFF15803D),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = PhoneNumberUtils.formatDisplay(call.phoneNumber),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = sdf.format(Date(call.startedAt)),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                RecordingStatusBadge(status = call.recordingStatus, language = language)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Lead Association Banner
            if (call.leadId != null && call.leadName != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "Lead", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "${call.leadName} (${call.leadCompany ?: "CRM Lead"})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            text = "MATCHED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF3C7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = Localization.get("no_lead_matched", language),
                            fontSize = 11.sp,
                            color = Color(0xFF92400E)
                        )
                        TextButton(
                            onClick = onPickLead,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("SELECT LEAD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                        }
                    }
                }
            }

            // Outcome & Notes Summary
            if (call.outcome.isNotBlank() || call.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                if (call.outcome.isNotBlank()) {
                    Text(
                        text = "Outcome: ${call.outcome}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (call.notes.isNotBlank()) {
                    Text(
                        text = "Notes: ${call.notes}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!call.followUpDate.isNullOrBlank()) {
                    Text(
                        text = "Follow-up: ${call.followUpDate}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD97706)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f).height(40.dp).testTag("btn_edit_call_${call.callId}"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.EditNote, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("LOG / OUTCOME", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onCall,
                    modifier = Modifier.height(40.dp).testTag("btn_redial_${call.callId}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCallDetailsDialog(
    call: CallRecord,
    language: com.example.ui.AppLanguage,
    onDismiss: () -> Unit,
    onSave: (outcome: String, notes: String, followUp: String?) -> Unit
) {
    var outcome by remember { mutableStateOf(if (call.outcome == "No Outcome Set") "Interested" else call.outcome) }
    var notes by remember { mutableStateOf(call.notes) }
    var followUp by remember { mutableStateOf(call.followUpDate ?: "03 Sep 2026") }

    val outcomesList = listOf(
        "Interested",
        "Proposal Requested",
        "Follow-up Scheduled",
        "Quotation Sent",
        "Closed / Won",
        "Closed / Lost",
        "Busy / Callback Later",
        "Wrong Number"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "LOG CALL OUTCOME & CRM NOTES",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Call with ${PhoneNumberUtils.formatDisplay(call.phoneNumber)} (${call.durationSeconds}s)",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Outcome selection chips
            Text("Select Call Outcome:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                outcomesList.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { item ->
                            FilterChip(
                                selected = outcome == item,
                                onClick = { outcome = item },
                                label = { Text(item, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Agent Notes & Discussion Summary") },
                modifier = Modifier.fillMaxWidth().testTag("input_call_notes"),
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = followUp,
                onValueChange = { followUp = it },
                label = { Text("Follow-up Date (e.g. 05 Sep 2026)") },
                modifier = Modifier.fillMaxWidth().testTag("input_call_followup"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = { onSave(outcome, notes, followUp) },
                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("btn_save_call_details"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CloudSync, contentDescription = "Save")
                Spacer(modifier = Modifier.width(8.dp))
                Text(Localization.get("save_notes", language), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadPickerDialog(
    call: CallRecord,
    leads: List<CrmLead>,
    language: com.example.ui.AppLanguage,
    onDismiss: () -> Unit,
    onSelectLead: (CrmLead) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = Localization.get("multi_leads_found", language),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Select CRM Lead to associate with ${call.phoneNumber}:",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(leads) { lead ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectLead(lead) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(lead.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${lead.company} • ${lead.phoneNumber}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.CheckCircle, contentDescription = "Select", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
