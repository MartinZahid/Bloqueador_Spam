package com.ladablocker.util

import org.junit.Assert.assertEquals
import org.junit.Test

class NormalizerTest {

    @Test
    fun `nacional de 10 digitos se conserva`() {
        assertEquals("3312345678", Normalizer.normalizeToNational("3312345678"))
    }

    @Test
    fun `prefijo 044 de 13 digitos se elimina`() {
        assertEquals("3312345678", Normalizer.normalizeToNational("0443312345678"))
    }

    @Test
    fun `prefijo 045 de 13 digitos se elimina`() {
        assertEquals("3312345678", Normalizer.normalizeToNational("0453312345678"))
    }

    @Test
    fun `prefijo 521 de 13 digitos se elimina`() {
        assertEquals("3312345678", Normalizer.normalizeToNational("5213312345678"))
    }

    @Test
    fun `prefijo 52 de 12 digitos se elimina`() {
        assertEquals("3312345678", Normalizer.normalizeToNational("523312345678"))
    }

    @Test
    fun `prefijo 01 de 12 digitos se elimina`() {
        assertEquals("3312345678", Normalizer.normalizeToNational("013312345678"))
    }

    @Test
    fun `prefijo 1 de 11 digitos se elimina`() {
        assertEquals("3312345678", Normalizer.normalizeToNational("13312345678"))
    }

    @Test
    fun `numero movil con formato extranjero se normaliza`() {
        assertEquals("3312345678", Normalizer.normalizeToNational("+52 1 33 1234-5678"))
    }

    @Test
    fun `numero fijo con formato extranjero se normaliza`() {
        assertEquals("3312345678", Normalizer.normalizeToNational("+52 33 1234 5678"))
    }

    @Test
    fun `044 de 12 digitos malformado ya no produce prefijo`() {
        assertEquals("044331234567", Normalizer.normalizeToNational("044331234567"))
    }

    @Test
    fun `045 de 12 digitos malformado ya no produce prefijo`() {
        assertEquals("045331234567", Normalizer.normalizeToNational("045331234567"))
    }

    @Test
    fun `vacio se conserva`() {
        assertEquals("", Normalizer.normalizeToNational(""))
    }
}