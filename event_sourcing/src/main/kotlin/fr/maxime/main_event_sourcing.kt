package fr.maxime

import fr.maxime.adapters.excel_adapter.excel_file_reader.kobolts.ExcelKoboltCreationDto
import fr.maxime.adapters.excel_adapter.excel_file_reader.kobolts.excelKoboltCreationColumns
import fr.maxime.adapters.excel_adapter.excel_file_reader.technicals.readExcelFileFromXlsxInputStream
import fr.maxime.exposed.KoboltViewsTable
import fr.maxime.exposed.dataBaseViews
import fr.maxime.kobolt.Kobolt
import fr.maxime.kobolt.command.createKoboltCommand
import fr.maxime.kobolt.command.rebirthKoboltCommand
import fr.maxime.kobolt.command.renameKoboltCommand
import fr.maxime.kobolt.koboltEventReader
import fr.maxime.kobolt.kobolt_id.KoboltId
import fr.maxime.kobolt.kobolt_id.KoboltIdSerializer
import fr.maxime.kobolt.query.KoboltView
import fr.maxime.kobolt.query.getKoboltViewQuery
import fr.maxime.pokomon.command.createPokomonCommand
import fr.maxime.pokomon.command.rebirthPokomonCommand
import fr.maxime.pokomon.command.renamePokomonCommand
import fr.maxime.pokomon.pokomon_id.PokomonId
import fr.maxime.technicals.Event
import fr.maxime.technicals.InstantSerializer
import fr.maxime.technicals.dataBaseEventKobolt
import fr.maxime.technicals.inMemoryViewsKobolt
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
import org.http4k.lens.MultipartFormField
import org.http4k.lens.MultipartFormFile
import org.http4k.lens.Validator
import org.http4k.lens.multipartForm
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.ApacheServer
import org.http4k.server.asServer
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.UUID

