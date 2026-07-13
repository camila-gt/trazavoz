package com.trazavoz.data.remote.arasaac

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArasaacPictogramDto(
    @SerialName("_id") val id: Int,
    val keywords: List<ArasaacKeywordDto> = emptyList()
)

@Serializable
data class ArasaacKeywordDto(
    val keyword: String,
    val type: Int? = null
)
