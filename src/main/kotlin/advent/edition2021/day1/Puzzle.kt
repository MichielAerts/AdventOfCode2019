package test.advent.edition2021.day1

import java.io.File

val day = 1;
val file = File("src/main/resources/edition2021/day${day}/input")

class Puzzle(val rawInput: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val input = rawInput.map { it.toInt() }
        println(input.countIncreases())
    }
    
    fun runPart2() {
        println("Part 2 of Day $day")
        val input = rawInput.map { it.toInt() }
        val sums = mutableListOf<Int>()
        for (i in 2 until input.size) {
            sums += input[i - 2] + input[i - 1] + input[i]
        }
        println(sums.countIncreases())
    }
}

private fun List<Int>.countIncreases(): Int = this.zipWithNext().count { pair -> pair.first < pair.second }

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

