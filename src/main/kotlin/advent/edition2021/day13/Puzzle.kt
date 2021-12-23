package test.advent.edition2021.day13

import test.advent.edition2021.Point
import java.io.File

val day = 13
val file = File("src/main/resources/edition2021/day${day}/input")

class Puzzle(private val rawInput: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val input = rawInput
        val paper = Paper.createPaper(input)
        println(paper)
    }
    
    fun runPart2() {
        println("Part 2 of Day $day")
        println(rawInput)      
    }
}

data class Paper(var dots: List<Point>, val foldInstructions: List<FoldInstruction>) {
    data class FoldInstruction(val axis: Char, val place: Int)
    
    companion object {
        fun createPaper(input: List<String>) : Paper {
            val dots = input.filter { it.matches("\\d+,\\d+".toRegex()) }
                .map { it.split(",") }
                .map { Point(it[0], it[1]) }
            val instructions = input.filter { it.startsWith("fold along") }
                .map {  it.split("=")}.map { FoldInstruction(it[0].last(), it[1].toInt()) }
            return Paper(dots, instructions)
        }
    }
}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

