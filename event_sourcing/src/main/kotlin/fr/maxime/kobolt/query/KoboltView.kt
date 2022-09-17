package fr.maxime.kobolt.query

import fr.maxime.exposed.KoboltViewsTable
import fr.maxime.exposed.dataBaseViews
import fr.maxime.kobolt.Kobolt
import fr.maxime.kobolt.command.KoboltRebirthEvent
import fr.maxime.kobolt.command.KoboltRenamedEvent
import fr.maxime.kobolt.kobolt_id.KoboltId
import fr.maxime.technicals.Event
import fr.maxime.technicals.InstantSerializer
import fr.maxime.technicals.ViewEvent
import fr.maxime.technicals.allInMemoryDataBaseGenericView
import fr.maxime.technicals.inMemoryViewsKobolt
import fr.maxime.technicals.jsonTool
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.encodeToJsonElement
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
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
                        inMemoryViewsKobolt.getViewFromCategoryAndId<KoboltView>(Kobolt.categoryView, data.koboltId)
                    if (view != null) {
                        koboltView = view.copy(name = data.name)
                        inMemoryViewsKobolt.views[Kobolt.categoryView]?.set(
                            data.koboltId.streamId,
                            jsonTool.encodeToJsonElement(koboltView)
                        )
                    }

                    transaction(dataBaseViews) {
                        KoboltViewsTable.update( { KoboltViewsTable.koboltId eq data.koboltId.streamId } ) {
                            it[name] = data.name
                        }
                    }
                }
            }

            KoboltRebirthEvent.type -> {
                val data = KoboltRebirthEvent.getData(event.data)
                if (data != null) {
                    val view =
                        inMemoryViewsKobolt.getViewFromCategoryAndId<KoboltView>(Kobolt.categoryView, data.koboltId)
                    if (view != null) {
                        koboltView = view.copy(birth = data.birth)
                        inMemoryViewsKobolt.views[Kobolt.categoryView]?.set(
                            data.koboltId.streamId,
                            jsonTool.encodeToJsonElement(koboltView)
                        )
                    }

                    transaction(dataBaseViews) {
                        KoboltViewsTable.update( { KoboltViewsTable.koboltId eq data.koboltId.streamId } ) {
                            it[birth] = data.birth
                        }
                    }
                }
            }

        }

        File("save/event_sourcing/debug_${Kobolt.categoryView}.json")
            .writeText(jsonTool.encodeToString(allInMemoryDataBaseGenericView[Kobolt.categoryView]))

        return koboltView
    }
}
