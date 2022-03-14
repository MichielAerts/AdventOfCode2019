package test.advent.edition2021.day22

import kotlinx.coroutines.*
import test.advent.edition2021.Point
import java.io.File
import kotlin.math.pow

val day = 22;
val file = File("src/main/resources/edition2021/day${day}/input")

class Puzzle(private val rawInput: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val cuboids = rawInput.map { Cuboid.createCuboid(it) }
        val size = 50
        val cube = Cube.createCube(size)
        cuboids.filter { it.inRegion(size) }.flatMap { it.getPoints() }.forEach { 
            cube.changePoint(it)
        }
        println(cube.countOn())
    }

    suspend fun runPart2() {
        println("Part 2 of Day $day")
        val cuboids = rawInput.map { Cuboid.createCuboid(it) }
        println(cuboids)
        val cube = EmptyCube()
        cuboids.forEach { cube.addCuboid(it) }
        println(cuboids.filter { it.powerStatus == Status.ON }.sumOf { it.getVolume() }.toDouble() / 200000.0.pow(3))
        println()
        cube.calculateOn()
        println(cube.on)
    }
}

enum class Status(val c: Char) {
    ON('#'), OFF('.');
}

data class EmptyCube(var on: Long = 0, val list: MutableList<Cuboid> = mutableListOf(),
                     val cutCubesList: MutableList<Cuboid> = mutableListOf()) {
    
    fun addCuboid(cuboid: Cuboid) {
        list.add(cuboid)
    }

    suspend fun calculateOn() {
        val xPoints = (list.flatMap { listOf(it.xRange.first, it.xRange.last) }.distinct().sorted().zipWithNext { first, second -> IntRange(first + 1, second - 1) } + 
                list.flatMap { listOf(it.xRange.first, it.xRange.last) }.distinct().sorted().map { IntRange(it, it) }).sortedBy { it.first }
        val yPoints = (list.flatMap { listOf(it.yRange.first, it.yRange.last) }.distinct().sorted().zipWithNext { first, second -> IntRange(first + 1, second - 1) } +
                list.flatMap { listOf(it.yRange.first, it.yRange.last) }.distinct().sorted().map { IntRange(it, it) }).sortedBy { it.first }
        val zPoints = (list.flatMap { listOf(it.zRange.first, it.zRange.last) }.distinct().sorted().zipWithNext { first, second -> IntRange(first + 1, second - 1) } +
                list.flatMap { listOf(it.zRange.first, it.zRange.last) }.distinct().sorted().map { IntRange(it, it) }).sortedBy { it.first }
//        for (xr in xPoints) {
//            println(xr)
//            for (yr in yPoints) for (zr in zPoints) {
//                val smallCube = Cuboid(xr, yr, zr)
//                if (list.lastOrNull { it.contains(smallCube) }?.powerStatus == Status.ON) {
//                    on += smallCube.getVolume()
//                }
//            }
//        }
//        println(on)
//        
        var on: Long = 0
        val results = mutableListOf<Deferred<Long>>()
        for (xr in xPoints) {
            println(xr)
            val result = CoroutineScope(Dispatchers.Default).async {
                var r: Long = 0
                for (yr in yPoints) for (zr in zPoints) {
                    val smallCube = Cuboid(xr, yr, zr)
                    if (list.lastOrNull { it.contains(smallCube) }?.powerStatus == Status.ON) {
//                        println(smallCube.getVolume())
                        r += smallCube.getVolume()
                    }
                }    
                return@async r
            }
            results.add(result)
        }
        println(results.awaitAll().sumOf { it })
    }
}

data class Cube(val cube: MutableList<MutableList<MutableList<Point>>>, val size: Int) {

    fun changePoint(p: Point) {
        cube[p.z + size][p.y + size][p.x + size] = p
    }
    
    fun countOn(): Int = cube.flatten().flatten().count { it.charValue == Status.ON.c }

    companion object {
        fun createCube(size: Int): Cube {
            val cube = mutableListOf<MutableList<MutableList<Point>>>()
            for (z in -size..size) {
                val plane = mutableListOf<MutableList<Point>>()
                for (y in -size..size) {
                    val row = mutableListOf<Point>()
                    for (x in -size..size) {
                        row.add(Point(x, y, z))
                    }
                    plane.add(row)
                }
                cube.add(plane)
            }
            return Cube(cube, size)
        }
    }
}

data class Cuboid(val xRange: IntRange, val yRange: IntRange, val zRange: IntRange, val powerStatus: Status = Status.OFF) {

    fun inRegion(size: Int) = listOf(
        xRange.first, xRange.last,
        yRange.first, yRange.last,
        zRange.first, zRange.last
    ).all { it in IntRange(-size, size) }

    fun getVolume() = (xRange.last - xRange.first + 1).toLong() * (yRange.last - yRange.first + 1).toLong() * (zRange.last - zRange.first + 1).toLong()

    fun getPoints(): List<Point> {
        val points = mutableListOf<Point>()
        for (x in xRange) for (y in yRange) for (z in zRange) {
            points.add(Point(x, y, z, powerStatus.c))
        }
        return points
    }

    fun contains(smallCube: Cuboid) = xRange.containsCube(smallCube.xRange) && yRange.containsCube(smallCube.yRange) && zRange.containsCube(smallCube.zRange)

    companion object {
        private val regex = "(on|off) x=(-?\\d+\\.\\.-?\\d+),y=(-?\\d+\\.\\.-?\\d+),z=(-?\\d+\\.\\.-?\\d+)".toRegex()
        fun createCuboid(input: String): Cuboid {
            println(input)
            val (_, status, xRange, yRange, zRange) = regex.matchEntire(input)!!.groupValues
            return Cuboid(
                IntRange.fromString(xRange),
                IntRange.fromString(yRange),
                IntRange.fromString(zRange),
                Status.valueOf(status.toUpperCase())
            )
        }
    }
}

private fun IntRange.containsCube(range: IntRange): Boolean = range.first in first..last && range.last in first..last

private fun IntRange.Companion.fromString(xRange: String): IntRange {
    val (start, end) = xRange.split("..").map { it.toInt() }
    return IntRange(start, end)
}

suspend fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
//    puzzle.runPart1()
    puzzle.runPart2()
}

