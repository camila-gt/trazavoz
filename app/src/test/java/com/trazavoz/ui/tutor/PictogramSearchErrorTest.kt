package com.trazavoz.ui.tutor

import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

class PictogramSearchErrorTest {
    @Test fun `missing pictograms are not a network error`() {
        assertNull(pictogramSearchErrorMessage(httpError(404)))
    }

    @Test fun `server errors do not blame connectivity`() {
        assertEquals(
            "ARASAAC no pudo completar la búsqueda. Inténtalo de nuevo más tarde.",
            pictogramSearchErrorMessage(httpError(503))
        )
    }

    @Test fun `lost generic signature is an internal error`() {
        assertEquals(
            "Ocurrió un error interno al buscar pictogramas.",
            pictogramSearchErrorMessage(ClassCastException("Class cannot be cast to ParameterizedType"))
        )
    }

    @Test fun `malformed json is a response error`() {
        assertEquals(
            "No pudimos interpretar la respuesta de ARASAAC.",
            pictogramSearchErrorMessage(SerializationException("Invalid response"))
        )
    }

    @Test fun `timeout has a specific message`() {
        assertEquals(
            "ARASAAC tardó demasiado en responder. Inténtalo de nuevo.",
            pictogramSearchErrorMessage(SocketTimeoutException())
        )
    }

    @Test fun `tls errors are not presented as missing internet`() {
        assertEquals(
            "No se pudo establecer una conexión segura con ARASAAC.",
            pictogramSearchErrorMessage(SSLHandshakeException("Certificate rejected"))
        )
    }

    @Test fun `connection failures retain the network message`() {
        assertEquals(
            "Error de red. Verificá tu conexión.",
            pictogramSearchErrorMessage(UnknownHostException())
        )
    }

    private fun httpError(code: Int) = HttpException(Response.error<Unit>(code, "".toResponseBody()))
}
