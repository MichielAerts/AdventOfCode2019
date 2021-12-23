package test.advent.day11

import java.io.File

val day = 11;
val file = File("src/main/resources/edition2019/day${day}/input")

fun main() {
    val intCodeList = file.readLines().get(0).split(",")
        .map { it.toLong() }
        .toMutableList()
    val totalMem = 10000
    val program = IntCode((intCodeList + LongArray(totalMem - intCodeList.size).toMutableList()).toMutableList())
    val grid = Grid(100, program)
    // first part
    grid.goPainting()
//    println(grid.panelsPainted.size)
    // second part
    println(grid.print())
    //BCKFPCRA
}

class Grid(
    val size: Int,
    val program: IntCode,
    val grid: MutableList<MutableList<Char>> = MutableList(size) { MutableList(size) { '.' } },
    var robot: Pair<Coor, Char> = Pair(Coor(size / 2, size / 2), '^'),
    val panelsPainted: MutableSet<Coor> = mutableSetOf()
) {

    fun goPainting() {
        var robotPosition = robot.first
        grid[robotPosition.y][robotPosition.x] = '#'
        var output = Pair(0, 0)
        var input: Char
        while (output.first != 99) {
            robotPosition = robot.first
            input = grid[robotPosition.y][robotPosition.x]
            output = program.runProgram(if (input == '.') 0 else 1)
            if (output.first != 99) paintAndMove(output)
        }
    }

    private fun paintAndMove(output: Pair<Int, Int>): Unit {
        val robotPosition = robot.first
        grid[robotPosition.y][robotPosition.x] = when (output.first) {
            0 -> '.'
            1 -> '#'
            else -> throw IllegalArgumentException("unexpected")
        }
        panelsPainted.add(robotPosition)
        val newDirection = when (output.second) {
            0 -> robot.second.turnLeft()
            1 -> robot.second.turnRight()
            else -> throw IllegalArgumentException("unexpected")
        }
        val newCoor = when (newDirection) {
            '^' -> Coor(robotPosition.x, robotPosition.y - 1)
            '>' -> Coor(robotPosition.x + 1, robotPosition.y)
            'v' -> Coor(robotPosition.x, robotPosition.y + 1)
            '<' -> Coor(robotPosition.x - 1, robotPosition.y)
            else -> throw IllegalArgumentException("unexpected")
        }
        robot = Pair(newCoor, newDirection)
    }

    fun print(): Unit = grid.forEach { println(it.joinToString(separator = "")) }
}

private fun Char.turnLeft(): Char {
    return when (this) {
        '^' -> '<'
        '>' -> '^'
        'v' -> '>'
        '<' -> 'v'
        else -> throw IllegalArgumentException("unexpected")
    }
}

private fun Char.turnRight(): Char {
    return when (this) {
        '^' -> '>'
        '>' -> 'v'
        'v' -> '<'
        '<' -> '^'
        else -> throw IllegalArgumentException("unexpected")
    }
}

data class Coor(val x: Int, val y: Int)

class IntCode(
    private val program: MutableList<Long>,
    var pos: Int = 0,
    var relativeBase: Int = 0
) {

    fun runProgram(input: Long): Pair<Int, Int> {
        val output = mutableListOf<Int>()
//        println("relative base: $relativeBase, input: $input, output: $output, new pos: $pos, program: ${program}")
        while (output.size < 2) {
            val operation = Operation.createOp(program[pos])
//            println("next operation $operation")
            if (operation.opCode == 99) return Pair(99, 99)
            val programOutput = performOperation(operation, input)
            programOutput?.let { output.add(it.toInt()) }
//            println("relative base: $relativeBase, output: $output, new pos: $pos, program: ${program}")
        }
        return Pair(output[0], output[1])
    }

    private fun performOperation(
        op: Operation,
        input: Long,
    ): Long? {
        val output = when (op.opCode) {
            1 -> this.processAddition(op)
            2 -> this.processMultiplication(op)
            3 -> this.saveInput(op, input)
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