package com.example.ui

import com.example.model.RecordingStatus

enum class AppLanguage {
    EN,
    BN
}

object Localization {

    fun get(key: String, lang: AppLanguage): String {
        return if (lang == AppLanguage.BN) {
            bnMap[key] ?: enMap[key] ?: key
        } else {
            enMap[key] ?: key
        }
    }

    fun getRecordingStatusLabel(status: RecordingStatus, lang: AppLanguage): String {
        return if (lang == AppLanguage.BN) status.labelBn else status.labelEn
    }

    private val enMap = mapOf(
        "app_title" to "Winstone Auto Call Recorder",
        "app_subtitle" to "Winstone CRM Companion",
        "crm_connected" to "CRM Connected",
        "device_registered" to "Device Registered",
        "recording_status" to "Recording Status",
        "todays_calls" to "Today's Calls",
        "recorded_calls" to "Recorded Calls",
        "pending_upload" to "Pending Upload",
        "failed_uploads" to "Failed Uploads",
        "followups_due" to "Follow-ups Due",
        "my_leads" to "MY LEADS",
        "recent_calls" to "RECENT CALLS",
        "recordings" to "RECORDINGS",
        "sync_now" to "SYNC NOW",
        "settings" to "SETTINGS",
        "diagnostics" to "DIAGNOSTICS",
        "test_call" to "TEST CALL",
        "search_leads_hint" to "Search lead by name, phone or company...",
        "call_lead" to "CALL",
        "view_timeline" to "TIMELINE",
        "incoming" to "Incoming",
        "outgoing" to "Outgoing",
        "duration" to "Duration",
        "outcome" to "Outcome",
        "notes" to "Notes",
        "follow_up" to "Follow-up",
        "save_notes" to "Save & Sync",
        "upload_recording" to "Upload Recording",
        "log_call" to "Log Call",
        "rec_unavailable_banner" to "Automatic call recording is unavailable on this device.",
        "rec_unavailable_detail" to "Android 16 voice communication security policy prevents direct cellular audio stream capture. Call metadata synchronization remains fully active.",
        "rec_uploaded_success" to "Recording uploaded successfully.",
        "call_logged_success" to "Call logged successfully.",
        "rec_upload_failed_retry" to "Recording upload failed. It will retry automatically.",
        "call_logged_rec_unavail" to "Call was logged, but audio recording is unavailable.",
        "session_expired" to "Your session has expired. Please sign in again.",
        "single_lead_matched" to "Matched CRM Lead",
        "multi_leads_found" to "Multiple leads found. Select the correct lead.",
        "no_lead_matched" to "No matching CRM lead found.",
        "login_title" to "Winstone CRM Sign In",
        "work_email" to "Work Email",
        "password" to "Password",
        "employee_id" to "Employee ID (Optional)",
        "sign_in" to "Sign In",
        "device_profile" to "Proton HyperX Profile",
        "hardware_model" to "Device Model",
        "android_version" to "Android Version",
        "build_version" to "OS Build",
        "capability_label" to "Recording Capability",
        "policy_title" to "Call Recording Policy",
        "policy_disclaimer" to "Call recording requirements vary by jurisdiction. Winstone is responsible for configuring and operating recording according to applicable law and company policy.",
        "authority_dashboard" to "Authority / Admin Fleet",
        "agent_stats" to "Agent Statistics",
        "total_calls" to "Total Calls",
        "audit_logs" to "CRM Audit Trail",
        "test_call_title" to "Test Call Mode & Verification",
        "run_test_call" to "RUN TEST CALL",
        "run_diagnostics" to "RUN FULL DIAGNOSTIC"
    )

