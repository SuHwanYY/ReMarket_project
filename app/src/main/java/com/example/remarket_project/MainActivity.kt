package com.example.remarket_project

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.remarket_project.auth.IntroActivity
import com.example.remarket_project.auth.SessionManager
import com.example.remarket_project.ui.home.HomeActivity

// 앱 시작 시 가장 먼저 실행되는 스플래시 화면
// 로그인 상태에 따라 홈 or 인트로 화면으로 이동
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // installSplashScreen()은 super.onCreate() 전에 호출해야 함
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // 스플래시 화면은 Compose로 구성 (나머지는 XML 사용)
        setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.main_logo),
                    contentDescription = null,
                    modifier = Modifier.size(300.dp)
                )
            }
        }

        // 저장된 토큰 불러오기 (자동 로그인 처리에 필요)
        SessionManager.loadToken(this)

        // 2초 후에 다음 화면으로 이동
        Handler(Looper.getMainLooper()).postDelayed({
            val next = if (SessionManager.isLoggedIn(this)) HomeActivity::class.java
                       else IntroActivity::class.java
            startActivity(Intent(this, next))
            finish() // 스플래시는 백스택에서 제거해야 뒤로가기로 돌아오지 않음
        }, 2000)
    }
}
