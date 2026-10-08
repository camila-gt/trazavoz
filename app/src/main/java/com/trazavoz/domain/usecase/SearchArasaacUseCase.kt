package com.trazavoz.domain.usecase

import com.trazavoz.data.remote.arasaac.ArasaacApiService
import javax.inject.Inject

data class SearchResult(val id: Int, val name: String, val imageUrl: String)

class SearchArasaacUseCase @Inject constructor(
    private val apiService: ArasaacApiService
) {
    suspend operator fun invoke(query: String): List<SearchResult> {
        val cleanQuery = query.trim().lowercase()
        if (cleanQuery.isBlank()) return emptyList()

        return try {
            val dtos = apiService.searchPictograms(cleanQuery)
            dtos.map { dto ->
                val primaryKeyword = dto.keywords.firstOrNull()?.keyword ?: ""
                SearchResult(
                    id = dto.id,
                    name = primaryKeyword.uppercase(),
                    imageUrl = "https://api.arasaac.org/api/pictograms/${dto.id}"
                )
            }
        } catch (e: Exception) {
            // Retrofit will throw HttpException with code 404 if no results are found
            emptyList()
        }
    }
}
