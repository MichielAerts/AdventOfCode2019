package test.advent.edition2021.day12

import org.jgrapht.graph.DefaultDirectedGraph
import java.io.File

val day = 12;
val file = File("src/main/resources/edition2021/day${day}/input")

class Puzzle(private val rawInput: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        println(rawInput)
        val cave = Cave.createSystem(rawInput)
        println(cave.findAllPaths())
    }
    
    fun runPart2() {
        println("Part 2 of Day $day")
        println(rawInput)
        val cave = Cave.createSystem(rawInput)
        println(cave.findAllPaths())
    }
}

data class Cave(val system: DefaultDirectedGraph<String, Edge>, val foundPaths: MutableList<List<String>> = mutableListOf()) {
    fun findAllPaths(): Int {
        findAllPaths("start", "end", mutableListOf("start"))
        return foundPaths.size
    }
    
    private fun findAllPaths(start: String, end: String, localPathList: List<String>) {
//        println("searching for $start till $end, $localPathList")
        if (start == end) {
            println(localPathList)
            foundPaths.add(localPathList)
            return
        }
        
        for (edge in system.outgoingEdgesOf(start)) {
            val target = edge.target
//            if (target.isSmall() && target in localPathList) continue
            if (target == "start" || (target.isSmall() && target in localPathList && localPathList.hasATwiceVisitedSmallCaveAlready())) continue 
            findAllPaths(target, end, localPathList + target)
        }
    }

    companion object {
        fun createSystem(input: List<String>) : Cave {
            val edges = input.map { line -> line.split("-") }.flatMap { listOf(Edge(it[0], it[1]), Edge(it[1], it[0])) }
            val nodes = edges.flatMap { listOf(it.source, it.target) }.distinct()
            val graph = DefaultDirectedGraph<String, Edge>(Edge::class.java)
            graph.apply {
                nodes.forEach { addVertex(it) }
                edges.forEach { addEdge(it.source, it.target, it) }
            }
            return Cave(graph)
        }
    }
}

private fun List<String>.hasATwiceVisitedSmallCaveAlready(): Boolean = this.filter { it.isSmall() }.groupingBy { it }.eachCount().containsValue(2)

private fun String.isSmall(): Boolean = this.toList().all { it.isLowerCase() }

class Edge(val source: String, val target: String)

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

