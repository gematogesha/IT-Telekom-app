package com.ittelekom.app.models

data class TariffInfo(
    val id: Int,
    val caption: String,
    val speed: String,
    val abonplata: String
)

data class Tariffs(
    val tariffs: List<TariffInfo>,
    override val message: String? = null,
    override val error: String? = null
) : MessageCarrier

data class SetTariffResponse(
    val success: Boolean,
    override val message: String? = null,
    override val error: String? = null
) : MessageCarrier