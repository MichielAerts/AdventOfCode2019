package test.advent.edition2022.day24

import test.advent.edition2021.getOrThrow
import test.advent.edition2022.Point
import test.advent.edition2022.getDirectNeighbours
import test.advent.edition2022.getPoint
import test.advent.edition2022.to2DGridOfPointsWithLetters
import java.io.File
import java.util.*

val day = 24
val file = File("src/main/resources/edition2022/day${day}/input")

class Puzzle(private val input: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val valley = Valley.create(input)
//        valley.print()
        valley.calculateBlizzards(500)
        println(valley.findShortestPath())
    }

    fun runPart2() {
        println("Part 2 of Day $day")
        val valley = Valley.create(input)
//        valley.print()
        valley.calculateBlizzards(2000)
        val stepsFirstTrip = valley.findShortestPath()
        println(stepsFirstTrip)
        val stepsAfterSecondTrip = valley.findShortestPath(valley.originalEnd, valley.originalStart, stepsFirstTrip)
        println(stepsAfterSecondTrip)
        val stepsAfterThirdTrip = valley.findShortestPath(valley.originalStart, valley.originalEnd, stepsAfterSecondTrip)
        println(stepsAfterThirdTrip)
    }
}

private const val EMPTY = '.'
private const val WALL = '#'
private val blizzards = setOf('>', '<', '^', 'v')

data class Valley(
    val valley: List<List<ValleyPoint>>,
    val blizzards: MutableMap<Int, List<List<ValleyPoint>>> = mutableMapOf(0 to valley.copy()),
    val originalStart: Point = valley.first().first { it.value == EMPTY },
    val originalEnd: Point = valley.last().first { it.value == EMPTY }
) {

    fun print() = valley.printB()
    fun calculateBlizzards(maxTime: Int) {
        for (t in 1..maxTime) {
            val blizzardPoints = valley.flatten().filter { it.blizzards.isNotEmpty() }
            val removals = blizzardPoints.map { Pair(it, it.blizzards) }
            val additions = blizzardPoints.flatMap { point -> point.blizzards.map { Pair(point.moveTo(valley, it), it) } }
            removals.forEach { it.first.blizzards.removeAll(it.second) }
            additions.forEach { it.first.blizzards.add(it.second) }
            blizzards[t] = valley.copy()
//            println("valley after time $t")
//            valley.printB()
        }
    }

    fun findShortestPath(start: Point = originalStart, end: Point = originalEnd, startTime: Int = 0): Int {
        var currentShortestRoute = Int.MAX_VALUE
        val pq = PriorityQueue<ValleyRoute>(compareBy { it.p.getManhattanDistance(end) })
        val cache = mutableSetOf<ValleyRoute>()
        pq.add(ValleyRoute(start, startTime))
        while (!pq.isEmpty()) {
            val (point, steps) = pq.remove()
//            println("$point in $steps")
//            if (pq.size % 1000 == 0) println(pq.size)
            if (point == end) {
                currentShortestRoute = steps
                println("new path with steps: $currentShortestRoute")
//                history.forEach { println(it) }
//                println(history)
                continue
            }
            val valleyForNextStep = blizzards.getOrThrow(steps + 1)
            val options = (valleyForNextStep.getDirectNeighbours(point).neighbours + valleyForNextStep.getPoint(point.x, point.y)!!)
                .filter { it.value != WALL && (it as ValleyPoint).blizzards.isEmpty() }
            for (target in options) {
                val newSteps = steps + 1
                if (newSteps >= currentShortestRoute) {
//                    println("cancel $target, $newSteps, more than $currentShortestRoute")
                    continue
                }
                if (newSteps + target.getManhattanDistance(end) > currentShortestRoute) {
//                    println("cancel $target, $newSteps plus ${target.getManhattanDistance(end)} more than $currentShortestRoute")
                    continue
                }
                val newRoute = ValleyRoute(target, newSteps)
                if (cache.contains(newRoute)) {
//                    println("cancel $target, $newSteps, already in cache")
                    continue
                }
                cache.add(newRoute)
                pq.add(newRoute)
            }
        }
        return currentShortestRoute
    }

    companion object {
        fun create(input: List<String>) = Valley(input.to2DGridOfPointsWithLetters()
            .map { row ->
                row.map {
                    ValleyPoint(
                        it.x, it.y,
                        if (it.value == WALL) WALL else EMPTY,
                        if (it.value in blizzards) mutableListOf(it.value) else mutableListOf()
                    )
                }
            })
    }
}

data class ValleyRoute(val p: Point, val steps: Int) 

class ValleyPoint(x: Int, y: Int, value: Char, val blizzards: MutableList<Char>) : Point(x, y, value = value) {
    fun moveTo(valley: List<List<ValleyPoint>>, blizzard: Char): ValleyPoint {
        var nextPoint = when (blizzard) {
            '>' -> valley.getPoint(x + 1, y)
            '<' -> valley.getPoint(x - 1, y)
            '^' -> valley.getPoint(x, y - 1)
            'v' -> valley.getPoint(x, y + 1)
            else -> throw IllegalArgumentException()
        }!!
        if (nextPoint.value == WALL) nextPoint = when(blizzard) {
            '>' -> valley.getPoint(1, y)
            '<' -> valley.getPoint(valley[0].size - 2, y)
            '^' -> valley.getPoint(x, valley.size - 2)
            'v' -> valley.getPoint(x, 1)
            else -> throw IllegalArgumentException()
        }!!
        return nextPoint as ValleyPoint
    }
    
    override fun copy() = ValleyPoint(x, y, value, blizzards.toMutableList())

    override fun toString(): String = "Point(x: $x, y: $y, c: $value, blizzards: $blizzards)"

}

fun List<List<ValleyPoint>>.copy(): List<List<ValleyPoint>> = this.map { it.map { it.copy() }.toList() }.toList()

fun List<List<ValleyPoint>>.printB() = this.forEach { println(it.map { 
    if (it.value == WALL) WALL else if (it.blizzards.size == 0) '.' else if (it.blizzards.size == 1) it.blizzards[0] else it.blizzards.size }
    .joinToString("")) }

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

