package com.example.remarket_project.model

// 처음 설계할 때 만든 모델 클래스 (지금은 ProductItem을 주로 씀)
data class Product(
    val id: Int,
    val title: String,
    val price: Int,
    val category: String,
    val status: ProductStatus,
    val wishCount: Int,
    val sellerName: String,
    val isWished: Boolean = false
)

// 판매 상태 enum
enum class ProductStatus(val label: String) {
    ON_SALE("판매중"),
    RESERVED("예약중"),
    SOLD_OUT("판매완료")
}
