package com.example.homework1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.homework1.ui.theme.Homework1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Homework1Theme {
                var showSecondScreen by rememberSaveable { mutableStateOf(false) }
                var email by rememberSaveable { mutableStateOf("") }
                var enteredPassword by rememberSaveable { mutableStateOf("") }
                val generatedPassword = rememberSaveable { makePassword() }

                if (showSecondScreen) {
                    SecondScreen(
                        email = email,
                        enteredPassword = enteredPassword,
                        generatedPassword = generatedPassword,
                        onBack = { showSecondScreen = false }
                    )
                } else {
                    FirstScreen(
                        onContinue = { enteredEmail, password ->
                            email = enteredEmail
                            enteredPassword = password
                            showSecondScreen = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FirstScreen(onContinue: (String, String) -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }

    val emailPattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun checkFields() {
        emailError = when {
            email.isBlank() -> "Введите адрес электронной почты"
            !emailPattern.matches(email) -> "Проверьте адрес электронной почты"
            else -> null
        }
        passwordError = when {
            password.isBlank() -> "Введите пароль"
            password.length < 8 ||
                !password.any { it in 'a'..'z' || it in 'A'..'Z' } ||
                !password.any { it in '0'..'9' } -> "Нужно минимум 8 символов, латинская буква и цифра"
            else -> null
        }

        if (emailError == null && passwordError == null) {
            onContinue(email, password)
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(R.string.authorization_title))
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.email_label)) },
                isError = emailError != null,
                supportingText = { emailError?.let { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.password_label)) },
                isError = passwordError != null,
                supportingText = { passwordError?.let { Text(it) } },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { checkFields() })
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { checkFields() }) {
                Text(text = stringResource(R.string.continue_button))
            }
        }
    }
}

@Composable
private fun SecondScreen(
    email: String,
    enteredPassword: String,
    generatedPassword: String,
    onBack: () -> Unit
) {
    var showPassword by rememberSaveable { mutableStateOf(false) }
    val passwordMatches = enteredPassword == generatedPassword
    val imageUrl = if (passwordMatches) {
        "https://picsum.photos/seed/homework2-success/700/450"
    } else {
        "https://picsum.photos/seed/homework2-failure/700/450"
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = email)
            Spacer(modifier = Modifier.height(16.dp))
            AsyncImage(
                model = imageUrl,
                contentDescription = stringResource(R.string.result_image_description),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (passwordMatches) {
                    stringResource(R.string.password_correct)
                } else {
                    stringResource(R.string.password_incorrect)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = generatedPassword,
                    onValueChange = {},
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.generated_password_label)) },
                    readOnly = true,
                    singleLine = true,
                    visualTransformation = if (showPassword) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    }
                )
                Button(onClick = { showPassword = !showPassword }) {
                    Text(
                        text = if (showPassword) {
                            stringResource(R.string.hide_password)
                        } else {
                            stringResource(R.string.show_password)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBack) {
                Text(text = stringResource(R.string.back_button))
            }
        }
    }
}

private fun makePassword(): String {
    val letters = ('a'..'z') + ('A'..'Z')
    val digits = '0'..'9'
    val allCharacters = letters + digits

    return (listOf(letters.random(), digits.random()) +
        List(8) { allCharacters.random() })
        .shuffled()
        .joinToString("")
}

