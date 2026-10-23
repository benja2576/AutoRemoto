package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectionStatus
import com.example.data.model.MotorState
import com.example.data.model.OperationMode
import com.example.data.security.UserSession
import com.example.ui.theme.MonochromeBlack
import com.example.ui.theme.MonochromeCardBorder
import com.example.ui.theme.MonochromeCardSurface
import com.example.ui.theme.MonochromeDarkSurface
import com.example.ui.theme.MonochromeLightGrey
import com.example.ui.theme.MonochromeMediumGrey
import com.example.ui.theme.MonochromeMutedText
import com.example.ui.theme.MonochromeSubtleGrey
import com.example.ui.theme.MonochromeWhite
import com.example.ui.viewmodels.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    session: UserSession,
    dashboardViewModel: DashboardViewModel,
    onNavigateToSecurity: () -> Unit,
    onNavigateToDocs: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val telemetry by dashboardViewModel.telemetry.collectAsState()
    val connectionStatus by dashboardViewModel.connectionStatus.collectAsState()
    val operationMode by dashboardViewModel.operationMode.collectAsState()
    val config by dashboardViewModel.config.collectAsState()

    var showConfigDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    // Pulso para alerta de proximidad en blanco y negro
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Scaffold(
        containerColor = MonochromeBlack,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Auto IoT Control",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MonochromeWhite
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Badge de Modo (Simulación vs Real) en Blanco y Negro
                            Surface(
                                color = if (operationMode == OperationMode.SIMULATION)
                                    MonochromeMediumGrey
                                else
                                    MonochromeWhite,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MonochromeCardBorder)
                            ) {
                                Text(
                                    text = if (operationMode == OperationMode.SIMULATION) "SIMULACIÓN" else "ESP32 REAL",
                                    color = if (operationMode == OperationMode.SIMULATION) MonochromeWhite else MonochromeBlack,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Operador: ${session.email}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MonochromeMutedText
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSecurity,
                        modifier = Modifier.testTag("nav_security_button")
                    ) {
                        Icon(Icons.Default.Security, contentDescription = "Seguridad ISO 27400", tint = MonochromeWhite)
                    }

                    IconButton(
                        onClick = onNavigateToDocs,
                        modifier = Modifier.testTag("nav_docs_button")
                    ) {
                        Icon(Icons.Default.Description, contentDescription = "Documentación", tint = MonochromeWhite)
                    }

                    IconButton(
                        onClick = { showConfigDialog = true },
                        modifier = Modifier.testTag("open_settings_button")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración", tint = MonochromeWhite)
                    }

                    IconButton(
                        onClick = { showLogoutConfirmDialog = true },
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión", tint = MonochromeLightGrey)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MonochromeBlack
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MonochromeBlack)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Selector Segmentado de Modo
            OperationModeSelector(
                currentMode = operationMode,
                onModeSelected = { dashboardViewModel.setOperationMode(it) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Barra de Estado de Conexión Wi-Fi / Heartbeat
            ConnectionStatusBar(
                status = connectionStatus,
                mode = operationMode,
                onReconnect = { dashboardViewModel.reconnect() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Banner de Alerta Crítica de Obstáculo en Blanco y Negro
            AnimatedVisibility(visible = telemetry.isObstacleDetected || telemetry.safetyStopTriggered) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .border(
                            width = 2.dp,
                            color = MonochromeWhite.copy(alpha = pulseAlpha),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = MonochromeCardSurface
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Peligro de obstáculo",
                            tint = MonochromeWhite,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "¡ALERTA DE OBSTÁCULO INMINENTE!",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MonochromeWhite
                            )
                            Text(
                                text = "Parada de seguridad preventiva aplicada a ${String.format("%.1f", telemetry.distanceCm)} cm.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MonochromeLightGrey
                            )
                        }

                        if (operationMode == OperationMode.SIMULATION) {
                            OutlinedButton(
                                onClick = { dashboardViewModel.resetDistanceObstacle() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MonochromeWhite),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MonochromeWhite)
                            ) {
                                Text("Alejar (+80cm)", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            // 4. Tarjetas de Telemetría (Distancia y Motor)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DistanceGaugeCard(
                    distanceCm = telemetry.distanceCm,
                    thresholdCm = config.obstacleThresholdCm,
                    modifier = Modifier.weight(1.3f)
                )

                MotorStateCard(
                    motorState = telemetry.motorState,
                    speedPwm = telemetry.speedPwm,
                    modifier = Modifier.weight(1.1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Métricas de Hardware y Conectividad
            HardwareMetricsRow(telemetry = telemetry, config = config)

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Panel de Mandos Direccionales y Parada de Emergencia
            VehicleControlPad(
                motorState = telemetry.motorState,
                onForward = { dashboardViewModel.moveForward() },
                onBackward = { dashboardViewModel.moveBackward() },
                onLeft = { dashboardViewModel.turnLeft() },
                onRight = { dashboardViewModel.turnRight() },
                onStop = { dashboardViewModel.stop() },
                onEmergencyStop = { dashboardViewModel.emergencyStop() },
                speed = telemetry.speedPwm,
                onSpeedChange = { dashboardViewModel.setSpeed(it) }
            )

            Spacer(modifier = Modifier.height(14.dp))
        }
    }

    if (showConfigDialog) {
        ConnectionSettingsDialog(
            currentConfig = config,
            onDismiss = { showConfigDialog = false },
            onSaveConfig = { newCfg ->
                dashboardViewModel.updateConfig(newCfg)
            }
        )
    }

    if (showLogoutConfirmDialog) {
        AlertDialog(
            containerColor = MonochromeCardSurface,
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = { Text("¿Cerrar Sesión Segura?", color = MonochromeWhite) },
            text = {
                Text(
                    "Se destruirán las claves temporales y tokens de sesión encriptados en memoria y almacenamiento local (Criterio 2 ISO/IEC 27400).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MonochromeLightGrey
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        dashboardViewModel.emergencyStop()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MonochromeWhite,
                        contentColor = MonochromeBlack
                    )
                ) {
                    Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancelar", color = MonochromeLightGrey)
                }
            }
        )
    }
}

/**
 * Selector segmentado de modo en blanco y negro.
 */
@Composable
fun OperationModeSelector(
    currentMode: OperationMode,
    onModeSelected: (OperationMode) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MonochromeDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MonochromeCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FilledTonalButton(
                onClick = { onModeSelected(OperationMode.SIMULATION) },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (currentMode == OperationMode.SIMULATION)
                        MonochromeWhite
                    else
                        Color.Transparent,
                    contentColor = if (currentMode == OperationMode.SIMULATION)
                        MonochromeBlack
                    else
                        MonochromeMutedText
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("mode_sim_button")
            ) {
                Text("Modo Simulación", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }

            FilledTonalButton(
                onClick = { onModeSelected(OperationMode.REAL_HARDWARE) },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (currentMode == OperationMode.REAL_HARDWARE)
                        MonochromeWhite
                    else
                        Color.Transparent,
                    contentColor = if (currentMode == OperationMode.REAL_HARDWARE)
                        MonochromeBlack
                    else
                        MonochromeMutedText
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("mode_real_button")
            ) {
                Text("Modo Real ESP32", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

/**
 * Muestra el estado del enlace en blanco y negro con micro-indicadores claros.
 */
@Composable
fun ConnectionStatusBar(
    status: ConnectionStatus,
    mode: OperationMode,
    onReconnect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MonochromeCardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MonochromeCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (status) {
                    is ConnectionStatus.Connected -> {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(MonochromeWhite)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Enlace Activo: ${status.ip} (${status.latencyMs} ms)",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MonochromeWhite
                        )
                    }
                    is ConnectionStatus.Connecting -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 2.dp,
                            color = MonochromeWhite
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Conectando al ESP32...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MonochromeLightGrey
                        )
                    }
                    is ConnectionStatus.Error -> {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MonochromeWhite, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Desconectado (Watchdog Activo)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MonochromeLightGrey
                        )
                    }
                    ConnectionStatus.Disconnected -> {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(MonochromeSubtleGrey))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Enlace Desconectado",
                            style = MaterialTheme.typography.bodySmall,
                            color = MonochromeMutedText
                        )
                    }
                }
            }

            if (status is ConnectionStatus.Error || status is ConnectionStatus.Disconnected) {
                IconButton(onClick = onReconnect, modifier = Modifier.size(28.dp).testTag("reconnect_button")) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reconectar", tint = MonochromeWhite)
                }
            }
        }
    }
}

