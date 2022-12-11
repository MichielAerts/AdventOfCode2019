package test

sealed class Bla {}
class Bla1 : Bla()
class Bla2 : Bla()

class Duck {
    companion object {
        fun herdDucks(no: Int) {
            repeat(no) {
                println("herding")
            }
        }
    }
}
fun main() {

    val l = mutableMapOf(1 to listOf(setOf(1, 2)))
    val newSet = setOf(2, 3)
    l.merge(2, listOf(newSet)) { old, new -> old + new }
    println(l)
//    Duck.herdDucks(5)
//    val first = { it: Double -> it * 2 }
//    val second: DoubleConv = { it * 3 }
//    val b: Bla = Bla2()
//    val x = when(b) {
//        is Bla1 -> println("bla1")
//        is Bla2 -> println("bla2")
//    }
//    runBlocking {
//        val job1 = launch {
//            delay(50)
//            println("bla")
//        }
//        val job2 = launch { println("bla2") }
//    }
//    println( combine(first, second)(3.0))
//    val catVet = Vet<Cat>()
//    val petVet = Vet<Pet>()
//
//    val petContest = Contest<Pet>(petVet)
//    petContest.addScore(Fish("A"), 10)
//    petContest.addScore(Cat("B"), 15)
//    println(petContest.getWinners())
//
//    petVet.treat(Fish("blub"))
//
//    val catContest = Contest<Cat>(petVet)
//
//    val petRetailer: Retailer<Pet> = CatRetailer()
}


class CatRetailer : Retailer<Cat> {
    override fun sell(): Cat {
        return Cat("Loekie")
    }
}

class Vet<T: Pet> {
    fun treat(t: T) {
        println("treat")
    }
}
interface Retailer<out T> {
    fun sell(): T
}

class Contest<T : Pet>(val vet: Vet<in T>) {
    val scores: MutableMap<T, Int> = mutableMapOf()

    fun addScore(contestant: T, score: Int) {
        if (score >= 0) scores.put(contestant, score)
    }
    fun getWinners(): Map<T, Int> {
        val highscore = scores.values.maxOrNull() ?: throw IllegalStateException("shouldn't be null")
        return scores.filter { it.value == highscore }
    }

}
abstract class Pet(val name: String) { }

class Dog(name: String) : Pet(name) {}
class Cat(name: String) : Pet(name) {}
class Fish(name: String) : Pet(name) {}



val rps = listOf("Rock", "Paper", "Scissors");

fun getGameChoice(): String {
    return rps.random()
}

