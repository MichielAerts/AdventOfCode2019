package test.advent.day5

import java.io.File

val day = 5;
val file = File("src/main/resources/day${day}/input")

fun main() {
    val intCodeList = file.readLines().get(0).split(",")
        .map { it.toInt() }
        .toMutableList()
    println(intCodeList)
    var pos = 0
    val input = 5
    var result: Int? = null
    while (Operation.createOp(intCodeList[pos]).opCode != 99) {
        val op = Operation.createOp(intCodeList[pos])
        println("pos: ${pos}, op: ${op.opCode}")
        val (steps, output) = performOperation(op, intCodeList, pos, input)
        output?.let { println("output: $it") }
        result = output
        pos += steps
        println(intCodeList)
    }
    println("result: ${result ?: "no result"}")
}

private fun performOperation(
    op: Operation,
    intCodeList: MutableList<Int>,
    pos: Int,
    input: Int = 0
): Pair<Int, Int?> {
    val output = when (op.opCode) {
        1 -> intCodeList.processAddition(pos, op.param1, op.param2)
        2 -> intCodeList.processMultiplication(pos, op.param1, op.param2)
        3 -> intCodeList.saveInput(pos, input)
        4 -> intCodeList.getOutput(pos, op.param1)
        5 -> intCodeList.jumpIfTrue(pos, op.param1, op.param2)
        6 -> intCodeList.jumpIfFalse(pos, op.param1, op.param2)
        7 -> intCodeList.lessThan(pos, op.param1, op.param2)
        8 -> intCodeList.equalz(pos, op.param1, op.param2)
        else -> throw IllegalArgumentException("no op code!")
    }
    return output
}

data class Operation(val opCode: Int, val param1: Int, val param2: Int, val param3: Int) {
    companion object {
        fun createOp(input: Int): Operation {
            val opCode = input % 100
            val str = input.toString().padStart(5, '0')
            return Operation(opCode, Character.getNumericValue(str[2]), Character.getNumericValue(str[1]), Character.getNumericValue(str[0]))
        }
    }
}


private fun MutableList<Int>.jumpIfTrue(pos: Int, param1: Int, param2: Int): Pair<Int, Int?> {
    val first = if (param1 == 0) this[this[pos + 1]] else this[pos + 1]
    val second = if (param2 == 0) this[this[pos + 2]] else this[pos + 2]
    val steps = if (first != 0) second - pos else 3
    return Pair(steps, null)
}


private fun MutableList<Int>.jumpIfFalse(pos: Int, param1: Int, param2: Int): Pair<Int, Int?> {
    val first = if (param1 == 0) this[this[pos + 1]] else this[pos + 1]
    val second = if (param2 == 0) this[this[pos + 2]] else this[pos + 2]
    val steps = if (first == 0) second - pos else 3
    return Pair(steps, null)
}

private fun MutableList<Int>.lessThan(pos: Int, param1: Int, param2: Int): Pair<Int, Int?> {
    val first = if (param1 == 0) this[this[pos + 1]] else this[pos + 1]
    val second = if (param2 == 0) this[this[pos + 2]] else this[pos + 2]
    val store = if (first < second) 1 else 0
    this[this[pos + 3]] = store
    return Pair(4, null)
}


private fun MutableList<Int>.equalz(pos: Int, param1: Int, param2: Int): Pair<Int, Int?> {
    val first = if (param1 == 0) this[this[pos + 1]] else this[pos + 1]
    val second = if (param2 == 0) this[this[pos + 2]] else this[pos + 2]
    val store = if (first == second) 1 else 0
    this[this[pos + 3]] = store
    return Pair(4, null)
}

private fun MutableList<Int>.processAddition(pos: Int, param1: Int, param2: Int): Pair<Int, Int?> {
    val first = if (param1 == 0) this[this[pos + 1]] else this[pos + 1]
    val second = if (param2 == 0) this[this[pos + 2]] else this[pos + 2]
    this[this[pos + 3]] = first + second
    return Pair(4, null)
}

private fun MutableList<Int>.processMultiplication(pos: Int, param1: Int, param2: Int): Pair<Int, Int?> {
    val first = if (param1 == 0) this[this[pos + 1]] else this[pos + 1]
    val second = if (param2 == 0) this[this[pos + 2]] else this[pos + 2]
    this[this[pos + 3]] = first * second
    return Pair(4, null)
}

private fun MutableList<Int>.saveInput(pos: Int, input: Int): Pair<Int, Int?> {
    this[this[pos + 1]] = input
    return Pair(2, null)
}

private fun MutableList<Int>.getOutput(pos: Int, param1: Int): Pair<Int, Int?> {
    val first = if (param1 == 0) this[this[pos + 1]] else this[pos + 1]
    return Pair(2, first)
}
