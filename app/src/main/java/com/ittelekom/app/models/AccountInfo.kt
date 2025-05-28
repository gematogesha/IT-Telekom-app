package com.ittelekom.app.models

import java.util.Locale


interface MessageCarrier {
    val message: String?
    val error: String?
}

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
    var services: List<ServiceInfo> = emptyList(),
    var pays: List<PaysInfo> = emptyList(),
    override val message: String? = null,
    override val error: String? = null
) : MessageCarrier

data class PayToDate(
    val to_date: String? = null,
    override val message: String? = null,
    override val error: String? = null
) : MessageCarrier

data class Services(
    val services: List<ServiceInfo>,
    override val message: String? = null,
    override val error: String? = null
) : MessageCarrier

data class ServiceInfo(
    val svc_name: String,
    val svc_descr: String,
    val svc_price: String,
    val svc_num_dog: String,
    val svc_address: String,
)

data class Pays(
    val pays: List<PaysInfo>,
    override val message: String? = null,
    override val error: String? = null
) : MessageCarrier

data class PaysInfo(
    val paydate: String,
    val caption: String,
    val remark: String,
    val volume: String
)

data class SetBlock(
    override val message: String? = null,
    override val error: String? = null
) : MessageCarrier



fun groupPayments(pays: List<PaysInfo>): Map<String, Map<String, Double>> {
    return pays.groupBy { it.caption }.mapValues { (_, group) ->
        group.groupBy { remark ->
            processRemark(remark.remark)
        }.mapKeys { (key, _) ->
            capitalizeWords(key)
        }.mapValues { (_, subgroup) ->
            subgroup.sumOf { it.volume.toDouble() }
                .let { kotlin.math.abs(it) }
                .let { String.format(Locale.US, "%.2f", it).toDouble() }
        }
    }
}

// Функция обработки remark
fun processRemark(remark: String): String {
    val cleanedRemark = remark.replace(Regex("login:.*"), "").trim()
    val lowerRemark = cleanedRemark.lowercase()

    return when {
        "абон.плата" in lowerRemark -> "Абонентская плата"
        "внесение денег" in lowerRemark -> "Внесение денег"
        else -> extractKeyWords(cleanedRemark)
    }
}

// Извлечение ключевых слов из remark
fun extractKeyWords(remark: String): String {
    return remark.lowercase()
        .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}

// Функция для преобразования строки в Title Case
fun capitalizeWords(input: String): String {
    val exceptions = setOf("тв", "вк", "ср", "ок", "рф")

    return input.lowercase()
        .split(" ")
        .joinToString(" ") { word ->
            if (word in exceptions) {
                word.uppercase()
            } else {
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("ru", "RU")) else it.toString() }
            }
        }
}