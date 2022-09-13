package fr.maxime.kobolt.command

import fr.maxime.kobolt.Kobolt
import fr.maxime.kobolt.command.KoboltCreatedEvent.koboltCreatedEventHandler
import fr.maxime.kobolt.kobolt_id.KoboltId
import fr.maxime.kobolt.kobolt_id.KoboltIdSerializer
import fr.maxime.kobolt.query.KoboltView
import fr.maxime.technicals.Event
import fr.maxime.technicals.InstantSerializer
import fr.maxime.technicals.addViewEventToEventListener
import fr.maxime.technicals.dataBaseEventKobolt
import fr.maxime.technicals.dataBaseViewKobolt
import fr.maxime.technicals.jsonTool
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import java.time.Instant

// ---------
// COMMAND :
// ---------

fun createKoboltCommand(koboltId: KoboltId, name: String, birth: Instant) {
    koboltCreatedEventHandler(koboltId, name, birth)
}

// -------
// EVENT :
// -------

@Serializable
data class KoboltCreatedData(
    @Serializable(with = KoboltIdSerializer::class)
    val koboltId: KoboltId,
    val name: String,
    @Serializable(with = InstantSerializer::class)
    val birth: Instant
)

object KoboltCreatedEvent {

    const val type = "kobolt_created"
    const val version = 1

    fun koboltCreatedEventHandler(
        id: KoboltId,
        name: String,
        birth: Instant
    ) {

        // ------
        // VIEW :
        // ------
        val jsonElement = jsonTool.encodeToJsonElement(KoboltView(id, name, birth))
        dataBaseViewKobolt.createView(Kobolt.categoryView, id, jsonElement)

        // --------------------
        // VIEW EVENT HANDLER :
        // --------------------
        addViewEventToEventListener(
            Kobolt.categoryEvent,
            jsonTool.decodeFromJsonElement<KoboltView>(dataBaseViewKobolt.views[Kobolt.categoryView]?.get(id.streamId)!!)
        )

        // -------
        // EVENT :
        // -------
        val dataJson = jsonTool.encodeToJsonElement(
            KoboltCreatedData(
                koboltId = id,
                name = name,
                birth = birth,
            )
        )
        val event = Event(type, version, dataJson)
        dataBaseEventKobolt.addEvent(Kobolt.categoryEvent, id, event)

    }

    fun getData(dataJsonElement: JsonElement?): KoboltCreatedData? =
        dataJsonElement?.let {
            jsonTool.decodeFromJsonElement(it)
        }

}