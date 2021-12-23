package test.advent.edition2021.day3

import test.advent.edition2021.transpose
import java.io.File

val day = 3;
val file = File("src/main/resources/edition2021/day${day}/input")

class Puzzle(private val rawInput: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val input = rawInput.map { it.toList() }.transpose()
        val gammaRate = input.map { it.countMostOccurringBit() }.joinToString("").toInt(2)
        val epsilonRate = input.map { it.countLeastOccurringBit() }.joinToString("").toInt(2)
        println("gammarate: $gammaRate, epsilonrate: $epsilonRate, multiplied: ${gammaRate * epsilonRate}")
    }

    fun runPart2() {
        println("Part 2 of Day $day")
        val oxygenRating = findRating(List<Char>::countMostOccurringBit)
        val co2Rating = findRating(List<Char>::countLeastOccurringBit)
        println("oxygen rating: $oxygenRating, co2 rating: $co2Rating, multiplication: ${oxygenRating * co2Rating}")
    }

    private fun findRating(selectionFunc: List<Char>.() -> Char): Int {
        val size = rawInput[0].length
        var input = rawInput.map { it.toList() }
        for (i in 0 until size) {
            val selection = input.transpose()[i]
            val bit = selection.selectionFunc()
            input = input.filter { it[i] == bit }
            if (input.size == 1) return input[0].joinToString("").toInt(2)
        }
        throw IllegalStateException("couldn't select something")
    }
}

private fun List<Char>.countMostOccurringBit(): Char = if (this.count { it == '1' } >= this.count { it == '0' }) '1' else '0'

private fun List<Char>.countLeastOccurringBit(): Char = if (this.count { it == '1' } < this.count { it == '0' }) '1' else '0'

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
//    puzzle.runPart1()
    puzzle.runPart2()
}

