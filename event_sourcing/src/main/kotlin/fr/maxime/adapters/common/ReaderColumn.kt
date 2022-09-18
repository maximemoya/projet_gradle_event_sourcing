package fr.maxime.adapters.common

enum class ReaderColumnType {
    Text, TextIterable, TextDate
}

class ReaderColumn(
    val name: String,
    val optional: Boolean = false,
    val columnType: ReaderColumnType = ReaderColumnType.Text,
)