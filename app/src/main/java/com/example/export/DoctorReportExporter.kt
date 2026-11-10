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

    fun buildDoctorClinicalHtmlReport(
        patientNameOrAlias: String,
        soberDays: Int,
        soberHours: Int,
        diaryEntries: List<WellnessDiaryEntity>,
        vitalsReadings: List<PhysiologicalReadingEntity>,
        selectedSupplements: Set<String>
    ): String {
        val generatedDate = dateFormatter.format(Date())
        val avgHeartRate = if (vitalsReadings.isNotEmpty()) {
            vitalsReadings.map { it.heartRateBpm }.average().toInt()
        } else 0
        val avgStress = if (vitalsReadings.isNotEmpty()) {
            vitalsReadings.map { it.stressIndex }.average().toInt()
        } else 0
        val avgCraving = if (diaryEntries.isNotEmpty()) {
            String.format(Locale.US, "%.1f", diaryEntries.map { it.cravingLevel }.average())
        } else "0.0"

        val vitalsRows = vitalsReadings.take(25).joinToString("\n") { v ->
            val dateStr = dateFormatter.format(Date(v.timestampMillis))
            val note = CryptoVault.decrypt(v.encryptedNote)
            val criticalBadge = if (v.isCriticalAlert) "<strong style='color:#B93829;'>ДА (Критический)</strong>" else "Норма"
            """
            <tr>
                <td>$dateStr</td>
                <td>${v.heartRateBpm} уд/мин</td>
                <td>${v.systolicBp}/${v.diastolicBp}</td>
                <td>${v.stressIndex}% (HRV ${v.hrvRmssdMs} мс)</td>
                <td>${v.sourceDevice}</td>
                <td>$criticalBadge</td>
                <td>$note</td>
            </tr>
            """.trimIndent()
        }

        val diaryRows = diaryEntries.take(25).joinToString("\n") { d ->
            val dateStr = dateFormatter.format(Date(d.timestampMillis))
            val emotion = runCatching { EmotionalState.valueOf(d.emotionalStateName).titleRu }
                .getOrDefault(d.emotionalStateName)
            val symptoms = CryptoVault.decrypt(d.encryptedSymptoms)
            val comment = CryptoVault.decrypt(d.encryptedComment)
            val hasPhoto = if (!d.photoUriString.isNullOrBlank()) "Прикреплено" else "—"
            """
            <tr>
                <td>$dateStr</td>
                <td>$emotion</td>
                <td>${d.cravingLevel} / 10</td>
                <td>${d.sleepHours} ч</td>
                <td>$symptoms</td>
                <td>$comment</td>
                <td>$hasPhoto</td>
            </tr>
            """.trimIndent()
        }

        val supplementsText = if (selectedSupplements.isEmpty()) {
            "Не отмечены пользователем"
        } else {
            selectedSupplements.joinToString(", ")
        }

        return """
        <!DOCTYPE html>
        <html lang="ru">
        <head>
            <meta charset="UTF-8">
            <title>Клиническая выписка самоконтроля — Ясный Берег</title>
            <style>
                body { font-family: sans-serif; color: #1C241E; margin: 24px; line-height: 1.5; }
                h1 { color: #1B5E3A; border-bottom: 2px solid #1B5E3A; padding-bottom: 8px; }
                h2 { color: #28586B; margin-top: 24px; }
                .summary-box { background: #F0EBE1; padding: 16px; border-radius: 8px; margin-bottom: 20px; }
                table { width: 100%; border-collapse: collapse; margin-top: 12px; font-size: 14px; }
                th, td { border: 1px solid #C8C2B6; padding: 8px 10px; text-align: left; }
                th { background: #D7EFE0; color: #0A331C; }
                .footer { margin-top: 32px; font-size: 12px; color: #4A554D; }
            </style>
        </head>
        <body>
            <h1>Клиническая сводка для лечащего врача (Нарколог / Терапевт / Кардиолог)</h1>
            <div class="summary-box">
                <p><strong>Пациент / Профиль:</strong> $patientNameOrAlias</p>
                <p><strong>Дата формирования отчета:</strong> $generatedDate</p>
                <p><strong>Текущий период воздержания от алкоголя:</strong> $soberDays дн. ($soberHours ч.)</p>
                <p><strong>Средний уровень тяги по дневнику:</strong> $avgCraving из 10</p>
                <p><strong>Средняя ЧСС / Индекс физиологического стресса:</strong> $avgHeartRate уд/мин / $avgStress%</p>
                <p><strong>Отмеченные нутрицевтики и витамины:</strong> $supplementsText</p>
            </div>

            <h2>1. Динамика физиологических показателей и стресса (BLE / Тонометр)</h2>
            <table>
                <thead>
                    <tr>
                        <th>Дата и время</th>
                        <th>Пульс (ЧСС)</th>
                        <th>АД (мм рт. ст.)</th>
                        <th>Индекс стресса / ВСР</th>
                        <th>Источник</th>
                        <th>Критический порог</th>
                        <th>Примечание</th>
                    </tr>
                </thead>
                <tbody>
                    $vitalsRows
                </tbody>
            </table>

            <h2>2. Дневник самочувствия, сна и эмоционального состояния</h2>
            <table>
                <thead>
                    <tr>
                        <th>Дата и время</th>
                        <th>Эмоциональный фон</th>
                        <th>Тяга (0-10)</th>
                        <th>Сон</th>
                        <th>Физические симптомы</th>
                        <th>Комментарий пациента</th>
                        <th>Фото</th>
                    </tr>
                </thead>
                <tbody>
                    $diaryRows
                </tbody>
            </table>

            <div class="footer">
                Сформировано локально в приложении «Ясный Берег». Личные медицинские данные хранятся на устройстве пользователя с шифрованием AES-256-GCM (Android Keystore) и расшифрованы только по запросу пациента для передачи врачу.
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    fun buildDoctorCsvReport(
        diaryEntries: List<WellnessDiaryEntity>,
        vitalsReadings: List<PhysiologicalReadingEntity>
    ): String {
        val sb = StringBuilder()
        sb.appendLine("Тип_записи;Дата_время;Эмоция_или_Источник;Тяга_или_ЧСС;Сон_или_АД;Стресс_процент;Симптомы;Комментарий")
        diaryEntries.forEach { d ->
            val dateStr = dateFormatter.format(Date(d.timestampMillis))
            val emotion = runCatching { EmotionalState.valueOf(d.emotionalStateName).titleRu }
                .getOrDefault(d.emotionalStateName)
            val symptoms = CryptoVault.decrypt(d.encryptedSymptoms).replace(";", ",")
            val comment = CryptoVault.decrypt(d.encryptedComment).replace(";", ",")
            sb.appendLine("ДНЕВНИК;$dateStr;$emotion;Тяга ${d.cravingLevel}/10;Сон ${d.sleepHours}ч;-;$symptoms;$comment")
        }
        vitalsReadings.forEach { v ->
            val dateStr = dateFormatter.format(Date(v.timestampMillis))
            val note = CryptoVault.decrypt(v.encryptedNote).replace(";", ",")
            sb.appendLine("ФИЗИОЛОГИЯ;$dateStr;${v.sourceDevice};ЧСС ${v.heartRateBpm};АД ${v.systolicBp}/${v.diastolicBp};${v.stressIndex}%;Критический:${v.isCriticalAlert};$note")
        }
        return sb.toString()
    }

    fun shareClinicalReportFile(
        context: Context,
        fileName: String,
        mimeType: String,
        content: String
    ) {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, fileName)
        file.writeText(content, Charsets.UTF_8)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Медицинская выписка самоконтроля — Ясный Берег")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(shareIntent, "Отправить отчет лечащему врачу")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun printOrSavePdfReport(context: Context, htmlContent: String) {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val jobName = "Отчет_Для_Врача_Ясный_Берег"
                val printAdapter = view.createPrintDocumentAdapter(jobName)
                printManager?.print(
                    jobName,
                    printAdapter,
                    PrintAttributes.Builder().build()
                )
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }
}
