package fr.maxime.adapters.csv_adapter.technicals

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.InputStream
import java.io.InputStreamReader
import java.util.UUID

enum class CsvReaderColumnType {
    Text, TextIterable
}

class CsvReaderColumn(
    val name: String,
    val optional: Boolean = false,
    val columnType: CsvReaderColumnType = CsvReaderColumnType.Text,
)

data class CsvReaderResult<out T>(
    val results: List<T> = listOf(),
    val errors: List<String> = listOf()
)

inline fun <reified T> readCsvFileFromInputStream(
    inputStream: InputStream,
    csvObjectColumns: List<CsvReaderColumn>
): CsvReaderResult<T> {

    val delimiterColumn = ";"
    val delimiterArray = ","
    val columnsObjectNames = csvObjectColumns.map { it.name }
    val columnsObjectNamesRequired = csvObjectColumns.filter { !it.optional }.map { it.name }
    val columnsObjectNamesOptional = csvObjectColumns.filter { it.optional }.map { it.name }
    var columnsNamesInFile: List<String>
    val columnsMapInFile = mutableMapOf<Int, String>()
    val objMapList = mutableListOf<Map<String, Any?>>()
    val errorLogs = mutableListOf<String>()

    val rows = InputStreamReader(inputStream).readLines()
    rows.forEachIndexed { indexRow, row ->
        if (indexRow == 0) {

            // init columnNames:
            columnsNamesInFile = row.split(delimiterColumn).map { it.trim() }
            columnsNamesInFile.forEachIndexed { i, name ->
                columnsMapInFile[i] = name
            }

            // check columnNames errors:
            columnsObjectNamesRequired.forEach { columnNameRequired ->
                if (!columnsNamesInFile.contains(columnNameRequired)) {
                    errorLogs.add("\n!Error! the required column name: '$columnNameRequired' is not present in Csv input file")
                }
            }
            columnsObjectNamesOptional.forEach { columnNameOptional ->
                if (!columnsNamesInFile.contains(columnNameOptional)) {
                    errorLogs.add("\n/Warning\\ the optional column name: '$columnNameOptional' is not present in Csv input file")
                }
            }
            if (errorLogs.size > 0) {
                errorLogs.add("\n=> the list of required columns is: $columnsObjectNamesRequired")
                errorLogs.add("\n=> the list of optional columns is: $columnsObjectNamesOptional")
            }

        } else {

            val objMap = mutableMapOf<String, Any?>()
            val errorsBuffer = mutableListOf<String>()

            val values = row.split(delimiterColumn)
            values.forEachIndexed { indexCell, value ->

                val columnName = columnsMapInFile[indexCell]?.trim()
                var columnValue: Any? = value.trim()

                if (columnName != null && columnsObjectNames.contains(columnName)) {

                    csvObjectColumns.forEach { csvColumn ->

                        if (csvColumn.name == columnName && value.isBlank()) {
                            columnValue = null
                            if (!csvColumn.optional) {
                                errorsBuffer.add("\n!Error! at row: (${indexRow + 1}) in column: '$columnName' => find empty cell for required parameter")
                            }
                        } else if (csvColumn.name == columnName && csvColumn.columnType == CsvReaderColumnType.TextIterable) {
                            columnValue = (columnValue as String).split(delimiterArray).map { it.trim() }
                        }
                    }
                    objMap[columnName] = columnValue
                }
            }
            objMapList.add(objMap)
            errorLogs.addAll(errorsBuffer)
        }
    }

    val mapper = jacksonObjectMapper()
    val jsons = objMapList.map { user -> mapper.writeValueAsString(user) }
    val userObjects: List<T> = jsons.mapNotNull { json ->
        try {
            mapper.readValue<T>(json)
        } catch (e: Exception) {
            errorLogs.add("\ncan not deserialize ${T::class.java.simpleName} from : $json")
            null
        }
    }

    return (CsvReaderResult(userObjects, errorLogs))
}

/**
 * Comma Separated Values
 * improved with array
 * '[]' characters are reserved
 * use these characters to put CSV inside as an array of values separated by comma
 * example :
 * HEADER : Column1,Column2,Column3
 * VALUES : valeur1,[pierre,paul,jacques],valeur2
 */
