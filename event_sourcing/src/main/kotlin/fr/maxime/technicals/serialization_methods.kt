package fr.maxime.technicals

import fr.maxime.extra.Animals
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.UUID
import kotlin.reflect.full.allSuperclasses

val jsonTool = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
    encodeDefaults = true
}

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }
}

object UUIDNullableSerializer : KSerializer<UUID?> {
    override val descriptor = PrimitiveSerialDescriptor("UUID?", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID? {
        return UUID.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: UUID?) {
        encoder.encodeString(value.toString())
    }
}

object InstantSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }
}

fun getTypes(obj: Any): List<String> =
    buildList {
        if (obj::class.simpleName != null) {
            add(obj::class.simpleName!!)
        }
        val classes = obj::class.allSuperclasses
        addAll(classes.mapNotNull { it.simpleName })
    }.reversed()

object AnimalsSecondarySerializer : KSerializer<Animals> {
    override val descriptor = PrimitiveSerialDescriptor("Animals", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Animals {
        return when (decoder.decodeString()) {
            Animals.Dog.BergerAllemand::class.java.simpleName -> {
                Animals.Dog.BergerAllemand
            }
            Animals.Dog.BergerBelge::class.java.simpleName -> {
                Animals.Dog.BergerBelge
            }
            Animals.Cat::class.java.simpleName -> {
                Animals.Cat
            }
            else -> {
                Animals.None
            }
        }
    }

    override fun serialize(encoder: Encoder, value: Animals) {
        when (value) {
            is Animals.Dog -> {
                encoder.encodeString(value::class.java.simpleName)
            }
            is Animals.Cat -> {
                encoder.encodeString(value::class.java.simpleName)
            }
            else -> {
                encoder.encodeString(Animals.None::class.java.simpleName)
            }
        }
    }
}
