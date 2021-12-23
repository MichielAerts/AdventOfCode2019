package test.advent.day1

import java.io.File
import java.lang.Math.floor
import java.lang.Math.max

val day = 1;
val file = File("src/main/resources/edition2019/day${day}/input")

fun main() {
    val input = file.readLines()
//    val res = input.map { requiredFuel(it.toInt())}.sum()
//    println(res)
    val res = input.map { calcTotalRequiredFuel(it.toInt()) }.sum()
    println(res)
}

fun calcTotalRequiredFuel(mass: Int): Int {
    val res = requiredFuel(mass)
    return if (res <= 0) res else res + calcTotalRequiredFuel(res)
}

fun requiredFuel(mass: Int): Int = max(floor(mass / 3.0).toInt() - 2, 0)