package com.example.telephony

/**
 * Utility functions for Phone Number normalization and formatting,
 * specifically handling Bangladesh (+880) mobile and landline formats,
 * as well as generic E.164 numbers.
 */
object PhoneNumberUtils {

    /**
     * Normalizes a phone number to a consistent internal format (e.g. +8801XXXXXXXXX).
     * Does not modify or replace the original display number in the UI.
     */
    fun normalize(rawNumber: String?): String {
        if (rawNumber.isNullOrBlank()) return ""

        // Strip non-digit characters except leading '+'
        val cleaned = rawNumber.trim().replace(Regex("[^0-9+]"), "")

        // Handle Bangladesh national formats
        return when {
            // Case 1: Already has international +880 prefix
            cleaned.startsWith("+880") -> {
                cleaned
            }
            // Case 2: Has 880 prefix without plus
            cleaned.startsWith("880") -> {
                "+$cleaned"
            }
            // Case 3: Local BD mobile starting with 01 (e.g. 01712345678)
            cleaned.startsWith("01") && (cleaned.length == 11) -> {
                "+88$cleaned"
            }
            // Case 4: Local BD mobile without leading zero (e.g. 1712345678)
            cleaned.startsWith("1") && (cleaned.length == 10) -> {
                "+880$cleaned"
            }
            // Case 5: Standard plus international
            cleaned.startsWith("+") -> {
                cleaned
            }
            // Fallback: Default with plus prefix if purely digits
            else -> {
                if (cleaned.isNotBlank()) "+$cleaned" else ""
            }
        }
    }

    /**
     * Formats normalized or raw phone numbers into clean presentation string.
     */
    fun formatDisplay(number: String): String {
        val norm = normalize(number)
        return if (norm.startsWith("+8801") && norm.length == 14) {
            // Format +880 1XXX-XXXXXX
            "${norm.substring(0, 4)} ${norm.substring(4, 8)}-${norm.substring(8)}"
        } else {
            number
        }
    }

    /**
     * Checks if two phone numbers match under normalization.
     */
    fun isMatch(number1: String?, number2: String?): Boolean {
        if (number1.isNullOrBlank() || number2.isNullOrBlank()) return false
        val n1 = normalize(number1)
        val n2 = normalize(number2)
        return n1 == n2 || n1.endsWith(n2.takeLast(10)) || n2.endsWith(n1.takeLast(10))
    }
}
