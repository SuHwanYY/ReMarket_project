package com.example.remarket_project.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.remarket_project.R
import com.example.remarket_project.ui.home.HomeActivity
import com.example.remarket_project.viewmodel.AuthViewModel

class LoginActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    startActivity(Intent(this, HomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
            )
        }
    }
}

@Composable
private fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading.observeAsState(false)
    val loginSuccess by viewModel.loginSuccess.observeAsState(null)
    val error by viewModel.error.observeAsState(null)

    // 에러 토스트 — error가 null이 아닐 때만 실행
    LaunchedEffect(error) {
        if (error != null) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    // 로그인 성공 시 세션 저장 후 홈으로 이동
    LaunchedEffect(loginSuccess) {
        loginSuccess?.let { data ->
            SessionManager.saveSession(
                context = context,
                token = data.token,
                userId = data.user.id,
                nickname = data.user.nickname,
                email = data.user.email
            )
            Toast.makeText(context, "로그인 성공!", Toast.LENGTH_SHORT).show()
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.main_logo),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = null
            },
            label = { Text("이메일") },
            isError = emailError != null,
            supportingText = if (emailError != null) ({ Text(emailError!!) }) else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp, vertical = 5.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                if (it.length <= 20) {
                    password = it
                    passwordError = null
                }
            },
            label = { Text("비밀번호") },
            isError = passwordError != null,
            supportingText = if (passwordError != null) ({ Text(passwordError!!) }) else null,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp, vertical = 5.dp)
        )

        Button(
            onClick = {
                val trimmedEmail = email.trim()
                var valid = true

                if (trimmedEmail.isEmpty()) {
                    emailError = "이메일을 입력해주세요"
                    valid = false
                } else if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                    emailError = "올바른 이메일 형식이 아닙니다"
                    valid = false
                }

                if (password.isEmpty()) {
                    passwordError = "비밀번호를 입력해주세요"
                    valid = false
                }

                if (valid) {
                    viewModel.login(trimmedEmail, password)
                }
            },
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.mainColor)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text("로그인")
        }
    }
}
