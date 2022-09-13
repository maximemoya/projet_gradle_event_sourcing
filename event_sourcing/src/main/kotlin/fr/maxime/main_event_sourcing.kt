package fr.maxime

import fr.maxime.kobolt.Kobolt
import fr.maxime.kobolt.command.createKoboltCommand
import fr.maxime.kobolt.command.rebirthKoboltCommand
import fr.maxime.kobolt.command.renameKoboltCommand
import fr.maxime.kobolt.koboltEventReader
import fr.maxime.kobolt.kobolt_id.KoboltId
import fr.maxime.kobolt.kobolt_id.KoboltIdNullableSerializer
import fr.maxime.kobolt.kobolt_id.KoboltIdSerializer
import fr.maxime.kobolt.query.KoboltView
import fr.maxime.kobolt.query.getKoboltViewQuery
import fr.maxime.pokomon.command.createPokomonCommand
import fr.maxime.pokomon.command.rebirthPokomonCommand
import fr.maxime.pokomon.command.renamePokomonCommand
import fr.maxime.pokomon.pokomon_id.PokomonId
import fr.maxime.technicals.InstantSerializer
import fr.maxime.technicals.dataBaseEventKobolt
import fr.maxime.technicals.dataBaseViewKobolt
import fr.maxime.technicals.getViewsFromCategory
import fr.maxime.technicals.jsonTool
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.encodeToJsonElement
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Method.PUT
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.ApacheServer
import org.http4k.server.asServer
import java.time.Instant
import java.util.UUID

fun main() {

    scenarioKoboltA()
//    scenarioKoboltB()
//    scenarioPokomonA()

//    scenarioHttp4K()

}

fun scenarioKoboltA() {
    val fakeId = UUID.randomUUID()
    val fakeKoboltId = KoboltId(fakeId)
    renameKoboltCommand(fakeKoboltId, "kbw32")
    koboltEventReader(fakeKoboltId)

    val kobolt1Id = KoboltId(UUID.randomUUID())
    createKoboltCommand(
        koboltId = kobolt1Id,
        name = "kb1",
        birth = Instant.parse("1950-01-03T08:00:00Z")
    )
    koboltEventReader(kobolt1Id)
    renameKoboltCommand(kobolt1Id, "kbw460")
    koboltEventReader(kobolt1Id)
    rebirthKoboltCommand(kobolt1Id, Instant.parse("1956-07-08T09:00:00Z"))
    koboltEventReader(kobolt1Id)

    val kobolt2Id = KoboltId(UUID.randomUUID())
    createKoboltCommand(
        koboltId = kobolt2Id,
        name = "kb2",
        birth = Instant.parse("1960-01-03T08:00:00Z")
    )
    koboltEventReader(kobolt2Id)
    renameKoboltCommand(kobolt2Id, "kb00x32")

    println(
        "\n-------" +
                "\nQUERIES" +
                "\n-------" +
                "\nGET ALL KOBOLT VIEWS"
    )

    val views = dataBaseViewKobolt.getViewsFromCategory<KoboltView>(Kobolt.categoryView)
    val jsonViews = views.map { jsonTool.encodeToJsonElement(it) }
    println(jsonViews)

    println("\nGET VIEW FROM KoboltID: ${views[0].koboltId}")
    println(getKoboltViewQuery(views[0].koboltId))

}

fun scenarioPokomonA() {

    val pokomonId = PokomonId("poko1", UUID.randomUUID())
    createPokomonCommand(
        pokomonId = pokomonId,
        name = "POKO1",
        birth = Instant.parse("1950-01-03T08:00:00Z")
    )
    val pokomon2Id = PokomonId("poko2", UUID.randomUUID())
    createPokomonCommand(
        pokomonId = pokomon2Id,
        name = "POKO2",
        birth = Instant.parse("1960-01-03T08:00:00Z")
    )
    renamePokomonCommand(pokomon2Id, "NewPoko2")
    rebirthPokomonCommand(pokomon2Id, Instant.now())
    renamePokomonCommand(pokomon2Id, "NewPoko2")

}

@Serializable
data class KoboltCreateDto(
    @Serializable(with = KoboltIdSerializer::class)
    val koboltId: KoboltId = KoboltId(UUID.randomUUID()),
    val name: String = "kobolt",
    @Serializable(with = InstantSerializer::class)
    val birth: Instant = Instant.now(),
)

