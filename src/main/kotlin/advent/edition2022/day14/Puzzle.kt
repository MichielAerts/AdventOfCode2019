package test.advent.edition2022.day14

import test.advent.edition2022.*
import java.io.File

val day = 14
val file = File("src/main/resources/edition2022/day${day}/input")

class Puzzle(private val input: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val paths = input.map { Path.create(it) }
        val maxX = paths.flatMap { it.points }.maxByOrNull { it.x } ?: throw IllegalStateException()
        val maxY = paths.flatMap { it.points }.maxByOrNull { it.y } ?: throw IllegalStateException()
        val cave = Cave.createCave(maxX, maxY, paths)
        cave.caveSystem.printV()
        while (!cave.sandIsFallingOff) {
            cave.addSand()
        }
        println(cave.countSand())
    }

    fun runPart2() {
        println("Part 2 of Day $day")
        val paths = input.map { Path.create(it) }
        val maxX = paths.flatMap { it.points }.maxByOrNull { it.x } ?: throw IllegalStateException()
        val maxY = paths.flatMap { it.points }.maxByOrNull { it.y } ?: throw IllegalStateException()
        val cave = Cave.createCaveWithFloor(maxX, maxY, paths)
//        cave.caveSystem.printV()
        while (!cave.isFull) {
            cave.addSand()
        }
        cave.caveSystem.printV()
        println(cave.countSand())
    }
}

private const val SAND = 'o'
private const val ROCK = '#'
private const val EMPTY = '.'

data class Cave(
    val caveSystem: List<List<Point>>,
    val maxY: Int,
    var sand: Int = 0,
    var sandIsFallingOff: Boolean = false,
    var isFull: Boolean = false
) {

    fun addSand() {
        var currentPoint = Point(500, 0)
        caveSystem.changePoint(currentPoint, SAND)
        var isMoving = true
        while (isMoving) {
            val pointBelow = caveSystem.pointBelow(currentPoint)
            val pointLeftBelow = caveSystem.pointLeftBelow(currentPoint)
            val pointRightBelow = caveSystem.pointRightBelow(currentPoint)
            val newPoint = when {
                pointBelow.value == EMPTY -> pointBelow
                pointLeftBelow.value == EMPTY -> pointLeftBelow
                pointRightBelow.value == EMPTY -> pointRightBelow
                else -> {
                    isMoving = false
                    if (sand++ % 100 == 0) caveSystem.printV()
                    currentPoint
                }
            }
            caveSystem.changePoint(currentPoint, EMPTY)
            caveSystem.changePoint(newPoint, SAND)
            currentPoint = newPoint
//            caveSystem.printV()
//            if (pointBelow.y > maxY) {
//                sandIsFallingOff = true
//                caveSystem.changePoint(currentPoint, EMPTY)
//                break
//            }
        }
        if (caveSystem.getPoint(500, 0)!!.value == SAND) {
            isFull = true
        }
    }

    fun countSand(): Int = caveSystem.findAllValuesInGrid(SAND).count()

    companion object {
        fun createCave(maxX: Point, maxY: Point, rockPaths: List<Path>): Cave {
            val caveSystem = initEmptyGrid(endX = maxX.x + 2, endY = maxY.y + 2)
            rockPaths.map { it.getCompletePath() }.forEach { caveSystem.changePoints(it, ROCK) }
            return Cave(caveSystem, maxY.y)
        }

        fun createCaveWithFloor(maxX: Point, maxY: Point, rockPaths: List<Path>): Cave {
            val caveSystem = initEmptyGrid(endX = maxX.x + maxY.y + 2, endY = maxY.y + 2)
            val floor = Path(listOf(Point(0, maxY.y + 2), Point(maxX.x + maxY.y + 2, maxY.y + 2)))
            (rockPaths + floor).map { it.getCompletePath() }.forEach { caveSystem.changePoints(it, ROCK) }
            return Cave(caveSystem, maxY.y)
        }
    }
}

data class Path(val points: List<Point>) {
    fun getCompletePath(): Set<Point> =
        points.zipWithNext().map { it.first.getPointsInLineTo(it.second) }.flatten().toSet()

    companion object {
        fun create(input: String): Path = Path(input.split(" -> ")
            .map { it.split(",") }.map { Point(it[0], it[1]) })
    }
}

//fun List<List<Point>>.pointBelow(currentPoint: Point): Point = this.findPoint(currentPoint.x, currentPoint.y + 1)
//fun List<List<Point>>.pointLeftBelow(currentPoint: Point): Point = this.findPoint(currentPoint.x - 1, currentPoint.y + 1)
//fun List<List<Point>>.pointRightBelow(currentPoint: Point): Point = this.findPoint(currentPoint.x + 1, currentPoint.y + 1)

fun List<List<Point>>.pointBelow(currentPoint: Point): Point =
    this.getPoint(currentPoint.x, currentPoint.y + 1) ?: throw IllegalStateException()

fun List<List<Point>>.pointLeftBelow(currentPoint: Point): Point =
    this.getPoint(currentPoint.x - 1, currentPoint.y + 1) ?: throw IllegalStateException()

fun List<List<Point>>.pointRightBelow(currentPoint: Point): Point =
    this.getPoint(currentPoint.x + 1, currentPoint.y + 1) ?: throw IllegalStateException()

fun List<List<Point>>.findPoint(x: Int, y: Int): Point = this.flatten().findOrThrow { it.x == x && it.y == y }

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
//    puzzle.runPart1()
    puzzle.runPart2()
}

