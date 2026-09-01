package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RecordingMetadata
import com.example.model.RecordingStatus
import com.example.ui.Localization
import com.example.ui.WinstoneViewModel
import com.example.ui.components.RecordingStatusBadge
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingsScreen(
    viewModel: WinstoneViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val recordings by viewModel.allRecordings.collectAsState()
    val playbackState by viewModel.audioPlayer.playbackState.collectAsState()
    var selectedCallIdForUpload by remember { mutableStateOf<String>("manual_call_01") }

    // File picker for manual authorized audio upload fallback
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                if (inputStream != null) {
                    val fileName = "authorized_audio_${System.currentTimeMillis()}.m4a"
                    viewModel.uploadManualRecording(
                        callId = selectedCallIdForUpload,
                        inputStream = inputStream,
                        fileName = fileName,
                        leadId = null
                    )
                }
            } catch (e: Exception) {
                viewModel.showMessage("Failed to load audio: ${e.message}")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Localization.get("recordings", language), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_recordings_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { audioPickerLauncher.launch("audio/*") },
                        modifier = Modifier.testTag("btn_manual_upload_picker")
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = "Manual Upload")
                    }
                }
            )
        },
        bottomBar = {
            // Audio Player Bar if active
            if (playbackState.isPlaying || playbackState.currentRecordingId != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth().testTag("bar_audio_player"),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (playbackState.isPlaying) {
                                        viewModel.audioPlayer.pause()
                                    } else {
                                        viewModel.audioPlayer.resume()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (playbackState.isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                    contentDescription = "Play/Pause",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Playing Call Recording",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${playbackState.durationMs / 1000}s audio track",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.audioPlayer.stop() }) {
                            Icon(Icons.Default.Close, contentDescription = "Stop")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Manual Fallback Action Header
            Card(
                modifier = Modifier.fillMaxWidth().testTag("card_manual_upload_header"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Manual Recording Upload",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "If auto recording is restricted, upload authorized audio directly to CRM",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { audioPickerLauncher.launch("audio/*") },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_upload_audio_cta")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = "Upload", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("UPLOAD", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "RECORDING ARCHIVE & UPLOAD QUEUE (${recordings.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (recordings.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.GraphicEq,
                            contentDescription = "Empty",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No audio recording files in local storage.\nManual uploads or supported call recordings will appear here.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(recordings, key = { it.recordingId }) { rec ->
                        RecordingItemCard(
                            recording = rec,
                            language = language,
                            isPlaying = playbackState.currentRecordingId == rec.recordingId && playbackState.isPlaying,
                            onPlay = { viewModel.audioPlayer.play(rec.recordingId, rec.filePath) },
                            onPause = { viewModel.audioPlayer.pause() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecordingItemCard(
    recording: RecordingMetadata,
    language: com.example.ui.AppLanguage,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit
) {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val file = File(recording.filePath)
    val sizeKb = if (recording.fileSize > 0) recording.fileSize / 1024 else file.length() / 1024

    Card(
        modifier = Modifier.fillMaxWidth().testTag("rec_item_${recording.recordingId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = { if (isPlaying) onPause() else onPlay() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Column {
                    Text(
                        text = "Call ${recording.callId.takeLast(10)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${sdf.format(Date(recording.createdAt))} • ${sizeKb} KB • ${recording.mimeType}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            RecordingStatusBadge(status = recording.uploadStatus, language = language)
        }
    }
}
