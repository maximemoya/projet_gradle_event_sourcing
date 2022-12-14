package fr.maxime.exposed

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.countDistinct
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.stringLiteral
import org.jetbrains.exposed.sql.substring
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.trim
import org.jetbrains.exposed.sql.update
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

object StarWarsFilmsTable : Table("STAR_WARS_FILMS") {
    val name = text("name").nullable()
    override val primaryKey = PrimaryKey(name)
}

// COLUMNS
object StarWarsFilms_Simple : Table() {
    val id = integer("id").autoIncrement()
    val sequelId = integer("sequel_id").uniqueIndex()
    val name = varchar("name", 50)
    val director = varchar("director", 50)
    override val primaryKey = PrimaryKey(id, name = "PK_StarWarsFilms_Id")
}

// PRIMARY KEY
object StarWarsFilms : IntIdTable() {
    val sequelId = integer("sequel_id").uniqueIndex()
    val name = varchar("name", 50)
    val director = varchar("director", 50)
}

// FOREIGN KEYS
object Players : Table() {
    val sequelId = integer("sequel_id")
        .uniqueIndex()
        .references(StarWarsFilms.sequelId)

    //or
//    val sequelId = reference("sequel_id", StarWarsFilms.sequelId).uniqueIndex()

    val name = varchar("name", 50)
}

fun testExposed() {

//    Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver", user = "myself", password = "secret")
//    Database.connect({ DriverManager.getConnection("jdbc:h2:mem:test;MODE=MySQL") })

//    val dataSource: DataSource by lazy {
//        HikariDataSource(
//            HikariConfig().also { config ->
//                config.jdbcUrl = "jdbc:postgresql://$host:$port/$database"
//                config.username = user
//                config.password = password
//                config.connectionTimeout = 10_000 // 10 seconds
//            }
//        )
//    }
//    POSTGRES_PASSWORD
//    POSTGRES_USER

    val db1 = Database.connect("jdbc:postgresql://localhost:5432/postgres", driver = "org.postgresql.Driver", user = "test", password = "test")

    transaction(db1) {

        // LOGIN
//        addLogger(StdOutSqlLogger)

        // Creating Tables
        SchemaUtils.create(StarWarsFilmsTable, Players)
        StarWarsFilmsTable.insertIgnore {
            it[name] = "The jedi knight"
        }
        StarWarsFilmsTable.insertIgnore {
            it[name] = "The jedi knight2"
        }
        StarWarsFilmsTable.insertIgnore {
            it[name] = "The jedi knight"
        }

        commit()

        val query = StarWarsFilms.selectAll()
        query.forEach {
            assertTrue { it[StarWarsFilms.sequelId] >= 7 }
        }

        StarWarsFilms.slice(StarWarsFilms.name, StarWarsFilms.director)
            .selectAll()
            .forEach {
                assertTrue { it[StarWarsFilms.name].startsWith("The") }
            }

        StarWarsFilms.slice(StarWarsFilms.name.countDistinct())

    }


}

// --------------
// EXAMPLE GITHUB
// --------------

object Users : Table() {
    val id = varchar("id", 10) // Column<String>
    val name = varchar("name", length = 50) // Column<String>
    val cityId = (integer("city_id") references Cities.id).nullable() // Column<Int?>

    override val primaryKey = PrimaryKey(id, name = "PK_User_ID") // name is optional here
}

object Cities : Table() {
    val id = integer("id").autoIncrement() // Column<Int>
    val name = varchar("name", 50) // Column<String>

    override val primaryKey = PrimaryKey(id, name = "PK_Cities_ID")
}

fun main() {
    //jdbc:h2:tcp://localhost:9092/default
    Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver", user = "root", password = "")

    transaction {
//        addLogger(StdOutSqlLogger)

        SchemaUtils.create (Cities, Users)

        val saintPetersburgId = Cities.insert {
            it[name] = "St. Petersburg"
        } get Cities.id

        val munichId = Cities.insert {
            it[name] = "Munich"
        } get Cities.id

        val pragueId = Cities.insert {
            it.update(name, stringLiteral("   Prague   ").trim().substring(1, 2))
        }[Cities.id]

        val pragueName = Cities.select { Cities.id eq pragueId }.single()[Cities.name]
        assertEquals(pragueName, "Pr")

        Users.insert {
            it[id] = "andrey"
            it[name] = "Andrey"
            it[cityId] = saintPetersburgId
        }

        Users.insert {
            it[id] = "sergey"
            it[name] = "Sergey"
            it[cityId] = munichId
        }

        Users.insert {
            it[id] = "eugene"
            it[name] = "Eugene"
            it[cityId] = munichId
        }

        Users.insert {
            it[id] = "alex"
            it[name] = "Alex"
            it[cityId] = null
        }

        Users.insert {
            it[id] = "smth"
            it[name] = "Something"
            it[cityId] = null
        }

        Users.update({ Users.id eq "alex"}) {
            it[name] = "Alexey"
        }

        Users.deleteWhere{ Users.name like "%thing"}

        println("All cities:")

        for (city in Cities.selectAll()) {
            println("${city[Cities.id]}: ${city[Cities.name]}")
        }

        println("Manual join:")
        (Users innerJoin Cities).slice(Users.name, Cities.name).
        select {(Users.id.eq("andrey") or Users.name.eq("Sergey")) and
                Users.id.eq("sergey") and Users.cityId.eq(Cities.id)}.forEach {
            println("${it[Users.name]} lives in ${it[Cities.name]}")
        }

        println("Join with foreign key:")


        (Users innerJoin Cities).slice(Users.name, Users.cityId, Cities.name).
        select { Cities.name.eq("St. Petersburg") or Users.cityId.isNull()}.forEach {
            if (it[Users.cityId] != null) {
                println("${it[Users.name]} lives in ${it[Cities.name]}")
            }
            else {
                println("${it[Users.name]} lives nowhere")
            }
        }

        println("Functions and group by:")

        ((Cities innerJoin Users).slice(Cities.name, Users.id.count()).selectAll().groupBy(Cities.name)).forEach {
            val cityName = it[Cities.name]
            val userCount = it[Users.id.count()]

            if (userCount > 0) {
                println("$userCount user(s) live(s) in $cityName")
            } else {
                println("Nobody lives in $cityName")
            }
        }

//        SchemaUtils.drop (Users, Cities)
    }
}