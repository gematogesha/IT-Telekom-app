package com.example.it_telekom_app.network

import com.example.it_telekom_app.models.AccountInfo
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

    @POST("remove_token")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<ResponseBody>
}