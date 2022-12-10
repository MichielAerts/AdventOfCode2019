package test.advent.edition2022.day1

import test.advent.edition2022.splitBy
import java.io.File

val day = 1;
val file = File("src/main/resources/edition2022/day${day}/input")

class Puzzle(private val input: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        println(input.splitBy { it.isEmpty() }
            .map { carriedByElf -> carriedByElf.sumOf { foodItem -> foodItem.toInt() } }
            .maxByOrNull { it })
    }
    
    fun runPart2() {
        println("Part 2 of Day $day")
        println(input.splitBy { it.isEmpty() }
            .map { carriedByElf -> carriedByElf.sumOf { foodItem -> foodItem.toInt() } }
            .sortedDescending().take(3).sum())
    }
}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

