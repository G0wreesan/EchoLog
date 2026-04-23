package com.echolog.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.echolog.app.ui.MainAppShell
import com.echolog.app.ui.auth.*
import com.echolog.app.ui.theme.EchoLogTheme
import com.echolog.app.viewmodel.RegistrationViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.compose.BackHandler

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EchoLogTheme {
                var currentFlow by remember { mutableStateOf("LOGIN") }
                val registrationViewModel: RegistrationViewModel = hiltViewModel()

                // Session Check on App Start
                LaunchedEffect(Unit) {
                    val user = registrationViewModel.checkSession()
                    if (user != null) {
                        currentFlow = "MAIN"
                    }
                }

                BackHandler(enabled = currentFlow != "LOGIN" && currentFlow != "MAIN") {
                    currentFlow = when (currentFlow) {
                        "REGISTER_STEP_B" -> "REGISTER_STEP_A"
                        "REGISTER_STEP_C" -> "REGISTER_STEP_B"
                        "PROFILE_REVIEW" -> "REGISTER_STEP_C"
                        else -> "LOGIN"
                    }
                }

                AnimatedContent(
                    targetState = currentFlow,
                    transitionSpec = {
                        (slideInHorizontally { it } + fadeIn())
                            .togetherWith(slideOutHorizontally { -it } + fadeOut())
                    },
                    label = "flow_navigation"
                ) { targetState ->
                    when (targetState) {
                        "LOGIN" -> LoginScreen(
                            viewModel = registrationViewModel, // Pass it here
                            onLoginSuccess = { currentFlow = "MAIN" },
                            onNavigateToRegister = { currentFlow = "REGISTER_STEP_A" },
                            onContinueAsGuest = { currentFlow = "MAIN" }
                        )

                        "REGISTER_STEP_A" -> RegistrationStepA(
                            viewModel = registrationViewModel,
                            onNext = { currentFlow = "REGISTER_STEP_B" },
                            onBack = { currentFlow = "LOGIN" },
                            onContinueAsGuest = { currentFlow = "MAIN" }
                        )

                        "REGISTER_STEP_B" -> RegistrationStepB(
                            viewModel = registrationViewModel,
                            onNext = { currentFlow = "REGISTER_STEP_C" },
                            onBack = { currentFlow = "REGISTER_STEP_A" }
                        )

                        "REGISTER_STEP_C" -> RegistrationStepC(
                            viewModel = registrationViewModel,
                            onComplete = { currentFlow = "PROFILE_REVIEW" }, // TRIGGERS REVIEW
                            onBack = { currentFlow = "REGISTER_STEP_B" }
                        )

                        "PROFILE_REVIEW" -> ProfileReviewScreen(
                            viewModel = registrationViewModel,
                            onFinish = {
                                registrationViewModel.finalizeAccount {
                                    currentFlow = "LOGIN"
                                }
                            }
                        )

                        "MAIN" -> MainAppShell(
                            registrationViewModel = registrationViewModel, // Use the new parameter name here
                            onLogout = {
                                registrationViewModel.logout()
                                currentFlow = "LOGIN"
                            }
                        )
                    }
                }
            }
        }
    }
}