package test.advent.edition2022.day8

import test.advent.edition2022.product
import test.advent.edition2022.takeWhileInclusive
import test.advent.edition2022.to2DGridOfPoints
import java.io.File

val day = 8;
val file = File("src/main/resources/edition2022/day${day}/input")

class Puzzle(private val input: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val trees = input.to2DGridOfPoints()
        println(
            trees.flatten()
                .count { point -> point.getViews(trees).any { view -> view.isEmpty() || view.all { it.z < point.z } } })
    }

    fun runPart2() {
        println("Part 2 of Day $day")
        val trees = input.to2DGridOfPoints()
        println(
            trees.flatten()
                .map { point -> point.getViews(trees).map { it.takeWhileInclusive { it.z < point.z }.count() } }
                .maxOf { it.product() }
        )
    }
}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

