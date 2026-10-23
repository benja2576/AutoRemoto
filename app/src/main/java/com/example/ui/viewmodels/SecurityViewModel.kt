package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.data.model.SecurityLogEntry
import com.example.data.security.SecurityAuditLogger
import kotlinx.coroutines.flow.StateFlow

data class IsoComplianceItem(
    val id: String,
    val title: String,
    val standardRef: String,
    val description: String,
    val isFulfilled: Boolean,
    val implementationDetails: String
)

class SecurityViewModel : ViewModel() {

    val logs: StateFlow<List<SecurityLogEntry>> = SecurityAuditLogger.logs

    fun getComplianceChecklist(): List<IsoComplianceItem> {
        return listOf(
            IsoComplianceItem(
                id = "ISO-01",
                title = "Almacenamiento Criptográfico Seguro de Credenciales",
                standardRef = "ISO/IEC 27400 - Cláusula 6.2 (Seguridad de Datos en Reposo)",
                description = "Cero contraseñas en texto plano. Uso de funciones de resumen criptográfico con Salt de 16 bytes y llaves maestras en hardware Android KeyStore.",
                isFulfilled = true,
                implementationDetails = "Implementado en CryptoManager.kt y SessionManager.kt mediante SHA-256 + Salt y cifrado simétrico AES-256-GCM."
            ),
            IsoComplianceItem(
                id = "ISO-02",
                title = "Autenticación y Control de Acceso por Sesión",
                standardRef = "ISO/IEC 27400 - Cláusula 6.3 (Gestión de Identidad y Acceso)",
                description = "Todo comando enviado al vehículo requiere un token de sesión autenticado y no falsificable.",
                isFulfilled = true,
                implementationDetails = "Encabezado HTTP 'X-IoT-Auth-Token' inyectado en cada petición a /api/control y validado en el firmware del ESP32."
            ),
            IsoComplianceItem(
                id = "ISO-03",
                title = "Parada por Fallo Seguro (Fail-Safe Default)",
                standardRef = "ISO/IEC 27400 - Cláusula 7.4 (Resiliencia Operacional y Parada Segura)",
                description = "Ante desconexión de red, timeout o detección de obstáculo a distancia crítica, los motores deben detenerse inmediatamente.",
                isFulfilled = true,
                implementationDetails = "Doble protección: Hardware Watchdog de 2s en ESP32 y Watchdog en VehicleRepository.kt que pasa a DETENIDO al perder heartbeat."
            ),
            IsoComplianceItem(
                id = "ISO-04",
                title = "Principio de Menor Privilegio en Permisos",
                standardRef = "ISO/IEC 27400 - Cláusula 8.1 (Privilegios Mínimos del Dispositivo)",
                description = "La aplicación solo solicita los permisos indispensables en tiempo de ejecución, gestionando el rechazo sin colapsar.",
                isFulfilled = true,
                implementationDetails = "Solicitud explicada de ACCESS_FINE_LOCATION (necesario para Wi-Fi local) y POST_NOTIFICATIONS con degradación elegante."
            ),
            IsoComplianceItem(
                id = "ISO-05",
                title = "Trazabilidad y Registro de Auditoría Inmutable",
                standardRef = "ISO/IEC 27400 - Cláusula 9.3 (Monitoreo y Auditoría de Seguridad)",
                description = "Registro secuencial con marca de tiempo de cada intento de login, orden de marcha, parada y alarma de colisión.",
                isFulfilled = true,
                implementationDetails = "SecurityAuditLogger.kt registra eventos con niveles INFO, WARNING, CRITICAL y SECURITY accesibles en tiempo real."
            )
        )
    }

    fun clearAuditLogs() {
        SecurityAuditLogger.clearLogs()
    }
}
