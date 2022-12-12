package test.advent.edition2022.day6

import test.advent.edition2022.hasAllDifferentCharacters
import java.io.File

val day = 6;
val file = File("src/main/resources/edition2022/day${day}/input")

class Puzzle(private val input: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        println(input[0].toList().windowed(4).indexOfFirst { it.hasAllDifferentCharacters() } + 4)
    }
    
    fun runPart2() {
        println("Part 2 of Day $day")
        println(input[0].toList().windowed(14).indexOfFirst { it.hasAllDifferentCharacters() } + 14)
    }
}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

