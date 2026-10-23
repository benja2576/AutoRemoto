package com.example.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.LogSeverity
import com.example.data.security.SecurityAuditLogger
import com.example.data.security.SessionManager
import com.example.data.security.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Authenticated(val session: UserSession) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

/**
 * ViewModel encargado del Login Seguro, verificación de credenciales con hash criptográfico salado
 * y persistencia cifrada de sesión (Android KeyStore + AES-256-GCM).
 */
class AuthViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _emailInput = MutableStateFlow("")
    val emailInput: StateFlow<String> = _emailInput.asStateFlow()

    private val _passwordInput = MutableStateFlow("")
    val passwordInput: StateFlow<String> = _passwordInput.asStateFlow()

    private val _isPasswordVisible = MutableStateFlow(false)
    val isPasswordVisible: StateFlow<Boolean> = _isPasswordVisible.asStateFlow()

    init {
        checkExistingSession()
    }

    private fun checkExistingSession() {
        val activeSession = sessionManager.getActiveSession()
        if (activeSession != null) {
            _uiState.value = AuthUiState.Authenticated(activeSession)
            SecurityAuditLogger.logEvent(
                eventType = "SESION_RESTAURADA",
                description = "Sesión cifrada restaurada para ${activeSession.email}",
                severity = LogSeverity.INFO,
                operator = activeSession.email
            )
        }
    }

    fun onEmailChange(newEmail: String) {
        _emailInput.value = newEmail
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }

    fun onPasswordChange(newPassword: String) {
        _passwordInput.value = newPassword
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }

    fun togglePasswordVisibility() {
        _isPasswordVisible.value = !_isPasswordVisible.value
    }

    /**
     * Llena las credenciales de evaluación TI3042 ES2 con un solo toque para agilizar la prueba.
     */
    fun fillEvaluationCredentials() {
        _emailInput.value = SessionManager.EVALUATION_EMAIL
        _passwordInput.value = SessionManager.EVALUATION_PASSWORD
    }

    fun login() {
        val email = _emailInput.value.trim()
        val password = _passwordInput.value

        if (email.isEmpty()) {
            _uiState.value = AuthUiState.Error("Por favor ingrese el correo electrónico del operador.")
            return
        }
        if (password.isEmpty()) {
            _uiState.value = AuthUiState.Error("Por favor ingrese la contraseña de acceso.")
            return
        }

        _uiState.value = AuthUiState.Loading

        viewModelScope.launch {
            val result = sessionManager.authenticate(email, password)
            result.onSuccess { session ->
                _uiState.value = AuthUiState.Authenticated(session)
                SecurityAuditLogger.logEvent(
                    eventType = "LOGIN_EXITOSO",
                    description = "Autenticación válida. Sesión cifrada creada con token AES-256.",
                    severity = LogSeverity.SECURITY,
                    operator = session.email
                )
                // Limpiar contraseña de la memoria efímera del ViewModel
                _passwordInput.value = ""
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.message ?: "Credenciales inválidas")
                SecurityAuditLogger.logEvent(
                    eventType = "LOGIN_FALLIDO",
                    description = "Intento de inicio de sesión fallido para: $email",
                    severity = LogSeverity.WARNING,
                    operator = email
                )
            }
        }
    }

    fun registerNewOperator(email: String, password: String, role: String, onComplete: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = sessionManager.registerOperator(email, password, role)
            if (result.isSuccess) {
                SecurityAuditLogger.logEvent(
                    eventType = "REGISTRO_OPERADOR",
                    description = "Nuevo operador registrado con credenciales saladas: $email ($role)",
                    severity = LogSeverity.SECURITY
                )
            }
            onComplete(result)
        }
    }

    fun logout() {
        val currentUser = (uiState.value as? AuthUiState.Authenticated)?.session?.email ?: "desconocido"
        sessionManager.logout()
        _uiState.value = AuthUiState.Idle
        _passwordInput.value = ""
        SecurityAuditLogger.logEvent(
            eventType = "LOGOUT",
            description = "Sesión cerrada de forma segura. Tokens y claves temporales revocadas.",
            severity = LogSeverity.SECURITY,
            operator = currentUser
        )
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val sessionManager = SessionManager(context.applicationContext)
                    return AuthViewModel(sessionManager) as T
                }
            }
    }
}
