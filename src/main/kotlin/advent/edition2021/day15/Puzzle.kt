package test.advent.edition2021.day15

import org.jgrapht.graph.DefaultDirectedGraph
import test.advent.edition2021.Point
import test.advent.edition2021.getDirectNeighbours
import test.advent.edition2021.getOrThrow
import test.advent.edition2021.transpose
import java.io.File

val day = 15;
val file = File("src/main/resources/edition2021/day${day}/input")

class Puzzle(private val rawInput: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val cave = Cave.createCave(rawInput)
//        println(cave.findPathWithLowestTotalRisk() - 1)
    }

    fun runPart2() {
        println("Part 2 of Day $day")
        val cave = Cave.createBigCave(rawInput)
//        println(cave)
        println(cave.findPathWithLowestTotalRisk() - 1)
    }
}

data class Cave(
    val points: List<List<Point>>,
    val system: DefaultDirectedGraph<Point, Edge> = DefaultDirectedGraph<Point, Edge>(Edge::class.java),
    var currentLowestTotalRisk: Int = (points[0] + points.transpose()[points.size - 1]).sumOf { it.z },
    val lowestRiskMap: MutableMap<Point, Int> = points.flatten().associateWith { currentLowestTotalRisk }.toMutableMap()
) {

    fun findPathWithLowestTotalRisk(): Int {
        findAllPaths(points[0][0], points[points.size - 1][points[0].size - 1], mutableListOf(points[0][0]))
        return currentLowestTotalRisk
    }

    private fun findAllPaths(start: Point, end: Point, localPathList: List<Point>, currentRisk: Int = 0) {
//        println("searching for $start till $end, $localPathList")
        if (start == end) {
            currentLowestTotalRisk = localPathList.sumOf { it.z }
            println("new path with risk: $currentLowestTotalRisk")
            return
        }

        for (edge in system.outgoingEdgesOf(start)) {
            val target = edge.target
            if (target in localPathList) continue
            val newRisk = currentRisk + target.z 
            if (newRisk > currentLowestTotalRisk) continue

            val lowestRisk = lowestRiskMap.getOrThrow(target)
            if (newRisk >= lowestRisk) continue
            lowestRiskMap[target] = newRisk
            println("new path to $target with risk: $currentRisk, $localPathList")

            findAllPaths(target, end, localPathList + target, newRisk)
        }
    }

    init {
        val directedEdges = points.flatten()
            .flatMap { p -> points.getDirectNeighbours(p).neighbours.flatMap { listOf(Edge(p, it), Edge(it, p)) } }
        system.apply {
            points.flatten().forEach { addVertex(it) }
            directedEdges.forEach { addEdge(it.source, it.target, it) }
        }
    }

    companion object {
        fun createCave(input: List<String>): Cave =
            Cave(input.mapIndexed { y, r ->
                r.toList().mapIndexed { x, v -> Point(x, y, Character.getNumericValue(v)) }
            })

        fun createBigCave(input: List<String>): Cave {
            val original =
                input.mapIndexed { y, r -> r.toList().mapIndexed { x, v -> Point(x, y, Character.getNumericValue(v)) } }
            val size = original.size
            val bigCave = mutableListOf<MutableList<Point>>()
            for (i in 0..4) { //v
                for (r in original) {
                    val newRow = mutableListOf<Point>()
                    for (j in 0..4) { //h
                        newRow.addAll(r.map { Point(it.x + j * size, it.y + i * size, 
                            if (it.z + (i + j) > 9) it.z + (i + j) - 9 else it.z + (i + j))  })
                    }
                    bigCave.add(newRow)
                }
            }
            return Cave(bigCave)
        }
    }
}

class Edge(val source: Point, val target: Point)

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

