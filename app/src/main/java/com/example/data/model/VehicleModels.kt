package com.example.data.model

/**
 * Estados del motor del vehículo IoT según la configuración del driver L298N.
 */
enum class MotorState(val label: String, val code: String) {
    STOPPED("DETENIDO", "STOP"),
    FORWARD("AVANZANDO", "FORWARD"),
    BACKWARD("RETROCEDIENDO", "BACKWARD"),
    TURNING_LEFT("GIRANDO IZQUIERDA", "LEFT"),
    TURNING_RIGHT("GIRANDO DERECHA", "RIGHT"),
    EMERGENCY_STOP("PARADA DE EMERGENCIA", "EMERGENCY_STOP")
}

/**
 * Modos de operación del sistema: Simulación de pruebas o Conexión real con hardware ESP32.
 */
enum class OperationMode(val label: String) {
    SIMULATION("Modo Simulación (Pruebas)"),
    REAL_HARDWARE("Modo Real (ESP32 Wi-Fi)")
}

/**
 * Estado de la conexión inalámbrica.
 */
sealed class ConnectionStatus {
    object Disconnected : ConnectionStatus()
    object Connecting : ConnectionStatus()
    data class Connected(val ip: String, val latencyMs: Long, val rssi: Int) : ConnectionStatus()
    data class Error(val message: String, val retryCount: Int) : ConnectionStatus()
}

/**
 * Telemetría en tiempo real recibida del ESP32 o generada por el simulador.
 */
data class VehicleTelemetry(
    val distanceCm: Float = 50.0f,
    val motorState: MotorState = MotorState.STOPPED,
    val speedPwm: Int = 180, // Rango 0-255 para control PWM de L298N
    val isObstacleDetected: Boolean = false,
    val batteryVoltage: Float = 7.4f, // Batería Li-Ion 2S (nominal 7.4V, máx 8.4V)
    val batteryPercentage: Int = 88,
    val uptimeSeconds: Long = 0L,
    val lastPingMs: Long = 0L,
    val wifiRssi: Int = -55,
    val safetyStopTriggered: Boolean = false,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)

/**
 * Payload JSON enviado en las peticiones de control al ESP32.
 */
data class ControlRequest(
    val command: String,
    val speed: Int = 180,
    val authToken: String = ""
)

/**
 * Parámetros de configuración del enlace y parámetros de seguridad.
 */
data class VehicleConfig(
    val esp32Ip: String = "192.168.4.1", // IP por defecto en modo AP del ESP32
    val port: Int = 80,
    val authToken: String = "AutoIoT-SecToken-2026",
    val obstacleThresholdCm: Float = 20.0f, // Distancia crítica de parada
    val watchdogTimeoutMs: Long = 2000L, // Timeout de parada segura
    val pollingIntervalMs: Long = 400L, // Frecuencia de actualización de telemetría
    val soundAlertsEnabled: Boolean = true,
    val hapticAlertsEnabled: Boolean = true
)

/**
 * Registro de auditoría para trazabilidad de seguridad según ISO/IEC 27400.
 */
data class SecurityLogEntry(
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String,
    val description: String,
    val severity: LogSeverity = LogSeverity.INFO,
    val operator: String = "operador"
)

enum class LogSeverity {
    INFO, WARNING, CRITICAL, SECURITY
}
