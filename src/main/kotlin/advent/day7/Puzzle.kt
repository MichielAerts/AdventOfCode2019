package test.advent.day7

import java.io.File

val day = 7;
val file = File("src/main/resources/day${day}/input")


fun main() {
    val intCodeList = file.readLines().get(0).split(",")
        .map { it.toInt() }
        .toMutableList()

    // first part
//    val setting = PhaseSetting(4, 3, 2, 1, 0)
//    val maxThrusterSignal = tryPhaseSettingFirstPart(setting, intCodeList)
    val allPhaseSettings = permutations(listOf(0, 1, 2, 3, 4)).map { PhaseSetting.ofList(it) }
    val maxThrusterSignal = allPhaseSettings.map { tryPhaseSetting(it, intCodeList) }.maxByOrNull { it.first }

    // second part
//    val allPhaseSettings = permutations(listOf(5, 6, 7, 8, 9)).map { PhaseSetting.ofList(it) }
//    val maxThrusterSignal = allPhaseSettings.map { tryPhaseSetting(it, intCodeList) }.maxByOrNull { it.first }
    println(maxThrusterSignal)
}

fun tryPhaseSetting(phaseSetting: PhaseSetting, intCodeList: MutableList<Int>): Pair<Int, PhaseSetting> {
    var input = 0
    var output: Int
    val programs: MutableMap<Int, IntCode> = hashMapOf()
    for (loop in 1..20) {
        for (amplifier in 1..5) {
            if (loop == 1) {
                val program = IntCode(intCodeList.toMutableList(), phaseSetting.getSetting(amplifier))
                programs.put(amplifier, program)
            }
            output = programs.getValue(amplifier).runProgram(input, firstRun = (loop == 1))
            input = output
            if (programs.values.all { !it.running }) {
                return Pair(output, phaseSetting)
            }
        }
    }
    throw IllegalStateException("Program didn't end")
}

fun tryPhaseSettingFirstPart(phaseSetting: PhaseSetting, intCodeList: MutableList<Int>): Pair<Int, PhaseSetting> {
    var input = 0
    var output = 0
    for (amplifier in 1..5) {
        output = runProgramFirstPart(phaseSetting.getSetting(amplifier), input, intCodeList.toMutableList())
        input = output
    }
    return Pair(output, phaseSetting)
}

class IntCode(
    private val program: MutableList<Int>,
    private val setting: Int,
    var pos: Int = 0,
    var running: Boolean = true
) {
    fun runProgram(inputProgram: Int, firstRun: Boolean = false): Int {
        var input = if (firstRun) setting else inputProgram
        var output: Int? = null
        while (running && output == null) {
            val op = Operation.createOp(program[pos])
            if (op.opCode == 99) {
                running = false
                output = input
                break
            }
            val (steps, outputRun, nextInput) = performOperation(op, program, pos, input)
            if (nextInput && firstRun) {
                input = inputProgram
            }
            output = outputRun
            pos += steps
        }
        return output!!
    }
}

fun runProgramFirstPart(setting: Int, input: Int, intCodeList: MutableList<Int>): Int {
//    println(intCodeList)
    var pos = 0
    var inputProgram = setting
    var result: Int? = null
    while (Operation.createOp(intCodeList[pos]).opCode != 99) {
        val op = Operation.createOp(intCodeList[pos])
        println("pos: ${pos}, op: ${op.opCode}")
        val (steps, output, nextInput) = performOperation(op, intCodeList, pos, inputProgram)
        if (nextInput) inputProgram = input
        output?.let { println("output: $it") }
        result = output
        pos += steps
        println(intCodeList)
    }
//    println("result: ${result ?: "no result"}")
    return result!!
}

data class PhaseSetting(val s1: Int = 0, val s2: Int = 0, val s3: Int = 0, val s4: Int = 0, val s5: Int = 0) {
    fun getSetting(no: Int): Int {
        return when (no) {
            1 -> s1
            2 -> s2
            3 -> s3
            4 -> s4
            5 -> s5
            else -> throw IllegalArgumentException("no way")
        }
    }

    companion object {
        fun ofList(list: List<Int>): PhaseSetting = PhaseSetting(list[0], list[1], list[2], list[3], list[4])
    }
}

private fun performOperation(
    op: Operation,
    intCodeList: MutableList<Int>,
    pos: Int,
    input: Int
): Triple<Int, Int?, Boolean> {
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
    val nextInput = op.opCode == 3
    return Triple(output.first, output.second, nextInput)
}

data class Operation(val opCode: Int, val param1: Int, val param2: Int, val param3: Int) {
    companion object {
        fun createOp(input: Int): Operation {
            val opCode = input % 100
            val str = input.toString().padStart(5, '0')
            return Operation(
                opCode,
                Character.getNumericValue(str[2]),
                Character.getNumericValue(str[1]),
                Character.getNumericValue(str[0])
            )
        }
    }
}

fun <T> permutations(list: List<T>): Set<List<T>> {
    if (list.isEmpty()) return setOf(emptyList())

    val result: MutableSet<List<T>> = mutableSetOf()
    for (i in list.indices) {
        permutations(list - list[i]).forEach { item ->
            result.add(item + list[i])
        }
    }
    return result
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
