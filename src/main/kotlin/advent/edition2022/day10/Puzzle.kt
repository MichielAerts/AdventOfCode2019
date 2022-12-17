package test.advent.edition2022.day10

import java.io.File

val day = 10
val file = File("src/main/resources/edition2022/day${day}/input")

class Puzzle(private val input: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val signal = createSignal()
        val cycles = listOf(20, 60, 100, 140, 180, 220)
        println(cycles.sumOf { it * signal[it - 1] })
    }

    private fun createSignal(): MutableList<Int> {
        val instructions = input.map { Instruction.create(it) }
        val signal = mutableListOf(1)

        for (instruction in instructions) {
            when (instruction.type) {
                Instruction.InstructionType.NOOP -> signal.add(signal.last())
                Instruction.InstructionType.ADDX -> {
                    signal.add(signal.last())
                    signal.add(signal.last() + instruction.amount)
                }
            }
        }
        return signal
    }

    fun runPart2() {
        println("Part 2 of Day $day")
        val signal = createSignal()
        val screen = Screen.create(signal)
        screen.print()     
    }
}

data class Screen(val screen: List<String>) {
    fun print() = screen.forEach { println(it) }
    
    companion object {
        fun create(list: List<Int>): Screen {
            return Screen((0 until 240).map { 
                val currentPosSprite = list[it]
                if (it % 40 in currentPosSprite - 1 .. currentPosSprite + 1) "#" else "."
            }.chunked(40).map { it.joinToString("") })
        }
    }
}
data class Instruction(val type: InstructionType, val amount: Int = 0) {
    enum class InstructionType { NOOP, ADDX }

    companion object {
        fun create(input: String): Instruction = when {
            input.startsWith("noop") -> Instruction(InstructionType.NOOP)
            input.startsWith("addx ") -> Instruction(InstructionType.ADDX, input.substringAfter("addx ").toInt())
            else -> throw IllegalArgumentException("no")
        }
    }
}
fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