fun scenarioKoboltB() {
    var txtInput = ""
    while (txtInput != "exit") {

        println("enter your command: ")
        txtInput = readln()

        val split = txtInput.trim().split(" ")
        if (split.size == 3) {

            if (split[0].lowercase() == "post") {
                if (split[1].lowercase() == "kobolt") {
                    try {
                        val koboltCreatedData =
                            jsonTool.decodeFromString<KoboltCreateDto>(split[2])
                        createKoboltCommand(koboltCreatedData.koboltId, koboltCreatedData.name, koboltCreatedData.birth)
                        println("Aggregate Kobolt created, KoboltId: " + koboltCreatedData.koboltId)

                    } catch (_: Exception) {
                        println("can not decode 'KoboltCreateDto' from: " + split[2])
                    }
                } else {
                    println("wrong url => <${split[1]}>?")
                }
            } else {
                println("wrong command => <${split[0]}>? {post/put/get/delete}")
            }
        } else {
            println("wrong command size => <command> <url> <body?>")
        }

    }
}

@Serializable
data class KoboltPostDto(
    val name: String = "kobolt",
    @Serializable(with = InstantSerializer::class)
    val birth: Instant = Instant.now(),
)

@Serializable
data class KoboltPutDto(
    @Serializable(with = KoboltIdSerializer::class)
    val koboltId: KoboltId,
    val name: String? = null,
    @Serializable(with = InstantSerializer::class)
    val birth: Instant? = null,
)

@Serializable
data class KoboltGetDto(
    @Serializable(with = KoboltIdNullableSerializer::class)
    val koboltId: KoboltId? = null,
)

fun scenarioHttp4K() {

    val koboltPostLens = Body.auto<KoboltPostDto>().toLens()
    val koboltPutLens = Body.auto<KoboltPutDto>().toLens()
    val koboltGetLens = Body.auto<KoboltGetDto>().toLens()

    val httpHandler: HttpHandler = routes(

        "/hello" bind GET to { request ->
            val nameParam = request.queries("name")
            var namesTxt = ""
            nameParam.forEach { name -> namesTxt += "$name " }
            if (namesTxt.isEmpty()) {
                Response(OK).body("hello kobolt")
            } else {
                Response(OK).body("hello ${namesTxt.trim()}")
            }
        },

        "/kobolt" bind POST to { request ->
            val body = koboltPostLens(request)
            val id = createKoboltCommand(KoboltId(UUID.randomUUID()), body.name, body.birth)
            Response(OK).body("kobolt successfully created: $id")
        },

        "/kobolt" bind PUT to { request ->
            val body = koboltPutLens(request)
            val oldKobolt = Kobolt.invoke(body.koboltId)
            if (oldKobolt != null) {
                if (body.name != null) {
                    val oldName = oldKobolt.name
                    if (oldName != body.name) {
                        renameKoboltCommand(body.koboltId, body.name)
                    }
                }
                if (body.birth != null) {
                    val oldBirth = oldKobolt.birth
                    if (oldBirth != body.birth) {
                        rebirthKoboltCommand(body.koboltId, body.birth)
                    }
                }
                Response(OK).body("modify successfully kobolt: ${body.koboltId}")
            } else {
                Response(NOT_FOUND).body("can not find kobolt: ${body.koboltId}")
            }
        },

        "/kobolt" bind GET to { request ->
            val body = koboltGetLens(request)
            if (body.koboltId == null) {
                val views = getViewsFromCategory<KoboltView>(Kobolt.categoryView)
                val viewsJson = views.map { jsonTool.encodeToJsonElement(it) }
                Response(OK).body(viewsJson.toString())
            } else {
                val view =
                    dataBaseViewKobolt.getViewFromCategoryAndId<KoboltView>(Kobolt.categoryView, body.koboltId)
                if (view != null) {
                    val viewJson = jsonTool.encodeToJsonElement(view)
                    Response(OK).body(viewJson.toString())
                } else {
                    Response(NOT_FOUND).body("can not find kobolt: ${body.koboltId}")
                }
            }
        },

        "/kobolt" bind DELETE to { request ->
            val body = koboltGetLens(request)
            if (body.koboltId == null) {
                Response(BAD_REQUEST).body("no kobolt to delete")
            } else {
                if (
                    dataBaseViewKobolt.deleteViewFromCategoryAndId(Kobolt.categoryView, body.koboltId)
                    && dataBaseEventKobolt.deleteEvents(Kobolt.categoryEvent, body.koboltId)
                ) {
                    Response(OK).body("delete successfully kobolt: ${body.koboltId}")
                } else {
                    Response(OK).body("can not find kobolt: ${body.koboltId}")
                }
            }
        },

        )
    httpHandler.asServer(ApacheServer(port = 8080)).start()

//    val httpClient: HttpHandler = OkHttp()
//    val response = httpClient(Request(GET, "http://localhost:8080/hello"))
//    println(response.status)

}
