package test.advent.edition2022.day3

import test.advent.edition2022.day3.RuckSack.Companion.getOverlapOfThreeRuckSacks
import test.advent.edition2022.day3.RuckSack.Companion.priority
import test.advent.edition2022.getOrThrow
import java.io.File

val day = 3;
val file = File("src/main/resources/edition2022/day${day}/input")

class Puzzle(private val input: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        println(input.map { RuckSack.create(it) }
            .map { it.getOverlap().first() }
            .sumOf { priority.getOrThrow(it) })
    }
    
    fun runPart2() {
        println("Part 2 of Day $day")
        println(input.map { RuckSack.create(it) }
            .map { it.getAllItems() }
            .chunked(3)
            .map { getOverlapOfThreeRuckSacks(it) }
            .sumOf { priority.getOrThrow(it.first())}) 
    }
}

data class RuckSack (val first: List<String>, val second: List<String>) {
    
    fun getOverlap() = first.intersect(second)
    
    fun getAllItems() = (first + second).toSet()
    
    companion object {
        val priority = (('a'..'z') + ('A'..'Z')).mapIndexed { idx, c -> c.toString() to idx + 1 }.toMap()

        fun getOverlapOfThreeRuckSacks(it: List<Set<String>>) =
            it[0].intersect(it[1]).intersect(it[2])
        
        fun create(input: String): RuckSack {
            val half = input.length / 2
            val (first, second) = input.chunked(half).map { it.chunked(1) }
            return RuckSack(first, second)
        }
    }
}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

