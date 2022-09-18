package fr.maxime.adapters.excel_adapter.excel_file_reader.technicals

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import fr.maxime.adapters.excel_adapter.excel_file_reader.technicals.ExcelReaderColumnType.Text
import fr.maxime.adapters.excel_adapter.excel_file_reader.technicals.ExcelReaderColumnType.TextDate
import fr.maxime.adapters.excel_adapter.excel_file_reader.technicals.ExcelReaderColumnType.TextIterable
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream

enum class ExcelReaderColumnType {
    Text, TextIterable, TextDate
}

class ExcelReaderColumn(
    val name: String,
    val optional: Boolean = false,
    val columnType: ExcelReaderColumnType = Text,
)

data class ExcelReaderResult<out T>(
    val results: List<T> = listOf(),
    val errors: List<String> = listOf()
)

inline fun <reified T> readExcelFileFromXlsxInputStream(
    excelFileXlsxInputStream: InputStream,
    excelObjectColumns: List<ExcelReaderColumn>,
): ExcelReaderResult<T> {

    val workbook = WorkbookFactory.create(excelFileXlsxInputStream)
    val sheet = workbook.getSheetAt(0)
    val columnsObjectNames = excelObjectColumns.map { it.name }
    val columnsObjectNamesRequired = excelObjectColumns.filter { !it.optional }.map { it.name }
    val columnsObjectNamesOptional = excelObjectColumns.filter { it.optional }.map { it.name }
    val columnsNamesInFile = mutableListOf<String>()
    val objMapList = mutableListOf<Map<String, Any?>>()
    val errorLogs = mutableListOf<String>()

    for (row in sheet) {

        when (row.rowNum) {

            0 -> {

                // init columnNames:
                for (cell in row) {
                    val columnName = "${sheet.getRow(0).getCell(cell.columnIndex)}".trim()
                    columnsNamesInFile.add(columnName)
                }

                // check columnNames errors:
                columnsObjectNamesRequired.forEach { columnNameRequired ->
                    if (!columnsNamesInFile.contains(columnNameRequired)) {
                        errorLogs.add("\n!Error! the required column name: '$columnNameRequired' is not present in Excel input file")
                    }
                }
                columnsObjectNamesOptional.forEach { columnNameOptional ->
                    if (!columnsNamesInFile.contains(columnNameOptional)) {
                        errorLogs.add("\n/Warning\\ the optional column name: '$columnNameOptional' is not present in Excel input file")
                    }
                }
                if (errorLogs.size > 0) {
                    errorLogs.add("\n=> the list of required columns is: $columnsObjectNamesRequired")
                    errorLogs.add("\n=> the list of optional columns is: $columnsObjectNamesOptional")
                }

            }

            else -> {

                val objMap = mutableMapOf<String, Any?>()
                val errorsBuffer = mutableListOf<String>()

                for (cell in row) {
                    val columnName = "${sheet.getRow(0).getCell(cell.columnIndex)}".trim()
                    var columnValue: Any? = "${sheet.getRow(row.rowNum).getCell(cell.columnIndex)}".trim()

                    if(columnsObjectNames.contains(columnName)){

                        excelObjectColumns.forEach { excelColumn ->
                            if (excelColumn.name == columnName && columnValue == "") {
                                columnValue = null
                                if (!excelColumn.optional) {
                                    errorsBuffer.add("\n!Error! at row: (${row.rowNum + 1}) in column: '$columnName' => find empty cell for required parameter")
                                }
                            }
                            else if (excelColumn.name == columnName && excelColumn.columnType == TextIterable) {
                                columnValue = columnValue.toString().split(",").map { it.trim() }
                            } else if (excelColumn.name == columnName && excelColumn.columnType == TextDate) {
                                try {
                                    columnValue = cell.dateCellValue.toInstant().toString()
                                } catch (_: Exception) {
                                    errorsBuffer.add("\n!Error! at row: (${row.rowNum + 1}) in column: '$columnName' => expected Excel Date Format but found that: '$columnValue'")
                                    columnValue = null
                                }
                            }
                        }
                        objMap[columnName] = columnValue
                    }
                }

                if(objMap.values.all { it == null }) break
                objMapList.add(objMap)
                errorLogs.addAll(errorsBuffer)
            }
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
    return ExcelReaderResult(userObjects, errorLogs)
}