inline fun <reified T> readCsvMaximArrayFileFromInputStream(
    inputStream: InputStream,
    csvObjectColumns: List<CsvReaderColumn>
): CsvReaderResult<T> {

    val columnsObjectNames = csvObjectColumns.map { it.name }
    val columnsObjectNamesRequired = csvObjectColumns.filter { !it.optional }.map { it.name }
    val columnsObjectNamesOptional = csvObjectColumns.filter { it.optional }.map { it.name }
    var columnsNamesInFile: List<String>
    val columnsMapInFile = mutableMapOf<Int, String>()
    val objMapList = mutableListOf<Map<String, Any?>>()
    val errorLogs = mutableListOf<String>()

    val mapOfArrays = mutableMapOf<String, String>()

    val rows = InputStreamReader(inputStream).readLines()
    rows.forEachIndexed { indexRow, row ->
        if (indexRow == 0) {

            // init columnNames:
            columnsNamesInFile = row.split(",").map { it.trim() }
            columnsNamesInFile.forEachIndexed { i, name ->
                columnsMapInFile[i] = name
            }

            // check columnNames errors:
            columnsObjectNamesRequired.forEach { columnNameRequired ->
                if (!columnsNamesInFile.contains(columnNameRequired)) {
                    errorLogs.add("\n!Error! the required column name: '$columnNameRequired' is not present in Csv input file")
                }
            }
            columnsObjectNamesOptional.forEach { columnNameOptional ->
                if (!columnsNamesInFile.contains(columnNameOptional)) {
                    errorLogs.add("\n/Warning\\ the optional column name: '$columnNameOptional' is not present in Csv input file")
                }
            }
            if (errorLogs.size > 0) {
                errorLogs.add("\n=> the list of required columns is: $columnsObjectNamesRequired")
                errorLogs.add("\n=> the list of optional columns is: $columnsObjectNamesOptional")
            }

        } else {

            // ARRAY SYSTEM:
            var newRow = row
            while (true) {
                if (newRow.contains("[").and(newRow.contains("]"))) {
                    val firstSplit = newRow.split("[", limit = 2)
                    val secondSplit = newRow.split("]", limit = 2)

                    val array = newRow.substring(
                        firstSplit[0].length,
                        secondSplit[0].length + 1,
                    )

                    val uuid = UUID.randomUUID().toString()
                    newRow = newRow.replaceRange(
                        firstSplit[0].length,
                        secondSplit[0].length + 1,
                        uuid
                    )
                    mapOfArrays[uuid] = array

                } else break
            }

            val objMap = mutableMapOf<String, Any?>()
            val errorsBuffer = mutableListOf<String>()

            val values = newRow.split(",")
            values.forEachIndexed { indexCell, value ->

                val columnName = columnsMapInFile[indexCell]?.trim()
                var columnValue: Any? = value.trim()

                if (columnName != null && columnsObjectNames.contains(columnName)) {

                    csvObjectColumns.forEach { csvColumn ->

                        if (csvColumn.name == columnName && value.isBlank()) {
                            columnValue = null
                            if (!csvColumn.optional) {
                                errorsBuffer.add("\n!Error! at row: (${indexRow + 1}) in column: '$columnName' => find empty cell for required parameter")
                            }
                        } else if (csvColumn.name == columnName && csvColumn.columnType == CsvReaderColumnType.TextIterable) {
                            columnValue = mapOfArrays[columnValue]
                                .toString()
                                .removeSurrounding("[", "]")
                                .split(",")
                                .filter { it != "null" }
                            if (((columnValue as List<*>).isEmpty())) {
                                columnValue = null
                                if (!csvColumn.optional) {
                                    errorsBuffer.add("\n!Error! at row: (${indexRow + 1}) in column: '$columnName' => find wrong array format")
                                } else {
                                    errorsBuffer.add("\n/Warning\\ at row: (${indexRow + 1}) in column: '$columnName' => find wrong array format for this optional column")
                                }
                            }
                        }
                    }
                    objMap[columnName] = columnValue
                }
            }
            objMapList.add(objMap)
            errorLogs.addAll(errorsBuffer)
        }
    }

    val mapper = jacksonObjectMapper()
    val jsons = objMapList.map { user -> mapper.writeValueAsString(user) }
    val userObjects: List<T> = jsons.mapNotNull { json ->
        try {
            mapper.readValue<T>(json)
        } catch (e: Exception) {
            errorLogs.add("\ncan not deserialize ${T::class.java.simpleName} from : $json")
            null
        }
    }

    return (CsvReaderResult(userObjects, errorLogs))
}
