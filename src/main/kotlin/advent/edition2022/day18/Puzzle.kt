package test.advent.edition2022.day18

import test.advent.edition2022.Point
import test.advent.edition2022.getDirectNeighbours3D
import test.advent.edition2022.initEmpty3DGrid
import test.advent.edition2022.printV3D
import java.io.File

val day = 18
val file = File("src/main/resources/edition2022/day${day}/input")

class Puzzle(private val input: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val cubes = input.map { it.split(",") }.map { Point(it[0], it[1], it[2]) }
        println(cubes.sumOf { it.unconnectedSides(cubes) })
    }
    
    fun runPart2() {
        println("Part 2 of Day $day")
        val pond = Pond.create(input)
        pond.penetrateWater()
        pond.grid.printV3D()
        println(pond.cubes.sumOf { it.connectedTo(pond.grid, WATER) })
    }
}

private const val WATER = 'W'

data class Pond(val grid: List<List<List<Point>>>, val cubes: List<Point>) {
    
    fun penetrateWater() {
        var waterMoving = true
        while (waterMoving) {
            var waterMoved = 0
            for (p in grid.flatten().flatten()) {
                if (p.value == WATER) {
                    grid.getDirectNeighbours3D(p).neighbours.forEach { 
                        if (it !in cubes && it.value != WATER) {
                            it.value = WATER
                            waterMoved++
                        }
                    }
                }
            }
            println(waterMoved)
            if (waterMoved == 0) waterMoving = false
        }
    }
    
    companion object {
        fun create(input: List<String>): Pond {
            val cubes = input.map { it.split(",") }.map { Point(it[0].toInt() + 1, it[1].toInt() + 1, it[2].toInt() + 1) }
            val endX = cubes.maxOf { it.x } + 1
            val endY = cubes.maxOf { it.y } + 1
            val endZ = cubes.maxOf { it.z } + 1
            val grid = initEmpty3DGrid(
                endX = endX,
                endY = endY,
                endZ = endZ
            )
            grid.flatten().flatten().forEach { 
                if (it.x == 0 || it.x == endX || it.y == 0 || it.y == endY || it.z == 0 || it.z == endZ) it.value =
                    WATER
            }
            return Pond(grid, cubes)
        }
    }
}
fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

