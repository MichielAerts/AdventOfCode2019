package test.advent.edition2021.day6

import java.io.File

val day = 6;
val file = File("src/main/resources/edition2021/day${day}/input")

class Puzzle(private val rawInput: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val days = 80
        var input = rawInput[0].split(",").map { it.toInt() }
        for (day in 1..days) {
            println("day: $day")
            input = input.simulateDay()
            println(input)
        }
        println(input.size)
    }

    fun runPart2() {
        println("Part 2 of Day $day")
        val days = 256
        var input = rawInput[0].split(",").map { it.toInt() }
            .groupingBy { it }.eachCount()
            .entries.associate { it.key to it.value.toLong() }
        println("initial state: $input")
        for (day in 1 .. days) {
            input = input.simulateDay()
            println("after day $day: $input")
        }
        println(input.values.sum())
    }
}

private fun Map<Int, Long>.simulateDay(): Map<Int, Long> {
    val newMap = mutableMapOf<Int, Long>()
    for ((key, value) in this.entries) {
        when (key) {
            8 -> newMap[7] = value
            7 -> newMap[6] = newMap.getOrDefault(6, 0) + value
            in 1..6 -> newMap[key - 1] = value
            0 -> {
                newMap[6] = newMap.getOrDefault(6, 0) + value
                newMap[8] = value
            }
        }
    }
    return newMap
}

private fun List<Int>.simulateDay(): List<Int> {
    val newList = mutableListOf<Int>()
    for (e in this) {
        if (e == 0) {
            newList.add(6)
            newList.add(8)
        } else {
            newList.add((e - 1))
        }
    }
    return newList
}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
//    puzzle.runPart1()
    puzzle.runPart2()
}

