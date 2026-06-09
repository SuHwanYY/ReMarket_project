package com.example.remarket_project.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.remarket_project.databinding.ActivityJoinBinding
import com.example.remarket_project.viewmodel.AuthViewModel

// 회원가입 화면
class JoinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJoinBinding

    private val viewModel: AuthViewModel by viewModels()

    private val genderOptions = listOf("M", "W")
    private val regionOptions = listOf(
        "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종",
        "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupDropdowns()

        // 회원가입 버튼 클릭
        binding.joinBtn.setOnClickListener {
            if (validate()) {
                viewModel.register(
                    email = binding.emailArea.text.toString().trim(),
                    password = binding.pwdArea.text.toString(),
                    nickname = binding.nicknameArea.text.toString().trim(),
                    gender = binding.genderArea.text.toString(),
                    region = binding.regionArea.text.toString()
                )
            }
        }

        // 회원가입 성공하면 로그인 화면으로 이동
        viewModel.registerSuccess.observe(this) { success ->
            if (!success) return@observe
            Toast.makeText(this, "회원가입이 완료되었습니다!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        viewModel.isLoading.observe(this) { loading ->
            binding.joinBtn.isEnabled = !loading
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    // 성별, 지역 드롭다운 설정
    private fun setupDropdowns() {
        binding.genderArea.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genderOptions)
        )
        binding.regionArea.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, regionOptions)
        )
    }

    // 회원가입 전 입력값 검사 (서버에 보내기 전에 먼저 확인)
    private fun validate(): Boolean {
        var isValid = true
        val email      = binding.emailArea.text.toString().trim()
        val pwd        = binding.pwdArea.text.toString()
        val pwdConfirm = binding.pwdConfirmArea.text.toString()
        val nickname   = binding.nicknameArea.text.toString().trim()
        val gender     = binding.genderArea.text.toString()
        val region     = binding.regionArea.text.toString()

        // 이메일 형식 확인
        if (email.isEmpty()) {
            binding.emailLayout.error = "이메일을 입력해주세요"; isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = "올바른 이메일 형식이 아닙니다 (예: example@email.com)"; isValid = false
        } else binding.emailLayout.error = null

        // 비밀번호 조건: 8자 이상, 숫자+영문 포함
        if (pwd.isEmpty()) {
            binding.pwdLayout.error = "비밀번호를 입력해주세요"; isValid = false
        } else if (pwd.length < 8) {
            binding.pwdLayout.error = "비밀번호는 8자 이상이어야 합니다"; isValid = false
        } else if (!pwd.any { it.isDigit() }) {
            binding.pwdLayout.error = "비밀번호에 숫자를 포함해주세요"; isValid = false
        } else if (!pwd.any { it.isLetter() }) {
            binding.pwdLayout.error = "비밀번호에 영문자를 포함해주세요"; isValid = false
        } else binding.pwdLayout.error = null

        // 비밀번호 확인
        if (pwdConfirm.isEmpty()) {
            binding.pwdConfirmLayout.error = "비밀번호 확인을 입력해주세요"; isValid = false
        } else if (pwd != pwdConfirm) {
            binding.pwdConfirmLayout.error = "비밀번호가 일치하지 않습니다"; isValid = false
        } else binding.pwdConfirmLayout.error = null

        // 닉네임 2자 이상 체크
        if (nickname.isEmpty()) {
            binding.nicknameLayout.error = "닉네임을 입력해주세요"; isValid = false
        } else if (nickname.length < 2) {
            binding.nicknameLayout.error = "닉네임은 2자 이상이어야 합니다"; isValid = false
        } else binding.nicknameLayout.error = null

        // 성별, 지역 선택 여부
        if (gender !in genderOptions) {
            binding.genderLayout.error = "성별을 선택해주세요"; isValid = false
        } else binding.genderLayout.error = null

        if (region !in regionOptions) {
            binding.regionLayout.error = "지역을 선택해주세요"; isValid = false
        } else binding.regionLayout.error = null

        return isValid
    }
}
