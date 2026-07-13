package com.trazavoz.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SilabeadorTest {

    @Test
    fun testMonosilabas() {
        assertEquals("SOL", Silabeador.separar("SOL"))
        assertEquals("PAN", Silabeador.separar("PAN"))
        assertEquals("FLOR", Silabeador.separar("FLOR"))
        assertEquals("REY", Silabeador.separar("REY"))
    }

    @Test
    fun testPalabrasSimples() {
        assertEquals("ME-SA", Silabeador.separar("MESA"))
        assertEquals("CA-JA", Silabeador.separar("CAJA"))
        assertEquals("PE-LO-TA", Silabeador.separar("PELOTA"))
        assertEquals("PLÁ-TA-NO", Silabeador.separar("PLÁTANO"))
    }

    @Test
    fun testDiptongos() {
        assertEquals("CAU-SA", Silabeador.separar("CAUSA"))
        assertEquals("RUI-DO", Silabeador.separar("RUIDO"))
        assertEquals("PIA-NO", Silabeador.separar("PIANO"))
        assertEquals("CIU-DAD", Silabeador.separar("CIUDAD"))
        assertEquals("VIEN-TO", Silabeador.separar("VIENTO"))
        assertEquals("CIE-LO", Silabeador.separar("CIELO"))
    }

    @Test
    fun testHiatos() {
        assertEquals("TE-A-TRO", Silabeador.separar("TEATRO"))
        assertEquals("PO-E-MA", Silabeador.separar("POEMA"))
        assertEquals("DÍ-A", Silabeador.separar("DÍA"))
        assertEquals("MA-ÍZ", Silabeador.separar("MAÍZ"))
        assertEquals("CO-O-PE-RAR", Silabeador.separar("COOPERAR"))
        assertEquals("CHI-Í-TA", Silabeador.separar("CHIÍTA"))
    }

    @Test
    fun testTriptongos() {
        assertEquals("MIAU", Silabeador.separar("Miau"))
        assertEquals("U-RU-GUAY", Silabeador.separar("Uruguay"))
    }

    @Test
    fun testHacheMuda() {
        assertEquals("CO-HE-TE", Silabeador.separar("COHETE"))
        assertEquals("PROHI-BIR", Silabeador.separar("PROHIBIR"))
        assertEquals("BÚ-HO", Silabeador.separar("BÚHO"))
        assertEquals("A-HU-MAR", Silabeador.separar("AHUMAR"))
    }

    @Test
    fun testGruposConsonanticosInseparables() {
        assertEquals("HA-BLAR", Silabeador.separar("HABLAR"))
        assertEquals("COM-PRAR", Silabeador.separar("COMPRAR"))
        assertEquals("CONS-TRUIR", Silabeador.separar("CONSTRUIR"))
        assertEquals("A-TLAS", Silabeador.separar("ATLAS"))
    }

    @Test
    fun testDigrafos() {
        assertEquals("PE-RRO", Silabeador.separar("PERRO"))
        assertEquals("LLU-VIA", Silabeador.separar("LLUVIA"))
        assertEquals("O-CHO", Silabeador.separar("OCHO"))
    }

    @Test
    fun testPalabrasLargas() {
        assertEquals("TRANS-CRI-BIR", Silabeador.separar("TRANSCRIBIR"))
        assertEquals("CONS-TI-TU-CIÓN", Silabeador.separar("CONSTITUCIÓN"))
    }
}
