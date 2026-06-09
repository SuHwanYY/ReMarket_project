package com.example.remarket_project.ui.mypage

import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.remarket_project.auth.SessionManager
import com.example.remarket_project.databinding.ActivityEditProfileBinding
import com.example.remarket_project.repository.UserRepository
import kotlinx.coroutines.launch

// 프로필 수정 화면 (닉네임/지역)
class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    private val regions = listOf(
        "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종",
        "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남", "제주"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val regionAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, regions)
        binding.actvRegion.setAdapter(regionAdapter)

        // 현재 닉네임 미리 채워두기
        binding.etNickname.setText(SessionManager.getNickname(this))
        binding.actvRegion.setText("", false)

        binding.btnSave.setOnClickListener { save() }
    }

    private fun save() {
        val nickname = binding.etNickname.text?.toString()?.trim()
        val region   = binding.actvRegion.text?.toString()?.trim()

        // 아무것도 안 입력하면 수정 불가
        if (nickname.isNullOrEmpty() && region.isNullOrEmpty()) {
            Toast.makeText(this, "수정할 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSave.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = UserRepository.updateProfile(
                    nickname = nickname?.ifEmpty { null }, // 빈 문자열은 null 로 처리 (서버에서 무시)
                    region   = region?.ifEmpty { null }
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.nickname?.let {
                        SessionManager.saveNickname(this@EditProfileActivity, it)
                    }
                    Toast.makeText(this@EditProfileActivity, "프로필이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(
                        this@EditProfileActivity,
                        response.body()?.message ?: "수정에 실패했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditProfileActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnSave.isEnabled = true
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }
}
