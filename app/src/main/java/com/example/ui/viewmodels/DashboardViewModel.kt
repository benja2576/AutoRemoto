package com.example.ui.viewmodels

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ConnectionStatus
import com.example.data.model.MotorState
import com.example.data.model.OperationMode
import com.example.data.model.VehicleConfig
import com.example.data.model.VehicleTelemetry
import com.example.data.repository.VehicleRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel del Panel de Control y Telemetría del Vehículo IoT.
 * Soporta rotación de pantalla, estados persistentes y alertas hápticas de obstáculos.
 */
class DashboardViewModel(
    application: Application,
    private val repository: VehicleRepository
) : AndroidViewModel(application) {

    val telemetry: StateFlow<VehicleTelemetry> = repository.telemetry
    val connectionStatus: StateFlow<ConnectionStatus> = repository.connectionStatus
    val operationMode: StateFlow<OperationMode> = repository.operationMode
    val config: StateFlow<VehicleConfig> = repository.config

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    init {
        // Monitorear telemetría para disparar vibración al detectar obstáculos cercanos
        viewModelScope.launch {
            telemetry.collect { tele ->
                if (tele.isObstacleDetected && config.value.hapticAlertsEnabled) {
                    triggerObstacleHapticAlert()
                }
            }
        }
    }

    fun moveForward() {
        triggerLightHaptic()
        repository.moveForward()
    }

    fun moveBackward() {
        triggerLightHaptic()
        repository.moveBackward()
    }

    fun turnLeft() {
        triggerLightHaptic()
        repository.turnLeft()
    }

    fun turnRight() {
        triggerLightHaptic()
        repository.turnRight()
    }

    fun stop() {
        triggerMediumHaptic()
        repository.stop()
    }

    fun emergencyStop() {
        triggerHeavyHaptic()
        repository.emergencyStop()
    }

    fun setSpeed(speed: Int) {
        repository.setSpeed(speed)
    }

    fun setOperationMode(mode: OperationMode) {
        repository.setOperationMode(mode)
    }

    fun updateConfig(newConfig: VehicleConfig) {
        repository.updateConfig(newConfig)
    }

    fun resetDistanceObstacle() {
        repository.resetDistanceObstacle()
    }

    fun reconnect() {
        repository.reconnect()
    }

    private fun triggerLightHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(30)
            }
        } catch (_: Exception) { }
    }

    private fun triggerMediumHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(60)
            }
        } catch (_: Exception) { }
    }

    private fun triggerHeavyHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 150), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(200)
            }
        } catch (_: Exception) { }
    }

    private fun triggerObstacleHapticAlert() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 40, 80), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(100)
            }
        } catch (_: Exception) { }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repo = VehicleRepository(scope = kotlinx.coroutines.GlobalScope)
                    return DashboardViewModel(application, repo) as T
                }
            }
    }
}
