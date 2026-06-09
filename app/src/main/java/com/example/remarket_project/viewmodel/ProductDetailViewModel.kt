package com.example.remarket_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket_project.network.dto.ProductItem
import com.example.remarket_project.repository.ProductRepository
import com.example.remarket_project.repository.WishlistRepository
import kotlinx.coroutines.launch

// 상품 상세 화면 ViewModel
class ProductDetailViewModel : ViewModel() {

    private val _product = MutableLiveData<ProductItem?>()
    val product: LiveData<ProductItem?> = _product

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _actionSuccess = MutableLiveData<String?>()
    val actionSuccess: LiveData<String?> = _actionSuccess

    // 삭제 성공하면 Fragment에 뒤로 가라고 신호 보냄
    private val _navigateBack = MutableLiveData(false)
    val navigateBack: LiveData<Boolean> = _navigateBack

    fun loadProduct(id: Int) {
        viewModelScope.launch {
            try {
                val response = ProductRepository.getProductById(id)
                if (response.isSuccessful && response.body()?.success == true) {
                    _product.value = response.body()?.data
                } else {
                    _error.value = "상품 정보를 불러올 수 없습니다."
                }
            } catch (e: Exception) {
                _error.value = "네트워크 오류가 발생했습니다."
            }
        }
    }

    // 찜 후 상세 정보 다시 불러와서 wishCount 최신 값 반영
    fun toggleWish(productId: Int, isCurrentlyWished: Boolean) {
        viewModelScope.launch {
            try {
                if (isCurrentlyWished) WishlistRepository.removeWishlist(productId)
                else WishlistRepository.addWishlist(productId)
                loadProduct(productId)
            } catch (e: Exception) {
                _error.value = "찜 처리에 실패했습니다."
            }
        }
    }

    fun updateStatus(productId: Int, status: String) {
        viewModelScope.launch {
            try {
                val response = ProductRepository.updateStatus(productId, status)
                if (response.isSuccessful) {
                    loadProduct(productId) // 변경 후 다시 로드
                    _actionSuccess.value = "상태가 변경되었습니다."
                } else {
                    _error.value = "상태 변경에 실패했습니다."
                }
            } catch (e: Exception) {
                _error.value = "상태 변경에 실패했습니다."
            }
        }
    }

    fun deleteProduct(productId: Int) {
        viewModelScope.launch {
            try {
                val response = ProductRepository.deleteProduct(productId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _navigateBack.value = true
                } else {
                    _error.value = "삭제에 실패했습니다."
                }
            } catch (e: Exception) {
                _error.value = "네트워크 오류가 발생했습니다."
            }
        }
    }

    // 새 상품 로드 전에 이전 데이터 지우기
    fun clearProduct() { _product.value = null }
    fun clearError() { _error.value = null }
    fun clearActionSuccess() { _actionSuccess.value = null }
    fun clearNavigateBack() { _navigateBack.value = false }
}
