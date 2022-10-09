package fr.maxime.adapters.csv_adapter.technicals

import arrow.core.continuations.Effect
import arrow.core.continuations.EffectScope
import arrow.core.continuations.effect
import arrow.core.getOrHandle
import fr.maxime.adapters.common.UserToCreate
import fr.maxime.adapters.common.divisionsUserHeader
import fr.maxime.adapters.common.emailUserHeader
import fr.maxime.adapters.common.firstNameUserHeader
import fr.maxime.adapters.common.lastNameUserHeader
import fr.maxime.adapters.common.passwordUserHeader
import fr.maxime.adapters.common.userHeaders
import fr.maxime.adapters.common.usernameUserHeader
import kotlinx.coroutines.runBlocking
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

fun readUsersCsvFileFromInputStream(
    inputStream: InputStream,
): Effect<List<String>, List<UserToCreate>> =
    effect {

//        val records = CSVFormat.Builder.create()
//            .setIgnoreHeaderCase(true)
//            .setSkipHeaderRecord(true)
//            .setTrim(true)
//            .setIgnoreEmptyLines(true)
//            .setIgnoreSurroundingSpaces(true)
//            .setHeader()
//            .build()
//            .parse(InputStreamReader(inputStream))

        val records: CSVParser = CSVFormat.Builder.create()
            .setIgnoreHeaderCase(true)
            .setSkipHeaderRecord(true)
            .setTrim(true)
            .setIgnoreEmptyLines(true)
            .setIgnoreSurroundingSpaces(true)
            .setHeader()
            .build()
            .parse(InputStreamReader(inputStream, StandardCharsets.UTF_8))

        checkMandatoryColumns(records)

        records.map { record ->
            UserToCreate(
                firstName = record[firstNameUserHeader],
                lastName = record[lastNameUserHeader],
                username = record[usernameUserHeader],
                password = record[passwordUserHeader],
                divisions = record[divisionsUserHeader]
                    .split(",")
                    .map { it.trim() }
                    .toSet(),
                email = if (records.headerNames.map { it.lowercase() }.contains(emailUserHeader)) record[emailUserHeader] else null,
                row = record.recordNumber.toInt()
            )
        }
    }

private suspend fun EffectScope<List<String>>.checkMandatoryColumns(records: CSVParser) {
    val csvHeaders = records.headerNames.map { it.lowercase() }
    var errors = userHeaders.mapNotNull { header ->
        if (!header.optional && !csvHeaders.contains(header.name))
            "the required column name: '${header.name}' is not present in Csv input file"
        else
            null
    }
    if (errors.isNotEmpty()) errors = errors.plus("headers input : ${records.headerNames}")
    ensure(errors.isEmpty()) { errors }
}

class T {}

fun main() {

    runBlocking {
        effect {
            val inputStream = T::class.java.classLoader.getResourceAsStream("csv_test_files/usersv2_ok.csv")
            if (inputStream != null) {
                val result = readUsersCsvFileFromInputStream(inputStream).bind()
                result.forEach { println(it) }
            } else {
                println("can not read inputStream")
            }
        }.toEither()
    }.getOrHandle { error -> error.forEach { println(it) } }

}
