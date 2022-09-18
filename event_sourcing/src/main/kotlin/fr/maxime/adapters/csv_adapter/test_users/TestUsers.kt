package fr.maxime.adapters.csv_adapter.test_users

import com.fasterxml.jackson.annotation.JsonProperty
import fr.maxime.adapters.csv_adapter.technicals.CsvReaderColumn
import fr.maxime.adapters.csv_adapter.technicals.CsvReaderColumnType
import fr.maxime.adapters.csv_adapter.technicals.readCsvFileFromInputStream

private val csvUserCreationColumns = listOf(
    CsvReaderColumn("Prénom"),
    CsvReaderColumn("Nom"),
    CsvReaderColumn("Email", optional = true),
    CsvReaderColumn("Identifiant"),
    CsvReaderColumn("Mot de passe"),
    CsvReaderColumn("Classes", columnType = CsvReaderColumnType.TextIterable),
    CsvReaderColumn("Friends", optional = true, columnType = CsvReaderColumnType.TextIterable),
)

private data class CsvUserCreationDto(
    @JsonProperty("Prénom") val firstName: String,
    @JsonProperty("Nom") val lastName: String,
    @JsonProperty("Email") val email: String?,
    @JsonProperty("Identifiant") val username: String,
    @JsonProperty("Mot de passe") val password: String,
    @JsonProperty("Classes") val divisions: Set<String>,
    @JsonProperty("Friends") val friends: Set<String>?,
)

fun testCsvReader() {

    class Test() {}

    val csvFileOK = "csv_test_files/users_column_ok.csv"
    val csvFileColumnEmptyCells = "csv_test_files/users_column_empty_cells.csv"
    val csvFileColumnArrayProblem = "csv_test_files/users_column_with_array_problem.csv"
    val csvFileColumnMissing = "csv_test_files/users_column_missing.csv"

    val listInputStream = listOf(
        Test::class.java.classLoader.getResourceAsStream(csvFileOK),
        Test::class.java.classLoader.getResourceAsStream(csvFileColumnEmptyCells),
        Test::class.java.classLoader.getResourceAsStream(csvFileColumnArrayProblem),
        Test::class.java.classLoader.getResourceAsStream(csvFileColumnMissing),
    )

    listInputStream.forEachIndexed { index, inputStream ->
        if (inputStream != null) {
            val result = readCsvFileFromInputStream<CsvUserCreationDto>(
                inputStream = inputStream,
                csvObjectColumns = csvUserCreationColumns,
            )

            println("\nTEST(${index + 1})\n")
            println("\tResults: (${result.results.size})\n")
            result.results.forEach { println(it) }
            println(
                "\n\t------------" +
                        "\n\tErrors: (${result.errors.size})"
            )
            result.errors.forEach { println(it) }
        } else {
            println("ERROR InputStream is null")
        }
    }

}

fun main() {
    testCsvReader()
}