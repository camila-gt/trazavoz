package com.trazavoz.data.remote.arasaac

import retrofit2.http.GET
import retrofit2.http.Path

interface ArasaacApiService {

    @GET("api/pictograms/es/search/{query}")
    suspend fun searchPictograms(
        @Path("query") query: String
    ): List<ArasaacPictogramDto>
}