    private val bnMap = mapOf(
        "app_title" to "উইনস্টোন অটো কল রেকর্ডার",
        "app_subtitle" to "উইনস্টোন সিআরএম সহযোগী",
        "crm_connected" to "CRM সংযুক্ত রয়েছে",
        "device_registered" to "ডিভাইস নিবন্ধিত",
        "recording_status" to "রেকর্ডিং স্ট্যাটাস",
        "todays_calls" to "আজকের মোট কল",
        "recorded_calls" to "রেকর্ডকৃত কল",
        "pending_upload" to "আপলোড অপেক্ষমাণ",
        "failed_uploads" to "আপলোড ব্যর্থ",
        "followups_due" to "ফলো-আপ বাকি",
        "my_leads" to "আমার লিডসমূহ",
        "recent_calls" to "সাম্প্রতিক কল",
        "recordings" to "রেকর্ডিংস",
        "sync_now" to "সিঙ্ক করুন",
        "settings" to "সেটিংস",
        "diagnostics" to "ডায়াগনস্টিকস",
        "test_call" to "টেস্ট কল",
        "search_leads_hint" to "নাম, ফোন বা কোম্পানি দিয়ে লিড খুঁজুন...",
        "call_lead" to "কল করুন",
        "view_timeline" to "টাইমলাইন",
        "incoming" to "ইনকামিং",
        "outgoing" to "আউটগোয়িং",
        "duration" to "সময়সীমা",
        "outcome" to "ফলাফল",
        "notes" to "নোট / বিবরণ",
        "follow_up" to "পরবর্তী ফলো-আপ",
        "save_notes" to "সংরক্ষণ ও সিঙ্ক",
        "upload_recording" to "রেকর্ডিং আপলোড করুন",
        "log_call" to "কল লগ করুন",
        "rec_unavailable_banner" to "এই ডিভাইসে স্বয়ংক্রিয় কল রেকর্ডিং উপলভ্য নয়।",
        "rec_unavailable_detail" to "অ্যান্ড্রয়েড ১৬ সিকিউরিটি পলিসির কারণে টু-ওয়ে সেলুলার অডিও সংরক্ষিত হচ্ছে না। কল মেটাডাটা ও টাইমলাইন সিঙ্ক চালু আছে।",
        "rec_uploaded_success" to "রেকর্ডিং সফলভাবে CRM-এ আপলোড হয়েছে।",
        "call_logged_success" to "কল সফলভাবে সংরক্ষণ করা হয়েছে।",
        "rec_upload_failed_retry" to "রেকর্ডিং আপলোড ব্যর্থ হয়েছে। ইন্টারনেট সংযোগ পাওয়া গেলে আবার চেষ্টা করা হবে।",
        "call_logged_rec_unavail" to "কল সংরক্ষণ করা হয়েছে, তবে অডিও রেকর্ডিং উপলভ্য নয়।",
        "session_expired" to "আপনার সেশনের মেয়াদ শেষ হয়েছে। অনুগ্রহ করে আবার লগইন করুন।",
        "single_lead_matched" to "সংযুক্ত CRM লিড",
        "multi_leads_found" to "একাধিক লিড পাওয়া গেছে। সঠিক লিডটি নির্বাচন করুন।",
        "no_lead_matched" to "কোনো মানানসই CRM লিড পাওয়া যায়নি।",
        "login_title" to "উইনস্টোন CRM লগইন",
        "work_email" to "অফিসিয়াল ইমেইল",
        "password" to "পাসওয়ার্ড",
        "employee_id" to "এমপ্লয়ি আইডি (ঐচ্ছিক)",
        "sign_in" to "লগইন করুন",
        "device_profile" to "প্রোটন হাইপার-এক্স প্রোফাইল",
        "hardware_model" to "ডিভাইস মডেল",
        "android_version" to "অ্যান্ড্রয়েড সংস্করণ",
        "build_version" to "বিল্ড নম্বর",
        "capability_label" to "রেকর্ডিং সক্ষমতা",
        "policy_title" to "কল রেকর্ডিং পলিসি ও সম্মতি",
        "policy_disclaimer" to "কল রেকর্ডিংয়ের আইনি প্রয়োজনীয়তা অঞ্চলভেদে ভিন্ন হতে পারে। প্রযোজ্য আইন ও নীতি মেনে রেকর্ডিং পরিচালনা করা উইনস্টোন কর্তৃপক্ষের দায়িত্ব।",
        "authority_dashboard" to "কর্তৃপক্ষ / অ্যাডমিন ড্যাশবোর্ড",
        "agent_stats" to "এজেন্ট কর্মক্ষমতা",
        "total_calls" to "সর্বমোট কল",
        "audit_logs" to "CRM অডিট ট্রেল",
        "test_call_title" to "টেস্ট কল মোড ও যাচাইকরণ",
        "run_test_call" to "টেস্ট কল চালান",
        "run_diagnostics" to "সম্পূর্ণ ডায়াগনস্টিক চালান"
    )
}
