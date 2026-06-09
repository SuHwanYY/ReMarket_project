package com.example.remarket_project.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Retrofit 설정 (API 통신에 사용)
// 에뮬레이터에서 로컬 서버에 접근할 때 10.0.2.2가 localhost를 가리킴
object RetrofitClient {

    const val BASE_URL = "http://10.0.2.2:3000/"

    // 로그인 토큰 (로그인 후에 자동으로 API 요청 헤더에 붙음)
    var authToken: String? = null

    // 모든 요청에 토큰을 자동으로 헤더에 추가해주는 인터셉터
    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder().apply {
            authToken?.let { header("Authorization", "Bearer $it") }
        }.build()
        chain.proceed(request)
    }

    // 요청/응답 내용을 Logcat에서 볼 수 있게 해주는 로깅 인터셉터
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    // lazy: 처음 사용할 때 한 번만 생성
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
