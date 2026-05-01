package com.example.savestate.ui.theme.screens.auth

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.savestate.R

@Composable
fun AuthScreen(
    modifier: Modifier,
    onLoginSuccess: () -> Unit
) {
    val viewModel: AuthViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var isLoginMode by rememberSaveable { mutableStateOf(true) }
    var email by rememberSaveable { mutableStateOf("") }
    var nickname by rememberSaveable { mutableStateOf("") }

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // logo
        Spacer(modifier = Modifier.height(64.dp))
        Box(
            modifier = Modifier
                .size(170.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.logo_auth),
                contentDescription = "Savestate login/register page",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(150.dp)
            )
        }
        // login / signup tabs
        Spacer(modifier = Modifier.height(20.dp))
        TabRow(
            selectedTabIndex = if (isLoginMode) 0 else 1
        ) {
            AuthTab(
                selected = isLoginMode,
                onClick = { isLoginMode = true },
                text = "Sign in"
            )
            AuthTab(
                selected = !isLoginMode,
                onClick = { isLoginMode = false },
                text = "Sign up"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        // input fields form
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // email
            AuthFormInputField(
                value = email,
                onValueChange = { email = it },
                label = "Email"
            )
            // nickname, only if on sign up
            if (!isLoginMode) {
                AuthFormInputField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = "Nickname"
                )
            }
            // password
            AuthFormInputField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                isPassword = true,
                isPasswordVisible = passwordVisible,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = "Toggle password visibility")
                    }
                },
            )
            // password confirmation
            AuthFormInputField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirm Password",
                isPassword = true,
                isPasswordVisible = passwordVisible,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )
        }
    }
}

@Composable
fun AuthTab(
    selected: Boolean,
    onClick: () -> Unit,
    text: String
) {
    Tab(
        selected = selected,
        onClick = onClick,
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge
            )
        }
    )
}

@Composable
fun AuthFormInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    keyboardOptions: KeyboardOptions =  KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = isPassword,
        visualTransformation =
            if (isPassword && !isPasswordVisible) PasswordVisualTransformation()
            else VisualTransformation.None,
        keyboardOptions = keyboardOptions,
        trailingIcon = trailingIcon
    )
}