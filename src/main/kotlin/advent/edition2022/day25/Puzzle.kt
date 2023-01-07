package test.advent.edition2022.day25

import test.advent.edition2021.getOrThrow
import java.io.File
import kotlin.math.pow
import kotlin.math.roundToLong

val day = 25
val file = File("src/main/resources/edition2022/day${day}/input")

class Puzzle(private val input: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        println(input)
        println(input.map { it.snafuToDecimal() }.map { it.decimalToSnafu() })
        println(input.sumOf { it.snafuToDecimal() }.decimalToSnafu())
    }
    
    fun runPart2() {
        println("Part 2 of Day $day")
        println(input)      
    }
}

private fun Long.decimalToSnafu(): String {
    val map = mapOf(2L to '2', 1L to '1', 0L to '0', -1L to '-', -2L to '=')
    val lastDigit = if (this % 5 <= 2) this % 5 else this % 5 - 5
    var snafu = map.getOrThrow(lastDigit).toString()
    var rest = this - lastDigit
    var power = 1
    while (rest > 0) {
        val factor = 5.0.pow(power).roundToLong()
        val n = rest % (factor * 5) / factor
        val next = if (n <= 2) n else n - 5
        snafu += map.getOrThrow(next)
        rest -= factor * next 
        power++
    }
    return snafu.reversed()
}

private fun String.snafuToDecimal(): Long {
    val map = mapOf('2' to 2L, '1' to 1L, '0' to 0L, '-' to -1L, '=' to -2L)
    return this.toList().mapIndexed { 
            idx, c -> 5.0.pow((this.length - 1 - idx)).roundToLong() * map.getOrThrow(c) }.sum()
}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

