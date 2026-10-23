package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MonochromeBlack
import com.example.ui.theme.MonochromeCardBorder
import com.example.ui.theme.MonochromeCardSurface
import com.example.ui.theme.MonochromeDarkSurface
import com.example.ui.theme.MonochromeLightGrey
import com.example.ui.theme.MonochromeMediumGrey
import com.example.ui.theme.MonochromeMutedText
import com.example.ui.theme.MonochromeWhite
import com.example.ui.viewmodels.AuthUiState
import com.example.ui.viewmodels.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToDocs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val uiState by authViewModel.uiState.collectAsState()
    val email by authViewModel.emailInput.collectAsState()
    val password by authViewModel.passwordInput.collectAsState()
    val isPasswordVisible by authViewModel.isPasswordVisible.collectAsState()

    var showRegisterDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MonochromeBlack)
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Logo en Blanco y Negro
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(MonochromeCardSurface)
                    .border(1.5.dp, MonochromeWhite, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = "Auto IoT Control",
                    tint = MonochromeWhite,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Auto IoT Control",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MonochromeWhite
            )

            Text(
                text = "Evaluación TI3042 ES2 • Vehículo ESP32 + L298N",
                style = MaterialTheme.typography.bodyMedium,
                color = MonochromeLightGrey,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Badges en Blanco y Negro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                SecurityBadgeBW(label = "AES-256 KeyStore", icon = Icons.Default.Key)
                Spacer(modifier = Modifier.width(8.dp))
                SecurityBadgeBW(label = "ISO/IEC 27400", icon = Icons.Default.Security)
                Spacer(modifier = Modifier.width(8.dp))
                SecurityBadgeBW(label = "Wi-Fi ESP32", icon = Icons.Default.Wifi)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tarjeta de Formulario de Login
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MonochromeCardSurface),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MonochromeCardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Inicio de Sesión Seguro",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MonochromeWhite
                    )
                    Text(
                        text = "Persistencia cifrada • Cero contraseñas en plano",
                        style = MaterialTheme.typography.labelSmall,
                        color = MonochromeMutedText
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Campo de Correo / Operador
                    OutlinedTextField(
                        value = email,
                        onValueChange = { authViewModel.onEmailChange(it) },
                        label = { Text("Operador / Correo", color = MonochromeLightGrey) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = MonochromeWhite)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MonochromeWhite,
                            unfocusedBorderColor = MonochromeCardBorder,
                            focusedTextColor = MonochromeWhite,
                            unfocusedTextColor = MonochromeWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Campo de Contraseña
                    OutlinedTextField(
                        value = password,
                        onValueChange = { authViewModel.onPasswordChange(it) },
                        label = { Text("Contraseña de Acceso", color = MonochromeLightGrey) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MonochromeWhite)
                        },
                        trailingIcon = {
                            IconButton(onClick = { authViewModel.togglePasswordVisibility() }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                    tint = MonochromeWhite
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                authViewModel.login()
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MonochromeWhite,
                            unfocusedBorderColor = MonochromeCardBorder,
                            focusedTextColor = MonochromeWhite,
                            unfocusedTextColor = MonochromeWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input")
                    )

                    // Mensaje de Error
                    if (uiState is AuthUiState.Error) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MonochromeDarkSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MonochromeWhite),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = (uiState as AuthUiState.Error).message,
                                color = MonochromeWhite,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botón de Iniciar Sesión en Blanco y Negro
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            authViewModel.login()
                        },
                        enabled = uiState !is AuthUiState.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MonochromeWhite,
                            contentColor = MonochromeBlack
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_button")
                    ) {
                        if (uiState is AuthUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MonochromeBlack,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "INICIAR SESIÓN",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botón de Carga Rápida para Evaluación
                    OutlinedButton(
                        onClick = {
                            authViewModel.fillEvaluationCredentials()
                            Toast.makeText(context, "Credenciales de evaluación TI3042 cargadas.", Toast.LENGTH_SHORT).show()
                        },
                        border = androidx.compose.foundation.BorderStroke(1.dp, MonochromeCardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MonochromeWhite),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("fill_eval_creds_button")
                    ) {
                        Text(
                            "⚡ Cargar Credenciales TI3042 (Docente)",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showRegisterDialog = true }) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = MonochromeWhite, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Registrar Operador", style = MaterialTheme.typography.bodySmall, color = MonochromeWhite)
                        }

                        TextButton(onClick = onNavigateToDocs) {
                            Text("Rúbrica & Manual", style = MaterialTheme.typography.bodySmall, color = MonochromeLightGrey)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Seguridad Criptográfica Nativa: AndroidKeyStore AES-256-GCM + Salt 16B",
                style = MaterialTheme.typography.labelSmall,
                color = MonochromeMutedText,
                textAlign = TextAlign.Center
            )
        }
    }

    if (showRegisterDialog) {
        RegisterOperatorDialogBW(
            onDismiss = { showRegisterDialog = false },
            onRegister = { newEmail, newPass, newRole ->
                authViewModel.registerNewOperator(newEmail, newPass, newRole) { result ->
                    result.onSuccess {
                        Toast.makeText(context, "Operador registrado con éxito.", Toast.LENGTH_SHORT).show()
                        showRegisterDialog = false
                    }.onFailure { err ->
                        Toast.makeText(context, "Error: ${err.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }
}

@Composable
fun SecurityBadgeBW(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        color = MonochromeCardSurface,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MonochromeCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MonochromeWhite, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = MonochromeWhite
            )
        }
    }
}

@Composable
fun RegisterOperatorDialogBW(
    onDismiss: () -> Unit,
    onRegister: (email: String, pass: String, role: String) -> Unit
) {
    var regEmail by remember { mutableStateOf("") }
    var regPass by remember { mutableStateOf("") }
    var regRole by remember { mutableStateOf("Operador de Campo") }

    AlertDialog(
        containerColor = MonochromeCardSurface,
        onDismissRequest = onDismiss,
        title = { Text("Registrar Nuevo Operador", color = MonochromeWhite) },
        text = {
            Column {
                Text(
                    "Las contraseñas son transformadas mediante hash SHA-256 + Salt y resguardadas con AES-256 en Android KeyStore.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MonochromeMutedText
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = regEmail,
                    onValueChange = { regEmail = it },
                    label = { Text("Correo del Operador", color = MonochromeLightGrey) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MonochromeWhite,
                        unfocusedBorderColor = MonochromeCardBorder,
                        focusedTextColor = MonochromeWhite,
                        unfocusedTextColor = MonochromeWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = regPass,
                    onValueChange = { regPass = it },
                    label = { Text("Contraseña (mínimo 6 caracteres)", color = MonochromeLightGrey) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MonochromeWhite,
                        unfocusedBorderColor = MonochromeCardBorder,
                        focusedTextColor = MonochromeWhite,
                        unfocusedTextColor = MonochromeWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = regRole,
                    onValueChange = { regRole = it },
                    label = { Text("Rol / Nivel de Acceso", color = MonochromeLightGrey) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MonochromeWhite,
                        unfocusedBorderColor = MonochromeCardBorder,
                        focusedTextColor = MonochromeWhite,
                        unfocusedTextColor = MonochromeWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onRegister(regEmail, regPass, regRole)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MonochromeWhite,
                    contentColor = MonochromeBlack
                )
            ) {
                Text("Registrar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MonochromeLightGrey)
            }
        }
    )
}
