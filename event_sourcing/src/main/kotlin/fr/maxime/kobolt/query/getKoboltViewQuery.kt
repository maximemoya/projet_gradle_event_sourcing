package fr.maxime.kobolt.query

import fr.maxime.kobolt.Kobolt
import fr.maxime.kobolt.kobolt_id.KoboltId
import fr.maxime.technicals.inMemoryViewsKobolt

fun getKoboltViewQuery(koboltId: KoboltId): KoboltView? =
    inMemoryViewsKobolt.getViewFromCategoryAndId(Kobolt.categoryView, koboltId)