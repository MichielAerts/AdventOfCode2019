package test.advent.day19

import java.io.File

val day = 19;
val file = File("src/main/resources/edition2019/day${day}/input")

fun main() {
    val intCodeList = file.readLines().get(0).split(",")
        .map { it.toLong() }
        .toMutableList()
    val totalMem = 10000
//    val totalMem = intCodeList.size
    val program = IntCode((intCodeList + LongArray(totalMem - intCodeList.size).toMutableList()).toMutableList())

    //part 1
    val reach = 20
    val beamPicture = getBeamPicture(program, reach)
//    println(beamPicture.flatten().count { it == '#' })
    beamPicture.forEach { println(it.joinToString("")) }
    val topLeft = getTopLeftForSquare(beamPicture, 100)
    println(topLeft) // too high 6241175 6191165
}

fun getTopLeftForSquare(beamPicture: List<List<Char>>, squareSize: Int): Coor {
    val reach = beamPicture.size
    for (y in 0 until reach) {
        for (x in 0 until reach) {
            if (beamPicture[y][x] == '#' && beamPicture[y + squareSize - 1][x] == '#'
                && beamPicture[y][x + squareSize - 1] == '#' && beamPicture[y + squareSize - 1][x + squareSize -1] == '#')
            return Coor(x, y)
        }
    }
    throw IllegalStateException("uhh..")
}

fun getBeamPicture(program: IntCode, reach: Int): List<List<Char>> {
    val picture1 = (0 until reach)
        .flatMap { y -> (0 until reach).map { x -> Pair(x, y) } }
        .map { program.run(Coor(it.first, it.second)) }
        .map { if (it == 0) '.' else '#' }
        .chunked(reach)
    val picture = MutableList(reach) { MutableList(reach) { ' ' } }
    for (y in 0 until reach) {
//        println(y)
        for (x in 0 until reach) {
            val c = Coor(x, y)
            val output = program.run(c)
//            println("coor: $c, output: $output")
            picture[y][x] = if (output == 0) '.' else '#'
        }
    }
    return picture1
}

private fun MutableList<MutableList<Char>>.set(coor: Coor, c: Char): Unit {
    this[coor.y][coor.x] = c
}

private fun List<List<Char>>.get(coor: Coor): Char = this[coor.y][coor.x]

data class Coor(val x: Int, val y: Int) {
    fun getNeighbours() = listOf(
        Coor(x, y - 1),
        Coor(x, y + 1),
        Coor(x - 1, y),
        Coor(x + 1, y)
    )
}

class IntCode(
    private val originalProgram: List<Long>,
    private var program: MutableList<Long> = originalProgram.toMutableList(),
    var pos: Int = 0,
    var relativeBase: Int = 0
) {
    lateinit var coor: Coor
    var inputCount = 0

    fun init() {
        pos = 0
        relativeBase = 0
        program = originalProgram.toMutableList()
    }

    fun run(coor: Coor): Int? {
        init()
        this.coor = coor
        inputCount = 0
        var currentOutput: Int? = null
//        println("relative base: $relativeBase, new pos: $pos, program: ${program.subList(0, min(500, program.size))}")
        while (true) {
            val operation = Operation.createOp(program[pos])
            if (operation.opCode == 99) break
            currentOutput = performOperation(operation)?.toInt()
//            println("relative base: $relativeBase, output: $currentOutput, op: $operation, new pos: $pos, program: ${program.subList(0, min(500, program.size))}")
            if (currentOutput != null) break
        }
        return currentOutput
    }

    fun getInput(): Int {
        val input = if (inputCount++ == 0) coor.x else coor.y
//        println("got input: $input")
        return input
    }

    private fun performOperation(
        op: Operation
    ): Long? {
        val output = when (op.opCode) {
            1 -> this.processAddition(op)
            2 -> this.processMultiplication(op)
            3 -> this.saveInput(op)
            4 -> this.getOutput(op)
            5 -> this.jumpIfTrue(op)
            6 -> this.jumpIfFalse(op)
            7 -> this.lessThan(op)
            8 -> this.equalz(op)
            9 -> this.adjustRelativeBase(op)
            else -> throw IllegalArgumentException("no op code!")
        }
        return output
    }

    private fun getValue(position: Int, param: Int): Long {
        return when (param) {
            0 -> program[program[position].toInt()]
            1 -> program[position]
            2 -> program[program[position].toInt() + relativeBase]
            else -> throw IllegalArgumentException("whut")
        }
    }

    private fun setValue(position: Int, input: Long, param: Int) {
        return when (param) {
            0 -> program[program[position].toInt()] = input
            2 -> program[program[position].toInt() + relativeBase] = input
            else -> throw IllegalArgumentException("whut")
        }
    }

    private fun adjustRelativeBase(op: Operation): Long? {
        val first = this.getValue(pos + 1, op.param1)
        relativeBase += first.toInt()
        pos += 2
        return null
    }

    private fun jumpIfTrue(op: Operation): Long? {
        val first = this.getValue(pos + 1, op.param1)
        val second = this.getValue(pos + 2, op.param2)
        val steps = if (first != 0L) second - pos else 3
        pos += steps.toInt()
        return null
    }

    private fun jumpIfFalse(op: Operation): Long? {
        val first = this.getValue(pos + 1, op.param1)
        val second = this.getValue(pos + 2, op.param2)
        val steps = if (first == 0L) second - pos else 3
        pos += steps.toInt()
        return null
    }

    private fun lessThan(op: Operation): Long? {
        val first = this.getValue(pos + 1, op.param1)
        val second = this.getValue(pos + 2, op.param2)
        val store = if (first < second) 1 else 0
        this.setValue(pos + 3, store.toLong(), op.param3)
        pos += 4
        return null
    }

    private fun equalz(op: Operation): Long? {
        val first = this.getValue(pos + 1, op.param1)
        val second = this.getValue(pos + 2, op.param2)
        val store = if (first == second) 1 else 0
        this.setValue(pos + 3, store.toLong(), op.param3)
        pos += 4
        return null
    }

    private fun processAddition(op: Operation): Long? {
        val first = this.getValue(pos + 1, op.param1)
        val second = this.getValue(pos + 2, op.param2)
        this.setValue(pos + 3, first + second, op.param3)
        pos += 4
        return null
    }

    private fun processMultiplication(op: Operation): Long? {
        val first = this.getValue(pos + 1, op.param1)
        val second = this.getValue(pos + 2, op.param2)
        this.setValue(pos + 3, first * second, op.param3)
        pos += 4
        return null
    }

    private fun saveInput(op: Operation): Long? {
        val input = getInput().toLong()
        this.setValue(pos + 1, input, op.param1)
        pos += 2
        return null
    }

    private fun getOutput(op: Operation): Long? {
        val first = this.getValue(pos + 1, op.param1)
        pos += 2
        return first
    }

}


data class Operation(val opCode: Int, val param1: Int, val param2: Int, val param3: Int) {
    companion object {
        fun createOp(input: Long): Operation {
            val opCode = (input % 100).toInt()
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