package fr.maxime.pokomon.command

import fr.maxime.pokomon.Pokomon
import fr.maxime.pokomon.command.PokomonCreatedEvent.pokomonCreatedEventHandler
import fr.maxime.pokomon.pokomon_id.PokomonId
import fr.maxime.pokomon.pokomon_id.PokomonIdSerializer
import fr.maxime.pokomon.query.PokomonView
import fr.maxime.technicals.Event
import fr.maxime.technicals.InstantSerializer
import fr.maxime.technicals.addViewEventToEventListener
import fr.maxime.technicals.dataBaseEventPokomon
import fr.maxime.technicals.inMemoryViewsPokomon
import fr.maxime.technicals.jsonTool
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import java.time.Instant

// ---------
// COMMAND :
// ---------

fun createPokomonCommand(pokomonId: PokomonId, name: String, birth: Instant) {
    pokomonCreatedEventHandler(pokomonId, name, birth)
}

// -------
// EVENT :
// -------

@Serializable
data class PokomonCreatedData(
    @Serializable(with = PokomonIdSerializer::class)
    val pokomonId: PokomonId,
    val name: String,
    @Serializable(with = InstantSerializer::class)
    val birth: Instant
)

object PokomonCreatedEvent {

    const val type = "pokomon_created"
    const val version = 1

    fun pokomonCreatedEventHandler(
        id: PokomonId,
        name: String,
        birth: Instant
    ) {

        // ------
        // VIEW :
        // ------
        val jsonElement = jsonTool.encodeToJsonElement(PokomonView(id, name, birth))
        inMemoryViewsPokomon.createView(Pokomon.categoryView, id, jsonElement)

        // --------------------
        // VIEW EVENT HANDLER :
        // --------------------
        addViewEventToEventListener(
            Pokomon.categoryEvent,
            jsonTool.decodeFromJsonElement<PokomonView>(inMemoryViewsPokomon.views[Pokomon.categoryView]?.get(id.streamId)!!)
        )

        // -------
        // EVENT :
        // -------
        val dataJson = jsonTool.encodeToJsonElement(
            PokomonCreatedData(
                pokomonId = id,
                name = name,
                birth = birth,
            )
        )
        val event = Event(type, version, dataJson)
        dataBaseEventPokomon.addEvent(Pokomon.categoryEvent, id, event)

    }

    fun getData(dataJsonElement: JsonElement?): PokomonCreatedData? =
        dataJsonElement?.let {
            jsonTool.decodeFromJsonElement(it)
        }

}