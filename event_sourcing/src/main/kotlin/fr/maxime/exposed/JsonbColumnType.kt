package fr.maxime.exposed

//class JsonbColumnType<T : Any>(
//    private val javaType: JavaType,
//) : ColumnType() {
//    override fun sqlType(): String = "jsonb"
//
//    override fun valueFromDB(value: Any): T =
//        when (value) {
//            is ByteArray -> Jackson.mapper.readValue(value, javaType)
//            is String -> Jackson.mapper.readValue(value, javaType)
//            is PGobject -> Jackson.mapper.readValue(value.value, javaType)
//            else -> error("Unexpected value of type jsonb: $value of ${value::class.qualifiedName}")
//        }
//
//    override fun nonNullValueToString(value: Any): String = Jackson.mapper.writeValueAsString(value)
//
//    override fun notNullValueToDB(value: Any): Any =
//        PGobject().also {
//            it.type = "jsonb"
//            it.value = Jackson.mapper.writeValueAsString(value)
//        }
//
//    companion object {
//        inline operator fun <reified T : Any> invoke() =
//            JsonbColumnType<T>(Jackson.mapper.constructType(object : TypeReference<T>() {}))
//
//        inline fun <reified T : Any> Table.jsonb(name: String): Column<T> = registerColumn(name, JsonbColumnType<T>())
//    }
//}