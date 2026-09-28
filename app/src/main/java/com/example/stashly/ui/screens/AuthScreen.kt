package com.example.stashly.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.stashly.R
import com.example.stashly.ui.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    val email by authViewModel.email.collectAsState()
    val password by authViewModel.password.collectAsState()
    val confirmPassword by authViewModel.confirmPassword.collectAsState()
    val isSignUpMode by authViewModel.isSignUpMode.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo & Header
                Image(
                    painter = painterResource(id = R.drawable.ic_stashly_logo),
                    contentDescription = "Stashly Logo",
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Stashly Cloud",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Your Private Cross-Device Inbox",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Auth Form Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Segmented Toggle (Sign In / Sign Up)
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = !isSignUpMode,
                                onClick = { if (isSignUpMode) authViewModel.toggleAuthMode() },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                            ) {
                                Text("Sign In", style = MaterialTheme.typography.labelMedium)
                            }
                            SegmentedButton(
                                selected = isSignUpMode,
                                onClick = { if (!isSignUpMode) authViewModel.toggleAuthMode() },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                            ) {
                                Text("Sign Up", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { authViewModel.setEmail(it) },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { authViewModel.setPassword(it) },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password visibility"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Confirm Password (Sign Up Mode only)
                        if (isSignUpMode) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { authViewModel.setConfirmPassword(it) },
                                label = { Text("Confirm Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Error Banner
                        if (!errorMessage.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Submit Button
                        Button(
                            onClick = { authViewModel.submit(onAuthSuccess) },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Connecting...")
                            } else {
                                Text(
                                    text = if (isSignUpMode) "Create Account" else "Sign In",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Toggle Mode Text Button
                        TextButton(onClick = { authViewModel.toggleAuthMode() }) {
                            Text(
                                text = if (isSignUpMode) "Already have an account? Sign In" else "New to Stashly? Create an account",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
