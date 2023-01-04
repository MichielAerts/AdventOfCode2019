package test.advent.edition2022.day23

import test.advent.edition2022.*
import test.advent.edition2022.WindDirection.*
import java.io.File

val day = 23
val file = File("src/main/resources/edition2022/day${day}/input")

class Puzzle(private val input: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val rounds = 10
        val grove = Grove.create(input, rounds + 2)
        grove.grid.printV()
        for (round in 1..rounds) {
            grove.moveElves()
            println("after round $round, ${grove.currentMovers} movers")
            grove.grid.printV()
        }
        println(grove.countEmptyTilesInSmallestElfSquare())
    }

    fun runPart2() {
        println("Part 2 of Day $day")
        var round = 0
        val grove = Grove.create(input, 100)
//        grove.grid.printV()
        while(grove.currentMovers > 0) {
            grove.moveElves()
            println("${grove.currentMovers} movers")
            round++
//            grove.grid.printV()
        }
        println(round)
        grove.grid.printV()
//        println(grove.countEmptyTilesInSmallestElfSquare())
    }
}

private const val EMPTY = '.'
private const val ELF = '#'

data class Move(val source: Point, val target: Point)
data class Grove(
    var grid: List<List<Point>>,
    var directions: List<WindDirection> = listOf(N, S, W, E),
    var currentMovers: Int = 1
) {

    fun moveElves() {
        val moves = getElves().map { Move(it, getMove(it)) }
        val uniqueTargets = moves.groupingBy { it.target }.eachCount().filter { it.value == 1 }
        val actualMoves = moves.filter { it.source != it.target }.filter { it.target in uniqueTargets }
        currentMovers = actualMoves.size
        actualMoves.forEach { 
            grid.changePoint(it.source, EMPTY)
            grid.changePoint(it.target, ELF)
        }
        directions = directions.subListTillEnd(1) + directions[0]
    }

    private fun getMove(point: Point): Point {
        val surroundingPoints = grid.getSurroundingPoints(point)
        if (surroundingPoints.all { it.value.value == EMPTY }) return point
        val windDirection = directions.firstOrNull { d ->
            surroundingPoints
                .filter { it.key in WindDirection.getXwards(d) }.all { it.value.value == EMPTY }
        }
        return windDirection?.let { surroundingPoints.getOrThrow(it) } ?: point
    }

    fun countEmptyTilesInSmallestElfSquare(): Int {
        val elves = getElves()
        val minX = elves.minOf { it.x }
        val maxX = elves.maxOf { it.x }
        val minY = elves.minOf { it.y }
        val maxY = elves.maxOf { it.y }
        return grid.getSquare(minX, maxX, minY, maxY).flatten().count { it.value == EMPTY }
    }

    private fun getElves() = grid.flatten().filter { it.value == ELF }

    companion object {
        fun create(input: List<String>, margin: Int): Grove {
            val grid = initEmptyGrid(endX = 2 * margin + input[0].length, endY = 2 * margin + input.size)
            grid.changePoints(input.to2DGridOfPointsWithLetters()
                .flatten()
                .filter { it.value == ELF }
                .map { Point(it.x + margin, it.y + margin, value = it.value) }
                .toSet(), ELF)
            return Grove(grid)
        }
    }
}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

