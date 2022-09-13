package fr.maxime.technicals

import fr.maxime.kobolt.Kobolt
import fr.maxime.kobolt.kobolt_id.KoboltId
import fr.maxime.pokomon.Pokomon
import fr.maxime.pokomon.pokomon_id.PokomonId
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import java.io.File
import java.util.UUID

interface ViewEvent {
    fun update(event: Event): ViewEvent?
}

// ---------------
// DataBaseView V1
// ---------------
@Serializable
data class ViewsHashMap(
    val views: MutableMap<String, MutableMap<@Serializable(with = UUIDSerializer::class) UUID, JsonElement>> =
        mutableMapOf()
)

val dataBaseViewV1 = ViewsHashMap()

// -------------------
// GET DataBaseView V1
// -------------------
inline fun <reified T : ViewEvent> getViewFromCategoryAndId(aggregateCategoryView: String, id: UUID): T? =
    dataBaseViewV1.views[aggregateCategoryView]?.get(id)?.let { jsonTool.decodeFromJsonElement<T>(it) }

inline fun <reified T : ViewEvent> getViewsFromCategory(aggregateCategoryView: String): List<T> {
    val views = dataBaseViewV1.views[aggregateCategoryView]
    return views?.mapNotNull { view ->
        jsonTool.decodeFromJsonElement<T>(view.value)
    } ?: return listOf()
}

// ----------------------
// DELETE DataBaseView V1
// ----------------------
fun deleteViewFromCategoryAndId(aggregateCategoryView: String, id: UUID): Boolean {
    val result = dataBaseViewV1.views[aggregateCategoryView]?.remove(id) != null
    File("save/event_sourcing/debugViewsKobolt.json").writeText(jsonTool.encodeToString(dataBaseViewV1))
    return result
}

// ----------------------
// DataBaseGenericView V1
// ----------------------
@Serializable
data class DataBaseGenericView<T : Id>(
    val views: MutableMap<String, MutableMap<String, JsonElement>> = mutableMapOf()
) {
    fun createView(categoryView: String, id: T, jsonElement: JsonElement) {
        if (views[categoryView] != null) {
            views[categoryView]!!.put(id.streamId, jsonElement)
        } else {
            views[categoryView] = mutableMapOf(id.streamId to jsonElement)
        }
    }

    inline fun <reified V : ViewEvent> getViewFromCategoryAndId(categoryView: String, id: T): V? =
        views[categoryView]?.get(id.streamId)?.let { jsonTool.decodeFromJsonElement<V>(it) }


    inline fun <reified V : ViewEvent> getViewsFromCategory(categoryView: String): List<V> {
        val views = views[categoryView]
        return views?.mapNotNull { view ->
            jsonTool.decodeFromJsonElement<V>(view.value)
        } ?: return listOf()
    }

    fun deleteViewFromCategoryAndId(categoryView: String, id: T): Boolean {
        val result = views[categoryView]?.remove(id.streamId) != null
        if (result) {
            File("save/event_sourcing/debug_$categoryView.json").writeText(
                jsonTool.encodeToString(
                    allDataBaseGenericView[categoryView]
                )
            )
        }
        return result
    }
}

val dataBaseViewKobolt = DataBaseGenericView<KoboltId>()
val dataBaseViewPokomon = DataBaseGenericView<PokomonId>()
val allDataBaseGenericView = mapOf(
    Kobolt.categoryView to dataBaseViewKobolt,
    Pokomon.categoryView to dataBaseViewPokomon,
)
