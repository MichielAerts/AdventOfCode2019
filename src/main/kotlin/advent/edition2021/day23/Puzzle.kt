package test.advent.edition2021.day23

import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.DefaultDirectedGraph
import test.advent.edition2021.Pos
import test.advent.edition2021.findOrThrow
import test.advent.edition2021.getOrThrow
import test.advent.edition2021.subListTillEnd
import java.io.File
import java.util.*

val day = 23;
val file = File("src/main/resources/edition2021/day${day}/input")

class Puzzle(private val rawInput: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
//        val burrow = Burrow.createBurrow(rawInput)
//        burrow.print()
//        burrow.findLowestEnergyForOrganizing()
    }

    fun runPart2() {
        println("Part 2 of Day $day")
        println(rawInput)
        val burrow = BigBurrow.createBurrow(rawInput)
        burrow.print()
        burrow.findLowestEnergyForOrganizing()
    }
}

data class BigBurrow(
    val system: DefaultDirectedGraph<BigBurrowSpace, BigBurrowEdge>,
    val routes: List<BigBurrowRoute>,
    val initialPositions: String,
    val finalPosition: String,
) {

    fun findLowestEnergyForOrganizing() {
        // add map string, Int to keep track of lowest energy. create string representation of graph
        val pq = PriorityQueue<State>(compareBy { it.energy })
        val lowEnergyMap: MutableMap<String, Int> = mutableMapOf(initialPositions to 0)
        val energies = mutableSetOf(0)
        pq.add(State(initialPositions, 0))
        while (!pq.isEmpty()) {
            val (positions, energy) = pq.remove()
            if (energy % 1000 == 0 && energy !in energies) {
                println("current energy $energy")
                energies.add(energy)
            }
            if (positions == finalPosition) {
                println("new path with energy: $energy")
                continue
            }
            if (positions == "A........BDBC..DCB.DBACADCA") {
                println("current pos $positions, $energy")
            }
            val pods = getPods(positions)
            for (podAndPos in pods) {
                val (pod, pos) = podAndPos
                val destinations = when {
                    // in the right room, all below are also right
                    pos.inRoom && pod.name == pos.room && podsBelow(pos, pods).none { it != pod } -> listOf() 
                    // needs to get out
                    pos.inRoom -> getPositions { (it.inHallway && !it.rightOutsideRoom) || pod.name == it.room }
                    pos.inHallway -> getPositions { pod.name == it.room }
                    else -> throw IllegalStateException("shouldn't reach this")
                }.filter { it != pos }
//                println("pod $pod at $pos is on the move")
                val routes =
                    destinations.map { dest -> routes.findOrThrow { it.start == pos && it.end == dest } }
                        .filter { it.isReachable(positions, pod) }

                for (route in routes) {
//                    println("trying route $route")
                    val newPositions = updatePositionForMove(positions, pod, pos, route.end)
                    val newEnergy = energy + pod.energy * (route.route.size - 1)
                    val newState = State(newPositions, newEnergy)
                    val lowestEnergy = lowEnergyMap.getOrDefault(newPositions, Int.MAX_VALUE)
                    if (newEnergy >= lowestEnergy) continue
                    lowEnergyMap[newPositions] = newEnergy
                    pq.add(newState)
                }
            }
        }
    }

    private fun podsBelow(space : BigBurrowSpace, pods: List<Pair<Pod, BigBurrowSpace>>): List<Pod> 
    = pods.filter { it.second.isBelow(space) }.map { it.first } 

    private fun getPositions(filter: (BigBurrowSpace) -> Boolean): List<BigBurrowSpace> = system.vertexSet().filter(filter)

    fun print(positions: String = initialPositions) {
        val maxX = system.vertexSet().maxOf { it.x }
        val maxY = system.vertexSet().maxOf { it.y }
        var i = 0
        for (y in 0..maxY + 1) {
            for (x in 0..maxX + 1) {
                val space = system.vertexSet().find { it.x == x && it.y == y }
                print(if (space != null) positions[i++] else '#')
            }
            println()
        }
    }

    companion object {

        lateinit var posToIndex: SortedMap<BigBurrowSpace, Int>
        lateinit var indexToPos: SortedMap<Int, BigBurrowSpace>

        fun getPods(positions: String): List<Pair<Pod, BigBurrowSpace>> =
            positions.mapIndexed { index, c -> Pair(c, indexToPos.getOrThrow(index)) }
                .filter { it.first in 'A'..'D' }
                .map { Pair(Pod.valueOf(it.first.toString()), it.second) }

        fun getSpacesAndPods(positions: String): Map<BigBurrowSpace, Pod?> =
            positions.mapIndexed { index, c -> indexToPos.getOrThrow(index) to Pod.find(c) }.toMap()

        fun updatePositionForMove(oldPositions: String, pod: Pod, start: BigBurrowSpace, destination: BigBurrowSpace): String {
            val positions = oldPositions.toCharArray()
            positions[posToIndex.getOrThrow(start)] = '.'
            positions[posToIndex.getOrThrow(destination)] = pod.name[0]
            return String(positions)
        }

        fun createBurrow(input: List<String>): BigBurrow {
            val spaces =
                input.flatMapIndexed { y, row -> row.mapIndexed { x, p -> BigBurrowSpace.createSpace(x, y, p) } }.filterNotNull()
            val edges = spaces.map { s -> Pair(s, spaces.getConnectingSpaces(s)) }
                .flatMap { sAndN ->
                    sAndN.second.flatMap { neighbour ->
                        listOf(
                            BigBurrowEdge(sAndN.first, neighbour),
                            BigBurrowEdge(neighbour, sAndN.first)
                        )
                    }
                }
            val graph = DefaultDirectedGraph<BigBurrowSpace, BigBurrowEdge>(BigBurrowEdge::class.java)
            graph.apply {
                spaces.forEach { addVertex(it) }
                edges.forEach { addEdge(it.source, it.target, it) }
            }
            val dijkstraShortestPath = DijkstraShortestPath(graph)
            val routes = mutableListOf<BigBurrowRoute>()
            for (first in spaces) for (last in spaces) {
                if (first != last) routes.add(BigBurrowRoute(first, last, dijkstraShortestPath.getPath(first, last).vertexList))
            }
            val sortedPoints = spaces.sortedWith(compareBy({ it.y }, { it.x }))
            indexToPos = sortedPoints.mapIndexed { index, space -> index to space }.toMap().toSortedMap()
            posToIndex = sortedPoints.mapIndexed { index, space -> space to index }.toMap()
                .toSortedMap(compareBy({ it.y }, { it.x }))
            println(indexToPos)
            println(posToIndex)
            val initialPositions =
                input.joinToString("") { r -> r.toList().filter { it == '.' || it in 'A'..'D' }.joinToString("") }
            return BigBurrow(graph, routes, initialPositions, "...........ABCDABCDABCDABCD")
        }
    }
}

