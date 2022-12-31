package test.advent.edition2022.day20

import test.advent.edition2022.subListTillEnd
import java.io.File
import kotlin.math.absoluteValue

val day = 20
val file = File("src/main/resources/edition2022/day${day}/input")

class Puzzle(private val input: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val file = EncryptedFile.create(input)
        file.mix()
        println(listOf(1000L, 2000L, 3000L).sumOf { file.getNodeXPositionsFrom(file.zero, it).value })
    }
    
    fun runPart2() {
        println("Part 2 of Day $day")
        val decryptionKey = 811589153L
        val file = EncryptedFile.create(input, decryptionKey)
//        println("at start")
//        file.printFile()
        for (i in 1..10) {
            file.mix()
//            println("after mix $i")
//            file.printFile()
        }
        println(listOf(1000L, 2000L, 3000L).map { file.getNodeXPositionsFrom(file.zero, it, false).value })
        println(listOf(1000L, 2000L, 3000L).sumOf { file.getNodeXPositionsFrom(file.zero, it, false).value })
    }
}

data class EncryptedFile(var head: Node, var initialArrangement: List<Pair<Long, Node>>, val zero: Node) {

    fun mix() {
        for ((number, node) in initialArrangement) {
            removeNode(node)
            moveNodeXPositions(node, number)
//            println("moving $node")
//            printFile()
        }
//        printFile()
    }
    
    fun printFile() {
        print("$head,")
        var currentNode = head.nodeAfter
        while(currentNode != head) {
//            print("$currentNode,")
            currentNode = currentNode.nodeAfter
        }
//        println()
    }

    private fun moveNodeXPositions(node: Node, number: Long) {
        val newPos = getNodeXPositionsFrom(node, number)
        // insert node after newPos
        node.nodeBefore = newPos
        node.nodeAfter = newPos.nodeAfter
        newPos.nodeAfter.nodeBefore = node
        newPos.nodeAfter = node
    }

    fun getNodeXPositionsFrom(
        node: Node,
        number: Long,
        withMovingNumber: Boolean = true
    ): Node {
        var newPos = node
        // fix with modulus
        val listSize = if (withMovingNumber) initialArrangement.size - 1 else initialArrangement.size
        val move = number % listSize
        for (i in 1..move.absoluteValue) {
            newPos = if (number > 0) newPos.nodeAfter else newPos.nodeBefore
        }
        newPos = if (number > 0) newPos else newPos.nodeBefore
        return newPos
    }

    private fun removeNode(node: Node): Node {
        node.nodeBefore.nodeAfter = node.nodeAfter
        node.nodeAfter.nodeBefore = node.nodeBefore
        return node
    }

    companion object {
        fun create(input: List<String>, decryptionKey: Long = 1): EncryptedFile {
            val numbers = input.map { it.toLong() * decryptionKey }
            val head = Node(numbers[0])
            var previousNode = head
            var currentNode = head
            lateinit var zeroNode: Node
            val initialArrangement = mutableListOf(Pair(numbers[0], head))
            for (number in numbers.subListTillEnd(1)) {
                currentNode = Node(number)
                currentNode.nodeBefore = previousNode
                previousNode.nodeAfter = currentNode
                initialArrangement.add(Pair(number, currentNode))
                previousNode = currentNode
                if (number == 0L) {
                    zeroNode = currentNode
                }
            }
            currentNode.nodeAfter = head
            head.nodeBefore = currentNode
            return EncryptedFile(head, initialArrangement, zeroNode)
        }
    }
}

data class Node(val value: Long) {
    lateinit var nodeBefore: Node
    lateinit var nodeAfter: Node
    override fun toString(): String = value.toString()
}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
//    puzzle.runPart1()
    puzzle.runPart2()
}