/**
 * Visualizador de Distancia en Negro y Blanco.
 */
@Composable
fun DistanceGaugeCard(
    distanceCm: Float,
    thresholdCm: Float,
    modifier: Modifier = Modifier
) {
    val isCritical = distanceCm <= thresholdCm
    val isWarning = distanceCm in (thresholdCm + 0.1f)..35f

    val statusText = when {
        isCritical -> "¡OBSTÁCULO DETECTADO!"
        isWarning -> "Proximidad Cercana"
        else -> "Camino Despejado"
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MonochromeCardSurface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCritical) MonochromeWhite else MonochromeCardBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SENSOR HC-SR04",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MonochromeMutedText
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = String.format("%.1f", distanceCm),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                ),
                color = MonochromeWhite
            )

            Text(
                text = "centímetros",
                style = MaterialTheme.typography.labelSmall,
                color = MonochromeMutedText
            )

            Spacer(modifier = Modifier.height(8.dp))

            val progress = (distanceCm / 100f).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MonochromeWhite,
                trackColor = MonochromeMediumGrey
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MonochromeWhite,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Estado del Motor en Negro y Blanco.
 */
@Composable
fun MotorStateCard(
    motorState: MotorState,
    speedPwm: Int,
    modifier: Modifier = Modifier
) {
    val isMoving = motorState != MotorState.STOPPED && motorState != MotorState.EMERGENCY_STOP

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MonochromeCardSurface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isMoving) MonochromeWhite else MonochromeCardBorder
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "DRIVER L298N",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MonochromeMutedText
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(if (isMoving) MonochromeWhite else MonochromeDarkSurface)
                    .border(1.5.dp, MonochromeWhite, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (motorState) {
                        MotorState.FORWARD -> Icons.Default.KeyboardArrowUp
                        MotorState.BACKWARD -> Icons.Default.KeyboardArrowDown
                        MotorState.TURNING_LEFT -> Icons.AutoMirrored.Filled.KeyboardArrowLeft
                        MotorState.TURNING_RIGHT -> Icons.AutoMirrored.Filled.KeyboardArrowRight
                        MotorState.STOPPED, MotorState.EMERGENCY_STOP -> Icons.Default.Stop
                    },
                    contentDescription = motorState.label,
                    tint = if (isMoving) MonochromeBlack else MonochromeWhite,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = motorState.label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MonochromeWhite,
                textAlign = TextAlign.Center
            )

            Text(
                text = "PWM: $speedPwm / 255",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                color = MonochromeMutedText
            )
        }
    }
}

