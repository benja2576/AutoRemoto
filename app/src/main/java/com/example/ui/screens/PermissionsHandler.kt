package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.model.LogSeverity
import com.example.data.security.SecurityAuditLogger

/**
 * Gestor de permisos en tiempo de ejecución (Criterio 3: Permisos y Seguridad ISO/IEC 27400).
 * Maneja permisos requeridos para escaneo Wi-Fi local y notificaciones de obstáculos.
 */
@Composable
fun PermissionsHandler(
    onPermissionsEvaluated: () -> Unit = {}
) {
    val context = LocalContext.current
    var showExplanationDialog by remember { mutableStateOf(false) }
    var permissionsRejected by remember { mutableStateOf(false) }

    // Lista de permisos requeridos según la versión de Android
    val permissionsToRequest = remember {
        mutableListOf<String>().apply {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.NEARBY_WIFI_DEVICES)
            }
        }.toTypedArray()
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        if (allGranted) {
            SecurityAuditLogger.logEvent(
                eventType = "PERMISOS_CONCEDIDOS",
                description = "Todos los permisos de red local y notificaciones fueron concedidos.",
                severity = LogSeverity.SECURITY
            )
            onPermissionsEvaluated()
        } else {
            permissionsRejected = true
            showExplanationDialog = true
            SecurityAuditLogger.logEvent(
                eventType = "PERMISOS_DENEGADOS",
                description = "Uno o más permisos fueron rechazados por el operador.",
                severity = LogSeverity.WARNING
            )
        }
    }

    LaunchedEffect(Unit) {
        val missingPermissions = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isNotEmpty()) {
            launcher.launch(permissionsToRequest)
        } else {
            onPermissionsEvaluated()
        }
    }

    if (showExplanationDialog) {
        AlertDialog(
            containerColor = com.example.ui.theme.MonochromeCardSurface,
            onDismissRequest = { showExplanationDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = "Seguridad",
                        tint = com.example.ui.theme.MonochromeWhite,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Permisos Requeridos (ISO/IEC 27400)", color = com.example.ui.theme.MonochromeWhite)
                }
            },
            text = {
                Column {
                    Text(
                        "Auto IoT Control requiere permisos para operar con seguridad:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = com.example.ui.theme.MonochromeLightGrey
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = com.example.ui.theme.MonochromeDarkSurface
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.MonochromeCardBorder),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = com.example.ui.theme.MonochromeWhite)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Ubicación / Redes: Necesario en Android para escanear y comunicarse con el ESP32 en la red Wi-Fi local.",
                                style = MaterialTheme.typography.bodySmall,
                                color = com.example.ui.theme.MonochromeWhite
                            )
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = com.example.ui.theme.MonochromeDarkSurface
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.MonochromeCardBorder),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = com.example.ui.theme.MonochromeWhite)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Notificaciones: Necesario para emitir alertas críticas cuando se detecta un obstáculo a menos de 20 cm.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = com.example.ui.theme.MonochromeWhite
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Puede continuar en Modo Simulación sin permisos, pero para el hardware real son indispensables.",
                        style = MaterialTheme.typography.labelSmall,
                        color = com.example.ui.theme.MonochromeMutedText
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExplanationDialog = false
                        launcher.launch(permissionsToRequest)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = com.example.ui.theme.MonochromeWhite,
                        contentColor = com.example.ui.theme.MonochromeBlack
                    ),
                    modifier = Modifier.testTag("grant_permissions_button")
                ) {
                    Text("Reintentar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showExplanationDialog = false
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                    border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.MonochromeWhite),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = com.example.ui.theme.MonochromeWhite)
                ) {
                    Text("Abrir Ajustes")
                }
            }
        )
    }
}
