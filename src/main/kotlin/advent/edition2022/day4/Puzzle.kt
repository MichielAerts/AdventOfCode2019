package test.advent.edition2022.day4

import test.advent.edition2022.containsRange
import test.advent.edition2022.hasOverlap
import java.io.File

val day = 4;
val file = File("src/main/resources/edition2022/day${day}/input")

class Puzzle(private val input: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        println(input.map { PairOfElves.create(it) }.count { it.oneRangeIsFullyContained() })
    }
    
    fun runPart2() {
        println("Part 2 of Day $day")
        println(input.map { PairOfElves.create(it) }.count { it.hasOverlap() })
    }
}

data class Elf(val assignment: IntRange) {
    companion object {
        fun create(line: String): Elf {
            val (low, high) = line.split("-").map { it.toInt() }
            return Elf((low..high))
        }
    }
}

data class PairOfElves(val first: Elf, val second: Elf) {
    
    fun oneRangeIsFullyContained(): Boolean = first.assignment.containsRange(second.assignment) || second.assignment.containsRange(first.assignment)
    
    fun hasOverlap(): Boolean = first.assignment.hasOverlap(second.assignment)
    
    companion object {
        fun create(line: String): PairOfElves {
            val (first, second) = line.split(",")
            return PairOfElves(Elf.create(first), Elf.create(second))
        }
    }
}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

