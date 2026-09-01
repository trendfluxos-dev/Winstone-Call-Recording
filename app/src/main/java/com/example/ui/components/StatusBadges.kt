package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RecordingCapability
import com.example.model.RecordingStatus
import com.example.ui.AppLanguage
import com.example.ui.Localization

val GreenRecorded = Color(0xFF10B981)
val BluePending = Color(0xFF3B82F6)
val YellowUnavailable = Color(0xFFF59E0B)
val RedFailed = Color(0xFFEF4444)
val GrayNotRecorded = Color(0xFF6B7280)

@Composable
fun RecordingStatusBadge(
    status: RecordingStatus,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status) {
        RecordingStatus.RECORDED_LOCAL, RecordingStatus.UPLOADED -> Pair(Color(0xFFDCFCE7), Color(0xFF15803D))
        RecordingStatus.UPLOAD_PENDING, RecordingStatus.UPLOADING -> Pair(Color(0xFFDBEAFE), Color(0xFF1D4ED8))
        RecordingStatus.RECORDING_UNAVAILABLE -> Pair(Color(0xFFFEF3C7), Color(0xFFB45309))
        RecordingStatus.RECORDING_FAILED, RecordingStatus.UPLOAD_FAILED -> Pair(Color(0xFFFEE2E2), Color(0xFFB91C1C))
        RecordingStatus.RECORDING -> Pair(Color(0xFFFEE2E2), Color(0xFFDC2626))
        RecordingStatus.NOT_RECORDED, RecordingStatus.DELETED -> Pair(Color(0xFFF3F4F6), Color(0xFF4B5563))
    }

    val label = Localization.getRecordingStatusLabel(status, language)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Text(
                text = " $label",
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun CapabilityBadge(
    capability: RecordingCapability,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, labelEn, labelBn) = when (capability) {
        RecordingCapability.SUPPORTED -> Quad(Color(0xFFDCFCE7), Color(0xFF15803D), "Supported", "সক্রিয়")
        RecordingCapability.LIMITED -> Quad(Color(0xFFFEF3C7), Color(0xFFB45309), "Limited", "সীমিত")
        RecordingCapability.UNAVAILABLE -> Quad(Color(0xFFFEE2E2), Color(0xFFB91C1C), "Unavailable (Android 16)", "অনুপলব্ধ (অ্যান্ড্রয়েড ১৬)")
        RecordingCapability.UNKNOWN -> Quad(Color(0xFFF3F4F6), Color(0xFF4B5563), "Unknown", "অজানা")
    }

    val label = if (language == AppLanguage.BN) labelBn else labelEn

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
