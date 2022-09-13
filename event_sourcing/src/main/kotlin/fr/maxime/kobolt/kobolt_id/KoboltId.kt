package fr.maxime.kobolt.kobolt_id

import fr.maxime.technicals.Id
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.UUID

// ------------
// SERIALIZER :
// ------------

object KoboltIdSerializer : KSerializer<KoboltId> {
    override val descriptor = PrimitiveSerialDescriptor("KoboltId", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): KoboltId {
        return KoboltId(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: KoboltId) {
        encoder.encodeString(value.streamId)
    }
}

object KoboltIdNullableSerializer : KSerializer<KoboltId?> {
    override val descriptor = PrimitiveSerialDescriptor("KoboltId?", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): KoboltId? {
        val value = decoder.decodeString()
        return if (value != "null") {
            KoboltId(decoder.decodeString())
        } else {
            null
        }

    }

    override fun serialize(encoder: Encoder, value: KoboltId?) {
        encoder.encodeString(value?.streamId ?: "null")
    }
}

// ----
// ID :
// ----

@Serializable(with = KoboltIdSerializer::class)
class KoboltId(override val streamId: String) : Id {
    constructor(
        uid: UUID
    ) : this(streamId = setId(uid))

    override fun toString(): String = streamId

    fun getUid(): UUID {
        return UUID.fromString(streamId)
    }

    companion object {
        fun setId(uid: UUID) = uid.toString()
    }
}