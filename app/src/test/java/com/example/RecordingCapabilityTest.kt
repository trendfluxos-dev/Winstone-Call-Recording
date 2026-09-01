package com.example

import com.example.model.RecordingStatus
import com.example.ui.AppLanguage
import com.example.ui.Localization
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class RecordingCapabilityTest {

    @Test
    fun testBilingualLocalization() {
        val enMsg = Localization.get("rec_unavailable_banner", AppLanguage.EN)
        val bnMsg = Localization.get("rec_unavailable_banner", AppLanguage.BN)

        assertEquals("Automatic call recording is unavailable on this device.", enMsg)
        assertEquals("এই ডিভাইসে স্বয়ংক্রিয় কল রেকর্ডিং উপলভ্য নয়।", bnMsg)

        val enStatus = Localization.getRecordingStatusLabel(RecordingStatus.RECORDED_LOCAL, AppLanguage.EN)
        val bnStatus = Localization.getRecordingStatusLabel(RecordingStatus.RECORDED_LOCAL, AppLanguage.BN)

        assertEquals("Recorded Locally", enStatus)
        assertEquals("লোকালি সংরক্ষিত", bnStatus)
    }

    @Test
    fun testRecordingStatusesEnumCompleteness() {
        assertEquals("NOT_RECORDED", RecordingStatus.NOT_RECORDED.name)
        assertEquals("RECORDING", RecordingStatus.RECORDING.name)
        assertEquals("RECORDED_LOCAL", RecordingStatus.RECORDED_LOCAL.name)
        assertEquals("UPLOAD_PENDING", RecordingStatus.UPLOAD_PENDING.name)
        assertEquals("UPLOADING", RecordingStatus.UPLOADING.name)
        assertEquals("UPLOADED", RecordingStatus.UPLOADED.name)
        assertEquals("UPLOAD_FAILED", RecordingStatus.UPLOAD_FAILED.name)
        assertEquals("RECORDING_UNAVAILABLE", RecordingStatus.RECORDING_UNAVAILABLE.name)
        assertEquals("RECORDING_FAILED", RecordingStatus.RECORDING_FAILED.name)
        assertEquals("DELETED", RecordingStatus.DELETED.name)
    }
}
