package test.advent.day6

import java.io.File

val day = 6;
val file = File("src/main/resources/day${day}/input")

data class Node(val name: String, var orbits: Node?) {
    fun calcOrbits(): Int {
        var count = 0;
        var currentNode = this
        while (currentNode.orbits != null) {
            count++
            currentNode = currentNode.orbits!!
        }
        return count
    }

    fun getOrbitPath(): MutableList<String> {
        val list = mutableListOf<String>()
        var currentNode = this
        while (currentNode.orbits != null) {
            list.add(currentNode.name)
            currentNode = currentNode.orbits!!
        }
        return list
    }
}

class Graph(
    val com: Node = Node("COM", null),
    val nodes: MutableMap<String, Node> = hashMapOf("COM" to com)
) {
    fun addNode(pair: Pair<String, String>) {
        val (name, orbits) = pair
        nodes.putIfAbsent(orbits, Node(orbits, null))
        if (nodes.containsKey(name)) {
            nodes[name]?.orbits = nodes[orbits]
        } else {
            nodes[name] = Node(name, nodes[orbits])
        }
    }

    fun calculateTotalOrbits(): Int = nodes.values.map { it.calcOrbits() }.sum()
}

fun main() {

    // guesses 2770, too low
    val graph = Graph()
    val input = file.readLines()
        .map { it.split(")") }
        .map { Pair(it[1], it[0]) }
        .forEach { graph.addNode(it) }
    val allOrbitsYou = graph.nodes["YOU"]?.getOrbitPath()!!
    val allOrbitsSanta = graph.nodes["SAN"]?.getOrbitPath()!!
    val intersect = allOrbitsYou.intersect(allOrbitsSanta)
    val intersectMinusCross = intersect.minusElement(intersect.elementAt(0))
    println(allOrbitsYou)
    println(allOrbitsSanta)
    println(intersect)
    println(intersectMinusCross)
    val orbitsYou = allOrbitsYou - intersectMinusCross
    val orbitsSanta = allOrbitsSanta - intersect
    println(orbitsYou)
    println(orbitsSanta)
    val steps = orbitsYou + orbitsSanta.reversed()
    println(steps)
    println("number of transfers is ${steps.size - 3}")
}
