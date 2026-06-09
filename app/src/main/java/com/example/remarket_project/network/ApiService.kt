package com.example.remarket_project.network

import com.example.remarket_project.network.dto.AuthResponse
import com.example.remarket_project.network.dto.LoginRequest
import com.example.remarket_project.network.dto.LoginResponse
import com.example.remarket_project.network.dto.ProductItem
import com.example.remarket_project.network.dto.ProductListResponse
import com.example.remarket_project.network.dto.ProductResponse
import com.example.remarket_project.network.dto.RegisterRequest
import com.example.remarket_project.network.dto.SimpleResponse
import com.example.remarket_project.network.dto.UserProfileData
import com.example.remarket_project.network.dto.UserProfileResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

// 서버 API 목록을 인터페이스로 정리
// Retrofit이 이걸 보고 실제 네트워크 요청 코드를 자동 생성해줌
interface ApiService {

    // 인증 관련 API

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // 내 정보 조회 (토큰 필요)
    @GET("auth/me")
    suspend fun getMe(): Response<UserProfileResponse>

    // 상품 관련 API

    // 상품 목록 - 검색 조건들은 null이면 서버에서 무시
    @GET("products")
    suspend fun getProducts(
        @Query("keyword")  keyword:  String? = null,
        @Query("category") category: String? = null,
        @Query("minPrice") minPrice: Int?    = null,
        @Query("maxPrice") maxPrice: Int?    = null,
        @Query("region")   region:   String? = null
    ): Response<ProductListResponse>

    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: Int): Response<ProductResponse>

    // 이미지 파일이 있어서 Multipart로 전송
    @Multipart
    @POST("products")
    suspend fun createProduct(
        @Part("title")       title:       RequestBody,
        @Part("description") description: RequestBody?,
        @Part("price")       price:       RequestBody,
        @Part("category")    category:    RequestBody,
        @Part images: List<MultipartBody.Part>
    ): Response<ProductResponse>

    @Multipart
    @PUT("products/{id}")
    suspend fun updateProduct(
        @Path("id") id:          Int,
        @Part("title")       title:       RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("price")       price:       RequestBody?,
        @Part("category")    category:    RequestBody?,
        @Part images: List<MultipartBody.Part>
    ): Response<ProductResponse>

    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): Response<SimpleResponse>

    // 판매 상태 변경 (ON_SALE / RESERVED / SOLD_OUT)
    @PATCH("products/{id}/status")
    suspend fun updateStatus(
        @Path("id") id: Int,
        @Body body: Map<String, String>
    ): Response<ProductResponse>

    // 찜 관련 API

    @GET("wishlist")
    suspend fun getWishlist(): Response<ProductListResponse>

    @POST("wishlist/{productId}")
    suspend fun addWishlist(@Path("productId") productId: Int): Response<SimpleResponse>

    @DELETE("wishlist/{productId}")
    suspend fun removeWishlist(@Path("productId") productId: Int): Response<SimpleResponse>

    // 유저 관련 API

    @PUT("user/profile")
    suspend fun updateProfile(@Body body: Map<String, String>): Response<UserProfileResponse>

    // 내가 등록한 상품 목록
    @GET("user/products")
    suspend fun getMyProducts(): Response<ProductListResponse>
}
