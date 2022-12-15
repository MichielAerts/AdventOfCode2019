package test.advent.edition2022.day5

import test.advent.edition2022.getOrThrow
import test.advent.edition2022.splitBy
import test.advent.edition2022.subListTillEnd
import java.io.File

val day = 5;
val file = File("src/main/resources/edition2022/day${day}/input")

class Puzzle(private val input: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val (stacksInput, movesInput) = input.splitBy { it.isEmpty() }
        val moves = movesInput.map { Move.create(it) }
        val stacks = Stacks.create(stacksInput)
        moves.forEach { stacks.performMove(it) }
        println(stacks.getTop())
    }
    
    fun runPart2() {
        println("Part 2 of Day $day")
        val (stacksInput, movesInput) = input.splitBy { it.isEmpty() }
        val moves = movesInput.map { Move.create(it) }
        val stacks = Stacks.create(stacksInput)
        moves.forEach { stacks.performMove9001(it) }
        println(stacks.getTop())   
    }
}
// top of stack is first one in the deque 
data class Stacks(val stacks: MutableMap<Int, ArrayDeque<Char>>) {
    
    fun performMove(move: Move) {
        val target = stacks.getOrThrow(move.target)
        val source = stacks.getOrThrow(move.source)
        for (i in 1..move.quantity) {
            target.addFirst(source.removeFirst())
        }
//        println(stacks)
    }

    fun performMove9001(move: Move) {
        val target = stacks.getOrThrow(move.target)
        val source = stacks.getOrThrow(move.source)
        val craneStack = ArrayDeque<Char>()
        for (i in 1..move.quantity) {
            craneStack.addFirst(source.removeFirst())
        }
        for (i in 1..move.quantity) {
            target.addFirst(craneStack.removeFirst())
        }
//        println(stacks)
    }
    
    fun getTop(): String = (1..stacks.size).map { stacks.getOrThrow(it).first() }.joinToString("")

    companion object {
        fun create(input: List<String>): Stacks {
            val stacks = mutableMapOf<Int, ArrayDeque<Char>>()
            val stackPositions = mutableMapOf<Int, Int>()
            val stackNumbers = input.last()
            for ((idx, v) in stackNumbers.withIndex()) {
                if (v.isDigit()) {
                    stacks[v.digitToInt()] = ArrayDeque()
                    stackPositions[idx] = v.digitToInt()
                }
            }
            for (row in input.reversed().subListTillEnd(1)) {
                for ((idx, v) in row.withIndex()) {
                    if (v in 'A'..'Z') {
                        // top of stack is first one in the deque 
                        stacks.getOrThrow(stackPositions.getOrThrow(idx)).addFirst(v)
                    }
                }
            }
            return Stacks(stacks)
        }
    }
}

data class Move(val quantity: Int, val source: Int, val target: Int) {
    
    companion object {
        private val moveRegex = """move (\d+) from (\d+) to (\d+)""".toRegex()
        fun create(input: String): Move {
            //move 1 from 2 to 1
            val (_, quantity, source, target) = moveRegex.find(input)?.groupValues
                ?: throw IllegalStateException("no match on $input")
            return Move(quantity.toInt(), source.toInt(), target.toInt())
        }
    }
}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

