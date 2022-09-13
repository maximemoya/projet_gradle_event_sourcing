package fr.maxime.kobolt

import fr.maxime.kobolt.command.KoboltCreatedEvent
import fr.maxime.kobolt.command.KoboltRebirthEvent
import fr.maxime.kobolt.command.KoboltRenamedEvent
import fr.maxime.kobolt.kobolt_id.KoboltId
import fr.maxime.technicals.dataBaseEventKobolt
import java.time.Instant

// -----------
// AGGREGATE :
// -----------

data class Kobolt(
    val id: KoboltId,
    val name: String,
    val birth: Instant
) {
    companion object {
        const val categoryEvent = "kobolt_events"
        const val categoryView = "kobolt_views"
        fun invoke(id: KoboltId): Kobolt? {
            var kobolt: Kobolt? = null
            if (dataBaseEventKobolt.events[categoryEvent]?.get(id.streamId) != null){
                dataBaseEventKobolt.events[categoryEvent]?.get(id.streamId)?.forEach { event ->
                    when (event.type) {
                        KoboltCreatedEvent.type -> {
                            val data = KoboltCreatedEvent.getData(event.data)
                            if (data != null) {
                                kobolt = Kobolt(data.koboltId, data.name, data.birth)
                            }
                        }
                        KoboltRenamedEvent.type -> {
                            val data = KoboltRenamedEvent.getData(event.data)
                            if (data != null) {
                                kobolt = kobolt?.copy(name = data.name)
                            }
                        }
                        KoboltRebirthEvent.type -> {
                            val data = KoboltRebirthEvent.getData(event.data)
                            if (data != null) {
                                kobolt = kobolt?.copy(birth = data.birth)
                            }
                        }
                    }
                }
            }
            return kobolt
        }
    }
}

// ---------------------------
// OLD CONSTRUCTOR BY EVENTS :
// ---------------------------

fun koboltEventReader(id: KoboltId): Kobolt? {
    var kobolt: Kobolt? = null
    println(
        "Generate ${Kobolt::class.java.simpleName}:" +
                "\n -> reading in ${Kobolt.categoryEvent} at id: $id"
    )
    if (dataBaseEventKobolt.events[Kobolt.categoryEvent]?.get(id.streamId) != null){
        dataBaseEventKobolt.events[Kobolt.categoryEvent]?.get(id.streamId)?.forEach { event ->
            when (event.type) {
                KoboltCreatedEvent.type -> {
                    val data = KoboltCreatedEvent.getData(event.data)
                    if (data != null) {
                        kobolt = Kobolt(data.koboltId, data.name, data.birth)
                    }
                }
                KoboltRenamedEvent.type -> {
                    val data = KoboltRenamedEvent.getData(event.data)
                    if (data != null) {
                        kobolt = kobolt?.copy(name = data.name)
                    }
                }
                KoboltRebirthEvent.type -> {
                    val data = KoboltRebirthEvent.getData(event.data)
                    if (data != null) {
                        kobolt = kobolt?.copy(birth = data.birth)
                    }
                }
            }
            println("\t${event.type} => $kobolt {data:${event.data}}")
        }
    }
    else{
        println("\tno events to read for this kobolt")
    }
    println()
    return kobolt
}