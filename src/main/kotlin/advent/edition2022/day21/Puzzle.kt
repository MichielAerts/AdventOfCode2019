package test.advent.edition2022.day21

import test.advent.edition2022.contains
import test.advent.edition2022.getOrThrow
import test.advent.edition2022.toPair
import java.io.File

val day = 21
val file = File("src/main/resources/edition2022/day${day}/input")

private const val HUMN = "humn"

private const val ROOT = "root"

class Puzzle(private val input: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val monkeys = input.map { Monkey.create(it) }
        val riddle = Riddle(monkeys)
        println(riddle.solve())
    }

    fun runPart2() {
        println("Part 2 of Day $day")
        val map = input.map { it.split(": ").toPair() }.map { if (it.first == ROOT) 
            it.copy(it.first, it.second.replace("+", "=")) else it }.toMap().toMutableMap()
        var names = findMonkeysInRootEquation(map)
        while (names.size > 1 || !names.contains(HUMN)) {
            // reduce root equation
            var currentEquation = map.getOrThrow(ROOT)
            names = findMonkeysInRootEquation(map)
            println(names)
            for (name in names.filter { it != HUMN }) {
                currentEquation = currentEquation.replace(name, "(" + map.getOrThrow(name) + ")")
            }
            map[ROOT] = currentEquation
            println(map.getOrThrow(ROOT))
        }
    }

    private fun findMonkeysInRootEquation(
        map: MutableMap<String, String>
    ) = """(\w{4})""".toRegex().findAll(map.getOrThrow(ROOT)).map { it.value }.toList()
}

data class Riddle(
    val monkeys: List<Monkey>,
    val monkeyNames: Map<String, Monkey> = monkeys.associateBy { it.name },
    val monkeyNumbers: MutableMap<Monkey, Long?> = monkeys.associateWith { it.number }.toMutableMap()
) {
    
    fun solve(): Long {
        var time = 0
        while (monkeyNumbers.containsValue(null)) {
            for ((monkey, number) in monkeyNumbers) {
                if (number != null) continue
                if (monkey.number != null) monkeyNumbers[monkey] = monkey.number
                val left = monkeyNumbers[monkeyNames.getOrThrow(monkey.left!!)]
                val right = monkeyNumbers[monkeyNames.getOrThrow(monkey.right!!)]
                if (left != null && right != null) monkeyNumbers[monkey] = monkey.op?.invoke(left, right)
            }
            time++
        }
        return monkeyNumbers.getOrThrow(monkeyNames.getOrThrow(ROOT))!!
    }
}

data class Monkey(
    val name: String, val number: Long? = null,
    val left: String? = null, val right: String? = null, val op: ((Long, Long) -> Long)? = null
) {
    companion object {
        private val plus = """(\w+): (\w+) \+ (\w+)""".toRegex()
        private val minus = """(\w+): (\w+) - (\w+)""".toRegex()
        private val multiply = """(\w+): (\w+) \* (\w+)""".toRegex()
        private val divide = """(\w+): (\w+) / (\w+)""".toRegex()
        private val number = """(\w+): (\d+)""".toRegex()
        fun create(input: String): Monkey = when (input) {
            in plus -> {
                val (_, name, l, r) = plus.findValues(input)
                Monkey(name, left = l, right = r) { f, s -> f + s }
            }
            in minus -> {
                val (_, name, l, r) = minus.findValues(input)
                Monkey(name, left = l, right = r) { f, s -> f - s }
            }
            in multiply -> {
                val (_, name, l, r) = multiply.findValues(input)
                Monkey(name, left = l, right = r) { f, s -> f * s }
            }
            in divide -> {
                val (_, name, l, r) = divide.findValues(input)
                Monkey(name, left = l, right = r) { f, s -> f / s }
            }
            in number -> {
                val (_, name, n) = number.findValues(input)
                Monkey(name, number = n.toLong())
            }
            else -> throw IllegalArgumentException(input)
        }
    }
}

fun Regex.findValues(input: String): List<String> =
    this.find(input)?.groupValues ?: throw IllegalArgumentException(input)

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
//    puzzle.runPart1()
    puzzle.runPart2()
}

