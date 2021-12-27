package test.advent.edition2021.day14

import test.advent.edition2021.findOrThrow
import test.advent.edition2021.subListTillEnd
import java.io.File

val day = 14;
val file = File("src/main/resources/edition2021/day${day}/input")

class Puzzle(private val rawInput: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val template = rawInput[0].toList()
        val rules = rawInput.subListTillEnd(2).map { InsertionRule.createInsertionRule(it) }
        val polymer = Polymer(template, rules)
        
        val steps = 10
        for (i in 1 .. steps) {
            polymer.runStep()    
        }
        println("most: ${polymer.mostOccurring()}, least: ${polymer.leastOccurring()}, result = ${polymer.mostOccurring().value - polymer.leastOccurring().value}")
    }
    
    fun runPart2() {
        println("Part 2 of Day $day")
        val template = rawInput[0].toList()
        val rules = rawInput.subListTillEnd(2).map { InsertionRule.createInsertionRule(it) }
        val polymer = Polymer(template, rules)
        println(polymer.count)

        val steps = 40
        for (i in 1 .. steps) {
            polymer.runSuccinctStep()
            println(polymer.count)
        }
        println("most: ${polymer.mostOccurringSuccinct()}, least: ${polymer.leastOccurringSuccinct()}, result = ${polymer.mostOccurringSuccinct().value - polymer.leastOccurringSuccinct().value}")
    }
}

data class Polymer(var chain: List<Char>, val insertionRules: List<InsertionRule>, var count: MutableMap<Pair<Char, Char>, Long> = mutableMapOf()) {

    init {
        count = countPairs(chain)
    }

    fun runSuccinctStep() {
        val newCount = mutableMapOf<Pair<Char, Char>, Long>()
        for ((k, v) in count) {
            val (leftPair, rightPair) = insertionRules.findOrThrow { it.match(k) }.insert()
            newCount.merge(leftPair, v, Long::plus)
            newCount.merge(rightPair, v, Long::plus)
        }
        count = newCount
    }

    fun runStep() {
        val inserts = chain.zipWithNext()
            .map { p -> insertionRules.findOrThrow { it.match(p) } }
            .map { it.insert }
        val newChain = chain.zip(inserts).flatMap { it.toList() } + chain.last()
        chain = newChain
        println("size: ${chain.size}, $chain")
    }
    
    private fun countPairs(chain: List<Char>) = chain.zipWithNext().groupingBy { it }.eachCount().entries.associate { it.key to it.value.toLong() }.toMutableMap()
    
    fun mostOccurring() = chain.groupingBy { it }.eachCount().maxByOrNull { it.value } ?: throw IllegalStateException("couldn't")
    fun mostOccurringSuccinct() = count.entries.groupBy { it.key.first }
        .mapValues { it.value.sumOf { it.value } + if (chain.last() == it.key) 1 else 0 }
        .maxByOrNull { it.value } ?: throw IllegalStateException("couldn't")
    
    fun leastOccurring() = chain.groupingBy { it }.eachCount().minByOrNull { it.value } ?: throw IllegalStateException("couldn't")
    fun leastOccurringSuccinct() = count.entries.groupBy { it.key.first }
        .mapValues { it.value.sumOf { it.value } + if (chain.last() == it.key) 1 else 0 }
        .minByOrNull { it.value } ?: throw IllegalStateException("couldn't")    
}

data class InsertionRule(val elements: Pair<Char, Char>, val insert: Char) {
    
    fun match(pair: Pair<Char, Char>) : Boolean = elements.first == pair.first && elements.second == pair.second 
    
    companion object {
        fun createInsertionRule(input: String) : InsertionRule {
            // CH -> B
            val (elements, insert) = input.split(" -> ")  
            return InsertionRule(Pair(elements[0], elements[1]), insert[0])
        } 
    }
    
    fun insert() : List<Pair<Char, Char>> = listOf(Pair(elements.first, insert), Pair(insert, elements.second)) 
}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

