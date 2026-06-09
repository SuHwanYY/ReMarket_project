package com.example.remarket_project.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket_project.repository.ProductRepository
import kotlinx.coroutines.launch
import java.io.File

// 상품 등록/수정 화면 ViewModel
class AddEditProductViewModel : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun createProduct(
        title: String,
        description: String?,
        price: Int,
        category: String,
        imageFiles: List<File>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = ProductRepository.createProduct(title, description, price, category, imageFiles)
                if (response.isSuccessful && response.body()?.success == true) {
                    onSuccess()
                } else {
                    _error.value = "등록에 실패했습니다."
                }
            } catch (e: Exception) {
                _error.value = "네트워크 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun editProduct(
        id: Int,
        title: String?,
        description: String?,
        price: Int?,
        category: String?,
        imageFiles: List<File>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = ProductRepository.updateProduct(id, title, description, price, category, imageFiles)
                if (response.isSuccessful && response.body()?.success == true) {
                    onSuccess()
                } else {
                    _error.value = "수정에 실패했습니다."
                }
            } catch (e: Exception) {
                _error.value = "네트워크 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() { _error.value = null }
}
