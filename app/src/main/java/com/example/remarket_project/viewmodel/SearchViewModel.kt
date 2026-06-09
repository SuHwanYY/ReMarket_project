package com.example.remarket_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket_project.network.dto.ProductItem
import com.example.remarket_project.repository.ProductRepository
import com.example.remarket_project.repository.WishlistRepository
import kotlinx.coroutines.launch

// 검색 화면 ViewModel
class SearchViewModel : ViewModel() {

    private val _products = MutableLiveData<List<ProductItem>>()
    val products: LiveData<List<ProductItem>> = _products

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // 키워드/카테고리/가격/지역으로 검색 (null이면 해당 조건 무시)
    fun search(
        keyword: String?,
        category: String?,
        minPrice: Int?,
        maxPrice: Int?,
        region: String?
    ) {
        viewModelScope.launch {
            try {
                val response = ProductRepository.getProducts(keyword, category, minPrice, maxPrice, region)
                if (response.isSuccessful && response.body()?.success == true) {
                    _products.value = response.body()?.data ?: emptyList()
                } else {
                    _error.value = "검색에 실패했습니다."
                }
            } catch (e: Exception) {
                _error.value = "네트워크 오류가 발생했습니다."
            }
        }
    }

    // 찜 버튼: 서버 응답 기다리지 않고 바로 UI 반영
    fun toggleWish(productId: Int, isCurrentlyWished: Boolean) {
        viewModelScope.launch {
            try {
                if (isCurrentlyWished) WishlistRepository.removeWishlist(productId)
                else WishlistRepository.addWishlist(productId)

                _products.value = _products.value?.map { product ->
                    if (product.id == productId) product.copy(
                        isWished = !isCurrentlyWished,
                        wishCount = if (isCurrentlyWished) product.wishCount - 1 else product.wishCount + 1
                    ) else product
                }
            } catch (e: Exception) {
                _error.value = "찜 처리에 실패했습니다."
            }
        }
    }

    fun clearError() { _error.value = null }
}
