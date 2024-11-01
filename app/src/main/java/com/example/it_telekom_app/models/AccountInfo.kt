package com.example.it_telekom_app.models

data class AccountInfo(
    val name: String,
    val login: String,
    val balance: String,
    val userblock: Boolean,
    val num_dog: String,
    val tariff_caption: String,
    val hw_addr_for_dhcp: String,
    val sms_get: Boolean,
    val token_fsm: String
)