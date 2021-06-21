package test.advent.day18

import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.DefaultDirectedGraph
import java.io.File

typealias Route = Triple<List<Char>, Int, MutableMap<Int, Pair<Point, List<Edge>>>>

val day = 18;
val file = File("src/main/resources/day${day}/input")

fun main() {
    val rawInput = file.readLines().map { it.toList() }
    val input = replaceCenter(rawInput) // add false to keep original input (for part 1)
    input.forEach(::println)

    // build graph of connected tunnels?
    val points = getPoints(input)
    val edges = getEdges(points)
    val bigGraph = DefaultDirectedGraph<Point, DetailedEdge>(DetailedEdge::class.java)
    points.forEach { bigGraph.addVertex(it) }
    edges.forEach { bigGraph.addEdge(it.source, it.target, it) }

    val graph = createSimplifiedGraph(bigGraph)
    println(graph)

    val vault = Vault(graph)

    // part 1
//    val shortest = vault.findShortestRoute()
//    println(shortest)

    // part 2
    val shortest = vault.findShortestRouteMultiBot()

}

fun replaceCenter(rawInput: List<List<Char>>, replace: Boolean = true): List<List<Char>> {
    val vSize = rawInput.size
    val hSize = rawInput[0].size
    val vHalf = (vSize - 1) / 2
    val hHalf = (hSize - 1) / 2
    val change = mapOf(
        Pair(vHalf + -1, hHalf + -1) to '@',
        Pair(vHalf + 0, hHalf + -1) to '#',
        Pair(vHalf + 1, hHalf + -1) to '@',
        Pair(vHalf + -1, hHalf + 0) to '#',
        Pair(vHalf + 0, hHalf + 0) to '#',
        Pair(vHalf + 1, hHalf + 0) to '#',
        Pair(vHalf + -1, hHalf + 1) to '@',
        Pair(vHalf + 0, hHalf + 1) to '#',
        Pair(vHalf + 1, hHalf + 1) to '@',
    )
    val newGrid = MutableList(vSize) { MutableList(hSize) { ' ' } }
    for (y in 0 until vSize) {
        for (x in 0 until hSize) {
            newGrid[y][x] = rawInput[y][x]
            if (replace) newGrid[y][x] = change.getOrDefault(Pair(y, x), rawInput[y][x])
        }
    }
    return newGrid
}

class Vault(
    val graph: DefaultDirectedGraph<Point, Edge>,
    val allKeys: Set<Char> = graph.vertexSet().filter { it.key in 'a'..'z' }.map { it.key!! }.toSet(),
    val allRoutes: MutableList<Route> = mutableListOf(),
    // per route: collected Keys, moves, map of robots [0 - 3] and their positions and travelled route
    var shortestRoute: Pair<Int, Map<Int, Pair<Point, List<Edge>>>>? = null
) {

//    fun findShortestRoute(): Pair<Int, List<Edge>> {
//        val botPosition = graph.vertexSet().find { it.bot }!!
//        findValidRoutes(botPosition, mutableListOf(), Pair(0, listOf()), mutableMapOf())
//        return shortestRoute ?: throw IllegalStateException("couldn't find a shortest route")
//    }

    fun findShortestRouteMultiBot(): Pair<Int, Map<Int, Pair<Point, List<Edge>>>> {
        val botPositions = graph.vertexSet()
            .filter { it.bot }
            .mapIndexed { i, botPlace -> Pair(i, Pair(botPlace!!, listOf<Edge>())) }
            .toMap().toMutableMap()
        allRoutes.add(Triple(listOf(), 0, botPositions))
        findValidRoutes(mutableMapOf())
        return shortestRoute ?: throw IllegalStateException("couldn't find a shortest route")
    }

    fun findValidRoutes(
//        currentRoute: Triple<List<Char>, Int, MutableMap<Int, List<Edge>>>,
        cache: MutableMap<Point, MutableMap<Set<Char>, Int>>
    ) {
        var newRoutes: MutableList<Route>? = null
        while (newRoutes == null || newRoutes.size > 0) {
            newRoutes = mutableListOf()
            for (route in allRoutes) {
                val (obtainedKeys, moves, routes) = route
                if (obtainedKeys.toSet().size == allKeys.size) {
                    if (shortestRoute?.first == null || moves < shortestRoute?.first!!) {
                        println("found new shortest route ${moves}, ${routes}")
                        shortestRoute = Pair(moves, routes)
                    }
                } else {
                    for ((bot, placeAndRoute) in routes) {
                        val (currentPlace, currentRoute) = placeAndRoute
                        for (edge in graph.outgoingEdgesOf(currentPlace)) {
                            if (edge.hasDoor() && edge.isClosed(obtainedKeys)) continue
                            val newPlace = edge.target
                            val newKeys = obtainedKeys.toMutableList()
                            val newPlaceAndRoutes = routes.toMutableMap()
                            newPlace.key?.let { if (it !in obtainedKeys) newKeys.add(it) }
                            // Triple<List<Char>, Int, MutableMap<Int, Pair<Point, List<Edge>>>>

                            newPlaceAndRoutes[bot] = Pair(newPlace, currentRoute + edge)
                            val newRoute = Triple(newKeys.toList(), moves + edge.moves, newPlaceAndRoutes)
                            if (newRoute.second > (shortestRoute?.first ?: Int.MAX_VALUE)) continue

                            if (cache.containsKey(newPlace)) {
                                if (cache[newPlace]!!.keys.contains(newKeys.toSet())) {
                                    if (newRoute.second >= cache[newPlace]!![newKeys.toSet()]!!) {
                                        continue
                                    } else {
                                        cache[newPlace]!!.put(newKeys.toSet(), newRoute.second)
                                    }
                                } else {
                                    cache[newPlace]!!.put(newKeys.toSet(), newRoute.second)
                                }
                            } else {
                                cache.put(newPlace, mutableMapOf(newKeys.toSet() to newRoute.second))
                            }
//                            println(newRoute)
                            newRoutes.add(newRoute)
//                        findValidRoutes(
//                            cache
//                        )
                        }
                    }
                }
            }
            allRoutes.addAll(newRoutes)
//            allRoutes.forEach(::println)
        }

//    fun findValidRoutes(
//        currentPlace: Point,
//        obtainedKeys: MutableList<Char>,
//        currentRoute: Pair<Int, List<Edge>>,
//        cache: MutableMap<Point, MutableMap<Set<Char>, Int>>
//    ) {
//        if (obtainedKeys.toSet().size == allKeys.size) {
//            if (shortestRoute?.first == null || currentRoute.first < shortestRoute?.first!!) {
//                println("found new shortest route ${currentRoute.first}, ${currentRoute.second.map { it.target.key }}")
//                shortestRoute = currentRoute
//            }
//        }
//
//        for (edge in graph.outgoingEdgesOf(currentPlace)) {
//            val keys = obtainedKeys.toMutableList()
//            if (edge.hasDoor() && edge.isClosed(keys)) continue
//            val newPlace = edge.target
//            newPlace.key?.let { if (it !in keys) keys.add(it) }
//            val newRoute = Pair(currentRoute.first + edge.moves, currentRoute.second + edge)
//            if (newRoute.first > (shortestRoute?.first ?: Int.MAX_VALUE)) continue
//
//            if (cache.containsKey(newPlace)) {
//                if (cache[newPlace]!!.keys.contains(keys.toSet())) {
//                    if (newRoute.first >= cache[newPlace]!![keys.toSet()]!!) {
//                        continue
//                    } else {
//                        cache[newPlace]!!.put(keys.toSet(), newRoute.first)
//                    }
//                } else {
//                    cache[newPlace]!!.put(keys.toSet(), newRoute.first)
//                }
//            } else {
//                cache.put(newPlace, mutableMapOf(keys.toSet() to newRoute.first))
//            }
//
//            findValidRoutes(
//                newPlace,
//                keys.toMutableList(),
//                newRoute,
//                cache
//            )
//        }
    }
}

