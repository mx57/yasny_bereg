package com.example.export

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.FileProvider
import com.example.data.local.EmotionalState
import com.example.data.local.PhysiologicalReadingEntity
import com.example.data.local.WellnessDiaryEntity
import com.example.security.CryptoVault
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DoctorReportExporter {
    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.forLanguageTag("ru"))

    private fun escapeHtml(value: String): String = value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")

    fun buildDoctorClinicalHtmlReport(
        patientNameOrAlias: String,
        soberDays: Int,
        soberHours: Int,
        diaryEntries: List<WellnessDiaryEntity>,
        vitalsReadings: List<PhysiologicalReadingEntity>,
        selectedSupplements: Set<String>
    ): String {
        val generatedDate = dateFormatter.format(Date())
        val heartRates = vitalsReadings.map { it.heartRateBpm }.filter { it > 0 }
        val avgHeartRate = if (heartRates.isNotEmpty()) "${heartRates.average().toInt()} уд/мин" else "—"
        val avgCraving = if (diaryEntries.isNotEmpty()) {
            String.format(Locale.US, "%.1f из 10", diaryEntries.map { it.cravingLevel }.average())
        } else "—"
        val vitalsRows = vitalsReadings.take(100).joinToString("\n") { reading ->
            val dateStr = escapeHtml(dateFormatter.format(Date(reading.timestampMillis)))
            val note = escapeHtml(CryptoVault.decrypt(reading.encryptedNote))
            val bloodPressure = if (reading.systolicBp > 0 && reading.diastolicBp > 0) {
                "${reading.systolicBp}/${reading.diastolicBp}"
            } else "не записано"
            val hrv = if (reading.hrvRmssdMs > 0) "${reading.hrvRmssdMs} мс (если переданы RR-интервалы)" else "не записано"
            """
            <tr>
                <td>$dateStr</td>
                <td>${reading.heartRateBpm} уд/мин</td>
                <td>$bloodPressure</td>
                <td>$hrv</td>
                <td>${escapeHtml(reading.sourceDevice)}</td>
                <td>$note</td>
            </tr>
            """.trimIndent()
        }
        val diaryRows = diaryEntries.take(100).joinToString("\n") { entry ->
            val dateStr = escapeHtml(dateFormatter.format(Date(entry.timestampMillis)))
            val emotion = escapeHtml(runCatching { EmotionalState.valueOf(entry.emotionalStateName).titleRu }.getOrDefault(entry.emotionalStateName))
            val symptoms = escapeHtml(CryptoVault.decrypt(entry.encryptedSymptoms))
            val comment = escapeHtml(CryptoVault.decrypt(entry.encryptedComment))
            val hasPhoto = if (!entry.photoUriString.isNullOrBlank()) "Есть вложение" else "—"
            """
            <tr>
                <td>$dateStr</td>
                <td>$emotion</td>
                <td>${entry.cravingLevel}/10</td>
                <td>${entry.sleepHours} ч</td>
                <td>$symptoms</td>
                <td>$comment</td>
                <td>$hasPhoto</td>
            </tr>
            """.trimIndent()
        }
        val supplementsText = if (selectedSupplements.isEmpty()) {
            "Нет отмеченных пунктов"
        } else {
            selectedSupplements.map(::escapeHtml).sorted().joinToString(", ")
        }
        return """
        <!DOCTYPE html>
        <html lang="ru">
        <head>
            <meta charset="UTF-8">
            <title>Личная сводка — Ясный Берег</title>
            <style>
                body { font-family: sans-serif; color: #1C241E; margin: 24px; line-height: 1.5; }
                h1 { color: #1B5E3A; border-bottom: 2px solid #1B5E3A; padding-bottom: 8px; }
                h2 { color: #28586B; margin-top: 24px; }
                .summary-box { background: #F0EBE1; padding: 16px; border-radius: 8px; margin-bottom: 20px; }
                table { width: 100%; border-collapse: collapse; margin-top: 12px; font-size: 13px; }
                th, td { border: 1px solid #C8C2B6; padding: 8px 10px; text-align: left; }
                th { background: #D7EFE0; color: #0A331C; }
                .footer { margin-top: 32px; font-size: 12px; color: #4A554D; }
            </style>
        </head>
        <body>
            <h1>Личная сводка записей для обсуждения со специалистом</h1>
            <div class="summary-box">
                <p><strong>Профиль / псевдоним:</strong> ${escapeHtml(patientNameOrAlias)}</p>
                <p><strong>Дата формирования:</strong> ${escapeHtml(generatedDate)}</p>
                <p><strong>Время по счётчику пользователя:</strong> $soberDays дн. ($soberHours ч.)</p>
                <p><strong>Среднее значение личной оценки тяги:</strong> $avgCraving</p>
                <p><strong>Средний введённый пульс:</strong> $avgHeartRate</p>
                <p><strong>Отмечено пользователем как пункт для обсуждения (не подтверждение приёма):</strong> $supplementsText</p>
            </div>
            <p><strong>Важно:</strong> это пользовательские записи, не диагноз, не интерпретация анализов и не медицинское заключение. Отсутствие записи не означает норму.</p>

            <h2>Показатели, внесённые пользователем</h2>
            <table>
                <thead><tr><th>Дата и время</th><th>Пульс</th><th>Давление</th><th>HRV, если измерено датчиком</th><th>Источник</th><th>Заметка</th></tr></thead>
                <tbody>$vitalsRows</tbody>
            </table>

            <h2>Дневник самочувствия</h2>
            <table>
                <thead><tr><th>Дата и время</th><th>Состояние</th><th>Тяга (личная оценка)</th><th>Сон</th><th>Симптомы</th><th>Комментарий</th><th>Вложение</th></tr></thead>
                <tbody>$diaryRows</tbody>
            </table>
            <div class="footer">Сводка сформирована локально в приложении «Ясный Берег» по запросу пользователя. Передавайте её специалисту только если считаете это уместным.</div>
        </body>
        </html>
        """.trimIndent()
    }

    fun buildDoctorCsvReport(
        diaryEntries: List<WellnessDiaryEntity>,
        vitalsReadings: List<PhysiologicalReadingEntity>
    ): String {
        val output = StringBuilder()
        output.appendLine("Тип_записи;Дата_время;Эмоция_или_Источник;Тяга_или_ЧСС;Сон_или_АД;HRV_RMSSD_если_измерено;Симптомы;Комментарий")
        diaryEntries.forEach { entry ->
            val date = dateFormatter.format(Date(entry.timestampMillis))
            val emotion = runCatching { EmotionalState.valueOf(entry.emotionalStateName).titleRu }.getOrDefault(entry.emotionalStateName)
            val symptoms = CryptoVault.decrypt(entry.encryptedSymptoms).replace(";", ",")
            val comment = CryptoVault.decrypt(entry.encryptedComment).replace(";", ",")
            output.appendLine("ДНЕВНИК;$date;$emotion;Тяга ${entry.cravingLevel}/10;Сон ${entry.sleepHours}ч;—;$symptoms;$comment")
        }
        vitalsReadings.forEach { reading ->
            val date = dateFormatter.format(Date(reading.timestampMillis))
            val note = CryptoVault.decrypt(reading.encryptedNote).replace(";", ",")
            val pressure = if (reading.systolicBp > 0 && reading.diastolicBp > 0) "${reading.systolicBp}/${reading.diastolicBp}" else "не записано"
            val hrv = if (reading.hrvRmssdMs > 0) "${reading.hrvRmssdMs} мс" else "не записано"
            output.appendLine("ЗАМЕР;$date;${reading.sourceDevice};ЧСС ${reading.heartRateBpm};АД $pressure;$hrv;;$note")
        }
        return output.toString()
    }

    fun shareClinicalReportFile(context: Context, fileName: String, mimeType: String, content: String) {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, fileName)
        file.writeText(content, Charsets.UTF_8)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Личная сводка — Ясный Берег")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Выберите, куда передать сводку").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun printOrSavePdfReport(context: Context, htmlContent: String) {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val jobName = "Личная_сводка_Ясный_Берег"
                val printAdapter = view.createPrintDocumentAdapter(jobName)
                printManager?.print(jobName, printAdapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }
}
