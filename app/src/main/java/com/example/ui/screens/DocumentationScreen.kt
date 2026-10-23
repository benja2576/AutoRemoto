package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MonochromeBlack
import com.example.ui.theme.MonochromeCardBorder
import com.example.ui.theme.MonochromeCardSurface
import com.example.ui.theme.MonochromeDarkSurface
import com.example.ui.theme.MonochromeLightGrey
import com.example.ui.theme.MonochromeMutedText
import com.example.ui.theme.MonochromeWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentationScreen(
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }

    Scaffold(
        containerColor = MonochromeBlack,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = MonochromeWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Documentación TI3042 ES2", style = MaterialTheme.typography.titleMedium, color = MonochromeWhite)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_button_docs")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = MonochromeWhite)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta de Identificación
            Card(
                colors = CardDefaults.cardColors(containerColor = MonochromeCardSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MonochromeCardBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Proyecto: Auto IoT Control (Evaluación ES2)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MonochromeWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Asignatura: TI3042 • Monitoreo y Control Inalámbrico de Vehículo Robótico ESP32",
                        style = MaterialTheme.typography.bodySmall,
                        color = MonochromeLightGrey
                    )
                }
            }

            // Criterio 1: Herramientas y Problemática (20%)
            DocSectionCardBW(
                title = "1. Problemática y Arquitectura Hardware (20%)",
                icon = Icons.Default.Memory
            ) {
                Text(
                    "Problemática: En entornos industriales y robótica móvil, el control inalámbrico remoto sin retroalimentación de proximidad genera colisiones críticas e interrupciones de servicio. Se requiere un enlace inalámbrico en tiempo real con parada preventiva autónoma.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MonochromeLightGrey
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Conexión Electrónica y Pines del ESP32 NodeMCU:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MonochromeWhite
                )
                CodeSnippetBW(
                    """
                    ESP32 (GPIO) <----> Driver Puente H L298N:
                    • GPIO 25  <---> ENA (PWM Velocidad Motor Izquierdo)
                    • GPIO 26  <---> IN1 (Dirección Motor Izq +)
                    • GPIO 27  <---> IN2 (Dirección Motor Izq -)
                    • GPIO 14  <---> IN3 (Dirección Motor Der +)
                    • GPIO 12  <---> IN4 (Dirección Motor Der -)
                    • GPIO 13  <---> ENB (PWM Velocidad Motor Derecho)
                    
                    ESP32 (GPIO) <----> Sensor Ultrasónico HC-SR04:
                    • GPIO 5   <---> TRIG (Disparo ultrasónico)
                    • GPIO 18  <---> ECHO (Divisor de voltaje 5V a 3.3V)
                    • GND común entre Batería 7.4V, L298N y ESP32.
                    """.trimIndent()
                )
            }

            // Criterio 2: Login Seguro (20%)
            DocSectionCardBW(
                title = "2. Login Seguro y Almacenamiento Cifrado (20%)",
                icon = Icons.Default.Security
            ) {
                Text(
                    "Cero Contraseñas en Texto Plano: La contraseña ingresada nunca se almacena sin transformar. Se computa un hash criptográfico SHA-256 utilizando un Salt aleatorio de 16 bytes (SecureRandom).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MonochromeLightGrey
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Persistencia de Sesión con Android KeyStore:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MonochromeWhite
                )
                Text(
                    "Los tokens temporales de sesión se cifran mediante AES-256 en modo GCM (Galois/Counter Mode) con etiqueta de integridad de 128 bits. La clave maestra reside en el hardware seguro del dispositivo ('AndroidKeyStore'). Al cerrar sesión, los tokens son inmediatamente destruidos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MonochromeLightGrey
                )
            }

            // Criterio 3: Permisos y Seguridad ISO/IEC 27400 (20%)
            DocSectionCardBW(
                title = "3. Permisos en Ejecución e ISO/IEC 27400 (20%)",
                icon = Icons.Default.Security
            ) {
                Text(
                    "Permisos Mínimos: Se solicita ACCESS_FINE_LOCATION (mandatorio en el sistema Android para interactuar con redes Wi-Fi locales) y POST_NOTIFICATIONS (Android 13+). Si el usuario deniega los permisos, la aplicación activa un diálogo orientativo y permite continuar en Modo Simulación sin caídas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MonochromeLightGrey
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Medidas ISO/IEC 27400 implementadas: Cifrado en reposo (AES-256), autenticación por token en headers ('X-IoT-Auth-Token'), principio de parada por fallo seguro (Watchdog Fail-Safe de 2s) y registro de auditoría inmutable de eventos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MonochromeLightGrey
                )
            }

            // Criterio 4: Comunicación Inalámbrica Real (25%)
            DocSectionCardBW(
                title = "4. Comunicación Inalámbrica y Protocolo REST (25%)",
                icon = Icons.Default.Wifi
            ) {
                Text(
                    "El ESP32 puede operar como Punto de Acceso (AP 'AutoIoT_Car_AP' IP 192.168.4.1) o conectarse a una red Wi-Fi local. El protocolo de intercambio es HTTP/REST con formato JSON:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MonochromeLightGrey
                )
                Spacer(modifier = Modifier.height(8.dp))
                CodeSnippetBW(
                    """
                    1) GET /api/ping
                       Header: X-IoT-Auth-Token: <token>
                       Respuesta: {"pong": true, "timestamp": 1234567}
                    
                    2) GET /api/status
                       Respuesta: {
                         "distance": 42.5,
                         "motor_state": "FORWARD",
                         "speed": 180,
                         "obstacle": false,
                         "battery_v": 7.4,
                         "rssi": -55,
                         "uptime": 240
                       }
                    
                    3) POST /api/control
                       Body: {
                         "command": "FORWARD" | "BACKWARD" | "STOP" | "LEFT" | "RIGHT",
                         "speed": 180,
                         "auth_token": "AutoIoT-SecToken-2026"
                       }
                    """.trimIndent()
                )
            }

            // Criterio 5: Robustez y Despliegue (15%)
            DocSectionCardBW(
                title = "5. Robustez, Compilación y Despliegue (15%)",
                icon = Icons.Default.Code
            ) {
                Text(
                    "Robustez y Arquitectura: Implementado con Jetpack Compose y MVVM. Todas las llamadas de red se delegan a Kotlin Coroutines (Dispatchers.IO) sin bloquear el hilo principal (UI Thread). Mantiene el estado íntegro ante rotaciones de pantalla (StateFlow en ViewModel).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MonochromeLightGrey
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Instrucciones para Compilar y Desplegar en Windows:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MonochromeWhite
                )
                CodeSnippetBW(
                    """
                    PASO 1: Descargar y descomprimir el proyecto.
                    PASO 2: Abrir Android Studio -> File -> Open... -> Seleccionar la carpeta raíz.
                    PASO 3: Esperar la sincronización de Gradle (Ctrl + F9 para compilar).
                    PASO 4: Generar APK instalable:
                            Build -> Build Bundle(s) / APK(s) -> Build APK(s)
                            El APK se generará en:
                            app/build/outputs/apk/debug/app-debug.apk
                    PASO 5: Para instalar en el teléfono Android:
                            a) Conectar el teléfono por USB con Depuración USB activa.
                            b) Presionar el botón verde "Run 'app'" (Shift + F10).
                            o copiar el archivo app-debug.apk al teléfono y abrirlo.
                    PASO 6: Subir a GitHub:
                            git init
                            git add .
                            git commit -m "Auto IoT Control - TI3042 ES2"
                            git remote add origin https://github.com/TU_USUARIO/auto-iot-control.git
                            git push -u origin main
                    """.trimIndent()
                )
            }
        }
    }
}

@Composable
fun DocSectionCardBW(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MonochromeCardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MonochromeCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MonochromeWhite, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MonochromeWhite
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun CodeSnippetBW(code: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MonochromeDarkSurface)
            .border(1.dp, MonochromeCardBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 16.sp
            ),
            color = MonochromeWhite
        )
    }
}
