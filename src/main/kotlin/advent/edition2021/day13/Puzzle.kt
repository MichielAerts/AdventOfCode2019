package test.advent.edition2021.day13

import test.advent.edition2021.Point
import java.io.File
import java.util.*

val day = 13
val file = File("src/main/resources/edition2021/day${day}/input")

class Puzzle(private val rawInput: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val input = rawInput
        val paper = Paper.createPaper(input)
        println(paper)
        paper.foldOnce()
        println(paper.dots.sortedWith(compareBy({ it.y }, { it.x })))
        println(paper.dots.size)
    }

    fun runPart2() {
        println("Part 2 of Day $day")
        val paper = Paper.createPaper(rawInput)
        paper.fold()
        println(paper.dots.sortedWith(compareBy({ it.y }, { it.x })))
        println(paper.dots.size)
        paper.print()
    }
}

data class Paper(var dots: Set<Point>, val foldInstructions: Deque<FoldInstruction>) {
    data class FoldInstruction(val axis: Char, val place: Int) {
        fun fold(p: Point): Point {
            return when (axis) {
                'x' -> if (p.x > place) Point(2 * place - p.x, p.y) else p
                'y' -> if (p.y > place) Point(p.x, 2 * place - p.y) else p
                else -> throw IllegalStateException("unsupported axis")
            }
        }
    }

    fun print() {
        val maxX = dots.maxOf { it.x }
        val maxY = dots.maxOf { it.y }
        for (y in 0 .. maxY) {
            for (x in 0..maxX) {
                print(if (dots.contains(Point(x, y))) "#" else ".")
            }
            println("\n")
        }
    }

    fun foldOnce() {
        val instruction = foldInstructions.pop()
        dots = dots.map { instruction.fold(it) }.toSet()
    }

    fun fold() {
        while (foldInstructions.isNotEmpty()) {
            val instruction = foldInstructions.pop()
            dots = dots.map { instruction.fold(it) }.toSet()
        }
    }

    companion object {
        fun createPaper(input: List<String>): Paper {
            val dots = input.filter { it.matches("\\d+,\\d+".toRegex()) }
                .map { it.split(",") }
                .map { Point(it[0], it[1]) }
                .toSet()
            val instructions = input.filter { it.startsWith("fold along") }
                .map { it.split("=") }.map { Paper.FoldInstruction(it[0].last(), it[1].toInt()) }
            return Paper(dots, ArrayDeque(instructions))
        }
    }
}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