data class State(val positions: String, val energy: Int)

data class BigBurrowRoute(val start: BigBurrowSpace, val end: BigBurrowSpace, val route: List<BigBurrowSpace>) {
    fun isReachable(positions: String, movingPod: Pod): Boolean {
        val spacesAndPods = BigBurrow.getSpacesAndPods(positions)
        val spacesWithPods = spacesAndPods.filterValues { it != null }.keys

        if (route.subListTillEnd(1).any { it in spacesWithPods }) return false
        val destination = route.last()
        if (destination.inRoom) {
            //TODO check logic
            // if going into a room, all spaces below must have the same pod 
            
            val spacesBelow = BigBurrowSpace.getSpacesBelowInRoom(destination)
            val samePodsBelow = spacesAndPods.entries.filter { (space, pod) -> space.pos in spacesBelow && movingPod == pod }
            if (samePodsBelow.size != spacesBelow.size) return false
        }
        return true
    }
}


enum class Pod(val energy: Int) {
    A(1), B(10), C(100), D(1000);

    companion object {
        fun find(c: Char): Pod? = values().find { it.name == c.toString() }
    }
}

data class BigBurrowSpace(
    val x: Int,
    val y: Int,
    val inRoom: Boolean,
    val inHallway: Boolean = !inRoom,
    val room: String? = null,
    val rightOutsideRoom: Boolean = false,
    val pos: Pos = Pos(x, y)
) {

    fun isBelow(space: BigBurrowSpace): Boolean = x == space.x && y > space.y

    companion object {
        private val rightOutsideRoom = listOf(Pos(3, 1), Pos(5, 1), Pos(7, 1), Pos(9, 1))

        private val posToRoom = mapOf(
            Pos(3, 2) to "A",
            Pos(3, 3) to "A",
            Pos(3, 4) to "A",
            Pos(3, 5) to "A",
            Pos(5, 2) to "B",
            Pos(5, 3) to "B",
            Pos(5, 4) to "B",
            Pos(5, 5) to "B",
            Pos(7, 2) to "C",
            Pos(7, 3) to "C",
            Pos(7, 4) to "C",
            Pos(7, 5) to "C",
            Pos(9, 2) to "D",
            Pos(9, 3) to "D",
            Pos(9, 4) to "D",
            Pos(9, 5) to "D",
        )
        private val roomToPos = mapOf(
            "A" to listOf(Pos(3, 2), Pos(3, 3), Pos(3, 4), Pos(3, 5)),
            "B" to listOf(Pos(5, 2), Pos(5, 3), Pos(5, 4), Pos(5, 5)),
            "C" to listOf(Pos(7, 2), Pos(7, 3), Pos(7, 4), Pos(7, 5)),
            "D" to listOf(Pos(9, 2), Pos(9, 3), Pos(9, 4), Pos(9, 5)),
        )
        private val hallway = listOf(
            Pos(1, 1),
            Pos(2, 1),
            Pos(3, 1),
            Pos(4, 1),
            Pos(5, 1),
            Pos(6, 1),
            Pos(7, 1),
            Pos(8, 1),
            Pos(9, 1),
            Pos(10, 1),
            Pos(11, 1),
        )

        fun getSpacesBelowInRoom(space: BigBurrowSpace): List<Pos> {
            val rooms = roomToPos.getOrThrow(space.room!!)
            return rooms.filter { it.y > space.y }
        }

        fun createSpace(x: Int, y: Int, c: Char): BigBurrowSpace? {
            if (!(c == '.' || c in 'A'..'D')) return null
            val pos = Pos(x, y)
            return BigBurrowSpace(
                x = x,
                y = y,
                inRoom = pos !in hallway,
                room = posToRoom[pos],
                rightOutsideRoom = pos in rightOutsideRoom)
        }
    }
}




