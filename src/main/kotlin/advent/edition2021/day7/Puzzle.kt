package test.advent.edition2021.day7

import java.io.File
import kotlin.math.abs

val day = 7;
val file = File("src/main/resources/edition2021/day${day}/input")

class Puzzle(private val rawInput: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val input = rawInput[0].split(",").map { it.toInt() }
        val minimumSteps = ((input.minOrNull() ?: 0) .. (input.maxOrNull() ?: Int.MAX_VALUE))
            .map { n -> input.sumBy { abs(it - n) } }.minOrNull()
        println(minimumSteps)
    }
    
    fun runPart2() {
        println("Part 2 of Day $day")
        val input = rawInput[0].split(",").map { it.toInt() }
        val min = (input.minOrNull() ?: 0)
        val max = input.maxOrNull() ?: Int.MAX_VALUE
        
        val steps = mutableMapOf<Int, Int>()
        steps[0] = 0
        for (i in 1 .. max) {
            steps[i] = steps.getOrDefault(i - 1, 0) + i
        }
        
        val minimumSteps = (min .. max)
            .map { n -> input.sumBy { steps[abs(it - n)] ?: throw IllegalStateException("couldn't") } }
            .minOrNull()
        println(minimumSteps)
    }
}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

