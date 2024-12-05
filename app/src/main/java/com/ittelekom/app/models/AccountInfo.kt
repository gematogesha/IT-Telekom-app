package com.ittelekom.app.models

data class AccountInfo(
    val name: String,
    val login: String,
    val balance: String,
    val userblock: Boolean,
    val num_dog: String,
    val tariff_caption: String,
    val hw_addr_for_dhcp: String,
    val sms_get: Boolean,
    val token_fsm: String,
    var payToDate: PayToDate? = null,
    var services: List<ServiceInfo> = emptyList()
)

data class PayToDate(
    val to_date: String,
)

data class Services(
    val services: List<ServiceInfo>
)

data class ServiceInfo(
    val svc_name: String,
    val svc_descr: String,
    val svc_price: String,
    val svc_num_dog: String,
    val svc_address: String
)