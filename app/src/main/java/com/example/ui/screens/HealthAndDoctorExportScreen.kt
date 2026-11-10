package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.MethodsAndScienceCatalog
import com.example.data.local.PhysiologicalReadingEntity
import com.example. data.local.WellnessDiaryEntity
import com.example.export.DoctorReportExporter
import com.example.security.CryptoVault
import com.example.sensors.WearableTelemetryState
import com.example.ui.UserProfileState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HealthAndDoctorExportScreen(
    profile: UserProfileState,
    wearableState: WearableTelemetryState,
    vitalsReadings: List<PhysiologicalReadingEntity>,
    diaryEntries: List<WellnessDiaryEntity>,
    onStartBleScan: () -> Unit,
    onStopBleScan: () -> Unit,
    onConnectBleDevice: (String) -> Unit,
    onSaveBleReading: () -> Unit,
    onAddManualVitals: (Int, Int, Int, String, String) -> Unit,
    onBackToHome: () -> Unit
) {
    BackHandler(onBack = onBackToHome)
    val context = LocalContext.current

    // Manual Vitals Input State
    var bpmInput by remember { mutableStateOf("74") }
    var sysInput by remember { mutableStateOf("125") }
    var diaInput by remember { mutableStateOf("80") }
    var noteInput by remember { mutableStateOf("") }

    // Interactive Tap-to-Measure Pulse Counter State (for users without a BLE belt nearby)
    val tapTimestamps = remember { mutableStateListOf<Long>() }
    var measuredTapBpm by remember { mutableStateOf<Int?>(null) }
    var lastTapTime by remember { mutableLongStateOf(0L) }

    val blePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        onStartBleScan()
    }

    val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.forLanguageTag("ru")) }

    val supplementNames = remember(profile.checkedSupplementIds) {
        MethodsAndScienceCatalog.supplementsGuide
            .filter { profile.checkedSupplementIds.contains(it.id) }
            .map { it.name }
            .toSet()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Носимые устройства, Стресс и Отчет для Врача",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        // 1. BLE Wearable Heart Rate & Stress Integration Card
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ble_wearable_card"),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Watch,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Интеграция с носимыми устройствами (Bluetooth LE)",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = wearableState.statusMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (wearableState.currentHeartRateBpm > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Телеметрия с датчика: ЧСС ${wearableState.currentHeartRateBpm} уд/мин",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Индекс стресса: ${wearableState.currentStressIndex}% • ВСР (RMSSD): ${wearableState.currentHrvRmssdMs} мс",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = onSaveBleReading,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Сохранить замер с браслета в медицинский дневник")
                                }
                            }
                        }
                    }

                    if (wearableState.discoveredDevices.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Найденные Bluetooth-устройства:",
                            style = MaterialTheme.typography.labelLarge
                        )
                        wearableState.discoveredDevices.forEach { dev ->
                            OutlinedButton(
                                onClick = { onConnectBleDevice(dev.address) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text("Подключить: ${dev.name} (${dev.address})")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    blePermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.BLUETOOTH_SCAN,
                                            Manifest.permission.BLUETOOTH_CONNECT
                                        )
                                    )
                                } else {
                                    onStartBleScan()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 54.dp)
                                .testTag("scan_ble_wearables_button")
                        ) {
                            Icon(imageVector = Icons.Default.BluetoothSearching, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (wearableState.isScanning) "Идет поиск..." else "Найти браслет / пульсометр")
                        }
                        if (wearableState.isScanning) {
                            OutlinedButton(
                                onClick = onStopBleScan,
                                modifier = Modifier.heightIn(min = 54.dp)
                            ) {
                                Text("Стоп")
                            }
                        }
                    }
                }
            }
        }

        // 2. Interactive Wrist Pulse Tap Meter + Manual Vitals Entry (with Critical State Alert)
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("manual_vitals_card"),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Контроль пульса, давления и уровня стресса",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "При пульсе ≥ 100 уд/мин или давлении ≥ 155 мм рт. ст. система автоматически включит критическое оповещение защиты от срыва.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    // Interactive Tap Pulse Counter for seniors without BLE sensors
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Быстрый замер пульса по касанию (без браслета):",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Приложите два пальца к запястью и нажимайте кнопку в такт ударам сердца (5–8 нажатий):",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val now = System.currentTimeMillis()
                                        if (now - lastTapTime > 3000L) {
                                            tapTimestamps.clear()
                                        }
                                        lastTapTime = now
                                        tapTimestamps.add(now)
                                        if (tapTimestamps.size >= 4) {
                                            val intervals = tapTimestamps.zipWithNext { a, b -> b - a }
                                            val avgMs = intervals.average()
                                            if (avgMs > 250) {
                                                val bpm = (60_000.0 / avgMs).roundToInt().coerceIn(45, 190)
                                                measuredTapBpm = bpm
                                                bpmInput = bpm.toString()
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 54.dp)
                                        .testTag("pulse_tap_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    Icon(imageVector = Icons.Default.TouchApp, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (measuredTapBpm != null) "Пульс: $measuredTapBpm уд/мин (Нажатий: ${tapTimestamps.size})"
                                        else "Нажимать в такт пульсу (${tapTimestamps.size})"
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = bpmInput,
                            onValueChange = { bpmInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Пульс (ЧСС)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("vitals_bpm_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = sysInput,
                            onValueChange = { sysInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Верхнее АД") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("vitals_sys_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = diaInput,
                            onValueChange = { diaInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Нижнее АД") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("vitals_dia_input"),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("Комментарий к замеру (например: после прогулки / вечер)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            onAddManualVitals(
                                bpmInput.toIntOrNull() ?: 74,
                                sysInput.toIntOrNull() ?: 125,
                                diaInput.toIntOrNull() ?: 80,
                                if (measuredTapBpm != null) "Пульсометр касаний / Тонометр" else "Тонометр / Ручной ввод",
                                noteInput.ifBlank { "Плановый контроль состояния" }
                            )
                            noteInput = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .testTag("save_vitals_button")
                    ) {
                        Text(
                            text = "Рассчитать стресс и сохранить замер",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        // 3. Export Data for Consulting a Physician Card
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("doctor_export_card"),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Экспорт данных для консультации с врачом",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Формирует полную клиническую выписку (стаж трезвости, динамика ЧСС, АД, индекс стресса, принимаемые биодобавки и расшифрованный дневник самочувствия) для вашего нарколога, терапевта или кардиолога.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            val html = DoctorReportExporter.buildDoctorClinicalHtmlReport(
                                patientNameOrAlias = profile.userName,
                                soberDays = profile.soberDays,
                                soberHours = profile.totalSoberHours,
                                diaryEntries = diaryEntries,
                                vitalsReadings = vitalsReadings,
                                selectedSupplements = supplementNames
                            )
                            DoctorReportExporter.printOrSavePdfReport(context, html)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 54.dp)
                            .testTag("print_pdf_doctor_report_button")
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Печать / Сохранить PDF-отчет для врача")
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = {
                                val html = DoctorReportExporter.buildDoctorClinicalHtmlReport(
                                    patientNameOrAlias = profile.userName,
                                    soberDays = profile.soberDays,
                                    soberHours = profile.totalSoberHours,
                                    diaryEntries = diaryEntries,
                                    vitalsReadings = vitalsReadings,
                                    selectedSupplements = supplementNames
                                )
                                DoctorReportExporter.shareClinicalReportFile(
                                    context = context,
                                    fileName = "clinical_report_yasny_bereg.html",
                                    mimeType = "text/html",
                                    content = html
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 52.dp)
                                .testTag("share_html_doctor_report_button")
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Отправить врачу")
                        }

                        OutlinedButton(
                            onClick = {
                                val csv = DoctorReportExporter.buildDoctorCsvReport(
                                    diaryEntries = diaryEntries,
                                    vitalsReadings = vitalsReadings
                                )
                                DoctorReportExporter.shareClinicalReportFile(
                                    context = context,
                                    fileName = "medical_data_yasny_bereg.csv",
                                    mimeType = "text/csv",
                                    content = csv
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 52.dp)
                                .testTag("share_csv_doctor_report_button")
                        ) {
                            Icon(imageVector = Icons.Default.Description, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Таблица CSV")
                        }
                    }
                }
            }
        }

        // 4. Vitals & Stress History List
        item {
            Text(
                text = "Журнал физиологических замеров стресса (${vitalsReadings.size})",
                style = MaterialTheme.typography.titleLarge
            )
        }

        items(vitalsReadings, key = { it.id }) { reading ->
            val decryptedNote = remember(reading.encryptedNote) {
                CryptoVault.decrypt(reading.encryptedNote)
            }
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vitals_item_${reading.id}"),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (reading.isCriticalAlert) MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = dateFormatter.format(Date(reading.timestampMillis)),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = reading.sourceDevice,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Пульс: ${reading.heartRateBpm} уд/мин • АД: ${reading.systolicBp}/${reading.diastolicBp} мм рт. ст.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Индекс стресса: ${reading.stressIndex}% • Вариабельность (RMSSD): ${reading.hrvRmssdMs} мс",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (decryptedNote.isNotBlank()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        Text(
                            text = decryptedNote,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