fun List<BigBurrowSpace>.getConnectingSpaces(p: BigBurrowSpace): List<BigBurrowSpace> {
    val potentialNeighbours = listOf(
        Pos(p.x, p.y - 1),
        Pos(p.x - 1, p.y),
        Pos(p.x + 1, p.y),
        Pos(p.x, p.y + 1),
    )
    return filter { Pos(it.x, it.y) in potentialNeighbours }
}

data class BigBurrowEdge(val source: BigBurrowSpace, val target: BigBurrowSpace)

// PART 1! 



















data class Route(val start: Space, val end: Space, val route: List<Space>) {
    fun isReachable(positions: String, movingPod: Pod): Boolean {
        val spacesAndPods = Burrow.getSpacesAndPods(positions)
        val spacesWithPods = spacesAndPods.filterValues { it != null }.keys

        if (route.subListTillEnd(1).any { it in spacesWithPods }) return false
        val destination = route.last()
        if (destination.inRoom) {
            val (x, y) = Space.getOtherSpaceInRoom(destination)
            val e = spacesAndPods.entries.find { (k, v) -> k.x == x && k.y == y }!!
            if (e.value != null && movingPod != e.value) return false
        }
        return true
    }
}

data class Space(
    val x: Int,
    val y: Int,
    val inRoom: Boolean,
    val inHallway: Boolean = !inRoom,
    val isBottom: Boolean = false,
    val isTop: Boolean = inRoom && !isBottom,
    val room: String? = null,
    val rightOutsideRoom: Boolean = false
) {

    companion object {
        private val rightOutsideRoom = listOf(Pos(3, 1), Pos(5, 1), Pos(7, 1), Pos(9, 1))
        private val topRoom = 2
        private val bottomRoom = 3

        private val posToRoom = mapOf(
            Pos(3, 2) to "A",
            Pos(3, 3) to "A",
            Pos(5, 2) to "B",
            Pos(5, 3) to "B",
            Pos(7, 2) to "C",
            Pos(7, 3) to "C",
            Pos(9, 2) to "D",
            Pos(9, 3) to "D",
        )
        private val roomToPos = mapOf(
            "A" to listOf(Pos(3, 2), Pos(3, 3)),
            "B" to listOf(Pos(5, 2), Pos(5, 3)),
            "C" to listOf(Pos(7, 2), Pos(7, 3)),
            "D" to listOf(Pos(9, 2), Pos(9, 3)),
        )
        private val hallway = listOf(
            Pos(1, 1),
            Pos(2, 1),
            Pos(3, 1),
            Pos(4, 1),
            Pos(5, 1),
            Pos(6, 1),
            Pos(7, 1),
            Pos(8, 1),
            Pos(9, 1),
            Pos(10, 1),
            Pos(11, 1),
        )

        fun getOtherSpaceInRoom(space: Space): Pos {
            val rooms = roomToPos.getOrThrow(space.room!!)
            return rooms.findOrThrow { it.y != space.y }
        }

        fun createSpace(x: Int, y: Int, c: Char): Space? {
            if (!(c == '.' || c in 'A'..'D')) return null
            val pos = Pos(x, y)
            return Space(
                x = x,
                y = y,
                inRoom = pos !in hallway,
                isBottom = (pos !in hallway && pos.y == bottomRoom),
                room = posToRoom[pos],
                rightOutsideRoom = pos in rightOutsideRoom)
        }
    }
}

