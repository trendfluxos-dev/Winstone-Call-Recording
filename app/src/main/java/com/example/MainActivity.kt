package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.WinstoneViewModel
import com.example.ui.navigation.NavRoutes
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: WinstoneViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                val userMessage by viewModel.userMessage.collectAsState()
                val session by viewModel.session.collectAsState()

                // Permission launcher
                val permissionsLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) {
                    viewModel.runFullDiagnostics()
                }

                LaunchedEffect(Unit) {
                    val permissions = mutableListOf(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_PHONE_STATE
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    permissionsLauncher.launch(permissions.toTypedArray())
                }

                LaunchedEffect(userMessage) {
                    userMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearMessage()
                    }
                }

                val startDestination = if (session.isLoggedIn) NavRoutes.HOME else NavRoutes.LOGIN

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(NavRoutes.LOGIN) {
                            LoginScreen(
                                viewModel = viewModel,
                                onLoginSuccess = {
                                    navController.navigate(NavRoutes.HOME) {
                                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(NavRoutes.HOME) {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigate = { route -> navController.navigate(route) }
                            )
                        }
                        composable(NavRoutes.LEADS) {
                            LeadsScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(NavRoutes.RECENT_CALLS) {
                            RecentCallsScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(NavRoutes.RECORDINGS) {
                            RecordingsScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(NavRoutes.DIAGNOSTICS) {
                            DiagnosticsScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(NavRoutes.TEST_CALL) {
                            TestCallScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(NavRoutes.POLICY) {
                            PolicyScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(NavRoutes.AUTHORITY) {
                            AuthorityScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(NavRoutes.SETTINGS) {
                            SettingsScreen(
                                viewModel = viewModel,
                                onNavigate = { route -> navController.navigate(route) },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
