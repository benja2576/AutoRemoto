package com.example.data.repository

import com.example.data.model.ConnectionStatus
import com.example.data.model.ControlRequest
import com.example.data.model.LogSeverity
import com.example.data.model.MotorState
import com.example.data.model.OperationMode
import com.example.data.model.VehicleConfig
import com.example.data.model.VehicleTelemetry
import com.example.data.network.IoTCarApiClient
import com.example.data.security.SecurityAuditLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Repositorio central de datos y control del vehículo IoT.
 * Integra de forma desacoplada el Modo Real (Hardware ESP32 Wi-Fi) y el Modo Simulación.
 *
 * Cumple con los criterios de robustez, hilos secundarios (Coroutines), reconexión automática
 * y parada por fallo seguro (Fail-Safe Default según ISO/IEC 27400).
 */
class VehicleRepository(
    private val apiClient: IoTCarApiClient = IoTCarApiClient(),
    private val scope: CoroutineScope
) {

    private val _operationMode = MutableStateFlow(OperationMode.SIMULATION)
    val operationMode: StateFlow<OperationMode> = _operationMode.asStateFlow()

    private val _config = MutableStateFlow(VehicleConfig())
    val config: StateFlow<VehicleConfig> = _config.asStateFlow()

    private val _telemetry = MutableStateFlow(VehicleTelemetry())
    val telemetry: StateFlow<VehicleTelemetry> = _telemetry.asStateFlow()

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private var pollingJob: Job? = null
    private var simulationJob: Job? = null
    private var watchdogJob: Job? = null

    // Variables internas del simulador de física del vehículo
    private var simDistance = 65.0f
    private var simBatteryVoltage = 7.8f
    private var simUptimeSeconds = 0L

    init {
        // Iniciar en modo simulación para que el alumno pueda evaluar y probar de inmediato
        startCurrentMode()
    }

    fun setOperationMode(mode: OperationMode) {
        if (_operationMode.value == mode) return
        _operationMode.value = mode
        SecurityAuditLogger.logEvent(
            eventType = "CAMBIO_MODO",
            description = "Modo de operación cambiado a: ${mode.label}",
            severity = LogSeverity.INFO
        )
        // Detener motores antes de cambiar de modo por seguridad
        stopMotorsLocal()
        startCurrentMode()
    }

    fun updateConfig(newConfig: VehicleConfig) {
        _config.value = newConfig
        SecurityAuditLogger.logEvent(
            eventType = "CONFIGURACION_ACTUALIZADA",
            description = "IP: ${newConfig.esp32Ip}:${newConfig.port}, Umbral obstáculo: ${newConfig.obstacleThresholdCm}cm",
            severity = LogSeverity.INFO
        )
    }

    private fun startCurrentMode() {
        pollingJob?.cancel()
        simulationJob?.cancel()
        watchdogJob?.cancel()

        when (_operationMode.value) {
            OperationMode.SIMULATION -> startSimulation()
            OperationMode.REAL_HARDWARE -> startHardwarePolling()
        }
    }

    /**
     * MODO SIMULACIÓN: Simula el comportamiento del vehículo, sensor ultrasónico HC-SR04,
     * dinámica de distancia hacia obstáculos, voltaje de batería y activación de parada segura.
     */
    private fun startSimulation() {
        _connectionStatus.value = ConnectionStatus.Connected("127.0.0.1 (Virtual)", 14L, -42)
        SecurityAuditLogger.logEvent(
            eventType = "SIMULADOR_ACTIVO",
            description = "Simulador de telemetría y física de vehículo iniciado.",
            severity = LogSeverity.INFO
        )

        simulationJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(350)
                simUptimeSeconds++

                val currentMotorState = _telemetry.value.motorState
                val threshold = _config.value.obstacleThresholdCm

                // Simular variación de distancia según movimiento del vehículo
                when (currentMotorState) {
                    MotorState.FORWARD -> {
                        // Avanzando hacia la pared/obstáculo
                        simDistance -= (4.5f + Random.nextFloat() * 1.5f)
                        if (simDistance <= threshold) {
                            simDistance = threshold.coerceAtLeast(10f)
                            // PARADA DE EMERGENCIA POR OBSTÁCULO AUTOMÁTICA (Fail-Safe)
                            _telemetry.value = _telemetry.value.copy(
                                motorState = MotorState.STOPPED,
                                isObstacleDetected = true,
                                safetyStopTriggered = true
                            )
                            SecurityAuditLogger.logEvent(
                                eventType = "OBSTACULO_DETECTADO",
                                description = "Parada de seguridad automática activada a ${String.format("%.1f", simDistance)} cm.",
                                severity = LogSeverity.CRITICAL
                            )
                        }
                    }
                    MotorState.BACKWARD -> {
                        // Alejándose del obstáculo
                        simDistance += (4.0f + Random.nextFloat() * 1.0f)
                        if (simDistance > 180f) simDistance = 180f
                    }
                    else -> {
                        // En reposo, añadir pequeño jitter de lectura ultrasónica (±0.3 cm)
                        val noise = (Random.nextFloat() - 0.5f) * 0.6f
                        simDistance = (simDistance + noise).coerceIn(8f, 200f)
                    }
                }

                val obstacle = simDistance <= threshold
                // Descarga lenta de batería
                if (simUptimeSeconds % 30 == 0L) {
                    simBatteryVoltage = (simBatteryVoltage - 0.01f).coerceAtLeast(6.5f)
                }

                _telemetry.value = _telemetry.value.copy(
                    distanceCm = simDistance,
                    isObstacleDetected = obstacle,
                    batteryVoltage = simBatteryVoltage,
                    batteryPercentage = (((simBatteryVoltage - 6.4f) / (8.4f - 6.4f)) * 100).toInt().coerceIn(0, 100),
                    uptimeSeconds = simUptimeSeconds,
                    lastPingMs = (12L + Random.nextLong(15)),
                    wifiRssi = (-45 + Random.nextInt(8)),
                    lastUpdatedTimestamp = System.currentTimeMillis()
                )
            }
        }
    }

    /**
     * MODO REAL HARDWARE: Comunicación Wi-Fi local periódica hacia el ESP32.
     * Incorpora mecanismo Heartbeat y Fail-Safe Watchdog.
     */
    private fun startHardwarePolling() {
        _connectionStatus.value = ConnectionStatus.Connecting
        SecurityAuditLogger.logEvent(
            eventType = "CONEXION_WIFI_INICIADA",
            description = "Intentando enlace con ESP32 en ${_config.value.esp32Ip}:${_config.value.port}",
            severity = LogSeverity.INFO
        )

        pollingJob = scope.launch(Dispatchers.IO) {
            var failureCount = 0
            while (isActive) {
                val pingResult = apiClient.ping(_config.value)
                if (pingResult.isSuccess) {
                    val latency = pingResult.getOrDefault(0L)
                    val statusResult = apiClient.fetchStatus(_config.value)
                    if (statusResult.isSuccess) {
                        val fetchedTelemetry = statusResult.getOrNull()!!
                        _telemetry.value = fetchedTelemetry
                        _connectionStatus.value = ConnectionStatus.Connected(
                            ip = _config.value.esp32Ip,
                            latencyMs = latency,
                            rssi = fetchedTelemetry.wifiRssi
                        )
                        failureCount = 0

                        // Verificación de obstáculo y parada segura automática en la app
                        if (fetchedTelemetry.distanceCm <= _config.value.obstacleThresholdCm &&
                            fetchedTelemetry.motorState == MotorState.FORWARD
                        ) {
                            emergencyStop()
                            SecurityAuditLogger.logEvent(
                                eventType = "PARADA_SEGURA_OBSTACULO",
                                description = "Comando STOP enviado por proximidad crítica (${fetchedTelemetry.distanceCm} cm)",
                                severity = LogSeverity.CRITICAL
                            )
                        }
                    } else {
                        handleHardwareFailure(statusResult.exceptionOrNull()?.message ?: "Error al leer telemetría", ++failureCount)
                    }
                } else {
                    handleHardwareFailure(pingResult.exceptionOrNull()?.message ?: "ESP32 no responde a ping", ++failureCount)
                }

                delay(_config.value.pollingIntervalMs)
            }
        }

        startWatchdog()
    }

    /**
     * Watchdog de parada por fallo seguro (ISO/IEC 27400):
     * Si no se recibe respuesta del ESP32 dentro del tiempo límite, se asume pérdida de enlace
     * y se ordena parada inmediata para evitar colisiones del vehículo.
     */
    private fun startWatchdog() {
        watchdogJob?.cancel()
        watchdogJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(500)
                if (_operationMode.value == OperationMode.REAL_HARDWARE &&
                    _connectionStatus.value is ConnectionStatus.Connected
                ) {
                    val timeSinceLastUpdate = System.currentTimeMillis() - _telemetry.value.lastUpdatedTimestamp
                    if (timeSinceLastUpdate > _config.value.watchdogTimeoutMs) {
                        // Pérdida de comunicación detectada
                        stopMotorsLocal()
                        _connectionStatus.value = ConnectionStatus.Error(
                            message = "Watchdog: Enlace Wi-Fi perdido por más de ${_config.value.watchdogTimeoutMs} ms. Motores asegurados en DETENIDO.",
                            retryCount = 1
                        )
                        SecurityAuditLogger.logEvent(
                            eventType = "WATCHDOG_ACTIVADO",
                            description = "Pérdida de enlace Wi-Fi. Parada segura activada automáticamente.",
                            severity = LogSeverity.CRITICAL
                        )
                    }
                }
            }
        }
    }

    private fun handleHardwareFailure(errorMessage: String, failCount: Int) {
        if (failCount >= 3) {
            stopMotorsLocal()
            _connectionStatus.value = ConnectionStatus.Error(
                message = "Sin conexión con ESP32 (${_config.value.esp32Ip}): $errorMessage",
                retryCount = failCount
            )
        }
    }

    // ==========================================
    // Comandos de Dirección y Parada
    // ==========================================

    fun moveForward() {
        // Bloquear avance si hay obstáculo a distancia crítica
        if (_telemetry.value.distanceCm <= _config.value.obstacleThresholdCm) {
            SecurityAuditLogger.logEvent(
                eventType = "COMANDO_BLOQUEADO",
                description = "Avance rechazado: obstáculo presente a ${_telemetry.value.distanceCm} cm",
                severity = LogSeverity.WARNING
            )
            return
        }
        dispatchCommand(MotorState.FORWARD)
    }

    fun moveBackward() {
        dispatchCommand(MotorState.BACKWARD)
    }

    fun turnLeft() {
        dispatchCommand(MotorState.TURNING_LEFT)
    }

    fun turnRight() {
        dispatchCommand(MotorState.TURNING_RIGHT)
    }

    fun stop() {
        dispatchCommand(MotorState.STOPPED)
    }

    fun emergencyStop() {
        stopMotorsLocal()
        SecurityAuditLogger.logEvent(
            eventType = "PARADA_EMERGENCIA",
            description = "Parada de emergencia ejecutada por el operador.",
            severity = LogSeverity.CRITICAL
        )
        if (_operationMode.value == OperationMode.REAL_HARDWARE) {
            scope.launch(Dispatchers.IO) {
                apiClient.sendEmergencyStop(_config.value)
            }
        }
    }

    fun setSpeed(speed: Int) {
        val clamped = speed.coerceIn(80, 255)
        _telemetry.value = _telemetry.value.copy(speedPwm = clamped)
        // Si el motor está en movimiento, re-enviar comando con nueva velocidad
        if (_telemetry.value.motorState != MotorState.STOPPED) {
            dispatchCommand(_telemetry.value.motorState)
        }
    }

    fun resetDistanceObstacle() {
        // Para modo simulación: alejar el obstáculo a 80cm para continuar pruebas
        simDistance = 80.0f
        _telemetry.value = _telemetry.value.copy(
            distanceCm = 80.0f,
            isObstacleDetected = false,
            safetyStopTriggered = false
        )
        SecurityAuditLogger.logEvent(
            eventType = "REINICIO_SIMULADOR",
            description = "Obstáculo virtual restablecido a 80 cm.",
            severity = LogSeverity.INFO
        )
    }

    fun reconnect() {
        startCurrentMode()
    }

    private fun dispatchCommand(targetState: MotorState) {
        _telemetry.value = _telemetry.value.copy(
            motorState = targetState,
            safetyStopTriggered = false
        )

        SecurityAuditLogger.logEvent(
            eventType = "COMANDO_MOTOR",
            description = "Comando ejecutado: ${targetState.code} (Velocidad: ${_telemetry.value.speedPwm} PWM)",
            severity = LogSeverity.INFO
        )

        if (_operationMode.value == OperationMode.REAL_HARDWARE) {
            scope.launch(Dispatchers.IO) {
                val req = ControlRequest(
                    command = targetState.code,
                    speed = _telemetry.value.speedPwm,
                    authToken = _config.value.authToken
                )
                val result = apiClient.sendCommand(_config.value, req)
                if (result.isFailure) {
                    stopMotorsLocal()
                    SecurityAuditLogger.logEvent(
                        eventType = "ERROR_ENVIO_COMANDO",
                        description = "Fallo de envío de comando ${targetState.code}: ${result.exceptionOrNull()?.message}",
                        severity = LogSeverity.CRITICAL
                    )
                }
            }
        }
    }

    private fun stopMotorsLocal() {
        _telemetry.value = _telemetry.value.copy(motorState = MotorState.STOPPED)
    }
}
