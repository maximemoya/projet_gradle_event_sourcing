package fr.maxime.exposed

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.vendors.currentDialect
import java.nio.ByteBuffer
import java.util.UUID
import kotlin.reflect.KClass

class UuidColumnType<T : Any>(
    private val type: KClass<T>,
    private val read: (UUID) -> T,
    private val write: (T) -> UUID,
) : ColumnType() {
    override fun sqlType(): String = currentDialect.dataTypeProvider.uuidType()

    override fun valueFromDB(value: Any): T =
        when (value) {
            is UUID -> value
            is ByteArray -> ByteBuffer.wrap(value).let { b -> UUID(b.long, b.long) }
            is String -> UUID.fromString(value)
            else -> error("Unexpected value of type UUID: $value of ${value::class.qualifiedName}")
        }.let(read)

    override fun notNullValueToDB(value: Any): Any = currentDialect.dataTypeProvider.uuidToDB(valueToUUID(value))

    override fun nonNullValueToString(value: Any): String = "'${valueToUUID(value)}'"

    @Suppress("UNCHECKED_CAST")
    private fun valueToUUID(value: Any): UUID = when {
        type.isInstance(value) -> write(value as T)
        value is UUID -> value
        value is String -> UUID.fromString(value)
        value is ByteArray -> ByteBuffer.wrap(value).let { UUID(it.long, it.long) }
        else -> error("Unexpected value of type UUID: ${value.javaClass.canonicalName}")
    }

    companion object {
        inline fun <reified T : Any> Table.uuid(
            name: String,
            noinline read: (UUID) -> T,
            noinline write: (T) -> UUID,
        ): Column<T> =
            registerColumn(name, UuidColumnType(T::class, read, write))
    }
}