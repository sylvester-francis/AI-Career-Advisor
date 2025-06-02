package com.sylvester.careerbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.sylvester.careerbot.ui.ChatScreen
import com.sylvester.careerbot.ui.theme.CareerBotTheme
import com.sylvester.careerbot.utils.NetworkConnectivityObserver
import com.sylvester.careerbot.utils.ConnectivityObserver
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : ComponentActivity() {

    private lateinit var connectivityObserver: ConnectivityObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        // Initialize network observer
        connectivityObserver = NetworkConnectivityObserver(applicationContext)

        setContent {
            CareerBotTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                var isOnline by remember { mutableStateOf(true) }

                // Observe network connectivity
                LaunchedEffect(connectivityObserver) {
                    connectivityObserver.observe().onEach { status ->
                        isOnline = status == ConnectivityObserver.Status.Available

                        when (status) {
                            ConnectivityObserver.Status.Unavailable -> {
                                snackbarHostState.showSnackbar(
                                    message = "No internet connection",
                                    actionLabel = "Dismiss"
                                )
                            }
                            ConnectivityObserver.Status.Losing -> {
                                snackbarHostState.showSnackbar(
                                    message = "Internet connection is unstable",
                                    actionLabel = "Dismiss"
                                )
                            }
                            ConnectivityObserver.Status.Lost -> {
                                snackbarHostState.showSnackbar(
                                    message = "Internet connection lost",
                                    actionLabel = "Dismiss"
                                )
                            }
                            ConnectivityObserver.Status.Available -> {
                                if (!isOnline) {
                                    snackbarHostState.showSnackbar(
                                        message = "Back online!",
                                        actionLabel = "Dismiss"
                                    )
                                }
                            }
                        }
                    }.launchIn(this)
                }

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { _ ->
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        ChatScreen()
                    }
                }
            }
        }
    }
}