fun createSimplifiedGraph(bigGraph: DefaultDirectedGraph<Point, DetailedEdge>): DefaultDirectedGraph<Point, Edge> {
    val graph = DefaultDirectedGraph<Point, Edge>(Edge::class.java)
    val entrances = bigGraph.vertexSet().filter { it.bot }
    bigGraph.vertexSet().filter { it.key != null }.forEach { graph.addVertex(it) }
    val dijkstraShortestPath = DijkstraShortestPath(bigGraph)
    for (source in graph.vertexSet()) {
        for (target in graph.vertexSet()) {
            if (source == target) continue
            val shortestPath = dijkstraShortestPath
                .getPath(source, target)?.vertexList
            if (shortestPath?.count { it.key != null } == 2) {
                val doors = shortestPath.filter { it.door != null }.mapNotNull { it.door }
                graph.addEdge(source, target, Edge(source, target, doors, shortestPath.size - 1))
                graph.addEdge(target, source, Edge(target, source, doors, shortestPath.size - 1))
//                println("source: $source, target $target, path: $shortestPath")
            }
        }
    }
    entrances.forEach(graph::addVertex)
    for (entrance in entrances) {
        for (target in graph.vertexSet()) {
            if (entrance == target) continue
            val shortestPath = dijkstraShortestPath
                .getPath(entrance, target)?.vertexList
            if (shortestPath?.count { it.key != null } == 1) {
                val doors = shortestPath.filter { it.door != null }.mapNotNull { it.door }
                graph.addEdge(entrance, target, Edge(entrance, target, doors, shortestPath.size - 1))
//                println("source: $source, target $target, path: $shortestPath")
            }
        }
    }
    return graph
}

private fun getPoints(input: List<List<Char>>): MutableList<Point> {
    val points = mutableListOf<Point>()
    for (y in 1 until (input.size - 1)) {
        for (x in 1 until (input[0].size - 1)) {
            val c = input[y][x]
            val p = when (c) {
                '#' -> null
                '.' -> Point(x, y)
                '@' -> Point(x, y, bot = true)
                in 'a'..'z' -> Point(x, y, key = c)
                in 'A'..'Z' -> Point(x, y, door = c)
                else -> throw IllegalArgumentException("wow")
            }
            p?.let { points.add(p) }
        }
    }
    return points
}

private fun getEdges(points: List<Point>): List<DetailedEdge> {
    val edges = mutableListOf<DetailedEdge>()
    for (p in points) {
        points.filter { it.neighbours(p) }.forEach { edges += DetailedEdge(p, it) }
    }
    return edges
}

data class Edge(val source: Point, val target: Point, var doors: List<Char> = listOf(), val moves: Int) {
    fun hasDoor(): Boolean = doors.isNotEmpty()
    fun isClosed(obtainedKeys: List<Char>): Boolean = doors.any { it.toLowerCase() !in obtainedKeys }
}

data class DetailedEdge(val source: Point, val target: Point)

data class Point(val x: Int, val y: Int, val key: Char? = null, val door: Char? = null, val bot: Boolean = false) {
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