package com.trazavoz.ui.syllables

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SyllableBankTest {

    @Test
    fun bancoConsonanteRegular() {
        assertEquals(listOf("MA", "ME", "MI", "MO", "MU"), generarBancoSilabas("M"))
        assertEquals(listOf("PA", "PE", "PI", "PO", "PU"), generarBancoSilabas("P"))
    }

    @Test
    fun bancoConsonantesIrregularesConcatenaSimple() {
        assertEquals(listOf("CA", "CE", "CI", "CO", "CU"), generarBancoSilabas("C"))
        assertEquals(listOf("GA", "GE", "GI", "GO", "GU"), generarBancoSilabas("G"))
    }

    @Test
    fun bancoNormalizaMinusculas() {
        assertEquals(listOf("SA", "SE", "SI", "SO", "SU"), generarBancoSilabas("s"))
    }

    @Test
    fun bancoManejaEnie() {
        assertEquals(listOf("ÑA", "ÑE", "ÑI", "ÑO", "ÑU"), generarBancoSilabas("Ñ"))
    }

    @Test
    fun bancoVacioSiEntradaInvalida() {
        assertTrue(generarBancoSilabas("").isEmpty())
        assertTrue(generarBancoSilabas("MN").isEmpty())
    }

    @Test
    fun esConsonanteDistingueVocales() {
        assertTrue(esConsonante("M"))
        assertTrue(esConsonante("Ñ"))
        assertFalse(esConsonante("A"))
        assertFalse(esConsonante("e"))
        assertFalse(esConsonante(""))
    }
}
