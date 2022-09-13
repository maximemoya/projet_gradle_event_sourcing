package fr.maxime.pokomon.query

import fr.maxime.technicals.Event
import fr.maxime.technicals.InstantSerializer
import fr.maxime.technicals.ViewEvent
import fr.maxime.technicals.allDataBaseGenericView
import fr.maxime.technicals.dataBaseViewPokomon
import fr.maxime.technicals.jsonTool
import fr.maxime.pokomon.Pokomon
import fr.maxime.pokomon.command.PokomonRebirthEvent
import fr.maxime.pokomon.command.PokomonRenamedEvent
import fr.maxime.pokomon.pokomon_id.PokomonId
import fr.maxime.pokomon.pokomon_id.PokomonIdSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import java.time.Instant

// ------------
//  READ MODEL
// ------------

@Serializable
data class PokomonView(
    @Serializable(with = PokomonIdSerializer::class)
    val pokomonId: PokomonId,
    val name: String,
    @Serializable(with = InstantSerializer::class)
    val birth: Instant,
) : ViewEvent {
    override fun update(event: Event): ViewEvent? {

        var pokomonView: PokomonView? = null

        when (event.type) {

            PokomonRenamedEvent.type -> {
                val data = PokomonRenamedEvent.getData(event.data)
                if (data != null) {
                    val view =
                        dataBaseViewPokomon.getViewFromCategoryAndId<PokomonView>(Pokomon.categoryView, data.pokomonId)
                    if (view != null) {
                        pokomonView = view.copy(name = data.name)
                        dataBaseViewPokomon.views[Pokomon.categoryView]?.set(
                            data.pokomonId.streamId,
                            jsonTool.encodeToJsonElement(pokomonView)
                        )
                    }
                }
            }

            PokomonRebirthEvent.type -> {
                val data = PokomonRebirthEvent.getData(event.data)
                if (data != null) {
                    val view =
                        dataBaseViewPokomon.getViewFromCategoryAndId<PokomonView>(Pokomon.categoryView, data.pokomonId)
                    if (view != null) {
                        pokomonView = view.copy(birth = data.birth)
                        dataBaseViewPokomon.views[Pokomon.categoryView]?.set(
                            data.pokomonId.streamId,
                            jsonTool.encodeToJsonElement(pokomonView)
                        )
                    }
                }
            }

        }

        File("save/event_sourcing/debug_${Pokomon.categoryView}.json")
            .writeText(jsonTool.encodeToString(allDataBaseGenericView[Pokomon.categoryView]))

        return pokomonView
    }
}
