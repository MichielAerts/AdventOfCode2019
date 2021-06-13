package test.advent.day16

import java.io.File
import kotlin.math.absoluteValue

val day = 16;
val file = File("src/main/resources/day${day}/input")

fun main() {
    val initialInput = file.readLines().get(0).toList().map { Character.getNumericValue(it) }
//    println(initialInput)

    //part 1
//    val l = initialInput.size
//    val noPhases = 100
//    val basePattern = listOf(0, 1, 0, -1)
//    var input = initialInput
//    println(input.joinToString(""))
//    for (p in 1..noPhases) {
//        input = (input.indices).map { applyPhase(input, getPattern(basePattern, it, l)) }
//        println(input.joinToString(""))
//    }

// part 2 2nd try
    val repeat = 10000
    val l = initialInput.size
    val offset = initialInput.subList(0, 7).joinToString("").toInt()
    val rightSideLength = repeat * l - offset
    var input = initialInput.asSequence().repeat().take(repeat * l).toList().subList(rightSideLength, repeat * l).reversed()
    val noPhases = 100
//    println(input.joinToString(""))
    for (p in 1..noPhases) {
        var currentSum = 0
        val result = mutableListOf<Int>()
        for (i in 0 until rightSideLength) {
            currentSum += input[i]
            result += (currentSum % 10)
        }
        input = result
//        println("phase: $p done, input: ${input.joinToString("")}")
    }
    println(input.joinToString(""))
    println(input.subList(rightSideLength - 8, rightSideLength).reversed().joinToString(""))

    // part 2
//    val l = initialInput.size
//    val offset = initialInput.subList(0, 7).joinToString("").toInt()
////    val offsetAtStart = offset - (offset % l)
//    val offsetAtStart = (offset / l / 10) * 10 * l// take 10
////    println(offsetAtStart)
//    val noPhases = 100
//    val rep = 10000
//    println("l: $l, offset: $offset, div = ${offset.toDouble()/(l * rep)}")
//    println("d: ${offset.toDouble()/l}")
//    val map = (0 until (10 * l)).map { Pair(it + offsetAtStart, initialInput[it % l]) }.toMap()
//    println(map)
//    var input = map.toMap()
//    for (p in 1..noPhases) {
//        val result = mutableMapOf<Int, Int>()
//        val values = input.values
//        val sum = values.sum()
//        println("$sum, values: $values")
//        for ((off, value) in input) {
//            val (div, mod) = getDiv(off, l, rep)
//            val v = div * sum.toLong() + values.toList().subList(values.size - mod, values.size).sum()
////            println("div: $div, mod: $mod for offset: $off with value $value, v is $v")
//            result.put(off, (v % 10).toInt().absoluteValue)
//        }
//        println("after phase $p, result: $result")
//        input = result.toMap()
//    }
//    println(input)
}

fun getDiv(offset: Int, l: Int, rep: Int): Pair<Int, Int> {
    val totalLength = rep * l
    val remainder = totalLength - offset
    return Pair(remainder / (10 * l), remainder % (10 * l))
}

fun applyPhase(input: List<Int>, pattern: List<Int>): Int = ((input.indices).map { input[it] * pattern[it] }.sum() % 10).absoluteValue

fun applyPhase(input: List<Int>, index: Int): Int = ((input.indices).map { input[it] * if (it > index) 1 else 0 }.sum() % 10).absoluteValue

fun getPattern(basePattern: List<Int>, idx: Int, size: Int): List<Int> {
    if (idx % 10000 == 0) println(idx)
    return basePattern.asSequence().flatMap { i -> List(idx + 1) { i } }.repeat().drop(1).take(size).toList()
}

fun <T> Sequence<T>.repeat() = sequence { while (true) yieldAll(this@repeat) }

//    basePattern.asSequence().flatMap { List(idx + 1) { it } }.drop(1).take(size).toList()
