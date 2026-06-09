package com.example.remarket_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket_project.network.dto.ProductItem
import com.example.remarket_project.repository.ProductRepository
import kotlinx.coroutines.launch

// 내 판매 목록 ViewModel
class MySalesViewModel : ViewModel() {

    private val _myProducts = MutableLiveData<List<ProductItem>>()
    val myProducts: LiveData<List<ProductItem>> = _myProducts

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadMyProducts() {
        viewModelScope.launch {
            try {
                val response = ProductRepository.getMyProducts()
                if (response.isSuccessful && response.body()?.success == true) {
                    _myProducts.value = response.body()?.data ?: emptyList()
                } else {
                    _error.value = "내 판매 목록을 불러올 수 없습니다."
                }
            } catch (e: Exception) {
                _error.value = "네트워크 오류가 발생했습니다."
            }
        }
    }

    // 삭제 성공하면 목록 새로고침 + 콜백 호출
    fun deleteProduct(productId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = ProductRepository.deleteProduct(productId)
                if (response.isSuccessful && response.body()?.success == true) {
                    loadMyProducts()
                    onSuccess()
                } else {
                    _error.value = "삭제에 실패했습니다."
                }
            } catch (e: Exception) {
                _error.value = "네트워크 오류가 발생했습니다."
            }
        }
    }

    fun updateStatus(productId: Int, status: String) {
        viewModelScope.launch {
            try {
                val response = ProductRepository.updateStatus(productId, status)
                if (response.isSuccessful) {
                    loadMyProducts()
                } else {
                    _error.value = "상태 변경에 실패했습니다."
                }
            } catch (e: Exception) {
                _error.value = "상태 변경에 실패했습니다."
            }
        }
    }

    fun clearError() { _error.value = null }
}
