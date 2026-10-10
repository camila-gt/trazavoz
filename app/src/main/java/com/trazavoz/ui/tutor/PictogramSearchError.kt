package com.trazavoz.ui.tutor

import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLException

// A null message means the search completed without matching pictograms (HTTP 404).
internal fun pictogramSearchErrorMessage(error: Throwable): String? = when (error) {
    is HttpException -> if (error.code() == 404) null
        else "ARASAAC no pudo completar la búsqueda. Inténtalo de nuevo más tarde."
    is SerializationException -> "No pudimos interpretar la respuesta de ARASAAC."
    is SocketTimeoutException -> "ARASAAC tardó demasiado en responder. Inténtalo de nuevo."
    is SSLException -> "No se pudo establecer una conexión segura con ARASAAC."
    is IOException -> "Error de red. Verificá tu conexión."
    else -> "Ocurrió un error interno al buscar pictogramas."
}
