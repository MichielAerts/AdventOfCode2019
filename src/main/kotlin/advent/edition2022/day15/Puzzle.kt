package test.advent.edition2022.day15

import test.advent.edition2022.Pos
import java.io.File
import kotlin.math.absoluteValue

val day = 15
val file = File("src/main/resources/edition2022/day${day}/input")

class Puzzle(private val input: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val sensors = input.map { Sensor.create(it) }
        val y = 10
        println(
            (sensors.map { it.pointsBlockedAtY(y) }.pointsOutsideRange(-4, 26))
        )
    }
    
    fun runPart2() {
        println("Part 2 of Day $day")
        val sensors = input.map { Sensor.create(it) }
        val min = 0
//        val max = 20
        val max = 4000000
        for (y in min .. max) {
            if (y % (max / 100) == 0) println(y)
            // at every y, calculate intranges of blocked points, then check if there are no holes in it in the range
            sensors.map { it.pointsBlockedAtY(y) }.pointsOutsideRange(min, max).toSet().firstOrNull()?.let {
                println("$y, $it")
                println(max * it.toLong() + y)
            }
        }
    }
}

private fun List<IntRange>.pointsOutsideRange(start: Int, end: Int): List<Int> {
    val ranges = this.filter { !it.isEmpty() }
    val corners = ranges.flatMap {
        listOf(
            it.start - 1,
            it.start,
            it.start + 1,
            it.endInclusive - 1,
            it.endInclusive,
            it.endInclusive + 1
        )
    }
    return corners.filter { point -> ranges.none { it.contains(point) } && point >= start && point <= end }
}

data class Sensor(val pos: Pos, val closestBeacon: Beacon, val distance: Int = pos.getManhattanDistance(closestBeacon.pos)) {
    fun pointsBlockedAtY(y: Int): IntRange {
        val start = pos.x - distance + (pos.y - y).absoluteValue
        val end = pos.x + distance - (pos.y - y).absoluteValue
        return if (start <= end) IntRange(start, end) else IntRange.EMPTY
    }

    companion object {
        private val regex = """Sensor at x=(-?\d+), y=(-?\d+): closest beacon is at x=(-?\d+), y=(-?\d+)""".toRegex()
        fun create(input: String): Sensor {
            val (_, xs, ys, xb, yb) = regex.find(input)?.groupValues ?: throw IllegalArgumentException()
            return Sensor(Pos(xs, ys), Beacon(Pos(xb, yb)))
        }
    }
}

data class Beacon(val pos: Pos)

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
//    puzzle.runPart1()
    puzzle.runPart2()
}

