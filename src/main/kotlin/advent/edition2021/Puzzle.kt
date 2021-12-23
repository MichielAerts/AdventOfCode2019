package test.advent.edition2021

import java.io.File

val day = 1;
val file = File("src/main/resources/edition2021/day${day}/input")

class Puzzle(private val rawInput: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        println(rawInput)
    }
    
    fun runPart2() {
        println("Part 2 of Day $day")
        println(rawInput)      
    }
}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

