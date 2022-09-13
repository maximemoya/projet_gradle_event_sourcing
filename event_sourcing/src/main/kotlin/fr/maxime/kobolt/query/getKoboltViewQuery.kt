package fr.maxime.kobolt.query

import fr.maxime.kobolt.Kobolt
import fr.maxime.kobolt.kobolt_id.KoboltId
import fr.maxime.technicals.dataBaseViewKobolt

fun getKoboltViewQuery(koboltId: KoboltId): KoboltView? =
    dataBaseViewKobolt.getViewFromCategoryAndId(Kobolt.categoryView, koboltId)