package fr.maxime.excel_adapter.excel_file_reader.kobolts

import com.fasterxml.jackson.annotation.JsonProperty
import fr.maxime.excel_adapter.excel_file_reader.technicals.ExcelReaderColumn
import fr.maxime.excel_adapter.excel_file_reader.technicals.ExcelReaderColumnType.TextDate
import fr.maxime.excel_adapter.excel_file_reader.technicals.ExcelReaderColumnType.TextIterable

val excelKoboltCreationColumns = listOf(
    ExcelReaderColumn(
        name = "nom",
    ),
    ExcelReaderColumn(
        name = "date de naissance",
        columnType = TextDate,
    ),
    ExcelReaderColumn(
        name = "liste",
        columnType = TextIterable,
        optional = true,
    ),
)

data class ExcelKoboltCreationDto(
    @JsonProperty("nom") val name: String,
    @JsonProperty("date de naissance") val birth: String,
    @JsonProperty("liste") val aList: List<String>?,
)
