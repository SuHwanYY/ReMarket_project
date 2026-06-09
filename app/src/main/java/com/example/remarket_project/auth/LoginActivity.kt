package com.example.remarket_project.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.remarket_project.databinding.ActivityLoginBinding
import com.example.remarket_project.ui.home.HomeActivity
import com.example.remarket_project.viewmodel.AuthViewModel

// 로그인 화면
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    // ViewModel로 로그인 로직 분리
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 로그인 버튼 클릭 시 유효성 검사 먼저
        binding.joinSuccessBtn.setOnClickListener {
            if (validate()) {
                viewModel.login(
                    email = binding.emailArea.text.toString().trim(),
                    password = binding.pwdArea.text.toString()
                )
            }
        }

        // 로그인 성공 시 토큰이랑 유저 정보 저장하고 홈으로 이동
        viewModel.loginSuccess.observe(this) { loginData ->
            loginData ?: return@observe
            SessionManager.saveSession(
                context = this,
                token = loginData.token,
                userId = loginData.user.id,
                nickname = loginData.user.nickname,
                email = loginData.user.email
            )
            Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }

        // 로딩 중 버튼 비활성화 (중복 요청 방지)
        viewModel.isLoading.observe(this) { loading ->
            binding.joinSuccessBtn.isEnabled = !loading
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    // 입력값 유효성 검사
    private fun validate(): Boolean {
        var isValid = true
        val email = binding.emailArea.text.toString().trim()
        val pwd = binding.pwdArea.text.toString()

        if (email.isEmpty()) {
            binding.emailLayout.error = "이메일을 입력해주세요"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = "올바른 이메일 형식이 아닙니다"
            isValid = false
        } else {
            binding.emailLayout.error = null
        }

        if (pwd.isEmpty()) {
            binding.pwdLayout.error = "비밀번호를 입력해주세요"
            isValid = false
        } else {
            binding.pwdLayout.error = null
        }

        return isValid
    }
}
