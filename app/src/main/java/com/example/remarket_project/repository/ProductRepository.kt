package com.example.remarket_project.repository

import com.example.remarket_project.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

// 상품 관련 API 요청 모음
object ProductRepository {

    private val api get() = RetrofitClient.apiService

    // null로 넘기면 해당 조건 없이 전체 조회
    suspend fun getProducts(
        keyword: String? = null,
        category: String? = null,
        minPrice: Int? = null,
        maxPrice: Int? = null,
        region: String? = null
    ) = api.getProducts(keyword, category, minPrice, maxPrice, region)

    suspend fun getProductById(id: Int) = api.getProductById(id)

    suspend fun getMyProducts() = api.getMyProducts()

    // 텍스트 필드랑 이미지 파일을 같이 전송 (multipart)
    suspend fun createProduct(
        title: String,
        description: String?,
        price: Int,
        category: String,
        imageFiles: List<File>
    ) = api.createProduct(
        title.toRequestBody("text/plain".toMediaTypeOrNull()),
        description?.toRequestBody("text/plain".toMediaTypeOrNull()),
        price.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
        category.toRequestBody("text/plain".toMediaTypeOrNull()),
        imageFiles.map { file ->
            MultipartBody.Part.createFormData(
                "images", file.name,
                file.asRequestBody("image/*".toMediaTypeOrNull())
            )
        }
    )

    // 이미지 파일을 새로 보내면 교체, 안 보내면 기존 이미지 그대로 유지
    suspend fun updateProduct(
        id: Int,
        title: String?,
        description: String?,
        price: Int?,
        category: String?,
        imageFiles: List<File>
    ) = api.updateProduct(
        id,
        title?.toRequestBody("text/plain".toMediaTypeOrNull()),
        description?.toRequestBody("text/plain".toMediaTypeOrNull()),
        price?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull()),
        category?.toRequestBody("text/plain".toMediaTypeOrNull()),
        imageFiles.map { file ->
            MultipartBody.Part.createFormData(
                "images", file.name,
                file.asRequestBody("image/*".toMediaTypeOrNull())
            )
        }
    )

    suspend fun deleteProduct(id: Int) = api.deleteProduct(id)

    // 판매 상태 변경: ON_SALE, RESERVED, SOLD_OUT
    suspend fun updateStatus(id: Int, status: String) =
        api.updateStatus(id, mapOf("status" to status))
}
