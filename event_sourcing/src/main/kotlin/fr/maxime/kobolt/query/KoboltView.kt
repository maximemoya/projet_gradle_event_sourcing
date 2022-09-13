package fr.maxime.kobolt.query

import fr.maxime.kobolt.Kobolt
import fr.maxime.kobolt.command.KoboltRebirthEvent
import fr.maxime.kobolt.command.KoboltRenamedEvent
import fr.maxime.kobolt.kobolt_id.KoboltId
import fr.maxime.technicals.Event
import fr.maxime.technicals.InstantSerializer
import fr.maxime.technicals.ViewEvent
import fr.maxime.technicals.allDataBaseGenericView
import fr.maxime.technicals.dataBaseViewKobolt
import fr.maxime.technicals.jsonTool
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import java.time.Instant

// ------------
//  READ MODEL
// ------------

@Serializable
data class KoboltView(
    val koboltId: KoboltId,
    val name: String,
    @Serializable(with = InstantSerializer::class)
    val birth: Instant,
) : ViewEvent {
    override fun update(event: Event): ViewEvent? {

        var koboltView: KoboltView? = null

        when (event.type) {

            KoboltRenamedEvent.type -> {
                val data = KoboltRenamedEvent.getData(event.data)
                if (data != null) {
                    val view =
                        dataBaseViewKobolt.getViewFromCategoryAndId<KoboltView>(Kobolt.categoryView, data.koboltId)
                    if (view != null) {
                        koboltView = view.copy(name = data.name)
                        dataBaseViewKobolt.views[Kobolt.categoryView]?.set(
                            data.koboltId.streamId,
                            jsonTool.encodeToJsonElement(koboltView)
                        )
                    }
                }
            }

            KoboltRebirthEvent.type -> {
                val data = KoboltRebirthEvent.getData(event.data)
                if (data != null) {
                    val view =
                        dataBaseViewKobolt.getViewFromCategoryAndId<KoboltView>(Kobolt.categoryView, data.koboltId)
                    if (view != null) {
                        koboltView = view.copy(birth = data.birth)
                        dataBaseViewKobolt.views[Kobolt.categoryView]?.set(
                            data.koboltId.streamId,
                            jsonTool.encodeToJsonElement(koboltView)
                        )
                    }
                }
            }

        }

        File("save/event_sourcing/debug_${Kobolt.categoryView}.json")
            .writeText(jsonTool.encodeToString(allDataBaseGenericView[Kobolt.categoryView]))

        return koboltView
    }
}
