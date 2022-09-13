package fr.maxime.pokomon.command

import fr.maxime.pokomon.Pokomon
import fr.maxime.pokomon.command.PokomonRebirthEvent.pokomonRebirthEventHandler
import fr.maxime.pokomon.pokomon_id.PokomonId
import fr.maxime.pokomon.pokomon_id.PokomonIdSerializer
import fr.maxime.technicals.Event
import fr.maxime.technicals.InstantSerializer
import fr.maxime.technicals.dataBaseEventPokomon
import fr.maxime.technicals.jsonTool
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import java.time.Instant

fun rebirthPokomonCommand(pokomonId: PokomonId, birth: Instant) {
    if (dataBaseEventPokomon.exist(Pokomon.categoryEvent, pokomonId)) {
        val pokomon = Pokomon.invoke(pokomonId)
        if (pokomon != null && pokomon.birth != birth) {
            pokomonRebirthEventHandler(pokomonId, birth)
        }
    }
}

@Serializable
data class PokomonRebirthData(
    @Serializable(with = PokomonIdSerializer::class)
    val pokomonId: PokomonId,
    @Serializable(with = InstantSerializer::class)
    val birth: Instant,
)

object PokomonRebirthEvent {

    const val type = "pokomon_rebirth"
    const val version = 1

    fun pokomonRebirthEventHandler(
        pokomonId: PokomonId,
        birth: Instant,
    ) {
        val dataJson = jsonTool.encodeToJsonElement(PokomonRebirthData(pokomonId, birth))
        val event = Event(type, version, dataJson)
        dataBaseEventPokomon.addEvent(Pokomon.categoryEvent, pokomonId, event)
    }

    fun getData(dataJsonElement: JsonElement?): PokomonRebirthData? =
        dataJsonElement?.let { jsonTool.decodeFromJsonElement(it) }

}


