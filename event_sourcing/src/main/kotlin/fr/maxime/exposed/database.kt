package fr.maxime.exposed

import fr.maxime.kobolt.kobolt_id.KoboltId
import fr.maxime.kobolt.query.KoboltView
import fr.maxime.technicals.jsonTool
import kotlinx.serialization.json.encodeToJsonElement
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.UUID

val dataBaseViews = Database.connect(
    url = "jdbc:postgresql://localhost:5432/postgres",
    driver = "org.postgresql.Driver",
    user = "test",
    password = "test"
)

object KoboltViewsTable : Table("KOBOLT_VIEWS") {
    val koboltId = text("kobolt_id")
    val name = text("name")
    val birth = timestamp("birth")
    override val primaryKey = PrimaryKey(koboltId)
}

object KoboltEventsTable : Table("KOBOLT_EVENTS") {
    val eventId = text("event_id")
    val type = text("type")
    val version = integer("version")
    val timestamp = timestamp("timestamp")
    val data = text("data")
    override val primaryKey = PrimaryKey(eventId)
}

fun testDataBase() {

    transaction(dataBaseViews) {

        SchemaUtils.create(KoboltViewsTable, KoboltEventsTable)

        val id1 = UUID.fromString("788b38a9-bc84-490a-9fa6-b431882b9914")
        val id2 = UUID.fromString("b47f27f8-8628-4340-a329-f6688fcdddbb")
//        KoboltViewsTable.insertIgnore {
//            it[koboltId] = id1.toString()
//            it[name] = "Kobolt1"
//            it[birth] = Instant.now()
//        }
//        KoboltViewsTable.insertIgnore {
//            it[koboltId] = id2.toString()
//            it[name] = "Kobolt2"
//            it[birth] = Instant.now()
//        }
//
//        KoboltViewsTable.update({ KoboltViewsTable.koboltId eq id1.toString() }) {
//            it[name] = "Kbx001"
//            it[birth] = Instant.now()
//        }

        KoboltEventsTable.insertIgnore {
            it[type] = "creation"
            it[version] = 1
            it[data] = jsonTool.encodeToJsonElement(KoboltView(KoboltId(id2),"Kobolt1", Instant.now())).toString()
            it[eventId] = id1.toString()
            it[timestamp] = Instant.now()
        }

    }

}
