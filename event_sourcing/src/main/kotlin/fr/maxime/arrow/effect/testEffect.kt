package fr.maxime.arrow.effect

import arrow.core.continuations.effect
import arrow.core.continuations.ensureNotNull
import arrow.core.getOrHandle
import kotlinx.coroutines.runBlocking

fun abc(a: String): Int? {
    return if (a.length > 3) a.length
    else null
}

suspend fun main() {

    runBlocking {
        effect {
            val a = ensureNotNull(abc("abcd")) { "error not enough characters" }
            println(a)
        }.toEither()
    }.getOrHandle { s -> println(s) }

}
