package fr.maxime.pokomon.pokomon_id

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

object PokomonIdSerializer : KSerializer<PokomonId> {
    override val descriptor = PrimitiveSerialDescriptor("PokomonId", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): PokomonId {
        return PokomonId(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: PokomonId) {
        encoder.encodeString(value.streamId)
    }
}

// ----
// ID :
// ----

@Serializable(with = PokomonIdSerializer::class)
class PokomonId(override val streamId: String) : Id {
    constructor(
        name: String,
        uid: UUID,
    ) : this(streamId = setId(name, uid))

    override fun toString(): String = streamId

    fun getNameAndUid(): Pair<String, UUID> {
        val a = this.streamId.split("_")
        return a[0] to UUID.fromString(a[1])
    }

    companion object {
        fun setId(name: String, uid: UUID) = name + "_" + uid
    }
}