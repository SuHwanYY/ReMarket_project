package com.example.remarket_project.auth

import android.content.Context
import com.example.remarket_project.network.RetrofitClient

// 로그인 정보(토큰, 유저 ID, 닉네임, 이메일)를 SharedPreferences에 저장/불러오기
object SessionManager {

    private const val PREF_NAME = "remarket_prefs"
    private const val KEY_TOKEN    = "auth_token"
    private const val KEY_USER_ID  = "user_id"
    private const val KEY_NICKNAME = "nickname"
    private const val KEY_EMAIL    = "email"

    // 로그인 성공 시 토큰이랑 유저 정보 한번에 저장
    fun saveSession(context: Context, token: String, userId: Int, nickname: String, email: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_TOKEN, token)
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_NICKNAME, nickname)
            .putString(KEY_EMAIL, email)
            .apply()
        RetrofitClient.authToken = token
    }

    fun saveToken(context: Context, token: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_TOKEN, token)
            .apply()
        RetrofitClient.authToken = token
    }

    fun getToken(context: Context): String? =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TOKEN, null)

    fun getUserId(context: Context): Int =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_USER_ID, -1)

    fun getNickname(context: Context): String =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_NICKNAME, "") ?: ""

    fun getEmail(context: Context): String =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_EMAIL, "") ?: ""

    // 닉네임 수정했을 때 로컬 값도 업데이트
    fun saveNickname(context: Context, nickname: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_NICKNAME, nickname)
            .apply()
    }

    // 앱 시작 시 저장된 토큰을 불러와서 API 요청에 사용할 수 있게 설정
    fun loadToken(context: Context) {
        RetrofitClient.authToken = getToken(context)
    }

    // 로그아웃 시 저장된 정보 전부 삭제
    fun clearSession(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply()
        RetrofitClient.authToken = null
    }

    fun clearToken(context: Context) = clearSession(context)

    // 토큰이 있으면 로그인 상태로 판단
    fun isLoggedIn(context: Context): Boolean = getToken(context) != null
}
