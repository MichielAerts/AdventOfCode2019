package test.advent.day2

import java.io.File

val day = 2;
val file = File("src/main/resources/edition2019/day${day}/input")

fun main() {
    for (n in 0..99) {
        println(n)
        for (v in 0..99) {
            val res = runProgram(n, v)
            if (res == 19690720) {
                println("n: ${n}, v: ${v}, result pos 0: ${res}")
            }
        }
    }
}

private fun runProgram(noun: Int, verb: Int): Int {
    val intCodeList = file.readLines().get(0).split(",")
        .map { it.toInt() }
        .toMutableList()
    intCodeList[1] = noun
    intCodeList[2] = verb
    var pos = 0
    while (intCodeList[pos] != 99) {
        val opCode = intCodeList[pos]
        when (opCode) {
            1 -> intCodeList.processAddition(pos)
            2 -> intCodeList.processMultiplication(pos)
            else -> throw IllegalArgumentException("no op code!")
        }
        pos += 4
//        println(intCodeList)
    }
    val res = intCodeList[0]
    return res
}

private fun MutableList<Int>.processAddition(pos: Int) {
    this[this[pos + 3]] = this[this[pos + 1]] + this[this[pos + 2]]
}

private fun MutableList<Int>.processMultiplication(pos: Int) {
    this[this[pos + 3]] = this[this[pos + 1]] * this[this[pos + 2]]
}
