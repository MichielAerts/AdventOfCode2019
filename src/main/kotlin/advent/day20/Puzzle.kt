package test.advent.day20

import org.jgrapht.Graphs
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultDirectedWeightedGraph
import org.jgrapht.graph.DefaultWeightedEdge
import java.io.File

private val <E> DefaultDirectedWeightedGraph<Point, E>.entrance: Point
    get() = vertexSet().first { it.portal?.first == "AA" && it.z == 0 }

private val <E> DefaultDirectedWeightedGraph<Point, E>.exit: Point
    get() = vertexSet().first { it.portal?.first == "ZZ" && it.z == 0 }

val day = 20;
val file = File("src/main/resources/day${day}/input")

fun main() {
    val input = file.readLines().map { it.toList() }
    input.forEach { println(it.joinToString("")) }

    // build graph of connected tunnels?
    val points = getPoints(input)
    val edges = getEdges(points)

    val graph = DefaultDirectedGraph<Point, DetailedEdge>(DetailedEdge::class.java)
    graph.apply {
        for (point in points) addVertex(point)
        for (edge in edges) addEdge(edge.source, edge.target, edge)
    }
    // part 1: 656
//    val shortestPath = DijkstraShortestPath(graph)
//        .getPath(graph.entrance, graph.exit)?.vertexList ?: throw IllegalStateException("just couldn't")
//    println("shortest path: ${shortestPath.size - 1} steps, route: $shortestPath")

    //part 2 simplify graph, add recursive levels, check shortest path
    val outerGraph = createSimplifiedGraph(graph)
    val recursiveGraph = createRecursiveGraph(outerGraph, 100)
    val shortestPath = DijkstraShortestPath.findPathBetween(recursiveGraph, recursiveGraph.entrance, recursiveGraph.exit)?.edgeList ?: throw IllegalStateException("just couldn't")
    println("shortest path: ${shortestPath.sumBy { it.moves }} steps")
}

fun createRecursiveGraph(
    outermostGraph: DefaultDirectedWeightedGraph<Point, Edge>,
    levels: Int
): DefaultDirectedWeightedGraph<Point, Edge> {
    val fullGraph = DefaultDirectedWeightedGraph<Point, Edge>(Edge::class.java)
    Graphs.addGraph(fullGraph, outermostGraph)
    for (level in 1..levels) {
        fullGraph.apply {
            vertexSet().filter { it.z == level - 1 && it.portal?.first != "AA" && it.portal?.first != "ZZ" }
                .map { Point(it.x, it.y, it.portal, level) }
                .forEach { addVertex(it) }
            edgeSet().filter { it.source.z == level - 1 && !it.cross && !it.toEntranceOrExit() }
                .map {
                    Edge(
                        source = Point(it.source.x, it.source.y, it.source.portal, level),
                        target = Point(it.target.x, it.target.y, it.target.portal, level),
                        moves = it.moves
                    )
                }.forEach { 
                    val edge = Edge(it.source, it.target, it.moves)
                    addEdge(it.source, it.target, edge) 
                    setEdgeWeight(edge, edge.moves.toDouble()) }
            vertexSet().filter { it.z == level - 1 && it.portal?.second == Side.INNER }
                .forEach { outerPortalPoint ->
                    apply {
                        val innerPortalPoint = fullGraph.vertexSet().first {
                            it.z == level && it.portal?.first == outerPortalPoint.portal?.first
                                    && it.portal?.second == Side.OUTER
                        }
                        println("adding $outerPortalPoint <> $innerPortalPoint")
                        val oToI = Edge(outerPortalPoint, innerPortalPoint, 1, cross = true)
                        addEdge(outerPortalPoint, innerPortalPoint, oToI)
                        setEdgeWeight(oToI, 1.0)
                        val iToO = Edge(innerPortalPoint, outerPortalPoint, 1, cross = true)
                        addEdge(innerPortalPoint, outerPortalPoint, iToO)
                        setEdgeWeight(iToO, 1.0)
                    }
                }
        }
    }
    return fullGraph
}

fun createSimplifiedGraph(bigGraph: DefaultDirectedGraph<Point, DetailedEdge>): DefaultDirectedWeightedGraph<Point, Edge> {
    val graph = DefaultDirectedWeightedGraph<Point, Edge>(Edge::class.java)
    bigGraph.vertexSet().filter { it.portal != null }.forEach { graph.addVertex(it) }
    val dijkstraShortestPath = DijkstraShortestPath(bigGraph)
    for (source in graph.vertexSet()) {
        for (target in graph.vertexSet()) {
            if (source == target) continue
            val shortestPath = dijkstraShortestPath
                .getPath(source, target)?.vertexList
            if (shortestPath?.count { it.portal != null } == 2) {
                graph.apply {
                    val sToT = Edge(source, target, shortestPath.size - 1)
                    addEdge(source, target, sToT)
                    setEdgeWeight(sToT, sToT.moves.toDouble())
                    val tToS = Edge(target, source, shortestPath.size - 1)
                    addEdge(target, source, tToS)
                    setEdgeWeight(tToS, tToS.moves.toDouble())                    
                }
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
                '#', ' ', in 'A'..'Z' -> null
                '.' -> Point(x, y, input.getPortalNextTo(x, y))
                else -> throw IllegalArgumentException("wow")
            }
            p?.let { points.add(p) }
        }
    }
    return points
}

private fun List<List<Char>>.getPortalNextTo(x: Int, y: Int): Pair<String, Side>? {
    val vSize = this.size
    val hSize = this[0].size
    val neighbouringPoints = listOf(
        Pair(Point(x - 2, y), Point(x - 1, y)),
        Pair(Point(x + 1, y), Point(x + 2, y)),
        Pair(Point(x, y - 2), Point(x, y - 1)),
        Pair(Point(x, y + 1), Point(x, y + 2)),
    )
    for ((f, s) in neighbouringPoints) {
        val potentialPortal = get(f).toString() + get(s)
        if (potentialPortal.matches("[A-Z]{2}".toRegex())) {
            val side = if (x < 5 || x > (hSize - 5) || y < 5 || y > (vSize - 5)) Side.OUTER else Side.INNER
            return Pair(potentialPortal, side)
        }
    }
    return null
}

private fun List<List<Char>>.get(p: Point): Char =
    if (p.y >= this.size || p.y < 0 || p.x >= this[0].size || p.x < 0) ' ' else this[p.y][p.x]

private fun getEdges(points: List<Point>): List<DetailedEdge> {
    val edges = mutableListOf<DetailedEdge>()
    for (p in points) {
        points.filter { it.neighbours(p) }.forEach { edges += DetailedEdge(p, it) }
    }
    return edges
}

data class DetailedEdge(val source: Point, val target: Point)

data class Edge(val source: Point, val target: Point, val moves: Int, val cross: Boolean = false) :
    DefaultWeightedEdge() {
    fun toEntranceOrExit(): Boolean {
        val s = source.portal?.first
        val t = target.portal?.first
        return s == "AA" || s == "ZZ" || t == "AA" || t == "ZZ"
    }

    override fun getSource(): Any {
        return source
    }

    override fun getTarget(): Any {
        return target
    }
    
    override fun getWeight(): Double {
        return moves.toDouble()
    }
}

enum class Side { OUTER, INNER }

data class Point(val x: Int, val y: Int, val portal: Pair<String, Side>? = null, val z: Int = 0) {
    fun neighbours(p: Point): Boolean {
        return (x == p.x && (y == p.y - 1 || y == p.y + 1)) ||
                (y == p.y && (x == p.x - 1 || x == p.x + 1))
    }
}
