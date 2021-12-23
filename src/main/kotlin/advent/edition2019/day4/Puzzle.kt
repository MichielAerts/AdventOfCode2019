package test.advent.day4

import java.io.File

val day = 4;
val file = File("src/main/resources/edition2019/day${day}/input")

fun main() {
    val input = file.readLines()[0].split("-")
    val lb = input[0].toInt()
    val ub = input[1].toInt()
//    println(isValidPassword(123789))
//    println(matchesStrictRule("111122"))
    val res = (lb..ub).filter { isValidPassword(it) }.count()
    println(res)
}

val adj = (0..10).map { it.toString() }.map { it.repeat(2) }.toSet()
val adjStrict = (0..10).map { it.toString() }.map { Pair(it.repeat(2), it.repeat(3)) }.toSet()

fun isValidPassword(it: Int): Boolean {
    // Two adjacent digits are the same (like 22 in 122345).
    //Going from left to right, the digits never decrease; they only ever increase or stay the same (like 111123 or 135679).
    val str = it.toString()
    return adj.any { str.contains(it) } && str.toList().sorted().joinToString("") == str && matchesStrictRule(str)
}

fun matchesStrictRule(str: String): Boolean {
    // the two adjacent matching digits are not part of a larger group of matching digits.
    var result = false
    for (adjPair in adjStrict) {
        if (str.contains(adjPair.first) && !str.contains(adjPair.second)) {
            result = true
        }
    }
    return result
}


