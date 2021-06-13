package test.advent.day18

import org.jgrapht.Graphs
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.DefaultUndirectedGraph
import java.io.File


val day = 18;
val file = File("src/main/resources/day${day}/input")

fun main() {
    val input = file.readLines().map { it.toList() }
    input.forEach(::println)
    // get all keys plus positions
    val points = getPoints(input)
    val entrance = points.find { it.entrance }!!
    val keys = points.filter { it.key in 'a'..'z' }
    val doors = points.filter { it.door in 'A'..'Z' }.map { it.door!! }

    // build graph of connected tunnels?
    val edges = getEdges(points)
//    edges.forEach(::println)
    val graph = DefaultUndirectedGraph<Point, Edge>(Edge::class.java)
    points.forEach { graph.addVertex(it) }
    edges.forEach { graph.addEdge(it.source, it.target, it) }
    println(graph)
    // try out all possibilities key orders and find shortest path for each, TSP?
    val routes = findValidRoutes(entrance, keys, doors, graph).map { it.reversed() }
    println(routes)

    val results = routes
        .map { findShortestPath(listOf(entrance) + it, graph) }
        .filter { it.second.isNotEmpty() }
    results.forEach { println("${it.first.map { it.key }}, length: ${it.second.size}, steps: ${it.second}") }
}
var routesFound = 0

fun findValidRoutes(
    currentPlace: Point,
    keysStillToGet: List<Point>,
    closedDoors: List<Char>,
    originalGraph: DefaultUndirectedGraph<Point, Edge>
): Set<List<Point>> {
//    println("current $currentPlace, keys to get: $keysStillToGet, closedDoors: $closedDoors")
    val freshGraph = DefaultUndirectedGraph<Point, Edge>(Edge::class.java)
    Graphs.addGraph(freshGraph, originalGraph)
    val edgeByDoor =
        freshGraph.edgeSet().filter { it.hasDoor() }.filter { it.getDoor() in closedDoors }.groupBy { it.getDoor() }
    freshGraph.removeAllEdges(edgeByDoor.values.flatten())

    if (keysStillToGet.isEmpty()) {
        if (routesFound++ % 100 == 0) println(routesFound)
        return setOf(emptyList())
    }

    val result: MutableSet<List<Point>> = mutableSetOf()
    for (key in keysStillToGet) {
        val dijkstraShortestPath = DijkstraShortestPath(freshGraph)
        val shortestPath = dijkstraShortestPath
            .getPath(currentPlace, key)

        if (shortestPath != null) {
            findValidRoutes(key,
                keysStillToGet - key,
                closedDoors - (key.key!!.toUpperCase()),
                originalGraph)
                .forEach { item -> result.add(item + key)}
        }
    }
    return result
}

fun <T> permutations(list: List<T>): Set<List<T>> {
    println("current $list")

    if (list.isEmpty()) return setOf(emptyList())

    val result: MutableSet<List<T>> = mutableSetOf()
    for (i in list.indices) {
        permutations(list - list[i]).forEach { item ->
            result.add(item + list[i])
        }
    }
    return result
}

fun findShortestPath(
    keys: List<Point>,
    originalGraph: DefaultUndirectedGraph<Point, Edge>
): Pair<List<Point>, List<Point>> {
//    keys.forEach(::println)
    val freshGraph = DefaultUndirectedGraph<Point, Edge>(Edge::class.java)
    Graphs.addGraph(freshGraph, originalGraph)
//    freshGraph.edgeSet().forEach(::println)
    val edgeByDoor = freshGraph.edgeSet().filter { it.hasDoor() }.groupBy { it.getDoor() }
//    edgeByDoor.forEach(::println)
    freshGraph.removeAllEdges(edgeByDoor.values.flatten())
    val dijkstraShortestPath = DijkstraShortestPath(freshGraph)
    val totalPath = mutableListOf<Point>()
    for (k in 0 until (keys.size - 1)) {
        val shortestPath = dijkstraShortestPath
            .getPath(keys[k], keys[k + 1])
        val shortestPathPoints = shortestPath?.vertexList ?: return Pair(keys, emptyList())
//        println("shortest path: from ${keys[k]} to ${keys[k+1]}: \n$shortestPathPoints")
        edgeByDoor[keys[k + 1].key?.toUpperCase()]?.forEach { freshGraph.addEdge(it.source, it.target, it) }
        totalPath += shortestPathPoints.subList(1, shortestPathPoints.size)
    }
    return Pair(keys, totalPath)
}

private fun getPoints(input: List<List<Char>>): MutableList<Point> {
    val points = mutableListOf<Point>()
    for (y in 1 until (input.size - 1)) {
        for (x in 1 until (input[0].size - 1)) {
            val c = input[y][x]
            val p = when (c) {
                '#' -> null
                '.' -> Point(x, y)
                '@' -> Point(x, y, entrance = true)
                in 'a'..'z' -> Point(x, y, key = c)
                in 'A'..'Z' -> Point(x, y, door = c)
                else -> throw IllegalArgumentException("wow")
            }
            p?.let { points.add(p) }
        }
    }
    return points
}

private fun getEdges(points: List<Point>): List<Edge> {
    val edges = mutableListOf<Edge>()
    for (p in points) {
        points.filter { it.neighbours(p) }.forEach { edges += Edge(p, it) }
    }
    return edges
}

data class Edge(val source: Point, val target: Point) {
    fun hasDoor(): Boolean = (source.door != null || target.door != null)

    fun getDoor(): Char = source.door ?: (target.door ?: throw IllegalStateException("expected door"))
}

data class Point(val x: Int, val y: Int, val key: Char? = null, val door: Char? = null, val entrance: Boolean = false) {

    fun neighbours(p: Point): Boolean {
        return (x == p.x && (y == p.y - 1 || y == p.y + 1)) ||
                (y == p.y && (x == p.x - 1 || x == p.x + 1))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Point

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }


}

class Vault(val map: List<List<Char>>)
