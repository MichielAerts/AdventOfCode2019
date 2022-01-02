package test.advent.edition2021.day18

import test.advent.edition2021.day18.El.Type.*
import test.advent.edition2021.subListTillEnd
import java.io.File

val day = 18;
val file = File("src/main/resources/edition2021/day${day}/input")

class Puzzle(private val rawInput: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val input = rawInput.map { SnailFishNumber.createSnailFishNumber(it) }
        var number = input[0]
        for (item in 1 until input.size) {
            println("adding ${input[item].print()} to ${number.print()}")
            number = number.add(input[item])
            number.reduce()
        }
        println(number.print())
        println(number.calculateMagnitude())
    }

    fun runPart2() {
        println("Part 2 of Day $day")
        val input = rawInput.map { SnailFishNumber.createSnailFishNumber(it) }
        val results = input.flatMap { first ->
            input.map { second -> Triple(first, second, first.clone().addAndReduce(second.clone()).calculateMagnitude()) }
        }
        val max = results.maxByOrNull { it.third }
        println("max magnitude: ${max?.third}, from ${max?.first?.print()} and ${max?.second?.print()}")
    }
}

data class El(var value: String, val type: Type) {
    operator fun plus(other: MutableList<El>): MutableList<El> = (listOf(this) + other).toMutableList()
    operator fun plus(el: El): MutableList<El> = (listOf(this) + el).toMutableList()

    enum class Type {
        LEFT_SQ_HOOK, RIGHT_SQ_HOOK, NUMBER, COMMA;
    }

    companion object {
        fun createEl(input: Char): El {
            return when (input) {
                '[' -> El("[", Type.LEFT_SQ_HOOK)
                ']' -> El("]", Type.RIGHT_SQ_HOOK)
                ',' -> El(",", Type.COMMA)
                in '0'..'9' -> El(input.toString(), Type.NUMBER)
                else -> throw IllegalArgumentException("Unsupported")
            }
        }
    }
}

data class SnailFishNumber(var number: MutableList<El>) {
    fun add(other: SnailFishNumber): SnailFishNumber =
        SnailFishNumber(
            (El("[", El.Type.LEFT_SQ_HOOK) + this.number + El(",", COMMA) + other.number + El(
                "]",
                El.Type.RIGHT_SQ_HOOK
            )).toMutableList()
        )
    
    fun clone(): SnailFishNumber = SnailFishNumber(this.number.map { El(it.value, it.type) }.toMutableList())

    fun addAndReduce(other: SnailFishNumber): SnailFishNumber {
        println("adding ${other.print()} to ${this.print()}")
        val newNumber = SnailFishNumber((El("[", El.Type.LEFT_SQ_HOOK) + this.number + 
                El(",", COMMA) + other.number + El("]", El.Type.RIGHT_SQ_HOOK)).toMutableList())
        newNumber.reduce()
        return newNumber
    }

    fun reduce() {
        while (shouldExplode() || shouldSplit()) {
            while (shouldExplode()) {
                explode()
//                println(print())
            }
            if (shouldSplit()) {
                split()
//                println(print())
            }
        }
    }

    fun print() = this.number.joinToString("") { it.value }

    private fun split() {
        val index = this.number.indexOfFirst { it.type == NUMBER && it.value.toInt() >= 10 }
        val number = this.number.firstOrNull { it.type == NUMBER && it.value.toInt() >= 10 }!!.value.toInt()
        val newPair = El("[", El.Type.LEFT_SQ_HOOK) + El((number / 2).toString(), NUMBER) +
                El(",", COMMA) + El((number - (number / 2)).toString(), NUMBER) + El("]", El.Type.RIGHT_SQ_HOOK)
        this.number.removeAt(index)
        this.number.addAll(index, newPair)
    }

    private fun explode() {
        var level = 0
        var index = 0
        for (el in this.number) {
            when (el.type) {
                LEFT_SQ_HOOK -> level++
                RIGHT_SQ_HOOK -> level--
                else -> {
                }
            }
            index++
            if (level == 5) break
        }
        val left = this.number[index].value.toInt()
        val right = this.number[index + 2].value.toInt()
        val numberToTheLeft = this.number.subList(0, index).lastOrNull { it.type == NUMBER }
        val numberToTheRight = this.number.subListTillEnd(index + 4).firstOrNull { it.type == NUMBER }
        numberToTheLeft?.value = (numberToTheLeft!!.value.toInt() + left).toString()
        numberToTheRight?.value = (numberToTheRight!!.value.toInt() + right).toString()
        this.number = (this.number.subList(0, index - 1) + El(
            "0",
            NUMBER
        ) + this.number.subListTillEnd(index + 4)).toMutableList()
    }

    private fun shouldSplit(): Boolean = this.number.any { it.type == NUMBER && it.value.toInt() >= 10 }

    private fun shouldExplode(): Boolean {
        var level = 0
        for (el in this.number) {
            when (el.type) {
                LEFT_SQ_HOOK -> level++
                RIGHT_SQ_HOOK -> level--
                else -> {
                }
            }
            if (level == 5) return true
        }
        return false
    }

    fun calculateMagnitude(): Int {
        var number = print()
        while (number.contains('[')) {
            number = number.replace("\\[(\\d+),(\\d+)]".toRegex(), ::calculateMagnitude)
//            println(number)
        }
        return number.toInt()
    }

    private fun calculateMagnitude(matchResult: MatchResult): String {
        val (_, left, right) = matchResult.groupValues
        return (left.toInt() * 3 + right.toInt() * 2).toString()
    }

    companion object {
        fun createSnailFishNumber(input: String): SnailFishNumber =
            SnailFishNumber(input.toList().map { El.createEl(it) }.toMutableList())
    }
}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

