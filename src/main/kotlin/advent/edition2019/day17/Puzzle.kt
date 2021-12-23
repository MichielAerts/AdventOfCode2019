package test.advent.day17

import java.io.File

val day = 17;
val file = File("src/main/resources/edition2019/day${day}/input")

fun main() {
    val intCodeList = file.readLines().get(0).split(",")
        .map { it.toLong() }
        .toMutableList()
    //part 1
    val totalMem = 10000
    val program = IntCode((intCodeList + LongArray(totalMem - intCodeList.size).toMutableList()).toMutableList())
    val robot = Robot(program)
//    val view = robot.draw()
//    val grid = Grid()
//    grid.setView(view)
//    grid.print()
//    val intersections = grid.getIntersections()
//    println(intersections)
//    println(intersections.map { it.x * it.y }.sum())

    // part2
    val main = "A,B,A,C,B,A,C,B,A,C\n"
    val a = "L,6,L,4,R,12\n"
    val b = "L,6,R,12,R,12,L,8\n"
    val c = "L,6,L,10,L,10,L,6\n"
    val videofeed = "y\n"
    robot.addInput(main, a, b, c, videofeed)
//
//    max 20 chars, including comma
//    main program: A,B,A,C,B,A,C,B,A,C
//    A: L,6,L,4,R,12
//    B: L,6,R,12,R,12,L,8
//    C: L,6,L,10,L,10,L,6
//    end with newline = ascii 10
    // y or n videofeed
    program.robot = robot
    println(robot.draw())
//    println('툸'.toInt())
    // 53816 is too low
}

class Grid {
    lateinit var grid: MutableList<MutableList<Char>>

    fun print(): Unit = grid.forEach { println(it.joinToString(separator = "")) }

    fun setView(view: String) {
        val r = view.split("\n").filter { it.isNotEmpty() }.map { l -> l.toMutableList() }
        grid = MutableList(r.size) { MutableList(r[0].size) { ' ' } }
        for (y in 0 until r.size) {
            for (x in 0 until r[1].size) {
                grid.set(Coor(x, y), r[y][x])
            }
        }
    }

    fun getIntersections(): List<Coor> {
        val intersections = mutableListOf<Coor>()
        for (y in 1 until (grid.size - 1)) {
            for (x in 1 until (grid[0].size - 1)) {
                val currentCoor = Coor(x, y)
                val neighbors = currentCoor.getNeighbours()
                if (grid[y][x] == '#' && neighbors.all { grid.get(it) == '#' }) {
                    intersections += Coor(x, y)
                }
            }
        }
        return intersections
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

class Robot(val program: IntCode, var input: MutableList<Int> = mutableListOf(), var inputCount: Int = 0) {
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
    lateinit var robot: Robot

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
        val input = robot.getInput().toLong()
        println("retrieved input from robot: $input")
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