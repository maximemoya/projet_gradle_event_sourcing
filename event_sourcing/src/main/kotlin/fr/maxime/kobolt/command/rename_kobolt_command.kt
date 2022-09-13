package fr.maxime.kobolt.command

import fr.maxime.kobolt.Kobolt
import fr.maxime.kobolt.command.KoboltRenamedEvent.koboltRenamedEventHandler
import fr.maxime.kobolt.kobolt_id.KoboltId
import fr.maxime.technicals.Event
import fr.maxime.technicals.dataBaseEventKobolt
import fr.maxime.technicals.jsonTool
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

fun renameKoboltCommand(koboltId: KoboltId, name: String) {
    if (dataBaseEventKobolt.exist(Kobolt.categoryEvent, koboltId)) {
        val kobolt = Kobolt.invoke(koboltId)
        if (kobolt != null && kobolt.name != name) {
            koboltRenamedEventHandler(koboltId, name)
        }
    }
}

@Serializable
data class KoboltRenamedData(
    val koboltId: KoboltId,
    val name: String,
)

object KoboltRenamedEvent {

    const val type = "kobolt_renamed"
    const val version = 1

    fun koboltRenamedEventHandler(
        koboltId: KoboltId,
        name: String,
    ) {
        val dataJson = jsonTool.encodeToJsonElement(KoboltRenamedData(koboltId, name))
        val event = Event(type, version, dataJson)
        dataBaseEventKobolt.addEvent(Kobolt.categoryEvent, koboltId, event)
    }

    fun getData(dataJsonElement: JsonElement?): KoboltRenamedData? =
        dataJsonElement?.let {
            jsonTool.decodeFromJsonElement(it)
        }

}