package fr.maxime.pokomon.command

import fr.maxime.pokomon.Pokomon
import fr.maxime.pokomon.command.PokomonRenamedEvent.pokomonRenamedEventHandler
import fr.maxime.pokomon.pokomon_id.PokomonId
import fr.maxime.pokomon.pokomon_id.PokomonIdSerializer
import fr.maxime.technicals.Event
import fr.maxime.technicals.dataBaseEventPokomon
import fr.maxime.technicals.jsonTool
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

fun renamePokomonCommand(pokomonId: PokomonId, name: String) {
    if (dataBaseEventPokomon.exist(Pokomon.categoryEvent, pokomonId)) {
        val pokomon = Pokomon.invoke(pokomonId)
        if (pokomon != null && pokomon.name != name) {
            pokomonRenamedEventHandler(pokomonId, name)
        }
    }
}

@Serializable
data class PokomonRenamedData(
    @Serializable(with = PokomonIdSerializer::class)
    val pokomonId: PokomonId,
    val name: String,
)

object PokomonRenamedEvent {

    const val type = "pokomon_renamed"
    const val version = 1

    fun pokomonRenamedEventHandler(
        pokomonId: PokomonId,
        name: String,
    ) {
        val dataJson = jsonTool.encodeToJsonElement(PokomonRenamedData(pokomonId, name))
        val event = Event(type, version, dataJson)
        dataBaseEventPokomon.addEvent(Pokomon.categoryEvent, pokomonId, event)
    }

    fun getData(dataJsonElement: JsonElement?): PokomonRenamedData? =
        dataJsonElement?.let {
            jsonTool.decodeFromJsonElement(it)
        }

}