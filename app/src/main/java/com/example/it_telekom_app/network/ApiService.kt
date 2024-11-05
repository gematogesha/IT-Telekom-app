package com.example.it_telekom_app.network

import com.example.it_telekom_app.models.AccountInfo
import com.example.it_telekom_app.models.PayToDate
import com.example.it_telekom_app.models.Services
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<ResponseBody>

    @GET("accounts")
    suspend fun getAccountInfo(
        @Header("Authorization") token: String
    ): Response<AccountInfo>

    @GET("check_pay_to_date")
    suspend fun getPayToDate(
        @Header("Authorization") token: String
    ): Response<PayToDate>

    @GET("accounts/get_services")
    suspend fun getServices(
        @Header("Authorization") token: String
    ): Response<Services>

    @POST("remove_token")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<ResponseBody>
}

