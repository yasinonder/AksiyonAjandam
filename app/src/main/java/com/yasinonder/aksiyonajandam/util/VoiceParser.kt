package com.yasinonder.aksiyonajandam.util

import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

data class ParsedVoice(
    val title: String,
    val date: String?,
    val time: String?,
    val type: String?,
    val priority: String?
)

object VoiceParser {
    private val turkish = Locale("tr", "TR")

    fun parse(text: String): ParsedVoice {
        val lower = text.lowercase(turkish)

        val date = when {
            "yarın" in lower -> LocalDate.now().plusDays(1).toString()
            "bugün" in lower -> LocalDate.now().toString()
            else -> null
        }

        val timeRegex = Regex("""saat\s*(\d{1,2})(?:[:.]?(\d{2}))?""")
        val match = timeRegex.find(lower)
        val time = match?.let {
            val hour = it.groupValues[1].toIntOrNull()?.coerceIn(0, 23) ?: 9
            val minute = it.groupValues.getOrNull(2)
                ?.takeIf(String::isNotBlank)
                ?.toIntOrNull()
                ?.coerceIn(0, 59) ?: 0
            LocalTime.of(hour, minute).toString()
        }

        val type = when {
            listOf("araba", "araç", "servis", "yakıt").any { it in lower } -> "Araç"
            listOf("randevu", "doktor", "görüşme", "toplantı").any { it in lower } -> "Randevu"
            listOf("market", "alışveriş", "satın al").any { it in lower } -> "Alışveriş"
            listOf("evrak", "belge", "fatura", "dosya").any { it in lower } -> "Evrak"
            listOf("görev", "iş", "yapılacak").any { it in lower } -> "Görev"
            else -> null
        }

        val priority = when {
            "kritik" in lower || "acil" in lower -> "Kritik"
            "önemli" in lower -> "Önemli"
            else -> null
        }

        val cleaned = text
            .replace(Regex("""(?i)\bbugün\b|\byarın\b"""), "")
            .replace(timeRegex, "")
            .replace(Regex("""\s+"""), " ")
            .trim(' ', ',', '.', '-')
            .ifBlank { text.trim() }

        return ParsedVoice(
            title = cleaned,
            date = date,
            time = time,
            type = type,
            priority = priority
        )
    }
}
