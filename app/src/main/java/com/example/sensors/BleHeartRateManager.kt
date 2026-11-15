package com.example.sensors

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class WearableTelemetryState(
    val isScanning: Boolean = false,
    val isConnected: Boolean = false,
    val connectedDeviceName: String? = null,
    val currentHeartRateBpm: Int = 0,
    val currentHrvRmssdMs: Int = 0,
    val currentStressIndex: Int = 0,
    val statusMessage: String = "Готов к поиску Bluetooth-пульсометра или ручному замеру",
    val discoveredDevices: List<DiscoveredBleDevice> = emptyList()
)

data class DiscoveredBleDevice(
    val name: String,
    val address: String
)

/**
 * Reads pulse from Bluetooth Low Energy (BLE) devices using the standard GATT Heart Rate profile.
 * HRV is calculated only when actual RR intervals are transmitted; no stress score is inferred.
 */
class BleHeartRateManager(private val context: Context) {

    private val _state = MutableStateFlow(WearableTelemetryState())
    val state: StateFlow<WearableTelemetryState> = _state.asStateFlow()

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private var activeGatt: BluetoothGatt? = null
    private val recentRrIntervalsMs = ArrayDeque<Int>(32)

    companion object {
        val HEART_RATE_SERVICE_UUID: UUID =
            UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
        val HEART_RATE_MEASUREMENT_CHAR_UUID: UUID =
            UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
        val CLIENT_CHARACTERISTIC_CONFIG_UUID: UUID =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        fun calculateRmssdFromRrIntervals(rrIntervals: List<Int>): Int {
            if (rrIntervals.size < 3) return 0
            val diffsSquared = rrIntervals.zipWithNext { a, b -> (b - a).toDouble() * (b - a).toDouble() }
            return sqrt(diffsSquared.average()).roundToInt().coerceAtLeast(0)
        }
    }

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device ?: return
            val name = device.name ?: "BLE Браслет (${device.address.takeLast(5)})"
            val currentList = _state.value.discoveredDevices
            if (currentList.none { it.address == device.address }) {
                _state.value = _state.value.copy(
                    discoveredDevices = currentList + DiscoveredBleDevice(name, device.address),
                    statusMessage = "Найдено устройств: ${currentList.size + 1}. Нажмите для подключения."
                )
            }
        }

        override fun onScanFailed(errorCode: Int) {
            _state.value = _state.value.copy(
                isScanning = false,
                statusMessage = "Не удалось запустить BLE-поиск (код $errorCode). Проверьте, включен ли Bluetooth."
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        val adapter = bluetoothAdapter
        if (adapter == null || !adapter.isEnabled) {
            _state.value = _state.value.copy(
                isScanning = false,
                statusMessage = "Bluetooth отключен или недоступен на устройстве. Вы можете внести замер пульса вручную или использовать секундомер пульса ниже."
            )
            return
        }

        val scanner = adapter.bluetoothLeScanner
        if (scanner == null) {
            _state.value = _state.value.copy(
                isScanning = false,
                statusMessage = "Сканер Bluetooth LE недоступен. Используйте быстрый замер пульса или ручной ввод."
            )
            return
        }

        try {
            val filter = ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(HEART_RATE_SERVICE_UUID))
                .build()
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()
            _state.value = _state.value.copy(
                isScanning = true,
                discoveredDevices = emptyList(),
                statusMessage = "Поиск носимых пульсометров и фитнес-браслетов (профиль Heart Rate 0x180D)..."
            )
            scanner.startScan(listOf(filter), settings, scanCallback)
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isScanning = false,
                statusMessage = "Ошибка запуска BLE-сканера: ${e.localizedMessage ?: "Проверьте разрешения"}"
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        try {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        } catch (_: Exception) {
        }
        _state.value = _state.value.copy(
            isScanning = false,
            statusMessage = if (_state.value.discoveredDevices.isEmpty()) {
                "Поиск завершен. Если у вас нет BLE-датчика с открытым профилем пульса, используйте секундомер пульса или ручной ввод."
            } else {
                "Поиск завершен. Выберите устройство из списка."
            }
        )
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(address: String) {
        stopScan()
        val device = bluetoothAdapter?.getRemoteDevice(address) ?: return
        _state.value = _state.value.copy(
            statusMessage = "Подключение к ${device.name ?: address}..."
        )
        activeGatt?.close()
        activeGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    recentRrIntervalsMs.clear()
                    _state.value = _state.value.copy(
                        isConnected = true,
                        connectedDeviceName = gatt.device.name ?: address,
                        statusMessage = "Подключено: ${gatt.device.name ?: address}. Чтение телеметрии..."
                    )
                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    _state.value = _state.value.copy(
                        isConnected = false,
                        connectedDeviceName = null,
                        statusMessage = "Носимое устройство отключено."
                    )
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                val service = gatt.getService(HEART_RATE_SERVICE_UUID)
                val characteristic = service?.getCharacteristic(HEART_RATE_MEASUREMENT_CHAR_UUID)
                if (characteristic != null) {
                    gatt.setCharacteristicNotification(characteristic, true)
                    val descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID)
                    if (descriptor != null) {
                        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        gatt.writeDescriptor(descriptor)
                    }
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic
            ) {
                if (characteristic.uuid == HEART_RATE_MEASUREMENT_CHAR_UUID) {
                    parseHeartRateMeasurement(characteristic.value)
                }
            }
        })
    }

    private fun parseHeartRateMeasurement(bytes: ByteArray?) {
        if (bytes == null || bytes.isEmpty()) return
        val flags = bytes[0].toInt()
        val isUint16 = (flags and 0x01) != 0
        val hasRrInterval = (flags and 0x10) != 0

        var offset = 1
        val bpm = if (isUint16 && bytes.size >= 3) {
            val v = (bytes[offset].toInt() and 0xFF) or ((bytes[offset + 1].toInt() and 0xFF) shl 8)
            offset += 2
            v
        } else if (bytes.size >= 2) {
            val v = bytes[offset].toInt() and 0xFF
            offset += 1
            v
        } else {
            return
        }

        // Energy expended field present?
        if ((flags and 0x08) != 0) {
            offset += 2
        }

        if (hasRrInterval) {
            while (offset + 1 < bytes.size) {
                val rawRr = (bytes[offset].toInt() and 0xFF) or ((bytes[offset + 1].toInt() and 0xFF) shl 8)
                val rrMs = (rawRr * 1000) / 1024
                if (rrMs in 300..2000) {
                    if (recentRrIntervalsMs.size >= 20) recentRrIntervalsMs.removeFirst()
                    recentRrIntervalsMs.addLast(rrMs)
                }
                offset += 2
            }
        }

        val hrv = calculateRmssdFromRrIntervals(recentRrIntervalsMs.toList())
        _state.value = _state.value.copy(
            currentHeartRateBpm = bpm,
            currentHrvRmssdMs = hrv,
            currentStressIndex = 0,
            statusMessage = if (hrv > 0) "Измерено с датчика: пульс $bpm уд/мин; RR-интервалы доступны (HRV не является диагнозом)." else "Измерен пульс $bpm уд/мин; этот датчик не передал достаточно RR-интервалов для оценки HRV."
        )
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        try {
            activeGatt?.disconnect()
            activeGatt?.close()
        } catch (_: Exception) {
        }
        activeGatt = null
        _state.value = _state.value.copy(
            isConnected = false,
            connectedDeviceName = null,
            statusMessage = "Устройство отключено."
        )
    }
}
