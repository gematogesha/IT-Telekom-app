package com.ittelekom.app.network

import com.ittelekom.app.models.AccountInfo
import com.ittelekom.app.models.Logout
import com.ittelekom.app.models.PayToDate
import com.ittelekom.app.models.Pays
import com.ittelekom.app.models.Services
import com.ittelekom.app.models.SetBlock
import com.ittelekom.app.models.SetMac
import com.ittelekom.app.models.SetTariffResponse
import com.ittelekom.app.models.Tariffs
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
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

    @GET("accounts/get_pays")
    suspend fun getPays(
        @Header("Authorization") token: String
    ): Response<Pays>

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("accounts/change_tariff")
    suspend fun setTariff(
        @Header("Authorization") token: String,
        @Field("new_tariff_id") tariffId: Int
    ): Response<SetTariffResponse>

    @FormUrlEncoded
    @POST("accounts/undo_change_tariff")
    suspend fun undoChangeTariff(
        @Header("Authorization") token: String
    ): Response<Unit>

    @FormUrlEncoded
    @POST("accounts/change_mac")
    suspend fun setMac(
        @Header("Authorization") token: String,
        @Field("mac") mac: String
    ): Response<SetMac>

    @FormUrlEncoded
    @POST("accounts/setblock")
    suspend fun setBlock(
        @Header("Authorization") token: String
    ): Response<SetBlock>

    @FormUrlEncoded
    @POST("remove_token")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<Logout>

    @FormUrlEncoded
    @POST("submit_btn.php")
    suspend fun submitData(
        @Field("pin") pin: String,
        @Field("fio") fio: String,
        @Field("pay_summ") paySumm: String,
        @Field("phone") phone: String,
        @Field("add_pay_yookassa_btn") addPayYooKassaBtn: String
    ): Response<ResponseBody>
}

