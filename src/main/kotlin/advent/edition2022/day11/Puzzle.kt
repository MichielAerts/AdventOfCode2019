package test.advent.edition2022.day11

import test.advent.edition2022.contains
import test.advent.edition2022.findGroupAsInt
import test.advent.edition2022.product
import test.advent.edition2022.splitBy
import java.io.File
import kotlin.math.floor

val day = 11
val file = File("src/main/resources/edition2022/day${day}/input")

class Puzzle(private val input: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val monkeys = input.splitBy { it.isEmpty() }.map { Monkey.create(it) }
        println(monkeys)
        for (round in 1..20) {
            for (monkey in monkeys) {
                val (no, items, op, test, trueMonkey, falseMonkey) = monkey
                while (items.isNotEmpty()) {
                    val inspectedItem = items.removeFirst()
                    inspectedItem.worryLevel = floor((op(inspectedItem.worryLevel).toDouble() / 3)).toLong()
                    val testResult = test(inspectedItem)
                    monkeys[if (testResult) trueMonkey else falseMonkey].items.addLast(inspectedItem)
                    monkey.inspections += 1
                }
            }
//            println(monkeys)
            println("$round, ${monkeys.map { it.inspections }}")
        }
        println(monkeys.map { it.inspections }.sortedDescending().take(2).product())
    }

    fun runPart2() {
        println("Part 2 of Day $day")
        val monkeys = input.splitBy { it.isEmpty() }.map { Monkey.create(it) }
        val factor = input.filter { it.startsWith("  Test: divisible by ") }.map { it.substringAfter("  Test: divisible by ").toLong() }.product()
        println(monkeys)
        for (round in 1..10000) {
            for (monkey in monkeys) {
                val (no, items, op, test, trueMonkey, falseMonkey) = monkey
                while (items.isNotEmpty()) {
                    val inspectedItem = items.removeFirst()
                    inspectedItem.worryLevel = op(inspectedItem.worryLevel) % factor
                    val testResult = test(inspectedItem)
                    monkeys[if (testResult) trueMonkey else falseMonkey].items.addLast(inspectedItem)
                    monkey.inspections += 1
                }
            }
//            println("$round, ${monkeys.map { it.inspections }}")
        }
        println(monkeys.map { it.inspections }.sortedDescending().take(2).product())
    }
}

data class Monkey(
    val no: Int,
    val items: ArrayDeque<Item>,
    val operation: (Long) -> Long,
    val test: (Item) -> Boolean,
    val trueMonkey: Int,
    val falseMonkey: Int,
    var inspections: Long = 0
) {
    override fun toString(): String = "$no,${items.map { it.worryLevel }},$inspections"
        
    companion object {
        private val monkeyRegex = """Monkey (?<no>\d+):""".toRegex()
        private val opXRegex = """old \* old""".toRegex()
        private val opMultRegex = """old \* (?<fact>\d+)""".toRegex()
        private val opAddRegex = """old \+ (?<fact>\d+)""".toRegex()

        fun create(input: List<String>): Monkey {
            /*
            Monkey 0:
              Starting items: 79, 98
              Operation: new = old * 19
              Test: divisible by 23
                If true: throw to monkey 2
                If false: throw to monkey 3
             */
            val (monkeyLine, itemsLine, opLine) = input
            val (testLine, trueLine, falseLine) = input.drop(3)
            val no = monkeyRegex.findGroupAsInt(monkeyLine, "no")
            val items =
                ArrayDeque(itemsLine.substringAfter("Starting items: ").split(",").map { Item(it.trim().toLong()) })
            val op = when (val opLineOp = opLine.substringAfter("Operation: new = ")) {
                in opXRegex -> { a: Long -> a * a }
                in opMultRegex -> { a: Long -> a * opMultRegex.findGroupAsInt(opLineOp, "fact") }
                in opAddRegex -> { a: Long -> a + opAddRegex.findGroupAsInt(opLineOp, "fact") }
                else -> throw IllegalArgumentException("no")
            }
            val trueMonkey = trueLine.substringAfter("If true: throw to monkey ").toInt()
            val falseMonkey = falseLine.substringAfter("If false: throw to monkey ").toInt()
            val mod = testLine.substringAfter("Test: divisible by ").toInt()
            val test = { item: Item -> (item.worryLevel % mod == 0L) }
            return Monkey(no, items, op, test, trueMonkey, falseMonkey)
        }
    }
}

data class Item(var worryLevel: Long)

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

