package test.advent.edition2022.day16

import test.advent.edition2022.getOrThrow
import java.io.File
import java.util.*

val day = 16
val file = File("src/main/resources/edition2022/day${day}/input")

class Puzzle(private val input: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val valves = input.map { Valve.create(it) }.associateBy { it.name }
        val cave = Cave(valves)
        println(cave.findMaxPressureRelease())
    }
    
    fun runPart2() {
        println("Part 2 of Day $day")
        val valves = input.map { Valve.create(it) }.associateBy { it.name }
        val cave = Cave(valves)
        println(cave.findMaxPressureReleaseWithElephant())
        // 2178 too low
    }
}

data class Cave(val valves: Map<String, Valve>) {
 
    fun findMaxPressureReleaseWithElephant(): Int {
        val comparator: Comparator<PathWithElephant> = compareBy<PathWithElephant> { it.pressureReleased / it.moves }.reversed()
        val queue = PriorityQueue(comparator)
        val maxPressureMap = mutableMapOf<String, MutableMap<Int, Int>>()
        val maxPressureReleasedFoundMap = mutableMapOf<Int, Int>()
        val valvesWithFlow = valves.filter { it.value.flowRate > 0 }
        queue.add(PathWithElephant(0, "AA", "AA", 0, listOf()))
        while(queue.isNotEmpty()) {
            val (pressureReleased, myCurrentPlace, elephantsCurrentPlace, moves, openValves) = queue.remove()
            val maxPressureReleasedFound = maxPressureReleasedFoundMap.getOrPut(moves) { pressureReleased }
            if (pressureReleased > maxPressureReleasedFound) {
                println("new max pressure released for $moves found: $pressureReleased, $myCurrentPlace, $elephantsCurrentPlace, $moves, $openValves")
                maxPressureReleasedFoundMap[moves] = pressureReleased
            }
            if (moves > 13 && pressureReleased < (maxPressureReleasedFound / 1.1)) continue
            if (moves == 26) continue
            val mapKey = "$myCurrentPlace $elephantsCurrentPlace $openValves"
            if (maxPressureMap.containsKey(mapKey)) {
                val pressureReleasedInMoves = maxPressureMap[mapKey]!!
                if (pressureReleasedInMoves.containsKey(pressureReleased) 
                    && pressureReleasedInMoves[pressureReleased]!! <= moves) {
                    continue
                } else {
                    pressureReleasedInMoves[pressureReleased] = moves
                }
            } else {
                maxPressureMap[mapKey] = mutableMapOf(pressureReleased to moves)
            }
            val myValve = valves.getOrThrow(myCurrentPlace)
            val elephantValve = valves.getOrThrow(elephantsCurrentPlace)
            val myActions = myValve.tunnelsTo.map { Action(Action.Type.MOVE, it) } +
                    listOf(myCurrentPlace).filter { it !in openValves && it in valvesWithFlow }.map { Action(Action.Type.OPEN, it) }
            val elephantActions = elephantValve.tunnelsTo.map { Action(Action.Type.MOVE, it) } +
                    listOf(elephantsCurrentPlace).filter { it !in openValves && it in valvesWithFlow && it != myCurrentPlace }.map { Action(Action.Type.OPEN, it) }
            for (myAction in myActions) for (elephantAction in elephantActions) {
                val newPath = when(myAction.type) {
                    Action.Type.MOVE -> {
                        when (elephantAction.type) {
                            Action.Type.MOVE -> PathWithElephant(pressureReleased, myAction.target, 
                                elephantAction.target, moves + 1, openValves)
                            Action.Type.OPEN -> PathWithElephant(pressureReleased + elephantValve.flowRate * (26 - moves - 1), myAction.target,
                                elephantAction.target, moves + 1, openValves + elephantAction.target)
                        }
                    }
                    Action.Type.OPEN -> {
                        when (elephantAction.type) {
                            Action.Type.MOVE -> PathWithElephant(pressureReleased + myValve.flowRate * (26 - moves - 1), myAction.target,
                                elephantAction.target, moves + 1, openValves + myAction.target)
                            Action.Type.OPEN -> PathWithElephant(pressureReleased + myValve.flowRate * (26 - moves - 1) + elephantValve.flowRate * (26 - moves - 1), myAction.target,
                                elephantAction.target, moves + 1, openValves + myAction.target + elephantAction.target)
                        }
                    }
                }
                queue.add(newPath)
            }
        }
        return maxPressureReleasedFoundMap.maxOf { it.value }
    }   
    fun findMaxPressureRelease(): Int {
        val queue = ArrayDeque<Path>()
        val maxPressureMap = mutableMapOf<String, MutableMap<Int, Int>>()
        var maxPressureReleasedFound = 0
        queue.add(Path(0, "AA", 0, listOf()))
        while(queue.isNotEmpty()) {
            val (pressureReleased, currentPlace, moves, openValves) = queue.remove()
            if (moves == 30) {
                if (pressureReleased > maxPressureReleasedFound) {
                    println("new max pressure released found: $pressureReleased")
                    maxPressureReleasedFound = pressureReleased
                }
                continue
            }
            if (maxPressureMap.containsKey(currentPlace)) {
                val pressureReleasedInMoves = maxPressureMap[currentPlace]!!
                if (pressureReleasedInMoves.containsKey(pressureReleased) 
                    && pressureReleasedInMoves[pressureReleased]!! <= moves) {
                    continue
                } else {
                    pressureReleasedInMoves[pressureReleased] = moves
                }
            } else {
                maxPressureMap[currentPlace] = mutableMapOf(pressureReleased to moves)
            }
            val valve = valves.getOrThrow(currentPlace)
            val actions = valve.tunnelsTo.map { Action(Action.Type.MOVE, it) } + 
                    listOf(currentPlace).filter { it !in openValves }.map { Action(Action.Type.OPEN, it) }
            for (action in actions) {
                val newPath = when(action.type) {
                    Action.Type.MOVE -> Path(pressureReleased, action.target, moves + 1, openValves)
                    Action.Type.OPEN -> Path(pressureReleased + valve.flowRate * (30 - moves - 1), action.target, moves + 1, openValves + action.target)
                }
                queue.add(newPath)
            }
        }        
        return maxPressureReleasedFound
    }
}

data class Action(val type: Type, val target: String) {
    enum class Type { MOVE, OPEN }
}

data class Path(val pressureReleased: Int, val currentPlace: String, val moves: Int,
                val openValves: List<String>)

data class PathWithElephant(val pressureReleased: Int, val myCurrentPlace: String, val elephantsCurrentPlace: String,
                            val moves: Int, val openValves: List<String>)

data class Valve(val name: String, val flowRate: Int, val tunnelsTo: List<String>) {
    companion object {
        private val regex = """Valve (\w+) has flow rate=(\d+); tunnels? leads? to valves? (.*)""".toRegex()
        fun create(input: String): Valve {
            val (_, name, flow, tunnels) = regex.find(input)?.groupValues ?: throw IllegalArgumentException(input)
            return Valve(name, flow.toInt(), tunnels.split(", "))
        }
    }
}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

