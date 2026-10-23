package com.example.data.network

import com.example.data.model.ControlRequest
import com.example.data.model.MotorState
import com.example.data.model.VehicleConfig
import com.example.data.model.VehicleTelemetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Cliente de red HTTP / REST para la comunicación inalámbrica real con el ESP32.
 * Cumple con el Criterio 3 (Comunicación inalámbrica y protocolo documentado).
 *
 * Todas las operaciones se ejecutan estrictamente fuera del hilo principal usando Dispatchers.IO.
 */
class IoTCarApiClient {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(2000, TimeUnit.MILLISECONDS)
        .readTimeout(2000, TimeUnit.MILLISECONDS)
        .writeTimeout(2000, TimeUnit.MILLISECONDS)
        .retryOnConnectionFailure(false)
        .build()

    /**
     * Realiza un Ping al endpoint /api/ping del ESP32 y calcula la latencia en milisegundos.
     */
    suspend fun ping(config: VehicleConfig): Result<Long> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val url = "http://${config.esp32Ip}:${config.port}/api/ping"
        try {
            val request = Request.Builder()
                .url(url)
                .header("X-IoT-Auth-Token", config.authToken)
                .header("User-Agent", "AutoIoTControl-Android/1.0")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val latency = System.currentTimeMillis() - startTime
                    Result.success(latency)
                } else {
                    Result.failure(Exception("ESP32 respondió con código HTTP ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Consulta el estado y telemetría actual del vehículo en /api/status.
     * Retorna distancia (cm), estado de motor, presencia de obstáculo, voltaje y RSSI.
     */
    suspend fun fetchStatus(config: VehicleConfig): Result<VehicleTelemetry> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val url = "http://${config.esp32Ip}:${config.port}/api/status"
        try {
            val request = Request.Builder()
                .url(url)
                .header("X-IoT-Auth-Token", config.authToken)
                .header("User-Agent", "AutoIoTControl-Android/1.0")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Error HTTP ${response.code} al consultar telemetría"))
                }
                val bodyStr = response.body?.string() ?: ""
                val json = JSONObject(bodyStr)

                val distance = json.optDouble("distance", 50.0).toFloat()
                val motorStr = json.optString("motor_state", "STOPPED")
                val motorState = parseMotorState(motorStr)
                val speed = json.optInt("speed", 180)
                val isObstacle = json.optBoolean("obstacle", distance <= config.obstacleThresholdCm)
                val batteryV = json.optDouble("battery_v", 7.4).toFloat()
                val uptime = json.optLong("uptime", 0L)
                val rssi = json.optInt("rssi", -60)
                val latency = System.currentTimeMillis() - startTime

                val telemetry = VehicleTelemetry(
                    distanceCm = distance,
                    motorState = motorState,
                    speedPwm = speed,
                    isObstacleDetected = isObstacle,
                    batteryVoltage = batteryV,
                    batteryPercentage = calculateBatteryPercentage(batteryV),
                    uptimeSeconds = uptime,
                    lastPingMs = latency,
                    wifiRssi = rssi,
                    safetyStopTriggered = isObstacle && motorState == MotorState.STOPPED,
                    lastUpdatedTimestamp = System.currentTimeMillis()
                )
                Result.success(telemetry)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Envía comando de control de dirección o velocidad al endpoint /api/control.
     */
    suspend fun sendCommand(config: VehicleConfig, controlRequest: ControlRequest): Result<String> =
        withContext(Dispatchers.IO) {
            val url = "http://${config.esp32Ip}:${config.port}/api/control"
            try {
                val jsonPayload = JSONObject().apply {
                    put("command", controlRequest.command)
                    put("speed", controlRequest.speed)
                    put("auth_token", config.authToken)
                    put("timestamp", System.currentTimeMillis())
                }.toString()

                val body = jsonPayload.toRequestBody(jsonMediaType)
                val request = Request.Builder()
                    .url(url)
                    .header("X-IoT-Auth-Token", config.authToken)
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val respBody = response.body?.string() ?: "OK"
                        Result.success(respBody)
                    } else {
                        Result.failure(Exception("Fallo al enviar comando. Código HTTP: ${response.code}"))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Envía parada de emergencia rápida.
     */
    suspend fun sendEmergencyStop(config: VehicleConfig): Result<Unit> = withContext(Dispatchers.IO) {
        val stopRequest = ControlRequest(command = "STOP", speed = 0, authToken = config.authToken)
        sendCommand(config, stopRequest).map { }
    }

    private fun parseMotorState(state: String): MotorState {
        return when (state.uppercase()) {
            "FORWARD", "AVANZANDO" -> MotorState.FORWARD
            "BACKWARD", "RETROCEDIENDO" -> MotorState.BACKWARD
            "LEFT", "IZQUIERDA" -> MotorState.TURNING_LEFT
            "RIGHT", "DERECHA" -> MotorState.TURNING_RIGHT
            "EMERGENCY_STOP" -> MotorState.EMERGENCY_STOP
            else -> MotorState.STOPPED
        }
    }

    private fun calculateBatteryPercentage(voltage: Float): Int {
        // Para batería Li-ion 2S: Mínimo seguro 6.4V (0%), Nominal 7.4V (50%), Máximo 8.4V (100%)
        val pct = ((voltage - 6.4f) / (8.4f - 6.4f) * 100f).toInt()
        return pct.coerceIn(0, 100)
    }
}
