package com.example.remarket_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket_project.repository.ProductRepository
import com.example.remarket_project.repository.WishlistRepository
import kotlinx.coroutines.launch

// 마이페이지 통계(판매중/판매완료/찜 개수) ViewModel
class MyPageViewModel : ViewModel() {

    private val _onSaleCount = MutableLiveData(0)
    val onSaleCount: LiveData<Int> = _onSaleCount

    private val _soldOutCount = MutableLiveData(0)
    val soldOutCount: LiveData<Int> = _soldOutCount

    private val _wishCount = MutableLiveData(0)
    val wishCount: LiveData<Int> = _wishCount

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // 내 상품이랑 찜 목록 가져와서 개수 계산
    fun loadStats() {
        viewModelScope.launch {
            try {
                val productsResponse = ProductRepository.getMyProducts()
                if (productsResponse.isSuccessful) {
                    val products = productsResponse.body()?.data ?: emptyList()
                    _onSaleCount.value  = products.count { it.status == "ON_SALE" }
                    _soldOutCount.value = products.count { it.status == "SOLD_OUT" }
                }

                val wishlistResponse = WishlistRepository.getWishlist()
                if (wishlistResponse.isSuccessful) {
                    _wishCount.value = wishlistResponse.body()?.data?.size ?: 0
                }
            } catch (e: Exception) {
                _error.value = "데이터를 불러올 수 없습니다."
            }
        }
    }

    fun clearError() { _error.value = null }
}
