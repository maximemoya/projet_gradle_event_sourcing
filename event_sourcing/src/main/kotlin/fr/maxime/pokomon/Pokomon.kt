package fr.maxime.pokomon

import fr.maxime.pokomon.command.PokomonCreatedEvent
import fr.maxime.pokomon.command.PokomonRebirthEvent
import fr.maxime.pokomon.command.PokomonRenamedEvent
import fr.maxime.technicals.dataBaseEventPokomon
import fr.maxime.pokomon.pokomon_id.PokomonId
import java.time.Instant

// -----------
// AGGREGATE :
// -----------

data class Pokomon(
    val id: PokomonId,
    val name: String,
    val birth: Instant
) {
    companion object {
        const val categoryEvent = "pokomon_events"
        const val categoryView = "pokomon_views"
        fun invoke(id: PokomonId): Pokomon? {
            var pokomon: Pokomon? = null
            if (dataBaseEventPokomon.events[categoryEvent]?.get(id) != null) {
                dataBaseEventPokomon.events[categoryEvent]?.get(id)?.forEach { event ->
                    when (event.type) {
                        PokomonCreatedEvent.type -> {
                            val data = PokomonCreatedEvent.getData(event.data)
                            if (data != null) {
                                pokomon = Pokomon(data.pokomonId, data.name, data.birth)
                            }
                        }
                        PokomonRenamedEvent.type -> {
                            val data = PokomonRenamedEvent.getData(event.data)
                            if (data != null) {
                                pokomon = pokomon?.copy(id = data.pokomonId, name = data.name)
                            }
                        }
                        PokomonRebirthEvent.type -> {
                            val data = PokomonRebirthEvent.getData(event.data)
                            if (data != null) {
                                pokomon = pokomon?.copy(id = data.pokomonId, birth = data.birth)
                            }
                        }
                    }
                }
            }
            return pokomon
        }
    }
}

// ---------------------------
// OLD CONSTRUCTOR BY EVENTS :
// ---------------------------

fun pokomonEventReader(id: PokomonId): Pokomon? {

    var pokomon: Pokomon? = null
    println(
        "Generate Pokomon:" +
                "\n -> reading in ${Pokomon.categoryEvent} at id: $id"
    )

    if (dataBaseEventPokomon.events[Pokomon.categoryEvent]?.get(id) != null) {
        dataBaseEventPokomon.events[Pokomon.categoryEvent]?.get(id)?.forEach { event ->

            when (event.type) {

                PokomonCreatedEvent.type -> {
                    val data = PokomonCreatedEvent.getData(event.data)
                    if (data != null) {
                        pokomon = Pokomon(data.pokomonId, data.name, data.birth)
                    }
                }

                PokomonRenamedEvent.type -> {
                    val data = PokomonRenamedEvent.getData(event.data)
                    if (data != null) {
                        pokomon = pokomon?.copy(id = data.pokomonId, name = data.name)
                    }
                }

                PokomonRebirthEvent.type -> {
                    val data = PokomonRebirthEvent.getData(event.data)
                    if (data != null) {
                        pokomon = pokomon?.copy(id = data.pokomonId, birth = data.birth)
                    }
                }

            }
            println("\t${event.type} => $pokomon {data:${event.data}}")
        }
    } else {
        println("\tno events to read for this pokomon")
    }

    println()

    return pokomon

}
