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

data class SetTariffResponse(
    val success: Boolean,
    val error: String,
    val message: String,
    val data: Any? // Уточните структуру ответа
)