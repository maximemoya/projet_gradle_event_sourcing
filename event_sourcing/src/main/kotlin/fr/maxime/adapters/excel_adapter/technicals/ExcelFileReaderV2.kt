package fr.maxime.adapters.excel_adapter.technicals

import arrow.core.continuations.Effect
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
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream

data class HeaderColumn(
    val name: String,
    val optional: Boolean = false,
)

fun readUserFromXlsxInputStreamV2(
    excelFileXlsxInputStream: InputStream,
): Effect<List<String>, List<UserToCreate>> =
    effect {
        val workbook = WorkbookFactory.create(excelFileXlsxInputStream)
        val sheet = workbook.getSheetAt(0)
        val excelMapColumn = initializeHeader(sheet, userHeaders).bind()

        sheet.asSequence()
            .drop(1)
            .filter { row -> row.physicalNumberOfCells > 0 }
            .mapNotNull { row ->
                UserToCreate(
                    firstName = row.getCell(excelMapColumn[firstNameUserHeader]!!)?.stringCellValue ?: "",
                    lastName = row.getCell(excelMapColumn[lastNameUserHeader]!!)?.stringCellValue ?: "",
                    username = row.getCell(excelMapColumn[usernameUserHeader]!!)?.stringCellValue ?: "",
                    password = row.getCell(excelMapColumn[passwordUserHeader]!!)?.stringCellValue ?: "",
                    divisions = row.getCell(excelMapColumn[divisionsUserHeader]!!)?.stringCellValue
                        ?.split(",")
                        ?.map { it.trim() }
                        ?.toSet(),
                    email = if (excelMapColumn[emailUserHeader] != null) row.getCell(excelMapColumn[emailUserHeader]!!).stringCellValue
                    else null,
                    row = row.rowNum + 1
                )
            }.toList()
    }

private typealias excelHeader = String
private typealias excelColumnIndex = Int

private fun initializeHeader(
    sheet: Sheet,
    userHeaders: List<HeaderColumn>,
): Effect<List<String>, Map<excelHeader, excelColumnIndex>> =

    effect {
        val excelHeaders = sheet.getRow(0).map { cell ->
            cell.stringCellValue.lowercase().trim()
        }

        val errors = mutableListOf<String>()
        userHeaders.forEach { header ->
            if (!header.optional && !excelHeaders.contains(header.name)) {
                errors.add("the required column name: '${header.name}' is not present in Excel input file")
            }
        }
        if (errors.size > 0) {
            errors.add("=> the list of required columns is: ${userHeaders.filter { !it.optional }.map { it.name }}")
            errors.add("=> the list of optional columns is: ${userHeaders.filter { it.optional }.map { it.name }}")
        }
        ensure(errors.isEmpty()) { errors.toList() }

        val mapExcelHeaders = mutableMapOf<String, Int>()
        excelHeaders.forEachIndexed { index, excelHeader ->
            mapExcelHeaders[excelHeader] = index
        }
        mapExcelHeaders
    }


class Test()

fun main() {

    runBlocking {
        effect {
            val inputStream = Test::class.java.classLoader.getResourceAsStream("excel_test_files/test1.xlsx")
            if (inputStream != null) {
                val result = readUserFromXlsxInputStreamV2(inputStream).bind()
                result.forEach { println(it) }
            } else {
                println("can not read inputStream")
            }
        }.toEither()
    }.getOrHandle { error -> error.forEach { println(it) } }


}