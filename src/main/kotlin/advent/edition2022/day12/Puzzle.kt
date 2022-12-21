package test.advent.edition2022.day12

import test.advent.edition2021.getOrThrow
import test.advent.edition2022.*
import java.io.File
import java.util.*

val day = 12
val file = File("src/main/resources/edition2022/day${day}/input")

class Puzzle(private val input: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val area = Area.create(input)
        println(area.findShortestRoute(area.start))
    }

    fun runPart2() {
        println("Part 2 of Day $day")
        val area = Area.create(input)
        area.findShortestRouteFromAnyA()
        println(area.currentShortestRoute)
    }
}

data class Area(
    val map: List<List<Point>>,
    val start: Point = map.findSingleValueInGrid('S'),
    val end: Point = map.findSingleValueInGrid('E'),
    var currentShortestRoute: Int = Int.MAX_VALUE,
) {
    fun findShortestRouteFromAnyA(): Int {
        val aas = map.findAllValuesInGrid('a')
        aas.map { findShortestRoute(it) }
        return currentShortestRoute 
    }

    fun findShortestRoute(startingPoint: Point): Int {
        val pq = PriorityQueue<Route>(compareBy { it.length })
        val shortestRouteLengthPerPointMap = map.flatten().associateWith { Int.MAX_VALUE }
            .toMutableMap()
        pq.add(Route(startingPoint, 0))
        while (!pq.isEmpty()) {
            val (point, length) = pq.remove()
            if (point == end) {
                currentShortestRoute = length
                println("new path with length: $currentShortestRoute")

            }
            for (neighbour in map.getDirectNeighbours(point).neighbours) {
                val pV = if (point.value == 'S') 'a' else point.value
                val nV = if (neighbour.value == 'E') 'z' else neighbour.value
                if (nV - pV > 1) continue

                val newLength = length + 1
                if (newLength > currentShortestRoute) continue

                val shortestRouteForPoint = shortestRouteLengthPerPointMap.getOrThrow(neighbour)
                if (newLength >= shortestRouteForPoint) continue
                shortestRouteLengthPerPointMap[neighbour] = newLength
//                println("new path to $neighbour with length: $newLength")
                pq.add(Route(neighbour, newLength))
            }
        }
        return currentShortestRoute
    }

    companion object {
        fun create(input: List<String>) = Area(input.to2DGridOfPointsWithLetters())
    }
}

class Route(val p: Point, val length: Int) {
    operator fun component1() = p
    operator fun component2() = length
}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

