package com.example.data.security

import com.example.data.model.LogSeverity
import com.example.data.model.SecurityLogEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Registro de auditoría in-memory para trazabilidad de eventos de seguridad (ISO/IEC 27400).
 */
object SecurityAuditLogger {

    private val _logs = MutableStateFlow<List<SecurityLogEntry>>(emptyList())
    val logs: StateFlow<List<SecurityLogEntry>> = _logs.asStateFlow()

    init {
        logEvent(
            eventType = "SISTEMA_INICIADO",
            description = "Subordinado de seguridad inicializado con Android KeyStore AES-256.",
            severity = LogSeverity.INFO,
            operator = "system"
        )
    }

    fun logEvent(
        eventType: String,
        description: String,
        severity: LogSeverity = LogSeverity.INFO,
        operator: String = "operador"
    ) {
        val entry = SecurityLogEntry(
            id = UUID.randomUUID().toString().take(8),
            timestamp = System.currentTimeMillis(),
            eventType = eventType,
            description = description,
            severity = severity,
            operator = operator
        )
        // Mantener últimos 50 eventos para no sobrecargar memoria
        val current = _logs.value.toMutableList()
        current.add(0, entry)
        if (current.size > 50) {
            _logs.value = current.take(50)
        } else {
            _logs.value = current
        }
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }
}
