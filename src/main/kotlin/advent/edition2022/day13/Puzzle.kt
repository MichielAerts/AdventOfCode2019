package test.advent.edition2022.day13

import com.beust.klaxon.JsonArray
import com.beust.klaxon.Parser
import test.advent.edition2022.splitBy
import test.advent.edition2022.toPair
import java.io.File

val day = 13
val file = File("src/main/resources/edition2022/day${day}/input")
 
class Puzzle(private val input: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val parser = Parser.default()
        val pairs = input.splitBy { line -> line.isEmpty() }
            .mapIndexed { idx, pair -> idx + 1 to pair.map { parser.parse(StringBuilder(it)) as JsonArray<Any> }.toPair() }.toMap()
        println(pairs.filter { it.value.isInRightOrder() }.map { it.key }.sum())
    }
    
    fun runPart2() {
        println("Part 2 of Day $day")
        val parser = Parser.default()
        val packets = (input + "[[2]]" + "[[6]]").filter { line -> line.isNotEmpty() }
            .map { parser.parse(StringBuilder(it)) as JsonArray<Any> }.sortedWith(packetComparator())
        val result = (packets.indexOf(JsonArray(listOf(JsonArray(listOf(2))))) + 1) * (packets.indexOf(JsonArray(listOf(JsonArray(listOf(6))))) + 1) 
        println(result)
    }
}

enum class ORDERING { RIGHT, WRONG, NEUTRAL }

private fun packetComparator() =  Comparator<JsonArray<Any>> { a, b ->
    when(compare(a, b)) {
        ORDERING.RIGHT -> -1
        ORDERING.WRONG -> 1
        else -> throw IllegalStateException("shouldn't")
    }
}

private fun Pair<JsonArray<Any>, JsonArray<Any>>.isInRightOrder(): Boolean {
    val (left, right) = this
    return when(compare(left, right)) {
        ORDERING.RIGHT -> true
        ORDERING.WRONG -> false
        else -> throw IllegalStateException("shouldn't")
    }
}

fun compare(left: Int, right: Int): ORDERING =
    if (left < right) ORDERING.RIGHT else if (left > right) ORDERING.WRONG else ORDERING.NEUTRAL 

fun compare(left: JsonArray<*>, right: JsonArray<*>): ORDERING {
    var result = ORDERING.NEUTRAL
    for (i in 0 until left.size) {
        if (i > right.size - 1) {
//            println("right side ran out, result is ${ORDERING.WRONG}")
            return ORDERING.WRONG
        }
        val l = left[i]
        val r = right[i]
//        println("comparing $l and $r")
        if (l is Int && r is Int) {
            result = compare(l, r)
        } else if (l is JsonArray<*> && r is JsonArray<*>) {
            result = compare(l, r)
        } else if (l is JsonArray<*> && r is Int) {
            result = compare(l, JsonArray(listOf(r)))
        } else if (l is Int && r is JsonArray<*>) {
            result = compare(JsonArray(listOf(l)), r)
        }
//        println("comparing $l and $r, result is $result")
        if (result == ORDERING.NEUTRAL) continue else return result
    }
    if (left.size < right.size) {
//        println("left side ran out, result is ${ORDERING.RIGHT}")
        return ORDERING.RIGHT
    }
    return ORDERING.NEUTRAL
}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

