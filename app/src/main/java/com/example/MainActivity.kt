package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DocumentationScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PermissionsHandler
import com.example.ui.screens.SecurityComplianceScreen
import com.example.ui.theme.AutoIoTTheme
import com.example.ui.viewmodels.AuthUiState
import com.example.ui.viewmodels.AuthViewModel
import com.example.ui.viewmodels.DashboardViewModel
import com.example.ui.viewmodels.SecurityViewModel

enum class CurrentScreen {
    LOGIN,
    DASHBOARD,
    SECURITY_COMPLIANCE,
    DOCUMENTATION
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AutoIoTTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authViewModel: AuthViewModel = viewModel(
                        factory = AuthViewModel.provideFactory(this@MainActivity)
                    )
                    val dashboardViewModel: DashboardViewModel = viewModel(
                        factory = DashboardViewModel.provideFactory(application)
                    )
                    val securityViewModel: SecurityViewModel = viewModel()

                    AutoIoTAppNavigation(
                        authViewModel = authViewModel,
                        dashboardViewModel = dashboardViewModel,
                        securityViewModel = securityViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun AutoIoTAppNavigation(
    authViewModel: AuthViewModel,
    dashboardViewModel: DashboardViewModel,
    securityViewModel: SecurityViewModel
) {
    val authState by authViewModel.uiState.collectAsState()
    var currentSubScreen by remember { mutableStateOf<CurrentScreen?>(null) }

    // Gestor de permisos en tiempo de ejecución (Criterio 3: Permisos y Seguridad)
    PermissionsHandler()

    when (val state = authState) {
        is AuthUiState.Authenticated -> {
            when (currentSubScreen) {
                CurrentScreen.SECURITY_COMPLIANCE -> {
                    SecurityComplianceScreen(
                        securityViewModel = securityViewModel,
                        onNavigateBack = { currentSubScreen = null }
                    )
                }
                CurrentScreen.DOCUMENTATION -> {
                    DocumentationScreen(
                        onNavigateBack = { currentSubScreen = null }
                    )
                }
                else -> {
                    DashboardScreen(
                        session = state.session,
                        dashboardViewModel = dashboardViewModel,
                        onNavigateToSecurity = { currentSubScreen = CurrentScreen.SECURITY_COMPLIANCE },
                        onNavigateToDocs = { currentSubScreen = CurrentScreen.DOCUMENTATION },
                        onLogout = {
                            currentSubScreen = null
                            authViewModel.logout()
                        }
                    )
                }
            }
        }
        else -> {
            if (currentSubScreen == CurrentScreen.DOCUMENTATION) {
                DocumentationScreen(
                    onNavigateBack = { currentSubScreen = null }
                )
            } else {
                LoginScreen(
                    authViewModel = authViewModel,
                    onNavigateToDocs = { currentSubScreen = CurrentScreen.DOCUMENTATION }
                )
            }
        }
    }
}
