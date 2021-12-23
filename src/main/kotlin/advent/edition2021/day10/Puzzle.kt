package test.advent.edition2021.day10

import test.advent.edition2021.day10.SyntaxChecker.StatusReport.Status.*
import test.advent.edition2021.getOrThrow
import java.io.File
import java.util.*

val day = 10;
val file = File("src/main/resources/edition2021/day${day}/input")

class Puzzle(private val rawInput: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val input = rawInput
        val checker = SyntaxChecker()
        println(input.map { checker.check(it) }.filter { it.status == SyntaxChecker.StatusReport.Status.CORRUPT }
            .mapNotNull { it.corruptChar }
            .sumOf { checker.score.getOrThrow(it) })
    }

    fun runPart2() {
        println("Part 2 of Day $day")
        val input = rawInput
        val checker = SyntaxChecker()
        val scores = input.map { checker.check(it) }.filter { it.status == INCOMPLETE }
            .map { checker.finish(it.stack) }
            .map { checker.finishingScore(it) }
            .sorted()
        println(scores[scores.size / 2])
    }
}

class SyntaxChecker {
    private val open = listOf('(', '[', '{', '<')
    private val closed = listOf(')', ']', '}', '>')
    private val pairs = mapOf(')' to '(', ']' to '[', '}' to '{', '>' to '<',
        '(' to ')', '[' to ']', '{' to '}', '<' to '>')
    private val finishingScore = mapOf(')' to 1L, ']' to 2L, '}' to 3L, '>' to 4L)
    val score = mapOf(')' to 3, ']' to 57, '}' to 1197, '>' to 25137)

    fun check(inputStr: String): StatusReport {
        val stack = ArrayDeque<Char>()
        val input = inputStr.toList()
        for (char in input) {
            if (char in open) stack.addFirst(char)
            if (char in closed) {
                if (stack.isEmpty()) return StatusReport(CORRUPT, stack, char)
                if (stack.removeFirst() != pairs.getOrThrow(char)) return StatusReport(CORRUPT, stack, char)
            }
        }
        return if (stack.isEmpty()) StatusReport(COMPLETE, stack, null) else StatusReport(INCOMPLETE, stack, null)
    }

    fun finish(stack: ArrayDeque<Char>) : List<Char> = stack.map { pairs.getOrThrow(it)}
    
    fun finishingScore(chars: List<Char>) : Long = chars.fold(0L) { score, c -> 5 * score + finishingScore.getOrThrow(c) }

    data class StatusReport(val status: Status, val stack: ArrayDeque<Char>, val corruptChar: Char?) {
        enum class Status { COMPLETE, INCOMPLETE, CORRUPT }
    }
}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

