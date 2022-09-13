package fr.maxime.extra

import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.time.Duration
import java.time.Year

data class Film(
    val name: String,
    val duration: Duration,
    val year: Year,
)

fun OutputStream.writeCsvFilm(movies: List<Film>) {
    val writer = bufferedWriter()
    writer.write("""name, duration, year""")
    writer.newLine()
    movies.forEach {
        writer.write("${it.name}, ${it.duration.toSeconds()}, ${it.year}")
        writer.newLine()
    }
    writer.flush()
}

fun readCsvFilm(inputStream: InputStream): List<Film> {
    val reader = inputStream.bufferedReader()
    return reader.lineSequence()
        .filter { it.isNotBlank() }
        .map {
            val (name, duration, year) = it.split(',', ignoreCase = false, limit = 3)
            Film(name, Duration.ofSeconds(duration.trim().toLong()), Year.of(year.trim().toInt()))
        }.toList()
}

fun readCsv2Film(inputStream: InputStream): Map<String, Film> {
    val reader = inputStream.bufferedReader()
    val films = reader.lineSequence()
        .filter { it.isNotBlank() }
        .map {
            val (name, duration, year) = it.split(',', ignoreCase = false, limit = 3)
            Film(name, Duration.ofSeconds(duration.trim().toLong()), Year.of(year.trim().toInt()))
        }.toList()
    val hashmap = mutableMapOf<String, Film>()
    films.forEach { film -> hashmap[film.name] = film }
    return hashmap
}

// ---------
// ---------

data class User(val name: String, val password: String)

fun readListCsvUser(inputStream: InputStream): List<User> {
    val reader = inputStream.bufferedReader()
    return reader.lineSequence()
        .filter { it.isNotBlank() }
        .map {
            val (name, password) = it.split(',', ignoreCase = false, limit = 2)
            User(name, password)
        }.toList()
}

fun readHashMapCsv2User(inputStream: InputStream): Map<String, User> {
    val reader = inputStream.bufferedReader()
    val users = reader.lineSequence()
        .filter { it.isNotBlank() }
        .map {
            val (name, password) = it.split(',', ignoreCase = false, limit = 2)
            User(name, password)
        }.toList()
    val hashmap = mutableMapOf<String, User>()
    users.forEach { user -> hashmap[user.name] = user }
    return hashmap
}

fun OutputStream.writeCsvUser(users: List<User>) {
    val writer = bufferedWriter()
    writer.write("""username, password""")
    writer.newLine()
    users.forEach {
        writer.write("${it.name}, ${it.password}")
        writer.newLine()
    }
    writer.flush()
}

fun userCsvCreator(number:Int){
    val users = mutableListOf<User>()
    for (i in 0 until number){
        users.add(User("user$i","pass-user$i"))
    }
    FileOutputStream("users.csv").apply { writeCsvUser(users) }
}

fun main() {
//    val movies = readCsv2Film(File("films.csv").inputStream())
//    val movies = listOf(Film("coco", Duration.ofMinutes(120),Year.of(1999)))
//    FileOutputStream("filmsRefacto.csv").apply { writeCsvFilm(movies) }

//    val users = listOf(
//        User("Aaron1","Aaron1-pass"),
//        User("Aaron2","Aaron2-pass"),
//        User("Aaron3","Aaron3-pass"),
//    )

//    val a = User("a","s")
//    val inputStream: InputStream? =
//        a.javaClass.classLoader.getResourceAsStream("save/users.csv")

    userCsvCreator(10)
    val inputStream: InputStream? =
        User::class.java.classLoader.getResourceAsStream("save/users.csv")
    if (inputStream != null){
        val b = readListCsvUser(inputStream)
        println(b)
    }

//    val usersList = readListCsvUser(File("users.csv").inputStream())
//    val usersHashMap = readHashMapCsv2User(File("users.csv").inputStream())
//
//    println(usersList)
//    println()
//    println(usersHashMap["Aaron2"])

}