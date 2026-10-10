package com.trazavoz.domain.usecase

import com.trazavoz.data.remote.arasaac.ArasaacApiService
import com.trazavoz.data.remote.arasaac.ArasaacKeywordDto
import com.trazavoz.data.remote.arasaac.ArasaacPictogramDto
import com.trazavoz.di.NetworkModule
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Assert.*
import org.junit.Test

class SearchArasaacUseCaseTest {
    @Test fun `retrofit can parse suspend service before making any request`() {
        val retrofit = NetworkModule.provideRetrofit(OkHttpClient(), NetworkModule.provideJson())
            .newBuilder().validateEagerly(true).build()
        assertNotNull(retrofit.create(ArasaacApiService::class.java))
    }

    @Test fun `query is trimmed and results mapped`() = runTest {
        val api = FakeSearchApi()
        val result = SearchArasaacUseCase(api)(" MESA ").getOrThrow()
        assertEquals("mesa", api.query)
        assertEquals(listOf(SearchResult(3129, "MESA", "https://api.arasaac.org/api/pictograms/3129")), result)
    }

    @Test fun `blank queries do not call the api`() = runTest {
        val api = FakeSearchApi()
        assertTrue(SearchArasaacUseCase(api)("  ").getOrThrow().isEmpty())
        assertNull(api.query)
    }

    @Test fun `internal exceptions remain available for classification`() = runTest {
        val failure = ClassCastException("Missing generic signature")
        val api = FakeSearchApi().apply { error = failure }
        assertSame(failure, SearchArasaacUseCase(api)("mesa").exceptionOrNull())
    }

    @Test fun `cancellation is not converted into a search error`() = runTest {
        val cancellation = CancellationException("Cancelled")
        val api = FakeSearchApi().apply { error = cancellation }
        try {
            SearchArasaacUseCase(api)("mesa")
            fail("Cancellation must propagate")
        } catch (e: CancellationException) {
            assertSame(cancellation, e)
        }
    }
}

private class FakeSearchApi : ArasaacApiService {
    var query: String? = null
    var error: Exception? = null
    override suspend fun searchPictograms(query: String): List<ArasaacPictogramDto> {
        this.query = query
        error?.let { throw it }
        return listOf(ArasaacPictogramDto(3129, listOf(ArasaacKeywordDto("mesa"))))
    }
}
