package com.trazavoz.domain.usecase

import com.trazavoz.domain.model.Silabeador
import javax.inject.Inject

class SplitSyllablesUseCase @Inject constructor() {

    operator fun invoke(word: String): List<String> {
        val resultado = Silabeador.separar(word)
        if (resultado.isBlank()) return emptyList()
        return resultado.split("-").filter { it.isNotBlank() }
    }
}
