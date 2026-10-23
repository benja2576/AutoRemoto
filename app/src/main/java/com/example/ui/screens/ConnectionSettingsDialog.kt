package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.VehicleConfig
import com.example.data.network.IoTCarApiClient
import com.example.ui.theme.MonochromeBlack
import com.example.ui.theme.MonochromeCardBorder
import com.example.ui.theme.MonochromeCardSurface
import com.example.ui.theme.MonochromeDarkSurface
import com.example.ui.theme.MonochromeLightGrey
import com.example.ui.theme.MonochromeMutedText
import com.example.ui.theme.MonochromeWhite
import kotlinx.coroutines.launch

@Composable
fun ConnectionSettingsDialog(
    currentConfig: VehicleConfig,
    onDismiss: () -> Unit,
    onSaveConfig: (VehicleConfig) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var esp32Ip by remember { mutableStateOf(currentConfig.esp32Ip) }
    var portStr by remember { mutableStateOf(currentConfig.port.toString()) }
    var authToken by remember { mutableStateOf(currentConfig.authToken) }
    var thresholdStr by remember { mutableStateOf(currentConfig.obstacleThresholdCm.toInt().toString()) }
    var watchdogStr by remember { mutableStateOf(currentConfig.watchdogTimeoutMs.toString()) }
    var hapticsEnabled by remember { mutableStateOf(currentConfig.hapticAlertsEnabled) }
    var soundEnabled by remember { mutableStateOf(currentConfig.soundAlertsEnabled) }

    var isPinging by remember { mutableStateOf(false) }
    var pingResultText by remember { mutableStateOf<String?>(null) }
    var pingSuccess by remember { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MonochromeWhite,
        unfocusedBorderColor = MonochromeCardBorder,
        focusedTextColor = MonochromeWhite,
        unfocusedTextColor = MonochromeWhite,
        focusedLabelColor = MonochromeWhite,
        unfocusedLabelColor = MonochromeMutedText
    )

    AlertDialog(
        containerColor = MonochromeCardSurface,
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = MonochromeWhite)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Configuración de Enlace", color = MonochromeWhite)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Ajuste los parámetros de red Wi-Fi y seguridad del vehículo IoT:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MonochromeMutedText
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = esp32Ip,
                    onValueChange = { esp32Ip = it },
                    label = { Text("Dirección IP del ESP32") },
                    placeholder = { Text("192.168.4.1") },
                    singleLine = true,
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth().testTag("config_ip_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = portStr,
                    onValueChange = { portStr = it },
                    label = { Text("Puerto HTTP") },
                    placeholder = { Text("80") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = authToken,
                    onValueChange = { authToken = it },
                    label = { Text("Token Header X-IoT-Auth-Token") },
                    singleLine = true,
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = thresholdStr,
                    onValueChange = { thresholdStr = it },
                    label = { Text("Distancia Crítica de Parada (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = watchdogStr,
                    onValueChange = { watchdogStr = it },
                    label = { Text("Timeout de Watchdog (ms)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Vibration, contentDescription = null, tint = MonochromeWhite)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Vibración de alerta", style = MaterialTheme.typography.bodyMedium, color = MonochromeWhite)
                    }
                    Switch(
                        checked = hapticsEnabled,
                        onCheckedChange = { hapticsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MonochromeBlack,
                            checkedTrackColor = MonochromeWhite,
                            uncheckedThumbColor = MonochromeMutedText,
                            uncheckedTrackColor = MonochromeDarkSurface
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Botón de Prueba de Conexión / Ping
                OutlinedButton(
                    onClick = {
                        isPinging = true
                        pingResultText = null
                        scope.launch {
                            val tempConfig = currentConfig.copy(
                                esp32Ip = esp32Ip.trim(),
                                port = portStr.toIntOrNull() ?: 80,
                                authToken = authToken.trim()
                            )
                            val client = IoTCarApiClient()
                            val res = client.ping(tempConfig)
                            isPinging = false
                            if (res.isSuccess) {
                                pingSuccess = true
                                pingResultText = "¡Conexión Exitosa! RTT: ${res.getOrDefault(0)} ms"
                            } else {
                                pingSuccess = false
                                pingResultText = "Fallo de ping: ${res.exceptionOrNull()?.message ?: "Sin respuesta en $esp32Ip"}"
                            }
                        }
                    },
                    border = androidx.compose.foundation.BorderStroke(1.dp, MonochromeWhite),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MonochromeWhite),
                    modifier = Modifier.fillMaxWidth().testTag("test_ping_button")
                ) {
                    if (isPinging) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MonochromeWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verificando...")
                    } else {
                        Icon(Icons.Default.NetworkCheck, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Probar Conexión / Ping Real")
                    }
                }

                if (pingResultText != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MonochromeDarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MonochromeCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = pingResultText!!,
                            color = MonochromeWhite,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newCfg = currentConfig.copy(
                        esp32Ip = esp32Ip.trim(),
                        port = portStr.toIntOrNull() ?: 80,
                        authToken = authToken.trim(),
                        obstacleThresholdCm = thresholdStr.toFloatOrNull() ?: 20.0f,
                        watchdogTimeoutMs = watchdogStr.toLongOrNull() ?: 2000L,
                        hapticAlertsEnabled = hapticsEnabled,
                        soundAlertsEnabled = soundEnabled
                    )
                    onSaveConfig(newCfg)
                    Toast.makeText(context, "Configuración guardada.", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MonochromeWhite,
                    contentColor = MonochromeBlack
                ),
                modifier = Modifier.testTag("save_config_button")
            ) {
                Text("Guardar Cambios", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MonochromeLightGrey)
            }
        }
    )
}
