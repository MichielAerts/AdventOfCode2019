package test.advent.edition2022.day19

import org.paukov.combinatorics3.Generator
import test.advent.edition2022.getOrThrow
import java.io.File
import java.util.*
import java.util.stream.Collectors

val day = 19
val file = File("src/main/resources/edition2022/day${day}/input")

class Puzzle(private val input: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
//        println(blueprints.maxByOrNull { it.getMaxNumberOfGeodes(24) })
//        println(blueprints.sumOf { it.number * it.getMaxNumberOfGeodes(24) })
        val maxRobots = 10
        val robotsToBuildPermutations = Generator.permutation(0, 1, 2, 3)
            .withRepetitions(maxRobots)
            .stream()
//            .filter { it.contains(1) && it.contains(2) && it.indexOf(1) < it.indexOf(2) }
//            .map { it + 3 }
            .collect(Collectors.toList())
        println(robotsToBuildPermutations.size)
        val blueprints = input.map { Blueprint.create(it, robotsToBuildPermutations) }
        println(blueprints.sumOf { it.number * it.getMaxNumberOfGeodesEnumerateRobots(24) })
        // 1262
    }

    fun runPart2() {
        println("Part 2 of Day $day")
        val maxRobots = 10
        val robotsToBuildPermutations = Generator.permutation(0, 1, 2, 3)
            .withRepetitions(maxRobots)
            .stream()
            .collect(Collectors.toList())
        println(robotsToBuildPermutations.size)
        val blueprints = input.map { Blueprint.create(it, robotsToBuildPermutations) }
        println(listOf(blueprints[2]).map { it.getMaxNumberOfGeodesEnumerateRobots(32) })
    }
}

