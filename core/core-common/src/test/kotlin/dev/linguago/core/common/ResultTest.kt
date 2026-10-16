package dev.linguago.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultTest {
    @Test
    fun success_contieneValor() {
        val result: Result<Int> = Result.success(21)

        assertEquals(21, result.getOrNull())
    }

    @Test
    fun error_propagaNullEnGetOrNull() {
        val result: Result<Int> = Result.error(LinguaError.Unknown)

        assertNull(result.getOrNull())
    }

    @Test
    fun map_transformaSoloElExito() {
        val mapped: Result<String> = Result.success(2).map { it * 10 }.map { "v=$it" }
        val mappedError: Result<String> = Result.error(LinguaError.Unknown).map { "nope" }

        assertEquals("v=20", mapped.getOrNull())
        assertNull(mappedError.getOrNull())
    }

    @Test
    fun fold_ejecuta_la_rama_correcta() {
        val fromSuccess =
            Result.success(3).fold(
                onSuccess = { valor ->
                    "ok:$valor"
                },
                onError = { "err" },
            )

        val fromError =
            Result.error(LinguaError.Unknown).fold(
                onSuccess = { "ok" },
                onError = { "err" },
            )

        assertEquals("ok:3", fromSuccess)
        assertTrue(fromError.startsWith("err"))
    }
}
