package com.example.remarket_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket_project.network.dto.ProductItem
import com.example.remarket_project.repository.ProductRepository
import com.example.remarket_project.repository.WishlistRepository
import kotlinx.coroutines.launch

// 홈 화면 상품 목록 ViewModel
class HomeViewModel : ViewModel() {

    private val _products = MutableLiveData<List<ProductItem>>()
    val products: LiveData<List<ProductItem>> = _products

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // 처음 생성될 때 목록 바로 로드
    init { loadProducts() }

    fun loadProducts(keyword: String? = null, category: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = ProductRepository.getProducts(keyword, category)
                if (response.isSuccessful && response.body()?.success == true) {
                    _products.value = response.body()?.data ?: emptyList()
                } else {
                    _error.value = "상품을 불러올 수 없습니다."
                }
            } catch (e: Exception) {
                _error.value = "네트워크 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // "전체" 선택 시 필터 없이 전체 조회
    fun filterByCategory(category: String) {
        loadProducts(category = if (category == "전체") null else category)
    }

    // 찜 버튼: 서버 응답 기다리지 않고 UI 먼저 바꿈 (실패하면 다시 불러옴)
    fun toggleWish(productId: Int, isCurrentlyWished: Boolean) {
        viewModelScope.launch {
            try {
                if (isCurrentlyWished) WishlistRepository.removeWishlist(productId)
                else WishlistRepository.addWishlist(productId)

                // data class의 copy()로 해당 아이템만 수정
                _products.value = _products.value?.map { product ->
                    if (product.id == productId) product.copy(
                        isWished = !isCurrentlyWished,
                        wishCount = if (isCurrentlyWished) product.wishCount - 1 else product.wishCount + 1
                    ) else product
                }
            } catch (e: Exception) {
                _error.value = "찜 처리에 실패했습니다."
                loadProducts() // 실패 시 서버 원본 데이터로 롤백
            }
        }
    }

    fun clearError() { _error.value = null }
}
