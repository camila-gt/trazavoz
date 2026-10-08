package com.trazavoz.data.remote.arasaac

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArasaacPictogramDto(
    @SerialName("_id") val id: Int,
    @SerialName("keywords") val keywords: List<ArasaacKeywordDto> = emptyList()
)

@Serializable
data class ArasaacKeywordDto(
    @SerialName("keyword") val keyword: String,
    @SerialName("type") val type: Int? = null
)
