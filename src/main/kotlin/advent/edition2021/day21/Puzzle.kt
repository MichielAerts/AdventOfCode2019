package test.advent.edition2021.day21

import test.advent.edition2021.findGroupAsInt
import java.io.File

val day = 21;
val file = File("src/main/resources/edition2021/day${day}/input")

class Puzzle(private val rawInput: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val players = rawInput.map { Player.createPlayer(it) }
        val game = Game(player1 = players[0], player2 = players[1], targetScore = 1000)
        while (game.isOn()) {
            game.nextMove()
//            println(game)
        }
        println("loser points: ${game.getLoser().score}, throws: ${game.die.rolls}, result = ${game.getLoser().score * game.die.rolls}")
    }

    fun runPart2() {
        println("Part 2 of Day $day")
        val (p1, p2) = rawInput.map { Player.createPlayer(it) }
        val game = QuantumGame.createQuantumGame(p1, p2)
        println(game.map)
        val steps = 2 * 30
        for (step in 1..steps) {
            println(step)
            game.runStep(step)
        }
        println("end result: wins P1 ${game.winsPlayer1}, wins P2 ${game.winsPlayer2}")
        // P1 Pos score, P2 pos score, turn
    }
}

data class QuantumGame(var map: MutableMap<String, Long>, val die: DiracDie = DiracDie(), var winsPlayer1: Long = 0, var winsPlayer2: Long = 0) {
    
    fun runStep(step: Int) {
        // do three rolls!
        val turn = if (step % 2 == 1) Player.No.ONE else Player.No.TWO
        val newMap = mutableMapOf<String, Long>()
        for ((key, v) in map.entries) {
            val (p1, p2) = key.split(";")
            val newEntries = when (turn) {
                Player.No.ONE -> die.rolls.map { newScore(p1, it) }
                    .filter { !it.second }
                    .map{ it.first + ";$p2" }
                Player.No.TWO -> die.rolls.map { newScore(p2, it) }
                    .filter { !it.second }                    
                    .map { "$p1;" + it.first }
            }
            when (turn) {
                Player.No.ONE -> winsPlayer1 += (27 - newEntries.size) * v
                Player.No.TWO -> winsPlayer2 += (27 - newEntries.size) * v
            }
            newEntries.forEach { 
                newMap.merge(it, v, Long::plus)
            }
        }
        map = newMap
        println("after turn player $turn, total games: ${map.values.sum()}, $map")
    }

    private fun newScore(player: String, rolls: List<Int>): Pair<String, Boolean> {
        val (id, pos, score) = player.split(",")
        val potNewPos = pos.toInt() + rolls.sum()
        val newPos = if (potNewPos <= 10) potNewPos else potNewPos - 10
        val newScore = score.toInt() + newPos
        val won = newScore >= 21
        return Pair("$id,$newPos,$newScore", won)
    }


    companion object {
        fun createQuantumGame(p1: Player, p2: Player) = 
            QuantumGame(mutableMapOf("P1,${p1.position},${p1.score};P2,${p2.position},${p2.score}" to 1))
    }
}

data class DiracDie(var rolls: List<List<Int>> = listOf()) {
    init {
        val newList = mutableListOf<List<Int>>()
        for (i in 1..3) for (j in 1..3) for (k in 1..3) {
            newList.add(listOf(i, j, k))
        }
        rolls = newList.toList()
    }
}
data class Game(
    val player1: Player, val player2: Player, var nextPlayer: Player.No = Player.No.ONE,
    val die: DetDie = DetDie(), val dieRoller: Iterator<MutableList<Int>> = die.throwDie().iterator(),
    val targetScore: Int = 1000
) {
    fun isOn(): Boolean = !player1.won && !player2.won
    
    fun nextMove() {
        val rolls = dieRoller.next()
        val player = nextPlayer()
        player.addScore(rolls)
        moveToNextPlayer(player)
    }

    private fun moveToNextPlayer(player: Player) {
        nextPlayer = if (player.id == 1) Player.No.TWO else Player.No.ONE
    }

    private fun nextPlayer() = if (nextPlayer == Player.No.ONE) player1 else player2

    fun getLoser() = listOf(player1, player2).first { !it.won }
}

class DetDie(var rolls: Int = 0) {
    fun throwDie() = sequence { 
        val sides = 100
        var lastRoll = 0

        while(true) {
            val values = mutableListOf<Int>()
            for (roll in 1..3) {
                lastRoll += 1
                if (lastRoll == sides + 1) lastRoll = 1
                values.add(lastRoll)     
                rolls += 1
            }
            yield(values)
        }
    }
}

data class Player(val id: Int, var position: Int, var score: Int = 0, var won: Boolean = false) {
    fun addScore(rolls: List<Int>) {
        val potNewPos = position + rolls.sum()
        position = when {
            potNewPos <= 10 -> potNewPos 
            potNewPos % 10 == 0 -> 10
            else -> potNewPos % 10
        }
        score += position
        println("Player $id rolls $rolls and moves to space $position for a total score of $score.")
        if (score >= 1000) won = true
    }

    companion object {
        private val regex = "Player (?<id>\\d+) starting position: (?<pos>\\d+)".toRegex()
        fun createPlayer(input: String) = Player(
            regex.findGroupAsInt(input, "id"),
            regex.findGroupAsInt(input, "pos")
        )
    }

    enum class No {
        ONE, TWO;
    }

}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
//    puzzle.runPart1()
    puzzle.runPart2()
}

