package com.ladablocker.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ListsImporterTest {

    @Test
    fun `numero de 10 digitos es exacto`() {
        val r = ListsImporter.parse("3312345678\n")
        assertEquals(listOf(ValidNumber("3312345678", false)), r)
    }

    @Test
    fun `prefijo corto con asterisco es mask`() {
        val r = ListsImporter.parse("5532001*\n")
        assertEquals(listOf(ValidNumber("5532001", true)), r)
    }

    @Test
    fun `prefijo de 10 digitos con asterisco sigue siendo mask`() {
        val r = ListsImporter.parse("5547440145*\n")
        assertEquals(listOf(ValidNumber("5547440145", true)), r)
    }

    @Test
    fun `numero corto sin asterisco es mask`() {
        val r = ListsImporter.parse("5532001\n")
        assertEquals(listOf(ValidNumber("5532001", true)), r)
    }

    @Test
    fun `prefijo 044 malformado de 12 digitos se descarta`() {
        val r = ListsImporter.parse("044331234567\n")
        assertEquals(emptyList<ValidNumber>(), r)
    }

    @Test
    fun `prefijo 044 valido de 13 digitos normaliza a 10`() {
        val r = ListsImporter.parse("044331234567 8\n")
        assertEquals(listOf(ValidNumber("3312345678", false)), r)
    }

    @Test
    fun `comentarios y encabezados se ignoran`() {
        val raw = "# comentario\nBEGIN:VCARD\nVERSION:2.1\nFN:Juan\n3312345678\n"
        assertEquals(listOf(ValidNumber("3312345678", false)), ListsImporter.parse(raw))
    }

    @Test
    fun `lineas TEL de vCard se parsean`() {
        val r = ListsImporter.parse("TEL;HOME:+52 1 3312345678\n")
        assertEquals(listOf(ValidNumber("3312345678", false)), r)
    }

    @Test
    fun `duplicados se eliminan`() {
        val r = ListsImporter.parse("3312345678,3312345678\n")
        assertEquals(listOf(ValidNumber("3312345678", false)), r)
    }

    @Test
    fun `separadores csv funcionan`() {
        val r = ListsImporter.parse("3312345678;3320001111")
        assertEquals(
            listOf(ValidNumber("3312345678", false), ValidNumber("3320001111", false)),
            r
        )
    }
}