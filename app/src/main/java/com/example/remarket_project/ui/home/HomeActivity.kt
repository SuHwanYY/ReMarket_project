package com.example.remarket_project.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.remarket_project.R
import com.example.remarket_project.auth.IntroActivity
import com.example.remarket_project.auth.SessionManager
import com.example.remarket_project.databinding.ActivityHomeBinding

// 메인 화면 (바텀 네비게이션 + 드로어 메뉴)
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    // 다크모드 설정 저장용
    private val darkModePrefs by lazy { getSharedPreferences("app_settings", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 화면 그리기 전에 다크모드 먼저 적용해야 함
        applyDarkModeFromPrefs()

        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        // 탭 화면들은 뒤로가기 화살표 대신 햄버거 아이콘 표시
        val topLevelDestinations = setOf(
            R.id.homeFragment,
            R.id.searchFragment,
            R.id.wishlistFragment,
            R.id.myPageFragment
        )
        // DrawerLayout 연결해야 햄버거 클릭 시 드로어가 열림
        appBarConfiguration = AppBarConfiguration(topLevelDestinations, binding.drawerLayout)

        setupActionBarWithNavController(navController, appBarConfiguration)
        NavigationUI.setupWithNavController(binding.bottomNav, navController)

        setupDrawerHeader()
        setupDrawerMenu()
    }

    // 저장된 다크모드 설정 적용
    private fun applyDarkModeFromPrefs() {
        val isDark = getSharedPreferences("app_settings", MODE_PRIVATE)
            .getBoolean("dark_mode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    // 드로어 헤더에 닉네임, 이메일 표시
    private fun setupDrawerHeader() {
        val header = binding.navigationView.getHeaderView(0)
        header.findViewById<TextView>(R.id.tvHeaderNickname).text = SessionManager.getNickname(this)
        header.findViewById<TextView>(R.id.tvHeaderEmail).text = SessionManager.getEmail(this)
    }

    private fun setupDrawerMenu() {
        updateDarkModeTitle() // 현재 모드에 따라 메뉴 텍스트 초기화

        binding.navigationView.setNavigationItemSelectedListener { item ->
            binding.drawerLayout.closeDrawers()
            when (item.itemId) {

                R.id.nav_dark_mode -> {
                    val isDark = darkModePrefs.getBoolean("dark_mode", false)
                    darkModePrefs.edit().putBoolean("dark_mode", !isDark).apply()
                    AppCompatDelegate.setDefaultNightMode(
                        if (!isDark) AppCompatDelegate.MODE_NIGHT_YES
                        else AppCompatDelegate.MODE_NIGHT_NO
                    )
                    true
                }

                R.id.nav_notice -> {
                    showNoticeDialog()
                    true
                }

                R.id.nav_contact -> {
                    // 이메일 앱 열기
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:support@remarket.com")
                        putExtra(Intent.EXTRA_SUBJECT, "[ReMarket] 문의사항")
                    }
                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(intent)
                    } else {
                        // 이메일 앱 없을 때 안내
                        AlertDialog.Builder(this)
                            .setMessage("이메일 앱을 찾을 수 없습니다.\nsupport@remarket.com 으로 문의해 주세요.")
                            .setPositiveButton("확인", null)
                            .show()
                    }
                    true
                }

                R.id.nav_logout -> {
                    SessionManager.clearToken(this)
                    startActivity(Intent(this, IntroActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    true
                }

                else -> true
            }
        }
    }

    // 현재 다크모드 상태에 맞게 메뉴 텍스트 업데이트
    private fun updateDarkModeTitle() {
        val isDark = darkModePrefs.getBoolean("dark_mode", false)
        binding.navigationView.menu.findItem(R.id.nav_dark_mode)?.title =
            if (isDark) "다크 모드 끄기" else "다크 모드 켜기"
    }

    private fun showNoticeDialog() {
        val notices = arrayOf(
            "서비스 점검 안내 (2025.06.01)\n00:00 ~ 02:00 서버 점검이 예정되어 있습니다.",
            "ReMarket v1.0 출시\n중고거래 앱 리마켓이 정식 출시되었습니다!",
            "개인정보 처리방침 업데이트 (2025.05.15)\n내용을 확인해 주세요."
        )
        AlertDialog.Builder(this)
            .setTitle("공지사항")
            .setItems(notices.map { it.substringBefore("\n") }.toTypedArray()) { _, which ->
                AlertDialog.Builder(this)
                    .setTitle(notices[which].substringBefore("\n"))
                    .setMessage(notices[which].substringAfter("\n"))
                    .setPositiveButton("확인", null)
                    .show()
            }
            .setNegativeButton("닫기", null)
            .show()
    }

    // 툴바 햄버거/뒤로가기 아이콘 처리
    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
            || super.onSupportNavigateUp()
    }
}
