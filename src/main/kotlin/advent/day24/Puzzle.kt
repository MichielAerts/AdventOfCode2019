package test.advent.day24

import java.io.File
import kotlin.math.absoluteValue
import kotlin.math.pow

val day = 24;
val file = File("src/main/resources/day${day}/input")

val bioDiversityRatings = mutableSetOf<Long>()
val levels = 120
val gridSize = 5

fun main() {
    var grid = file.readLines().map { it.toList() }
    // step 1
//    while (true) {
//        grid = grid.runStep()
//        val bioDiversityRating = grid.calcBioDiversityRating()
//        if (bioDiversityRatings.contains(bioDiversityRating)) {
//            println(bioDiversityRating)
//            break
//        }
//        bioDiversityRatings.add(bioDiversityRating)
//    }

    // step 2
    var recursiveGrid = createGrid()
//    recursiveGrid.toSortedMap().forEach {
//        println("level = ${it.key}")
//        it.value.forEach { println(it.joinToString("")) }
//    }
    for (i in 0 until 200) {
        println(i)
        recursiveGrid = recursiveGrid.runStep()
//        recursiveGrid.toSortedMap().forEach {
//            println("level = ${it.key}")
//            it.value.forEach { println(it.joinToString("")) }
//        }
    }
    println("count = ${recursiveGrid.values.flatten().flatten().count { it == '#' }}")
    recursiveGrid.toSortedMap().forEach {
        println("level = ${it.key}")
        it.value.forEach { println(it.joinToString("")) }
    }
    //1924 too low
}

private fun MutableMap<Int, List<List<Char>>>.runStep(): MutableMap<Int, List<List<Char>>> {
    val newGrid = this.toMutableMap()
    for (l in keys) {
        if (l.absoluteValue == levels) {
            newGrid[l] = this[l]!!
            continue
        }
        val newLevel = MutableList(gridSize) { MutableList(gridSize) { '.' } }
        for (y in 0 until gridSize) {
            for (x in 0 until gridSize) {
                val current = get(l, x, y)
                val noOfNeighbouringBugs = getNeighbours(l, x, y).count { it == '#' }
                val new = when (current) {
                    '#' -> if (noOfNeighbouringBugs == 1) '#' else '.'
                    '.' -> if (noOfNeighbouringBugs in 1..2) '#' else current
                    else -> current
                }
                newLevel[y][x] = new
            }
        }
        newGrid[l] = newLevel
    }
    return newGrid
}

private fun createGrid(): MutableMap<Int, List<List<Char>>> {
    val recursiveGrid = mutableMapOf(0 to file.readLines().map { it.toList() })
    ((-levels..-1) + (1..levels)).forEach {
        recursiveGrid.put(
            it,
            MutableList(gridSize) { y -> MutableList(gridSize) { x -> if (x == 2 && y == 2) '?' else '.' } })
    }
    return recursiveGrid
}

private fun Map<Int, List<List<Char>>>.get(l: Int, x: Int, y: Int): Char =
    this[l]?.get(x, y) ?: throw IllegalStateException("noooo! couldn't find $l, $x, $y ")

private fun Map<Int, List<List<Char>>>.getNeighbours(l: Int, x: Int, y: Int): List<Char> {
    val neighbours = mutableListOf<Char>()
    //down in level is +1, first l then r
    when (x) {
        0 -> {
            neighbours.add(get(l - 1, 1, 2))
            neighbours.add(get(l, 1, y))
        }
        1 -> {
            neighbours.add(get(l, 0, y))
            if (y == 2) {
                neighbours.addAll(
                    listOf(
                        get(l + 1, 0, 0),
                        get(l + 1, 0, 1),
                        get(l + 1, 0, 2),
                        get(l + 1, 0, 3),
                        get(l + 1, 0, 4)
                    )
                )
            } else {
                neighbours.add((get(l, 2, y)))
            }
        }
        2 -> {
            if (y != 2) {
                neighbours.add(get(l, 1, y))
                neighbours.add(get(l, 3, y))
            }
        }
        3 -> {
            if (y == 2) {
                neighbours.addAll(
                    listOf(
                        get(l + 1, 4, 0),
                        get(l + 1, 4, 1),
                        get(l + 1, 4, 2),
                        get(l + 1, 4, 3),
                        get(l + 1, 4, 4)
                    )
                )
            } else {
                neighbours.add((get(l, 2, y)))
            }
            neighbours.add(get(l, 4, y))
        }
        4 -> {
            neighbours.add(get(l - 1, 3, 2))
            neighbours.add(get(l, 3, y))
        }
        else -> throw IllegalStateException("noooo")
    }
    // up then down
    when (y) {
        0 -> {
            neighbours.add(get(l - 1, 2, 1))
            neighbours.add(get(l, x, 1))
        }
        1 -> {
            neighbours.add(get(l, x, 0))
            if (x == 2) {
                neighbours.addAll(
                    listOf(
                        get(l + 1, 0, 0),
                        get(l + 1, 1, 0),
                        get(l + 1, 2, 0),
                        get(l + 1, 3, 0),
                        get(l + 1, 4, 0)
                    )
                )
            } else {
                neighbours.add((get(l, x, 2)))
            }
        }
        2 -> {
            if (x != 2) {
                neighbours.add(get(l, x, 1))
                neighbours.add(get(l, x, 3))
            }
        }
        3 -> {
            if (x == 2) {
                neighbours.addAll(
                    listOf(
                        get(l + 1, 0, 4),
                        get(l + 1, 1, 4),
                        get(l + 1, 2, 4),
                        get(l + 1, 3, 4),
                        get(l + 1, 4, 4)
                    )
                )
            } else {
                neighbours.add((get(l, x, 2)))
            }
            neighbours.add(get(l, x, 4))
        }
        4 -> {
            neighbours.add(get(l - 1, 2, 3))
            neighbours.add(get(l, x, 3))
        }
        else -> throw IllegalStateException("noooo")
    }
    return neighbours
}

private fun List<List<Char>>.calcBioDiversityRating(): Long =
    flatten().mapIndexed { idx, c -> if (c == '#') 2.0.pow(idx).toLong() else 0L }.sum()

private fun List<List<Char>>.runStep(): List<List<Char>> {
    val newGrid = MutableList(this.size) { MutableList(this[0].size) { '.' } }
    for (y in 0 until this.size) {
        for (x in 0 until this[0].size) {
            val current = get(x, y) ?: throw IllegalStateException("no way")
            val noOfNeighbouringBugs = getNeighbours(x, y).count { it == '#' }
            val new = when (current) {
                '#' -> if (noOfNeighbouringBugs == 1) '#' else '.'
                '.' -> if (noOfNeighbouringBugs in 1..2) '#' else current
                else -> current
            }
            newGrid[y][x] = new
        }
    }
    return newGrid
}

private fun List<List<Char>>.getNeighbours(x: Int, y: Int): List<Char> =
    listOfNotNull(get(x - 1, y), get(x + 1, y), get(x, y - 1), get(x, y + 1))


private fun List<List<Char>>.get(x: Int, y: Int): Char? =
    if (x in 0 until size && y in 0 until size) this[y][x] else null
