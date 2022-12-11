package test.advent.day3

import java.io.File
import java.lang.Math.abs

val day = 3;
val file = File("src/main/resources/edition2019/day${day}/input")

data class Location(val x: Int, val y: Int) {
    fun move(direction: String): Location {
        when (direction) {
            "R" -> return Location(this.x + 1, this.y)
            "L" -> return Location(this.x - 1, this.y)
            "U" -> return Location(this.x, this.y + 1)
            "D" -> return Location(this.x, this.y - 1)
            else -> throw IllegalArgumentException("what direction?")
        }
    }

    fun getManhattenDistance() = abs(this.x) + abs(this.y)
}

data class Operation(val direction: String, val steps: Int)

fun main() {
    val (movesA, movesB) = file.readLines()
        .map { it.split(",") }
//    println(movesA)
    val locationsA = movesA
        .map { Operation(it.substring(0, 1), it.substring(1).toInt()) }
        .fold(mutableListOf<Location>(), { locations, operation -> locations.addOp(operation) })
    val locationsB = movesB
        .map { Operation(it.substring(0, 1), it.substring(1).toInt()) }
        .fold(mutableListOf<Location>(), { locations, operation -> locations.addOp(operation) })

//    println(locationsA)
//    println(locationsB)
    val crosses = locationsA.intersect(locationsB)
    println(crosses)
//    println(crosses.map { it.getManhattenDistance() }.min())
//    println(locationsA.indexOfFirst { l -> l.equals(crosses.elementAt(1)) })
    println(
        crosses
            .map { l_cross ->
                Pair(locationsA.indexOfFirst { it.equals(l_cross) } + 1,
                    locationsB.indexOfFirst { it.equals(l_cross) } + 1)
            }
            .map { it.first + it.second }
            .minOrNull() ?: throw IllegalStateException("shouldn't be null")
    )

}

private fun MutableList<Location>.addOp(operation: Operation): MutableList<Location> {
    var lastLocation = if (this.isEmpty()) Location(0, 0) else this.last()
    for (step in 1..operation.steps) {
        val newLocation = lastLocation.move(operation.direction)
        this.add(newLocation)
        lastLocation = newLocation;
    }
    return this
}

