package test.advent.edition2021.day4

import test.advent.edition2021.subListTillEnd
import test.advent.edition2021.transpose
import java.io.File

val day = 4;
val file = File("src/main/resources/edition2021/day${day}/input")

class Puzzle(private val rawInput: List<String>) {

    fun runPart1() {
        println("Part 1 of Day $day")
        val input = rawInput
        val game = BingoGame.createGame(input)
        val (winner, number) = game.go()
        println(winner.getScore(number))
    }

    fun runPart2() {
        println("Part 2 of Day $day")
        val input = rawInput
        val game = BingoGame.createGame(input)
        val (lastWinner, number) = game.goUntilLastOne()
        println(lastWinner.getScore(number))
    }
}

data class BingoGame(val numbers: List<Int>, val boards: List<Board>) {
    fun go(): Pair<Board, Int> {
        for (number in numbers) {
            for (board in boards) {
                board.markNumber(number)
                if (board.won) return Pair(board, number)
            }
        }
        throw IllegalStateException("No Winner!")
    }

    fun goUntilLastOne(): Pair<Board, Int> {
        for (number in numbers) {
            for (board in boards) {
                board.markNumber(number)
                if (boards.all { it.won }) return Pair(board, number)
            }
        }
        throw IllegalStateException("No Winner!")
    }

    companion object {
        fun createGame(input: List<String>): BingoGame {
            val numbers = input[0].split(",").map { it.toInt() }
            val boards = input.subListTillEnd(1).chunked(6).map { Board.createBoard(it.subListTillEnd(1)) }
            return BingoGame(numbers, boards)
        }
    }
}

data class Board(val board: List<List<BingoNumber>>, var won: Boolean = false) {

    fun markNumber(drawnNumber: Int) {
        val bingoNumber = board.flatten().find { it.number == drawnNumber }
        bingoNumber?.marked = true
        won = (getRows().any { row -> row.all { it.marked } } || getColumns().any { col -> col.all { it.marked } })
    }

    fun getScore(drawnNumber: Int) = board.flatten().filter { !it.marked }.sumOf { it.number } * drawnNumber

    private fun getRows() = board

    private fun getColumns() = board.transpose()

    companion object {
        fun createBoard(input: List<String>): Board =
            Board(input.map { line -> line.trim().split("\\s+".toRegex()).map { BingoNumber(it.toInt()) } })
    }
}

data class BingoNumber(val number: Int, var marked: Boolean = false)

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

