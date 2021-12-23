package test.advent.edition2021.day2

import test.advent.edition2021.findGroupAsEnum
import test.advent.edition2021.findGroupAsInt
import java.io.File

val day = 2;
val file = File("src/main/resources/edition2021/day${day}/input")

class Puzzle(private val rawInput: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val finalPosition = rawInput.map { Move.create(it) }
            .fold(Position(0, 0), { pos, move -> pos + move })
        println("ending at $finalPosition, total = ${finalPosition.depth * finalPosition.hor}") 
    }

    fun runPart2() {
        println("Part 2 of Day $day")
        val finalPosition = rawInput.map { Move.create(it) }
            .fold(Position(0, 0, 0), { pos, move -> pos + move })
        println("ending at $finalPosition, total = ${finalPosition.depth * finalPosition.hor}") 
    }
}

private operator fun Position.plus(move: Move): Position {
    return when(move.direction) {
        Move.Direction.FORWARD -> Position(hor + move.amount, depth + move.amount * aim, aim)
        Move.Direction.DOWN -> Position(hor, depth, aim + move.amount)
        Move.Direction.UP -> Position(hor, depth, aim - move.amount)
    }
    // part 1
//    return when(move.direction) {
//        Move.Direction.FORWARD -> Position(hor + move.amount, depth)
//        Move.Direction.DOWN -> Position(hor, depth + move.amount)
//        Move.Direction.UP -> Position(hor, depth - move.amount)
//    }
}

data class Position(val hor: Int, val depth: Int, val aim: Int = 0)

data class Move(val direction: Direction, val amount: Int) {
    enum class Direction {
        FORWARD, DOWN, UP;
    }

    companion object {
        private val moveRegex = "(?<dir>\\w+) (?<amount>\\d+)".toRegex()
        fun create(str: String) = Move(
            moveRegex.findGroupAsEnum(str, "dir"),
            moveRegex.findGroupAsInt(str, "amount")
        )
    }
}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
//    puzzle.runPart1()
    puzzle.runPart2()
}

