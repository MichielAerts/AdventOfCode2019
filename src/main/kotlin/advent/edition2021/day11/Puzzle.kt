package test.advent.edition2021.day11

import test.advent.edition2021.Point
import test.advent.edition2021.getAllNeighbours
import java.io.File

val day = 11;
val file = File("src/main/resources/edition2021/day${day}/input")

class Puzzle(private val rawInput: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val input = rawInput
        val cavern = Cavern.createCavern(input)
        println(cavern)
        val steps = 300
        for (step in 1..steps) {
            cavern.runStep()
            println("step $step\n$cavern")
        }
    }

    fun runPart2() {
        println("Part 2 of Day $day")
        println(rawInput)
    }
}

class Cavern(val octopuses: List<List<Octopus>>, var currentFlashes: Int = 0, var totalFlashes: Int = 0) {
    fun runStep() {
        currentFlashes = 0
        octopuses.flatten().forEach { it.increaseLevel() }
        while (octopuses.flatten().count { it.shouldFlash() } > 0) {
            val flashers = octopuses.flatten().filter { it.shouldFlash() }.toList()
            flashers.forEach { it.flash(octopuses) }
            currentFlashes += flashers.size
        }
        totalFlashes += currentFlashes
        if (currentFlashes == octopuses.flatten().size) println("SYNC FLASHING!!")
        octopuses.flatten().filter { it.flashed }.forEach { it.reset() }
    }

    override fun toString(): String = 
        octopuses.joinToString("\n") 
        { it.map { it.z }.joinToString("") } + "\nflashes: $totalFlashes"

    companion object {
        fun createCavern(input: List<String>): Cavern =
            Cavern(input.mapIndexed { y, r ->
                r.toList().mapIndexed { x, v -> Octopus(x, y, Character.getNumericValue(v)) }
            })
    }
}

class Octopus(x: Int, y: Int, z: Int, var flashed: Boolean = false) : Point(x, y, z) {
    
    fun increaseLevel() {
        z += 1
    }
    
    fun shouldFlash() = z > 9 && !flashed
    
    fun flash(octopuses: List<List<Octopus>>) {
        octopuses.getAllNeighbours(this).neighbours.forEach { it.z += 1 }
        flashed = true
    }
    
    fun reset() {
        z = 0
        flashed = false
    }

}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

