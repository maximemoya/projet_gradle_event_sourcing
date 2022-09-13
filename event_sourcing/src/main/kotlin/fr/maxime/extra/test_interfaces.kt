package fr.maxime.extra

import fr.maxime.technicals.AnimalsSecondarySerializer
import fr.maxime.technicals.getTypes
import fr.maxime.technicals.jsonTool
import fr.maxime.extra.ViewEnum.TestV1
import fr.maxime.extra.ViewEnum.TestV2
import fr.maxime.technicals.Event
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import java.util.UUID

@Serializable
sealed interface Animals {

    @Serializable
    sealed interface Dog : Animals {
        @Serializable
        object BergerAllemand : Dog

        @Serializable
        object BergerBelge : Dog
    }

    @Serializable
    object Cat : Animals

    @Serializable
    object None : Animals

}

@Serializable
data class Animal(
    @Serializable(with = AnimalsSecondarySerializer::class)
    val type: Animals,
    private val types: List<String> = getTypes(type),
)

fun animalTest() {
    val a = Event("type", 1, jsonTool.encodeToJsonElement("data"))
    println(jsonTool.encodeToString(a))

    var animal = Animal(Animals.Dog.BergerBelge)
    println(animal)
    val b = jsonTool.encodeToString(animal)
    println(b)
    val c = jsonTool.decodeFromString<Animal>(b)
    println(c)

    animal = Animal(Animals.Dog.BergerAllemand)
    println(getTypes(animal.type))
}

interface MyId<C> {
    val id: C
}

data class TestMyId(override val id: UUID) : MyId<UUID> {

    fun toStreamId(): String = id.toString()

    object Serializer : KSerializer<TestMyId> {
        override val descriptor = PrimitiveSerialDescriptor("TestId", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder): TestMyId {
            return TestMyId(UUID.fromString(decoder.decodeString()))
        }

        override fun serialize(encoder: Encoder, value: TestMyId) {
            encoder.encodeString(value.toStreamId())
        }
    }
}

interface TestView {
    fun update(): TestView?
}

@Serializable
data class MyTestViewClass(
    @Serializable(with = TestMyId.Serializer::class)
    val id: TestMyId,
    val name: String,
    val number: Int,
) : TestView {
    override fun update(): TestView? {
        return null
    }
}

@Serializable
data class MyTestViewClass2(
    @Serializable(with = TestMyId.Serializer::class)
    val id: TestMyId,
    val name: String,
) : TestView {
    override fun update(): TestView? {
        return null
    }
}

fun createEnvironment() {
    val testView = MyTestViewClass(
        id = TestMyId(UUID.randomUUID()),
        name = "name",
        number = 42
    )
    val testView2 = MyTestViewClass(
        id = TestMyId(UUID.randomUUID()),
        name = "name2",
        number = 421
    )
    val testView3 = MyTestViewClass(
        id = TestMyId(UUID.randomUUID()),
        name = "name3",
        number = 5421
    )
    val listTestView = mutableListOf<JsonElement>()
    listTestView.add(jsonTool.encodeToJsonElement(testView))
    listTestView.add(jsonTool.encodeToJsonElement(testView2))
    listTestView.add(jsonTool.encodeToJsonElement(testView3))

    val txtListSaved = jsonTool.encodeToString(listTestView)

    File("save/event_sourcing/petit_test_json_element.json").writeText(txtListSaved)
}

enum class ViewEnum {
    TestV1, TestV2
}

fun createEnvironmentV2() {
    val testView = MyTestViewClass(
        id = TestMyId(UUID.randomUUID()),
        name = "name",
        number = 32
    )
    val testView2 = MyTestViewClass(
        id = TestMyId(UUID.randomUUID()),
        name = "name2",
        number = 321
    )
    val testView3 = MyTestViewClass2(
        id = TestMyId(UUID.randomUUID()),
        name = "name3",
    )
    val aMap = mutableMapOf<ViewEnum, MutableList<JsonElement>>()
    aMap[TestV1] = mutableListOf()
    aMap[TestV1]!!.add(jsonTool.encodeToJsonElement(testView))
    aMap[TestV1]!!.add(jsonTool.encodeToJsonElement(testView2))
    aMap[TestV2] = mutableListOf()
    aMap[TestV2]!!.add(jsonTool.encodeToJsonElement(testView3))

    val txtListSaved = jsonTool.encodeToString(aMap)

    File("save/event_sourcing/petit_test_json_elementV2.json").writeText(txtListSaved)
}

fun loadEnvironment(path: String = "save/event_sourcing/petit_test_json_element.json") {
    val txtListSaved = File(path).readText()
    try {
        val listTestViewRead = jsonTool.decodeFromString<MutableList<JsonElement>>(txtListSaved)
        listTestViewRead.forEach { jsonElement ->
            val view = getObjFromJsonElement<MyTestViewClass>(jsonElement)
            if (view != null) println(view)
        }
    } catch (_: SerializationException) {
        println(" /!\\ problem with JSON input")
    }
}

inline fun <reified T> getObjFromJsonElement(jsonElement: JsonElement): T? {
    return try {
        jsonTool.decodeFromJsonElement<T>(jsonElement)
    } catch (e: Exception) {
        println(" /!\\ problem: Type<${T::class.simpleName}> mismatch with JSON $jsonElement")
        null
    }
}

fun main() {

    val txtListSaved = File("save/event_sourcing/petit_test_json_elementV2.json").readText()
    try {
        val hashMapTestViewRead =
            jsonTool.decodeFromString<MutableMap<ViewEnum, MutableList<JsonElement>>>(txtListSaved)
        hashMapTestViewRead.forEach { viewEnum, listJson ->
            when (viewEnum) {
                TestV1 -> {
                    listJson.forEach { jsonElement ->
                        val view = getObjFromJsonElement<MyTestViewClass>(jsonElement)
                        if (view != null) println(view)
                    }
                }
                TestV2 -> {
                    listJson.forEach { jsonElement ->
                        val view = getObjFromJsonElement<MyTestViewClass2>(jsonElement)
                        if (view != null) println(view)
                    }
                }
            }

        }
    } catch (_: SerializationException) {
        println(" /!\\ problem with JSON input")
    }

}