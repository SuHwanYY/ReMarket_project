package com.example.remarket_project.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.remarket_project.R
import com.example.remarket_project.ui.home.HomeActivity

// 로그인/회원가입 선택 화면
// 이미 로그인된 상태면 이 화면 건너뛰고 홈으로 바로 이동
class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 토큰이 있으면 자동으로 홈으로 이동
        SessionManager.loadToken(this)
        if (SessionManager.isLoggedIn(this)) {
            startActivity(Intent(this, HomeActivity::class.java).apply {
                // 뒤로가기로 인트로 화면으로 돌아오지 않도록 플래그 설정
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_intro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 회원가입 버튼
        val joinBtn = findViewById<Button>(R.id.joinBtn)
        joinBtn.setOnClickListener {
            startActivity(Intent(this, JoinActivity::class.java))
        }

        // 로그인 버튼
        val loginBtn = findViewById<Button>(R.id.loginBtn)
        loginBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
