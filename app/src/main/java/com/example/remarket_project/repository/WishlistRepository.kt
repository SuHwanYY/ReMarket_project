package com.example.remarket_project.repository

import com.example.remarket_project.network.RetrofitClient

// 찜 관련 API 요청 모음
object WishlistRepository {

    private val api get() = RetrofitClient.apiService

    suspend fun getWishlist() = api.getWishlist()

    suspend fun addWishlist(productId: Int) = api.addWishlist(productId)

    suspend fun removeWishlist(productId: Int) = api.removeWishlist(productId)
}