/**
 * Fila de Métricas en Negro y Blanco.
 */
@Composable
fun HardwareMetricsRow(
    telemetry: com.example.data.model.VehicleTelemetry,
    config: com.example.data.model.VehicleConfig
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MonochromeCardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MonochromeCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BatteryChargingFull,
                    contentDescription = "Batería",
                    tint = MonochromeWhite,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "${String.format("%.1f", telemetry.batteryVoltage)}V (${telemetry.batteryPercentage}%)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MonochromeWhite
                    )
                    Text("Batería 2S", style = MaterialTheme.typography.labelSmall, color = MonochromeMutedText)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = "Señal Wi-Fi",
                    tint = MonochromeWhite,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "${telemetry.wifiRssi} dBm",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MonochromeWhite
                    )
                    Text("Señal Wi-Fi", style = MaterialTheme.typography.labelSmall, color = MonochromeMutedText)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = "Latencia",
                    tint = MonochromeWhite,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "${telemetry.lastPingMs} ms",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MonochromeWhite
                    )
                    Text("Latencia RTT", style = MaterialTheme.typography.labelSmall, color = MonochromeMutedText)
                }
            }
        }
    }
}

/**
 * Panel de Controles en Negro y Blanco de Alto Contraste.
 */
@Composable
fun VehicleControlPad(
    motorState: MotorState,
    onForward: () -> Unit,
    onBackward: () -> Unit,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onStop: () -> Unit,
    onEmergencyStop: () -> Unit,
    speed: Int,
    onSpeedChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MonochromeCardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MonochromeCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MANDOS DE TELEOPERACIÓN",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MonochromeWhite
            )
            Text(
                text = "Comandos protegidos con parada de fallo seguro",
                style = MaterialTheme.typography.labelSmall,
                color = MonochromeMutedText
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón AVANZAR
            Button(
                onClick = onForward,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (motorState == MotorState.FORWARD) MonochromeWhite else MonochromeDarkSurface,
                    contentColor = if (motorState == MotorState.FORWARD) MonochromeBlack else MonochromeWhite
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MonochromeWhite),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .width(170.dp)
                    .height(58.dp)
                    .testTag("btn_forward")
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("AVANZAR", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Fila Central: GIRAR IZQUIERDA - DETENER / PARADA SEGURA - GIRAR DERECHA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón IZQUIERDA
                Button(
                    onClick = onLeft,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (motorState == MotorState.TURNING_LEFT) MonochromeWhite else MonochromeDarkSurface,
                        contentColor = if (motorState == MotorState.TURNING_LEFT) MonochromeBlack else MonochromeWhite
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MonochromeCardBorder),
                    modifier = Modifier
                        .size(68.dp)
                        .testTag("btn_left")
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Girar Izquierda", modifier = Modifier.size(32.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                // BOTÓN CENTRAL DE DETENER / PARADA DE EMERGENCIA EN BLANCO Y NEGRO
                Button(
                    onClick = onEmergencyStop,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MonochromeWhite,
                        contentColor = MonochromeBlack
                    ),
                    border = androidx.compose.foundation.BorderStroke(2.dp, MonochromeWhite),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .height(72.dp)
                        .width(140.dp)
                        .testTag("emergency_stop_button")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Stop, contentDescription = "Detener", modifier = Modifier.size(28.dp))
                        Text(
                            text = "DETENER",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Botón DERECHA
                Button(
                    onClick = onRight,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (motorState == MotorState.TURNING_RIGHT) MonochromeWhite else MonochromeDarkSurface,
                        contentColor = if (motorState == MotorState.TURNING_RIGHT) MonochromeBlack else MonochromeWhite
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MonochromeCardBorder),
                    modifier = Modifier
                        .size(68.dp)
                        .testTag("btn_right")
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Girar Derecha", modifier = Modifier.size(32.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Botón RETROCEDER
            Button(
                onClick = onBackward,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (motorState == MotorState.BACKWARD) MonochromeWhite else MonochromeDarkSurface,
                    contentColor = if (motorState == MotorState.BACKWARD) MonochromeBlack else MonochromeWhite
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MonochromeWhite),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .width(170.dp)
                    .height(58.dp)
                    .testTag("btn_backward")
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("RETROCEDER", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Slider de Velocidad en Blanco y Negro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Velocidad de Motores:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MonochromeWhite
                )
                Text(
                    text = "$speed PWM",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MonochromeWhite
                )
            }

            Slider(
                value = speed.toFloat(),
                onValueChange = { onSpeedChange(it.toInt()) },
                valueRange = 100f..255f,
                steps = 15,
                colors = SliderDefaults.colors(
                    thumbColor = MonochromeWhite,
                    activeTrackColor = MonochromeWhite,
                    inactiveTrackColor = MonochromeMediumGrey
                ),
                modifier = Modifier.fillMaxWidth().testTag("speed_slider")
            )
        }
    }
}
