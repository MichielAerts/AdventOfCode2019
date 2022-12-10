package test.advent.edition2022.day2

import test.advent.edition2022.day2.RPSGame.Choice.*
import test.advent.edition2022.day2.RPSGame.Result.*
import test.advent.edition2022.getOrThrow
import java.io.File

val day = 2;
val file = File("src/main/resources/edition2022/day${day}/input")

class Puzzle(private val input: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
//        println(input.map { RPSGame.create(it) }.map { it.getScore() }.sum())
    }

    fun runPart2() {
        println("Part 2 of Day $day")
        println(input.map { RPSGame.create(it) }.map { it.getScore() }.sum())
    }
}

//data class RPSGame(val opponent: Choice, val me: Choice) {
data class RPSGame(val opponent: Choice, val res: Result) {

    enum class Result {
        WIN, LOSE, DRAW;

        fun score(): Int = when (this) {
            WIN -> 6
            DRAW -> 3
            LOSE -> 0
        }
    }

    enum class Choice {
        ROCK, PAPER, SCISSORS;

        fun score(): Int = when (this) {
            ROCK -> 1
            PAPER -> 2
            SCISSORS -> 3
        }

        fun play(o: Choice): Result {
            if (this == o) return Result.DRAW
            return when (this) {
                ROCK -> if (o == SCISSORS) WIN else LOSE
                SCISSORS -> if (o == PAPER) WIN else LOSE
                PAPER -> if (o == ROCK) WIN else LOSE
            }
        }

        fun needToPlay(r: Result): Choice {
            return when (r) {
                WIN -> if (this == SCISSORS) ROCK else if (this == PAPER) SCISSORS else PAPER
                DRAW -> this 
                LOSE -> if (this == ROCK) SCISSORS else if (this == SCISSORS) PAPER else ROCK
            }
        }
    }

//    fun getScore(): Int = me.score() + me.play(opponent).score()
    fun getScore(): Int = res.score() + opponent.needToPlay(res).score()

    companion object {
        private val mapOp = mapOf("A" to ROCK, "B" to PAPER, "C" to SCISSORS)
        private val mapMe = mapOf("X" to ROCK, "Y" to PAPER, "Z" to SCISSORS)
        private val mapRes = mapOf("X" to LOSE, "Y" to Result.DRAW, "Z" to WIN)
        fun create(str: String): RPSGame {
//            val (op, me) = str.split(" ")
//            return RPSGame(mapOp.getOrThrow(op), mapMe.getOrThrow(me))
            val (op, res) = str.split(" ")
            return RPSGame(mapOp.getOrThrow(op), mapRes.getOrThrow(res))
        }
    }
}


fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

