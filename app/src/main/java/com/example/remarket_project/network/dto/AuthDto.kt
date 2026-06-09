package com.example.remarket_project.network.dto

// 회원가입 요청에 필요한 데이터
data class RegisterRequest(
    val email: String,
    val password: String,
    val nickname: String,
    val gender: String,   // "M" 또는 "W"
    val region: String
)

// 로그인 요청에 필요한 데이터
data class LoginRequest(
    val email: String,
    val password: String
)

// 회원가입 응답
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: UserData?
)

data class UserData(
    val id: Int,
    val email: String,
    val nickname: String
)

// 로그인 응답 (토큰이랑 유저 정보 포함)
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: LoginData?
)

data class LoginData(
    val token: String,
    val user: UserData
)
