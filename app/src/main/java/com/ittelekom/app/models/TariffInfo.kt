package com.ittelekom.app.models

data class TariffInfo(
    val id: Int,
    val caption: String,
    val speed: String,
    val abonplata: String
)

data class Tariffs(
    val tariffs: List<TariffInfo>
)