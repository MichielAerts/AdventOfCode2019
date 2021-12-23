package test.advent.edition2021.day5

import java.io.File

val day = 5;
val file = File("src/main/resources/edition2021/day${day}/input")

class Puzzle(private val rawInput: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val input = rawInput.map { Line.createLine(it) }
        println(input.filter { it.isVertical() || it.isHorizontal() }
            .flatMap { it.getPoints() }.groupingBy { it }.eachCount().filter { it.value >= 2 }.size)
        
    }
    
    fun runPart2() {
        println("Part 2 of Day $day")
        val input = rawInput.map { Line.createLine(it) }
        println(input.flatMap { it.getPoints() }.groupingBy { it }.eachCount().filter { it.value >= 2 }.size)

    }
}
data class Coordinate(val x: Int, val y: Int) {
    constructor(xs: String, ys: String): this(xs.toInt(), ys.toInt())
}

data class Line(val start: Coordinate, val end: Coordinate) {
    
    fun isHorizontal() = start.y == end.y
    fun isVertical() = start.x == end.x
    private fun isToTheRight() = start.x < end.x
    private fun isToTheLeft() = start.x >= end.x
    private fun isUpwards() = start.y < end.y
    private fun isDownwards() = start.y >= end.y
    
    fun getPoints() : List<Coordinate> {
        return when {
            isHorizontal() && isToTheRight() -> (start.x .. end.x).map { Coordinate(it, start.y) }
            isHorizontal() && isToTheLeft() -> (start.x downTo end.x).map { Coordinate(it, start.y) }
            isVertical() && isUpwards() -> (start.y .. end.y).map { Coordinate(start.x, it) }
            isVertical() && isDownwards() -> (start.y downTo end.y).map { Coordinate(start.x, it) }
            // Diagonal
            isToTheRight() && isUpwards() -> (0 .. (end.x - start.x)).map { Coordinate(start.x + it, start.y + it) }
            isToTheLeft() && isUpwards() -> (0 .. (start.x - end.x)).map { Coordinate(start.x - it, start.y + it) }
            isToTheRight() && isDownwards() -> (0 .. (end.x - start.x)).map { Coordinate(start.x + it, start.y - it) }
            isToTheLeft() && isDownwards() -> (0 .. (start.x - end.x)).map { Coordinate(start.x - it, start.y - it) }
            else -> throw IllegalStateException("not supported")
        }
    }
    companion object {
        private val lineRegex = "(\\d+),(\\d+) -> (\\d+),(\\d+)".toRegex()

        fun createLine(input: String): Line {
            val (_, xs, ys, xe, ye) = lineRegex
                .find(input)?.groupValues ?: throw IllegalStateException("no match!")
            return Line(Coordinate(xs, ys), Coordinate(xe, ye))
        }
    }
}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

