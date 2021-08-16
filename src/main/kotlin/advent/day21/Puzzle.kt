package test.advent.day21

import java.io.File

val day = 21;
val file = File("src/main/resources/day${day}/input")

fun main() {
    val intCodeList = file.readLines().get(0).split(",")
        .map { it.toLong() }
        .toMutableList()
    val totalMem = 10000
    val program = IntCode((intCodeList + LongArray(totalMem - intCodeList.size).toMutableList()).toMutableList())
    val robot = Droid(program)

    //part 1
    val instructions = """
    |NOT A T
    |NOT B J
    |OR T J
    |NOT C T
    |OR T J
    |AND D J
    |NOT J T
    |OR E T
    |OR H T
    |AND T J
    |RUN
    |
    """.trimMargin()
    
    robot.addInput(instructions)
    program.droid = robot
    robot.draw()
    
    // part 2 
}

class Droid(val program: IntCode, var input: MutableList<Int> = mutableListOf(), var inputCount: Int = 0) {
    fun draw(): String = program.run().map { it.toChar() }.joinToString("")

    fun addInput(vararg inputs: String) {
        for(inputStr in inputs) {
            input += inputStr.toList().map { it.toInt() }
        }
    }

    fun getInput(): Int = input[inputCount++]
}

class IntCode(
    private val program: MutableList<Long>,
    var pos: Int = 0,
    var relativeBase: Int = 0
) {
    lateinit var droid: Droid

    fun run(): List<Int> {
//        println("relative base: $relativeBase, new pos: $pos, program: ${program}")
        val output = mutableListOf<Int>()
        while (true) {
            val operation = Operation.createOp(program[pos])
            if (operation.opCode == 99) break
            val currentOutput = performOperation(operation)?.toInt()
            currentOutput?.let {
                output += currentOutput
                print(currentOutput.toChar())
            }
//            println("relative base: $relativeBase, output: $output, new pos: $pos, program: ${program}")

        }
        println("output: $output")
        return output
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
        val input = droid.getInput().toLong()
//        println("retrieved input from robot: $input")
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