fun main() {

//    scenarioKoboltA()
//    scenarioPokomonA()
//    scenarioKoboltB()

    scenarioHttp4K()

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

    val views = inMemoryViewsKobolt.getViewsFromCategory<KoboltView>(Kobolt.categoryView)
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

// --------
// ROUTES :
// --------

val fileInputStream = MultipartFormFile.required("inputstream")
val universeField = MultipartFormField.string().map { value -> KoboltId(value) }.optional("koboltId")
val exelFormBody = Body.multipartForm(Validator.Strict, fileInputStream, universeField).toLens()

data class KoboltPostDto(
    val name: String = "kobolt",
//    @Serializable(with = InstantSerializer::class)
    val birth: Instant = Instant.now(),
)

data class KoboltPutDto(
//    @Serializable(with = KoboltIdSerializer::class)
    private val koboltId: String,
    val name: String? = null,
//    @Serializable(with = InstantSerializer::class)
    val birth: Instant? = null,
) {
    fun getKoboltId(): KoboltId {
        return KoboltId(koboltId)
    }
}

data class KoboltGetDto(
    private val koboltId: String? = null,
) {
    fun getKoboltId(): KoboltId? {
        return if (koboltId != null) {
            KoboltId(koboltId)
        } else null
    }
}

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
            val koboltId = createKoboltCommand(KoboltId(UUID.randomUUID()), body.name, body.birth)
            Response(OK).body("kobolt successfully created: $koboltId")
        },

        "/kobolt" bind PUT to { request ->
            val body = koboltPutLens(request)
            val oldKobolt = Kobolt.invoke(body.getKoboltId())
            if (oldKobolt != null) {
                if (body.name != null) {
                    val oldName = oldKobolt.name
                    if (oldName != body.name) {
                        renameKoboltCommand(body.getKoboltId(), body.name)
                    }
                }
                if (body.birth != null) {
                    val oldBirth = oldKobolt.birth
                    if (oldBirth != body.birth) {
                        rebirthKoboltCommand(body.getKoboltId(), body.birth)
                    }
                }
                Response(OK).body("modify successfully koboltId: ${body.getKoboltId()}")
            } else {
                Response(NOT_FOUND).body("can not find koboltId: ${body.getKoboltId()}")
            }
        },

        "/kobolt" bind GET to { request ->
            val body = koboltGetLens(request)
            val koboltId = body.getKoboltId()
            if (koboltId == null) {

                // ----------
                // DataBase :
                // ----------

                transaction(dataBaseViews) {
                    val query = KoboltViewsTable.selectAll()
                    val results = query.map {
                        KoboltView(
                            KoboltId(it[KoboltViewsTable.koboltId]),
                            it[KoboltViewsTable.name],
                            it[KoboltViewsTable.birth]
                        )
                    }
                    val viewJson = jsonTool.encodeToJsonElement(results)
                    Response(OK).body(viewJson.toString())
                }

                // ----------
                // InMemory :
                // ----------

//                val views = inMemoryViewsKobolt.getViewsFromCategory<KoboltView>(Kobolt.categoryView)
//                val viewsJson = views.map { jsonTool.encodeToJsonElement(it) }
//                Response(OK).body(viewsJson.toString())

            } else {

                // ----------
                // DataBase :
                // ----------

                transaction(dataBaseViews) {
                    val query = KoboltViewsTable.select { KoboltViewsTable.koboltId eq koboltId.streamId }
                    val results = query.map {
                        KoboltView(
                            KoboltId(it[KoboltViewsTable.koboltId]),
                            it[KoboltViewsTable.name],
                            it[KoboltViewsTable.birth]
                        )
                    }
                    if (results.isNotEmpty()) {
                        val viewJson = jsonTool.encodeToJsonElement(results[0])
                        Response(OK).body(viewJson.toString())
                    } else {
                        Response(NOT_FOUND).body("can not find koboltId: $koboltId")
                    }
                }

                // ----------
                // InMemory :
                // ----------

//                val view =
//                    inMemoryViewsKobolt.getViewFromCategoryAndId<KoboltView>(
//                        Kobolt.categoryView,
//                        koboltId
//                    )
//                if (view != null) {
//                    val viewJson = jsonTool.encodeToJsonElement(view)
//                    Response(OK).body(viewJson.toString())
//                } else {
//                    Response(NOT_FOUND).body("can not find koboltId: $koboltId")
//                }
            }
        },

        "/kobolt" bind DELETE to { request ->
            val body = koboltGetLens(request)
            val koboltId = body.getKoboltId()
            if (koboltId == null) {
                Response(BAD_REQUEST).body("no kobolt to delete")
            } else {

                // ----------
                // DataBase :
                // ----------

                transaction(dataBaseViews) {
                    KoboltViewsTable.deleteWhere { KoboltViewsTable.koboltId eq koboltId.streamId }
                }

                // ----------
                // InMemory :
                // ----------

                if (dataBaseEventKobolt.exist(Kobolt.categoryEvent, koboltId)) {

                    // todo: 13/09/2022 improve delete EVENT

                    val kobolt = Kobolt.invoke(koboltId)
                    if (kobolt != null) {
                        dataBaseEventKobolt.addEvent(
                            Kobolt.categoryEvent,
                            koboltId,
                            Event("aggregate_deleted", 1)
                        )
                    }
                }
                if (
                    inMemoryViewsKobolt.deleteViewFromCategoryAndId(Kobolt.categoryView, koboltId)
                ) {
                    Response(OK).body("delete successfully koboltId: $koboltId")
                } else {
                    Response(OK).body("can not find koboltId: $koboltId")
                }
            }
        },

        "/kobolt/excel" bind POST to { request ->

            val receivedForm = exelFormBody(request)

            val koboltIdParam = universeField(receivedForm)
            if (koboltIdParam != null) {
                println("test koboltId = $koboltIdParam")
            }

            val excelReaderResult = readExcelFileFromXlsxInputStream<ExcelKoboltCreationDto>(
                fileInputStream(receivedForm).content,
                excelKoboltCreationColumns,
            )

            excelReaderResult.results.forEach { println(it) }
            println("--- ERRORS ---")
            excelReaderResult.errors.forEach { println(it) }
            println()

            val koboltsIds =
                excelReaderResult.results.map { result ->
                    createKoboltCommand(KoboltId(UUID.randomUUID()), result.name, Instant.parse(result.birth))
                }

            Response(OK).body("kobolt successfully created: (${koboltsIds.size})\n$koboltsIds" +
                    "\n\nErrors logged:" +
                    "\n${excelReaderResult.errors}")
        }

    )
    httpHandler.asServer(ApacheServer(port = 8080)).start()

//    val httpClient: HttpHandler = OkHttp()
//    val response = httpClient(Request(GET, "http://localhost:8080/hello"))
//    println(response.status)

}
