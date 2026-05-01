package com.example.savestate.ui.theme.screens.auth

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.savestate.R
import com.example.savestate.ui.theme.components.AppButton
import com.example.savestate.ui.theme.components.GoogleButton
import com.example.savestate.ui.theme.components.TextDivider

@Composable
fun AuthScreen(
    modifier: Modifier,
    onLoginSuccess: () -> Unit
) {
    val viewModel: AuthViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var isLoginMode by rememberSaveable { mutableStateOf(true) }
    var email by rememberSaveable { mutableStateOf("") }

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // logo and blurred wallpaper
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            // background image
            Image(
                painter = painterResource(id = R.drawable.auth_background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // semi-transparent overlay to maintain primary color
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
            )
            // emulates a fade on the card with a gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                                0.5f to MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                0.9f to MaterialTheme.colorScheme.surface
                            )
                        )
                    )
            )

            // logo
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.logo_auth),
                contentDescription = "Savestate login/register page",
                tint = Color.White,
                modifier = Modifier
                    .size(190.dp)
                    .align(Alignment.Center)
            )
        }

        Column(
            modifier = modifier
                .fillMaxWidth()
                .offset(y = (-32).dp)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // login / signup tabs
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

            // input fields form
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.animateContentSize(
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                )
            ) {
                // email
                AuthFormInputField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email"
                )
                // password
                AuthFormInputField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    isPassword = true,
                    isPasswordVisible = passwordVisible,
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = image,
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    },
                )
                // password confirmation
                if (!isLoginMode) {
                    AuthFormInputField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirm Password",
                        isPassword = true,
                        isPasswordVisible = passwordVisible,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            AppButton(
                onClick = {}
            ) {
                Text(
                    text = if (isLoginMode) "Sign in" else "Create account",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            TextDivider("Or sign ${if (isLoginMode) "in" else "up"} with...")

            GoogleButton(
                text = if (isLoginMode) "Sign in with Google" else "Sign up with Google",
                onClick = {}
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
        keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default,
        trailingIcon = trailingIcon,
    )
}