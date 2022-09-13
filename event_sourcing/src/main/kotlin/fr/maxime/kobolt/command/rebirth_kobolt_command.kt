package fr.maxime.kobolt.command

import fr.maxime.kobolt.Kobolt
import fr.maxime.kobolt.command.KoboltRebirthEvent.koboltRebirthEventHandler
import fr.maxime.kobolt.kobolt_id.KoboltId
import fr.maxime.technicals.Event
import fr.maxime.technicals.InstantSerializer
import fr.maxime.technicals.dataBaseEventKobolt
import fr.maxime.technicals.jsonTool
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import java.time.Instant

fun rebirthKoboltCommand(koboltId: KoboltId, birth: Instant) {
    if (dataBaseEventKobolt.exist(Kobolt.categoryEvent, koboltId)) {
        val kobolt = Kobolt.invoke(koboltId)
        if (kobolt != null && kobolt.birth != birth) {
            koboltRebirthEventHandler(koboltId, birth)
        }
    }
}

@Serializable
data class KoboltRebirthData(
    val koboltId: KoboltId,
    @Serializable(with = InstantSerializer::class)
    val birth: Instant,
)

object KoboltRebirthEvent {

    const val type = "kobolt_rebirth"
    const val version = 1

    fun koboltRebirthEventHandler(
        koboltId: KoboltId,
        birth: Instant,
    ) {
        val dataJson = jsonTool.encodeToJsonElement(KoboltRebirthData(koboltId, birth))
        val event = Event(type, version, dataJson)
        dataBaseEventKobolt.addEvent(Kobolt.categoryEvent, koboltId, event)
    }

    fun getData(dataJsonElement: JsonElement?): KoboltRebirthData? =
        dataJsonElement?.let {
            jsonTool.decodeFromJsonElement(it)
        }

}


