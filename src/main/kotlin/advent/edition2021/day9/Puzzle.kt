package test.advent.edition2021.day9

import test.advent.edition2021.Point
import test.advent.edition2021.getDirectNeighbours
import java.io.File

val day = 9;
val file = File("src/main/resources/edition2021/day${day}/input")

class Puzzle(private val rawInput: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val input = rawInput
        println(HeatMap.createHeatMap(input).getLowestPoints().sumOf { it.getRiskLevel() })
    }
    
    fun runPart2() {
        println("Part 2 of Day $day")
        val input = rawInput
        val heatMap = HeatMap.createHeatMap(input)
        val lowestPoints = heatMap.getLowestPoints()
        println(lowestPoints.map { heatMap.getBasin(it) }
            .sortedByDescending { it.pointsInBasin.size }.take(3)
            .map { it.pointsInBasin.size }.reduce(Int::times))
    }
}

data class HeatMap(val points : List<List<Point>>) {
    fun getLowestPoints(): List<Point> = points.flatten()
        .map { p -> points.getDirectNeighbours(p) }
        .filter { pointAndNeighbours -> pointAndNeighbours.neighbours.all { it.z > pointAndNeighbours.point.z  } }
        .map { it.point }

    fun getBasin(lowestPoint: Point) : Basin {
        val basin = Basin(lowestPoint, mutableSetOf(lowestPoint))
        while (!basin.complete) {
            var stillAdding = false
            val currentPoints = basin.pointsInBasin.toList()
            for (point in currentPoints) {
                val lowNeighbours = points.getDirectNeighbours(point).neighbours.filter { it.z < 9 }
                val added = basin.pointsInBasin.addAll(lowNeighbours)
                if (added) stillAdding = true
            }
            if (!stillAdding) basin.complete = true
        }
        return basin
    }

    companion object {
        fun createHeatMap(input: List<String>) : HeatMap =
            HeatMap(input.mapIndexed { y, r -> r.toList().mapIndexed { x, v -> Point(x, y, Character.getNumericValue(v)) } })
    }
}

data class Basin(val lowestPoint: Point, val pointsInBasin: MutableSet<Point>, var complete: Boolean = false)

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

