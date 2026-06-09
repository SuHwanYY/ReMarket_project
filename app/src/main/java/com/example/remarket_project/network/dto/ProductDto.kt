package com.example.remarket_project.network.dto

// 판매자 정보
data class SellerInfo(
    val id: Int,
    val nickname: String,
    val region: String
)

// 상품 아이템 (목록/상세 모두 이 데이터 구조 사용)
data class ProductItem(
    val id: Int,
    val title: String,
    val description: String?,
    val price: Int,
    val category: String,
    val status: String,        // ON_SALE, RESERVED, SOLD_OUT
    val images: List<String>,  // 이미지 URL 목록
    val wishCount: Int,
    val isWished: Boolean,
    val seller: SellerInfo,
    val createdAt: String?
)

// 상품 목록 응답
data class ProductListResponse(
    val success: Boolean,
    val data: List<ProductItem>?
)

// 상품 단건 응답
data class ProductResponse(
    val success: Boolean,
    val message: String?,
    val data: ProductItem?
)

// 단순 성공/실패 응답 (삭제, 찜 등)
data class SimpleResponse(
    val success: Boolean,
    val message: String?
)

data class UserProfileResponse(
    val success: Boolean,
    val message: String?,
    val data: UserProfileData?
)

data class UserProfileData(
    val id: Int,
    val email: String,
    val nickname: String,
    val gender: String,
    val region: String
)
