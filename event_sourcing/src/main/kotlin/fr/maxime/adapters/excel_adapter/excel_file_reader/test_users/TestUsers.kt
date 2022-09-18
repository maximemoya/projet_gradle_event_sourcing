package fr.maxime.adapters.excel_adapter.excel_file_reader.test_users

import com.fasterxml.jackson.annotation.JsonProperty
import fr.maxime.adapters.excel_adapter.excel_file_reader.technicals.ExcelReaderColumn
import fr.maxime.adapters.excel_adapter.excel_file_reader.technicals.ExcelReaderColumnType
import fr.maxime.adapters.excel_adapter.excel_file_reader.technicals.readExcelFileFromXlsxInputStream
import java.io.InputStream

private val excelUserCreationColumns = listOf(
    ExcelReaderColumn("prénom"),
    ExcelReaderColumn("nom"),
    ExcelReaderColumn("Email", optional = true),
    ExcelReaderColumn("identifiant"),
    ExcelReaderColumn("mot de passe"),
    ExcelReaderColumn("classes", columnType = ExcelReaderColumnType.TextIterable),
)

private data class ExcelUserCreationDto(
    @JsonProperty("prénom")val firstName: String,
    @JsonProperty("nom")val lastName: String,
    @JsonProperty("Email")val email: String?,
    @JsonProperty("identifiant")val username: String,
    @JsonProperty("mot de passe")val password: String,
    @JsonProperty("classes")val divisions: Set<String>,
)

private fun testExcelReader(inputStreamUsers: List<InputStream?>) {
    var i = 0
    inputStreamUsers.forEach { inputStream ->
        i++
        println("\n================")
        println("\t=> TEST '$i':\n")

        if (inputStream != null) {

            // EXCEL READER:
            val result = readExcelFileFromXlsxInputStream<ExcelUserCreationDto>(
                excelFileXlsxInputStream = inputStream,
                excelObjectColumns = excelUserCreationColumns,
            )

            // LOG:
            println("DATA READ: (${result.results.size})")
            result.results.forEach { println(it) }
            if (result.results.isEmpty()) println("\t\t-can not read this excel sheet-")

            println()

            println("ERRORS:")
            if (result.errors.size > 1) {
                result.errors.forEach { println("\t$it") }
            }
            else println("\t--- no error ---")

        } else {
            println("ERROR InputStream is null")
        }
    }
}

fun testUsers() {
    val file1 = "excel_test_files/users_everything_ok.xlsx"
    val file2 = "excel_test_files/users_columns_shuffled.xlsx"
    val file3 = "excel_test_files/users_with_lowcase_email.xlsx"
    val file4 = "excel_test_files/users_wrong_identifiant.xlsx"
    val file5 = "excel_test_files/users_with_english_columns_name.xlsx"
    val file6 = "excel_test_files/users_with_english_columns_and_missing_text.xlsx"
    val file7 = "excel_test_files/users_with_half_ok_half_wrong_by_missing_cell.xlsx"

    class Test() {}
    val usersInputStream = listOf(
        Test::class.java.classLoader.getResourceAsStream(file1),
        Test::class.java.classLoader.getResourceAsStream(file2),
        Test::class.java.classLoader.getResourceAsStream(file3),
        Test::class.java.classLoader.getResourceAsStream(file4),
        Test::class.java.classLoader.getResourceAsStream(file5),
        Test::class.java.classLoader.getResourceAsStream(file6),
        Test::class.java.classLoader.getResourceAsStream(file7),
    )

    testExcelReader(usersInputStream)
}

fun main() {
    testUsers()
}
