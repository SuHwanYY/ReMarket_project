package com.example.remarket_project.repository

import com.example.remarket_project.network.RetrofitClient
import com.example.remarket_project.network.dto.LoginRequest
import com.example.remarket_project.network.dto.RegisterRequest

// 로그인/회원가입/프로필 관련 API 요청 모음
object UserRepository {

    private val api get() = RetrofitClient.apiService

    suspend fun login(request: LoginRequest) = api.login(request)

    suspend fun register(request: RegisterRequest) = api.register(request)

    suspend fun getMe() = api.getMe()

    // 변경할 항목만 Map에 담아서 보냄 (null이면 서버에서 기존 값 유지)
    suspend fun updateProfile(nickname: String?, region: String?) =
        api.updateProfile(buildMap {
            nickname?.let { put("nickname", it) }
            region?.let { put("region", it) }
        })
}
