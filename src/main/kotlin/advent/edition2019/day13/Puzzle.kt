package test.advent.day13

import java.io.File

val day = 13;
val file = File("src/main/resources/edition2019/day${day}/input")

fun main() {
    val intCodeList = file.readLines().get(0).split(",")
        .map { it.toLong() }
        .toMutableList()
    val totalMem = 10000
    val program = IntCode((intCodeList + LongArray(totalMem - intCodeList.size).toMutableList()).toMutableList())

    val grid = Grid(40, program)
    // first part
//    grid.drawGame()
//    println(grid.print())
//    println(grid.grid.flatten().count { it == 'B' })
    // second part
    program.grid = grid
    grid.drawGame()
    println(grid.score)
}

class Grid(
    val size: Int,
    val program: IntCode,
    val grid: MutableList<MutableList<Char>> = MutableList(size) { MutableList(size) { '.' } },
    var score: Int = 0
) {
    fun determineInput(): Long {
//        println("what should be the input...?")
//        print()
//        return 0
        val posBall = grid.findCoor('X')
        val posPaddle = grid.findCoor('P')
        val dx = posBall.x - posPaddle.x
        return if (dx > 0) 1 else if (dx < 0) -1 else 0
    }

    fun drawGame() {
        var output = Triple(0, 0, 0)
        while (output.first != 99) {
            output = program.runProgram()
            if (output.first != 99) paintTile(output)
        }
    }

    private fun paintTile(output: Triple<Int, Int, Int>): Unit {
        if (output.first == -1 && output.second == 0) {
            score = output.third
            return
        }
        grid[output.second][output.first] = when (output.third) {
            0 -> '.'
            1 -> 'W'
            2 -> 'B'
            3 -> 'P'
            4 -> 'X'
            else -> throw IllegalArgumentException("unexpected")
        }
    }

    fun print(): Unit = grid.forEach { println(it.joinToString(separator = "")) }
}

private fun List<List<Char>>.findCoor(c: Char): Coor {
    for (y in 0 until this.size) {
        for (x in 0 until this[0].size) {
            if (this[y][x] == c) {
                return Coor(x, y)
            }
        }
    }
    throw IllegalStateException("$c not found, but was expected")
}

data class Coor(val x: Int, val y: Int)

class IntCode(
    private val program: MutableList<Long>,
    var pos: Int = 0,
    var relativeBase: Int = 0
) {
    var grid: Grid? = null

    fun runProgram(): Triple<Int, Int, Int> {
        val output = mutableListOf<Int>()
//        println("relative base: $relativeBase, output: $output, new pos: $pos, program: ${program}")
        while (output.size < 3) {
            val operation = Operation.createOp(program[pos])
//            println("next operation $operation")
            if (operation.opCode == 99) return Triple(99, 99, 99)
            val programOutput = performOperation(operation)
            programOutput?.let { output.add(it.toInt()) }
//            println("relative base: $relativeBase, output: $output, new pos: $pos, program: ${program}")
        }
        return Triple(output[0], output[1], output[2])
    }

    private fun performOperation(
        op: Operation
    ): Long? {
        val output = when (op.opCode) {
            1 -> this.processAddition(op)
            2 -> this.processMultiplication(op)
            3 -> this.saveInput(op, grid?.determineInput() ?: throw IllegalStateException("no grid yet"))
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

    private fun saveInput(op: Operation, input: Long): Long? {
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