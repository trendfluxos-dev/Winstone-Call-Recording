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
import com.example.model.TestCallStep
import com.example.model.TestCallVerdict
import com.example.ui.AppLanguage
import com.example.ui.Localization
import com.example.ui.WinstoneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestCallScreen(
    viewModel: WinstoneViewModel,
    onBack: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    val steps by viewModel.testCallSteps.collectAsState()
    val verdict by viewModel.testCallVerdict.collectAsState()
    val isTesting by viewModel.isTestingCall.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Localization.get("test_call_title", language), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_test_call_back")) {
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
            // Run Test Action Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("card_test_call_action"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "End-to-End Test Call Simulation",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Verifies the 10-stage call tracking, Bangladesh phone normalization, lead matching, timeline sync, and Android 16 recording security compliance pipeline.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            lineHeight = 16.sp
                        )

                        Button(
                            onClick = { viewModel.runTestCallWorkflow() },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_run_test_call"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            if (isTesting) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Run")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(Localization.get("run_test_call", language), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Live MediaRecorder Foreground Service Controller
            item {
                var testPhone by remember { mutableStateOf("+880 1711 223344") }
                var isLiveRecordingActive by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("card_live_foreground_recorder"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "MediaRecorder",
                                tint = if (isLiveRecordingActive) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Live Foreground Recording & Sync",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Starts CallRecordingForegroundService with MediaRecorder. Persists in background even if app is minimized. On stop, Room triggers CrmSyncWorker to upload when online.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )

                        OutlinedTextField(
                            value = testPhone,
                            onValueChange = { testPhone = it },
                            label = { Text("Test Target Phone Number") },
                            modifier = Modifier.fillMaxWidth().testTag("input_test_phone"),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    isLiveRecordingActive = true
                                    viewModel.startForegroundCallRecording(testPhone)
                                },
                                enabled = !isLiveRecordingActive,
                                modifier = Modifier.weight(1f).height(48.dp).testTag("btn_start_fg_recording"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Icon(Icons.Default.PlayCircle, contentDescription = "Start", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Start FG", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    isLiveRecordingActive = false
                                    viewModel.stopForegroundCallRecording()
                                },
                                enabled = isLiveRecordingActive,
                                modifier = Modifier.weight(1f).height(48.dp).testTag("btn_stop_fg_recording"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                            ) {
                                Icon(Icons.Default.StopCircle, contentDescription = "Stop", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Stop & Sync", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Test Verdict Card
            if (verdict != TestCallVerdict.NOT_RUN) {
                item {
                    val display = when (verdict) {
                        TestCallVerdict.PASS -> VerdictDisplay(
                            title = "VERDICT: PASS",
                            desc = "All 10 verification steps passed including two-way recording.",
                            bgColor = Color(0xFFDCFCE7),
                            textColor = Color(0xFF15803D)
                        )
                        TestCallVerdict.PARTIAL -> VerdictDisplay(
                            title = "VERDICT: PARTIAL (EXPECTED ON ANDROID 16)",
                            desc = "Call metadata, normalization, lead matching & CRM timeline work 100%. Automatic audio is unavailable due to Android 16 voice security policy.",
                            bgColor = Color(0xFFFEF3C7),
                            textColor = Color(0xFFB45309)
                        )
                        TestCallVerdict.FAILED -> VerdictDisplay(
                            title = "VERDICT: FAILED",
                            desc = "One or more core CRM logging steps encountered an error.",
                            bgColor = Color(0xFFFEE2E2),
                            textColor = Color(0xFFB91C1C)
                        )
                        else -> VerdictDisplay("", "", Color.Transparent, Color.Transparent)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("card_test_verdict"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = display.bgColor)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = display.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = display.textColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = display.desc,
                                fontSize = 12.sp,
                                color = display.textColor,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // 10-Point Step Checklist
            if (steps.isNotEmpty()) {
                item {
                    Text(
                        text = "10-POINT VERIFICATION CHECKLIST",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(steps) { step ->
                    TestStepCard(step = step, language = language)
                }
            }
        }
    }
}

@Composable
fun TestStepCard(
    step: TestCallStep,
    language: AppLanguage
) {
    val title = if (language == AppLanguage.BN) step.titleBn else step.titleEn
    val desc = if (language == AppLanguage.BN) step.detailsBn else step.detailsEn

    Card(
        modifier = Modifier.fillMaxWidth().testTag("test_step_${step.stepNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(if (step.passed) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = step.stepNumber.toString(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (step.passed) Color(0xFF15803D) else Color(0xFFB45309)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (step.passed) "PASSED" else "SKIPPED (OS)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (step.passed) Color(0xFF15803D) else Color(0xFFB45309)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = desc,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (step.output.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Result: ${step.output}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private data class VerdictDisplay(val title: String, val desc: String, val bgColor: Color, val textColor: Color)
