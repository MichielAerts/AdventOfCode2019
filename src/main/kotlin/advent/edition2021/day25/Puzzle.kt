package test.advent.edition2021.day25

import test.advent.edition2021.Point
import test.advent.edition2021.day25.Direction.EAST
import test.advent.edition2021.day25.Direction.SOUTH
import java.io.File

val day = 25;
val file = File("src/main/resources/edition2021/day${day}/input")

class Puzzle(private val rawInput: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val floor = SeaFloor.createFloor(rawInput)
        floor.print()
        var movers = 1
        var step = 1
        while (movers > 0) {
            movers = floor.runStep()
            println("after step ${step++}:, movers: $movers")
//            floor.print()
        }
    }
    
    fun runPart2() {
        println("Part 2 of Day $day")
        println(rawInput)      
    }
}

class SeaFloor(var floor: List<List<Point>>) {

    fun runStep(): Int {
        val (floorAfterEastMove, eastMovers) = runStepForHerd(EAST)
        floor = floorAfterEastMove
        val (floorAfterSouthMove, southMovers) = runStepForHerd(SOUTH)
        floor = floorAfterSouthMove
        return eastMovers + southMovers
    }

    private fun runStepForHerd(dir: Direction): Pair<List<MutableList<Point>>, Int> {
        val newFloor = emptyFloor(floor[0].size, floor.size)
        val pointsMovedInto = mutableListOf<Point>()
        for (row in floor) for (point in row) {
            val (x, y) = point
            if (point.isHerd(dir) && point.willMove(dir, floor)) {
                newFloor[y][x] = Point(x, y)
                val (xNew, yNew) = point.willMoveTo(dir, floor)
                val newPoint = Point(xNew, yNew, charValue = dir.sign)
                newFloor[yNew][xNew] = newPoint
                pointsMovedInto.add(newPoint)
            } else {
                if (point in pointsMovedInto) continue
                newFloor[y][x] = Point(x, y, charValue = point.charValue)
            }
        }
        return Pair(newFloor, pointsMovedInto.size)
    }

    fun print() {
        floor.forEach { println(it.joinToString("", transform = { it.charValue.toString() }))}
        println()
    }
    
    companion object {
        fun createFloor(input: List<String>): SeaFloor = 
            SeaFloor(input.mapIndexed { y, r -> r.mapIndexed { x, v -> Point(x, y, charValue = v) } })

        fun emptyFloor(xSize: Int, ySize: Int): List<MutableList<Point>> = 
            (0 until ySize).map { y -> (0 until xSize).map { x -> Point(x, y) }.toMutableList()}
        
    }
}

enum class Direction(val sign: Char) { EAST('>'), SOUTH('v') }

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

