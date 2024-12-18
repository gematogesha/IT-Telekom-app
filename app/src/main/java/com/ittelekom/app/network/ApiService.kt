package com.ittelekom.app.network

import com.ittelekom.app.models.AccountInfo
import com.ittelekom.app.models.PayToDate
import com.ittelekom.app.models.Services
import com.ittelekom.app.models.Tariffs
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
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

    @GET("accounts/get_possible_tariff")
    suspend fun getTariffs(
        @Header("Authorization") token: String
    ): Response<Tariffs>

    @FormUrlEncoded
    @POST("accounts/change_tariff")
    suspend fun setTariff(
        @Header("Authorization") token: String,
        @Field("new_tariff_id") tariffId: Int
    ): Response<Unit>

    @POST("accounts/undo_change_tariff")
    suspend fun undoChangeTariff(
        @Header("Authorization") token: String
    ): Response<Unit>

    @POST("remove_token")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<ResponseBody>
}

