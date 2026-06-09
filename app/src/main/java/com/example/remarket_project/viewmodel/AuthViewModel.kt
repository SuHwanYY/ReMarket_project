package com.example.remarket_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket_project.network.dto.LoginData
import com.example.remarket_project.network.dto.LoginRequest
import com.example.remarket_project.network.dto.RegisterRequest
import com.example.remarket_project.repository.UserRepository
import kotlinx.coroutines.launch

// 로그인, 회원가입 화면에서 사용하는 ViewModel
class AuthViewModel : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // 로그인 성공 시 토큰+유저 정보 전달
    private val _loginSuccess = MutableLiveData<LoginData?>()
    val loginSuccess: LiveData<LoginData?> = _loginSuccess

    // 회원가입 성공 여부
    private val _registerSuccess = MutableLiveData(false)
    val registerSuccess: LiveData<Boolean> = _registerSuccess

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = UserRepository.login(LoginRequest(email, password))
                if (response.isSuccessful && response.body()?.success == true) {
                    _loginSuccess.value = response.body()?.data
                } else {
                    _error.value = response.body()?.message ?: "이메일 또는 비밀번호를 확인해주세요"
                }
            } catch (e: Exception) {
                _error.value = "서버에 연결할 수 없습니다. 네트워크를 확인해주세요."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(email: String, password: String, nickname: String, gender: String, region: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = UserRepository.register(RegisterRequest(email, password, nickname, gender, region))
                if (response.isSuccessful && response.body()?.success == true) {
                    _registerSuccess.value = true
                } else {
                    _error.value = response.body()?.message ?: "회원가입에 실패했습니다"
                }
            } catch (e: Exception) {
                _error.value = "서버에 연결할 수 없습니다. 네트워크를 확인해주세요."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() { _error.value = null }
}
