package fr.maxime.technicals

import fr.maxime.kobolt.Kobolt
import fr.maxime.kobolt.kobolt_id.KoboltId
import fr.maxime.pokomon.Pokomon
import fr.maxime.pokomon.pokomon_id.PokomonId
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement
import java.io.File
import java.time.Instant
import java.util.UUID

//------------
//  EVENT V1
//------------

@Serializable
data class Event(
    val type: String,
    val version: Int,
    val data: JsonElement?,
    @Serializable(with = UUIDSerializer::class)
    val eventId: UUID = UUID.randomUUID(),
    @Serializable(with = InstantSerializer::class)
    val timestamp: Instant = Instant.now(),
)

//---------------------
//  DATABASE EVENT V1
//---------------------

@Serializable
data class LocalDataBaseEventV1(
    val events: MutableMap<String, MutableMap<
            @Serializable(with = UUIDSerializer::class) UUID,
            MutableList<Event>>> =
        mutableMapOf()
) {

    fun addEvent(categoryEvent: String, id: UUID, event: Event) {
        println(
            "Save Event:" +
                    "\n -> writing in $categoryEvent at id: $id :\n\t$event"
        )

        if (events[categoryEvent] == null) {
            events[categoryEvent] = mutableMapOf(id to mutableListOf(event))
        } else {
            if (events[categoryEvent]!![id] == null) {
                events[categoryEvent]!![id] = mutableListOf(event)
            } else {
                events[categoryEvent]!![id]!!.add(event)
            }
        }
        println()
        File("save/event_sourcing/debugEventsKobolt.json").writeText(jsonTool.encodeToString(dataBaseEventV1))

        categoryEventListener[categoryEvent]?.forEach { viewEvent ->
            viewEvent.update(event)
        }
    }

    fun existEvents(categoryEvent: String, id: UUID) = events[categoryEvent]?.get(id) != null

    fun deleteEvents(aggregateCategoryEvent: String, id: UUID): Boolean {
        val result = dataBaseEventV1.events[aggregateCategoryEvent]?.remove(id) != null
        File("save/event_sourcing/debugEventsKobolt.json").writeText(jsonTool.encodeToString(dataBaseEventV1))
        return result
    }

}

val dataBaseEventV1 = LocalDataBaseEventV1()

//----------------------------
//  DATABASE EVENT Generic V1
//----------------------------

@Serializable
class LocalDataBaseEventGeneric<T : Id> {

    val events = mutableMapOf<String, MutableMap<String, MutableList<Event>>>()

    fun addEvent(categoryEvent: String, id: T, event: Event) {
        println(
            "Save Event:" +
                    "\n -> writing in $categoryEvent at id: $id :\n\t$event"
        )

        if (events[categoryEvent] == null) {
            events[categoryEvent] = mutableMapOf(id.streamId to mutableListOf(event))
        } else {
            if (events[categoryEvent]!![id.streamId] == null) {
                events[categoryEvent]!![id.streamId] = mutableListOf(event)
            } else {
                events[categoryEvent]!![id.streamId]!!.add(event)
            }
        }
        println()

        when (categoryEvent) {
            Kobolt.categoryEvent -> {
                val dataBase = allDataBaseEvent[categoryEvent] as LocalDataBaseEventGeneric<KoboltId>?
                if (dataBase != null) {
                    File("save/event_sourcing/debug_$categoryEvent.json").writeText(jsonTool.encodeToString(dataBase))
                }
            }
            Pokomon.categoryEvent -> {
                val dataBase = allDataBaseEvent[categoryEvent] as LocalDataBaseEventGeneric<PokomonId>?
                if (dataBase != null) {
                    File("save/event_sourcing/debug_$categoryEvent.json").writeText(jsonTool.encodeToString(dataBase))
                }
            }
        }



        categoryEventListener[categoryEvent]?.forEach { viewEvent ->
            viewEvent.update(event)
        }
    }

    fun exist(categoryEvent: String, id: T) = events[categoryEvent]?.get(id.streamId) != null

    fun deleteEvents(categoryEvent: String, id: T): Boolean {
        val result = events[categoryEvent]?.remove(id.streamId) != null
        if (result) {
            val dataBase = allDataBaseEvent[categoryEvent]
            if (dataBase != null) {
                File("save/event_sourcing/debug_$categoryEvent.json").writeText(jsonTool.encodeToString(dataBase))
            }
        }
        return result
    }

}

val dataBaseEventKobolt = LocalDataBaseEventGeneric<KoboltId>()
val dataBaseEventPokomon = LocalDataBaseEventGeneric<PokomonId>()
val allDataBaseEvent = mapOf(
    Kobolt.categoryEvent to dataBaseEventKobolt,
    Pokomon.categoryEvent to dataBaseEventPokomon,
)

//---------------------
//  EVENT LISTENER V1
//---------------------

val categoryEventListener = mutableMapOf<String, MutableList<ViewEvent>>()

fun addViewEventToEventListener(categoryEvent: String, viewEvent: ViewEvent) {

    if (categoryEventListener[categoryEvent] == null) {
        categoryEventListener[categoryEvent] = mutableListOf()
        categoryEventListener[categoryEvent]!!.add(viewEvent)
    } else {
        categoryEventListener[categoryEvent]?.add(viewEvent)
    }

}
