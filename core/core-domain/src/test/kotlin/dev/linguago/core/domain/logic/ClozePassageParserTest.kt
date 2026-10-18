package dev.linguago.core.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class ClozePassageParserTest {
    @Test
    fun `separa texto y referencias por orden de aparicion`() {
        val segments = ClozePassageParser.parse("Antes {{primer}} en medio {{segundo}} después.")

        assertEquals(
            listOf(
                ClozeSegment.Text("Antes "),
                ClozeSegment.BlankReference("primer"),
                ClozeSegment.Text(" en medio "),
                ClozeSegment.BlankReference("segundo"),
                ClozeSegment.Text(" después."),
            ),
            segments,
        )
    }

    @Test
    fun `pasaje sin huecos es un unico segmento de texto`() {
        assertEquals(listOf(ClozeSegment.Text("sin huecos")), ClozePassageParser.parse("sin huecos"))
    }

    @Test
    fun `referencias adyacentes no generan runs vacios`() {
        assertEquals(
            listOf(ClozeSegment.BlankReference("a"), ClozeSegment.BlankReference("b")),
            ClozePassageParser.parse("{{a}}{{b}}"),
        )
    }

    @Test
    fun `pasaje que empieza o termina con hueco`() {
        assertEquals(
            listOf(
                ClozeSegment.BlankReference("a"),
                ClozeSegment.Text(" y "),
                ClozeSegment.BlankReference("b"),
            ),
            ClozePassageParser.parse("{{a}} y {{b}}"),
        )
    }

    @Test
    fun `pasaje vacio no devuelve segmentos`() {
        assertEquals(emptyList<ClozeSegment>(), ClozePassageParser.parse(""))
    }

    @Test
    fun `id de hueco preservado tal cual incluidos espacios`() {
        assertEquals(
            listOf(ClozeSegment.BlankReference(" con espacios ")),
            ClozePassageParser.parse("{{ con espacios }}"),
        )
    }
}
