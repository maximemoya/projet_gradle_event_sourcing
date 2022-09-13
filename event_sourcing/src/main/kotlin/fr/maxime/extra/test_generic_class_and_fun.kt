package fr.maxime.extra

data class FireOut<out T>(
    val var1: T
) {

    fun getOut():T{
        return var1
    }

}

data class FireIn<in T>(
    val var1: String
) {
    fun printIn(i: T) {
        println(i)
    }
}

fun main() {
    val fin = FireIn<Int>("Bob")
    val fout = FireOut("Bob")

    fin.printIn(42)

}