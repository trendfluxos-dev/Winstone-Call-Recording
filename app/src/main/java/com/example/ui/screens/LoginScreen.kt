package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.Localization
import com.example.ui.WinstoneViewModel

@Composable
fun LoginScreen(
    viewModel: WinstoneViewModel,
    onLoginSuccess: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    val crmBaseUrl by viewModel.crmBaseUrl.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var employeeId by remember { mutableStateOf("") }
    var serverUrl by remember(crmBaseUrl) { mutableStateOf(crmBaseUrl) }
    var showServerConfig by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.PhoneInTalk,
                contentDescription = "App Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "WINSTONE CRM",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text = "Auto Call Recorder",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Proton HyperX • Android 16 Enterprise Companion",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = Localization.get("login_title", language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(Localization.get("work_email", language)) },
                    modifier = Modifier.fillMaxWidth().testTag("input_login_email"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(Localization.get("password", language)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().testTag("input_login_password"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = employeeId,
                    onValueChange = { employeeId = it },
                    label = { Text(Localization.get("employee_id", language)) },
                    modifier = Modifier.fillMaxWidth().testTag("input_login_employee_id"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                if (showServerConfig) {
                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it },
                        label = { Text("Winstone CRM Server") },
                        modifier = Modifier.fillMaxWidth().testTag("input_server_url"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Endpoint: ${if (serverUrl.length > 28) serverUrl.take(28) + "..." else serverUrl}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = { showServerConfig = !showServerConfig }) {
                        Text(if (showServerConfig) "Hide" else "Change", fontSize = 11.sp)
                    }
                }

                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        if (showServerConfig && serverUrl.isNotBlank()) {
                            viewModel.setServerUrl(serverUrl)
                        }
                        viewModel.login(email, password, employeeId) { ok ->
                            isLoading = false
                            if (ok) {
                                onLoginSuccess()
                            } else {
                                errorMessage = "Invalid credentials. Please verify your Winstone corporate account."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_submit_login"),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Lock, contentDescription = "Lock", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Localization.get("sign_in", language), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