data class Burrow(
    val system: DefaultDirectedGraph<Space, Edge>,
    val routes: List<Route>,
    val initialPositions: String,
    val finalPosition: String,
) {

    fun findLowestEnergyForOrganizing() {
        // add map string, Int to keep track of lowest energy. create string representation of graph
        val pq = PriorityQueue<State>(compareBy { it.energy })
        val lowEnergyMap: MutableMap<String, Int> = mutableMapOf(initialPositions to 0)
        pq.add(State(initialPositions, 0))
        while (!pq.isEmpty()) {
            val (positions, energy) = pq.remove()
            if (positions == finalPosition) {
                println("new path with energy: $energy")
                continue
            }
            val pods = getPods(positions)
            for (podAndPos in pods) {
                val (pod, pos) = podAndPos
                val destinations = when {
                    pos.inRoom && pod.name == pos.room && (pos.isBottom || pos.isTop && pod == otherPodInRoom(pods, Space.getOtherSpaceInRoom(pos))) -> listOf()
                    pos.inRoom -> getPositions { (it.inHallway && !it.rightOutsideRoom) || pod.name == it.room }
                    pos.inHallway -> getPositions { pod.name == it.room }
                    else -> throw IllegalStateException("shouldn't reach this")
                }.filter { it != pos }
//                println("pod $pod at $pos is on the move")
                val routes =
                    destinations.map { dest -> routes.findOrThrow { it.start == pos && it.end == dest } }
                        .filter { it.isReachable(positions, pod) }

                for (route in routes) {
//                    println("trying route $route")
                    val newPositions = updatePositionForMove(positions, pod, pos, route.end)
                    val newEnergy = energy + pod.energy * (route.route.size - 1)
                    val newState = State(newPositions, newEnergy)
                    val lowestEnergy = lowEnergyMap.getOrDefault(newPositions, Int.MAX_VALUE)
                    if (newEnergy >= lowestEnergy) continue
                    lowEnergyMap[newPositions] = newEnergy
                    pq.add(newState)
                }
            }
        }
    }

    private fun otherPodInRoom(pods: List<Pair<Pod, Space>>, space: Pos): Pod? = pods.find { it.second.x == space.x && it.second.y == space.y }?.first

    private fun getPositions(filter: (Space) -> Boolean): List<Space> = system.vertexSet().filter(filter)

    fun print(positions: String = initialPositions) {
        val maxX = system.vertexSet().maxOf { it.x }
        val maxY = system.vertexSet().maxOf { it.y }
        var i = 0
        for (y in 0..maxY + 1) {
            for (x in 0..maxX + 1) {
                val space = system.vertexSet().find { it.x == x && it.y == y }
                print(if (space != null) positions[i++] else '#')
            }
            println()
        }
    }

    companion object {

        lateinit var posToIndex: SortedMap<Space, Int>
        lateinit var indexToPos: SortedMap<Int, Space>

        fun getPods(positions: String): List<Pair<Pod, Space>> =
            positions.mapIndexed { index, c -> Pair(c, indexToPos.getOrThrow(index)) }
                .filter { it.first in 'A'..'D' }
                .map { Pair(Pod.valueOf(it.first.toString()), it.second) }

        fun getSpacesAndPods(positions: String): Map<Space, Pod?> =
            positions.mapIndexed { index, c -> indexToPos.getOrThrow(index) to Pod.find(c) }.toMap()

        fun updatePositionForMove(oldPositions: String, pod: Pod, start: Space, destination: Space): String {
            val positions = oldPositions.toCharArray()
            positions[posToIndex.getOrThrow(start)] = '.'
            positions[posToIndex.getOrThrow(destination)] = pod.name[0]
            return String(positions)
        }

        fun createBurrow(input: List<String>): Burrow {
            val spaces =
                input.flatMapIndexed { y, row -> row.mapIndexed { x, p -> Space.createSpace(x, y, p) } }.filterNotNull()
            val edges = spaces.map { s -> Pair(s, spaces.getConnectingSpaces(s)) }
                .flatMap { sAndN ->
                    sAndN.second.flatMap { neighbour ->
                        listOf(
                            Edge(sAndN.first, neighbour),
                            Edge(neighbour, sAndN.first)
                        )
                    }
                }
            val graph = DefaultDirectedGraph<Space, Edge>(Edge::class.java)
            graph.apply {
                spaces.forEach { addVertex(it) }
                edges.forEach { addEdge(it.source, it.target, it) }
            }
            val dijkstraShortestPath = DijkstraShortestPath(graph)
            val routes = mutableListOf<Route>()
            for (first in spaces) for (last in spaces) {
                if (first != last) routes.add(Route(first, last, dijkstraShortestPath.getPath(first, last).vertexList))
            }
            val sortedPoints = spaces.sortedWith(compareBy({ it.y }, { it.x }))
            indexToPos = sortedPoints.mapIndexed { index, space -> index to space }.toMap().toSortedMap()
            posToIndex = sortedPoints.mapIndexed { index, space -> space to index }.toMap()
                .toSortedMap(compareBy({ it.y }, { it.x }))
            println(indexToPos)
            println(posToIndex)
            val initialPositions =
                input.joinToString("") { r -> r.toList().filter { it == '.' || it in 'A'..'D' }.joinToString("") }
            return Burrow(graph, routes, initialPositions, "...........ABCDABCD")
        }
    }
}

fun List<Space>.getConnectingSpaces(p: Space): List<Space> {
    val potentialNeighbours = listOf(
        Pos(p.x, p.y - 1),
        Pos(p.x - 1, p.y),
        Pos(p.x + 1, p.y),
        Pos(p.x, p.y + 1),
    )
    return filter { Pos(it.x, it.y) in potentialNeighbours }
}

data class Edge(val source: Space, val target: Space)

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

