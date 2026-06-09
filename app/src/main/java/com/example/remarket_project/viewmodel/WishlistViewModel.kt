package com.example.remarket_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket_project.network.dto.ProductItem
import com.example.remarket_project.repository.WishlistRepository
import kotlinx.coroutines.launch

// 찜 목록 화면 ViewModel
class WishlistViewModel : ViewModel() {

    private val _wishlist = MutableLiveData<List<ProductItem>>()
    val wishlist: LiveData<List<ProductItem>> = _wishlist

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // 401 응답 시 true → Fragment에서 로그인 화면으로 이동
    private val _sessionExpired = MutableLiveData(false)
    val sessionExpired: LiveData<Boolean> = _sessionExpired

    fun loadWishlist() {
        viewModelScope.launch {
            try {
                val response = WishlistRepository.getWishlist()
                when {
                    response.isSuccessful && response.body()?.success == true -> {
                        _wishlist.value = response.body()?.data ?: emptyList()
                    }
                    response.code() == 401 -> {
                        _sessionExpired.value = true
                    }
                    else -> {
                        _error.value = "찜 목록을 불러올 수 없습니다."
                    }
                }
            } catch (e: Exception) {
                _error.value = "네트워크 오류가 발생했습니다."
            }
        }
    }

    // 찜 해제 후 목록 새로고침
    fun removeWishlist(productId: Int) {
        viewModelScope.launch {
            try {
                WishlistRepository.removeWishlist(productId)
                loadWishlist()
            } catch (e: Exception) {
                _error.value = "찜 제거에 실패했습니다."
            }
        }
    }

    fun clearError() { _error.value = null }
    fun clearSessionExpired() { _sessionExpired.value = false }
}