data class Blueprint(
    val number: Int,
    val robotConstructionMap: Map<Int, List<Int>>,
    val robotsToBuildPermutations: PriorityQueue<List<Int>>
) {

    fun getMaxNumberOfGeodesEnumerateRobots(maxMinutes: Int): Int {
        println("blueprint: $number")
        var currentMaxGeodes = 0
        while (!robotsToBuildPermutations.isEmpty()) {
            val robotsToBuild = robotsToBuildPermutations.remove()
            val resources = mutableListOf(0, 0, 0, 0)
            val robots = mutableListOf(1, 0, 0, 0)
            var idxRobotToBuild = 0
            for (i in 1..maxMinutes) {
                if (idxRobotToBuild >= robotsToBuild.size) {
                    (0..3).forEach { robotsToBuildPermutations.add(robotsToBuild + it) }
                    break
                }
                val nextRobotToBuild = robotsToBuild[idxRobotToBuild]
                if (canBuild(nextRobotToBuild, resources)) {
                    (0..3).forEach {
                        resources[it] =
                            resources[it] + robots[it] - robotConstructionMap.getOrThrow(nextRobotToBuild)[it]
                    }
                    robots[nextRobotToBuild] = robots[nextRobotToBuild] + 1
                    idxRobotToBuild++
                } else {
                    (0..3).forEach {
                        resources[it] = resources[it] + robots[it]
                    }
                }
                if (cantSurpassAnyMore(robots, resources, currentMaxGeodes, i, maxMinutes)) break
            }
            val geodes = resources[3]
            if (geodes > currentMaxGeodes) {
                currentMaxGeodes = geodes
                println("found max geodes: $currentMaxGeodes")
            }
        }
        return currentMaxGeodes
    }

    private fun canBuild(nextRobotToBuild: Int, resources: MutableList<Int>): Boolean {
        val resourcesNeeded = robotConstructionMap.getOrThrow(nextRobotToBuild)
        return (0..3).all { resources[it] >= resourcesNeeded[it] }
    }

    fun getMaxNumberOfGeodes(maxMinutes: Int): Int {
        val comparator = compareBy<State> { it.resources[3] }.reversed()
        val queue = PriorityQueue(comparator)
        val cache = mutableMapOf<String, Int>()
        queue.add(State())
        var currentMaxGeodes = 0
        while (queue.isNotEmpty()) {
            val (robots, resources, minutesPassed) = queue.remove()
            if (minutesPassed == maxMinutes) {
                val geodes = resources[3]
                if (geodes > currentMaxGeodes) {
                    currentMaxGeodes = geodes
                    println("found max geodes: $currentMaxGeodes")
                }
                continue
            }
            if (cantSurpassAnyMore(robots, resources, currentMaxGeodes, minutesPassed, maxMinutes)) continue
            val options = canBuildRobots(resources) + DontBuild.DONTBUILD
            // assume factory can only build one robot at a time?
            for (option in options) {
                // calculate new state after minute and add
                // generate resources
                var newResources = resources.toList()
                val newlyGeneratedResources = robots.toList()
                val newRobots = robots.toMutableList()
                when (option) {
                    is Robot -> {
                        newResources = (0..3).map {
                            newResources[it] + newlyGeneratedResources[it] - robotConstructionMap.getOrThrow(option.idx)[it]
                        }
                        newRobots[option.idx] = newRobots[option.idx] + 1
                    }
                    is DontBuild -> {
                        newResources = (0..3).map { newResources[it] + newlyGeneratedResources[it] }
                    }
                }
                val newState = State(newRobots, newResources, minutesPassed + 1)
//                println("adding $newState")
                val key = newState.toString()
                if (cache.containsKey(key) && cache[key]!! <= minutesPassed + 1) {
                    continue
                } else {
                    cache[key] = minutesPassed + 1
                }
                queue.add(newState)
            }
        }
        return currentMaxGeodes
    }

    private fun cantSurpassAnyMore(
        robots: List<Int>,
        resources: List<Int>,
        currentMaxGeodes: Int,
        minutesPassed: Int,
        maxMinutes: Int
    ): Boolean {
        if (currentMaxGeodes == 0) return false
        val minutesToGo = maxMinutes - minutesPassed
        val maxGeodeProduction = mapOf(
            1 to 0, 2 to 1, 3 to 3, 4 to 6, 5 to 10,
            6 to 15, 7 to 21, 8 to 28, 9 to 36, 10 to 45
        )
        val maxGeodes =
            resources[3] + robots[3] * minutesToGo + maxGeodeProduction.getOrDefault(minutesToGo, currentMaxGeodes + 1)
        return maxGeodes <= currentMaxGeodes
    }

    enum class Robot(val idx: Int) : Option {
        OREROBOT(0), CLAYROBOT(1),
        OBSIDIANROBOT(2), GEODEROBOT(3);

        companion object {
            fun get(idx: Int) = values().find { it.idx == idx } ?: throw IllegalArgumentException()
        }
    }

    enum class DontBuild : Option { DONTBUILD }
    interface Option

    private fun canBuildRobots(resources: List<Int>): List<Option> =
        Robot.values()
            .filter { robot -> (0..3).all { robotConstructionMap.getOrThrow(robot.idx)[it] <= resources[it] } }

    data class State(
        val robots: List<Int> = listOf(1, 0, 0, 0),
        val resources: List<Int> = listOf(0, 0, 0, 0),
        val minutesPassed: Int = 0
    ) {
        override fun toString(): String =
            robots.joinToString("") + resources.joinToString("")
    }

    companion object {
        private val regex =
            """Blueprint (\d+): Each ore robot costs (\d+) ore. Each clay robot costs (\d+) ore. Each obsidian robot costs (\d+) ore and (\d+) clay. Each geode robot costs (\d+) ore and (\d+) obsidian.""".toRegex()

        fun create(input: String, robotsToBuildPermutations: MutableList<List<Int>>): Blueprint {
            val (_, no, oreRobotOreCost, clayRobotOreCost, obsidianRobotOreCost,
                obsidianRobotClayCost, geodeRobotOreCost, geodeRobotObsidianCost) = regex.find(input)?.groupValues
                ?: throw IllegalArgumentException("couldn't match ")
            val queue = PriorityQueue(compareBy<List<Int>> { it.size }.reversed())
            queue.addAll(robotsToBuildPermutations)
            return Blueprint(
                no.toInt(), mapOf(
                    0 to listOf(oreRobotOreCost.toInt(), 0, 0, 0),
                    1 to listOf(clayRobotOreCost.toInt(), 0, 0, 0),
                    2 to listOf(obsidianRobotOreCost.toInt(), obsidianRobotClayCost.toInt(), 0, 0),
                    3 to listOf(geodeRobotOreCost.toInt(), 0, geodeRobotObsidianCost.toInt(), 0)
                ), queue
            )
        }
    }
}

private operator fun List<String>.component6(): String = this[5]
private operator fun List<String>.component7(): String = this[6]
private operator fun List<String>.component8(): String = this[7]

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
//    puzzle.runPart1()
    puzzle.runPart2()
}

