package test.advent.day23

import kotlinx.coroutines.*
import java.io.File
import java.util.*

val day = 23;
val file = File("src/main/resources/edition2019/day${day}/input")
val packets = mutableMapOf<Long, Queue<Pair<Long, Long>>>()

fun main() {
    val intCodeList = file.readLines().get(0).split(",")
        .map { it.toLong() }
        .toMutableList()

    val noComputers = 50
    (0 until noComputers).forEach { packets.put(it.toLong(), LinkedList()) }
    val nat = NotAlwaysTransmitting()

    val computers = (0 until noComputers).map {
        Pair(
            it,
            Computer(IntCode.createProgram(intCodeList), it.toLong(), nat)
        )
    }
    nat.computers = computers

    runBlocking {
        coroutineScope {
            computers.forEach { launch(Dispatchers.IO) { it.second.start() } }
            launch(Dispatchers.IO) { nat.startMonitoring() }
        }
    }

    //part 1
    // part 2 
}

class NotAlwaysTransmitting(var lastPacket: Pair<Long, Long> = Pair(0, 0)) {

    lateinit var computers: List<Pair<Int, Computer>>

    fun updateNatPacket(newPacket: Pair<Long, Long>) {
        println("update packet, new packet $newPacket, last packet: $lastPacket")
        if (lastPacket == newPacket) {
            println("!!! Found double !!!")
            computers.forEach { it.second.shutDown() }
        }
        lastPacket = newPacket
    }

    private fun networkIsIdle(): Boolean =
        (packets.values.all { it.isEmpty() } && computers.all { it.second.isIdle })

    private fun sendPacket() {
        packets[0]!!.add(lastPacket)
    }

    suspend fun startMonitoring() {
        while (true) {
            if (networkIsIdle()) sendPacket()
            delay(200)
        }
    }
}

class Computer(
    val program: IntCode,
    val networkAddress: Long,
    val nat: NotAlwaysTransmitting,
    var isIdle: Boolean = false,
    var initialized: Boolean = false,
    var shutDown: Boolean = false,
    var currentInput: Pair<Long, Long>? = null
) {

    init {
        program.computer = this
    }

    fun start() {
        println("starting $networkAddress")
        program.run()
    }

    fun sendPacket(output: Triple<Long, Long, Long>) {
        println("$networkAddress is sending packet $output")
        val address = output.first
        val newPacket = Pair(output.second, output.third)
        if (address == 255L) {
            nat.updateNatPacket(newPacket)
        } else {
            packets[address]!!.add(newPacket)
        }
    }

    fun getInput(): Long {
        when {
            !initialized -> {
                initialized = true
                return networkAddress
            }
            currentInput != null -> {
                isIdle = false
                val input = currentInput!!.second
                currentInput = null
                return input
            }
            packets.containsKey(networkAddress) && !packets[networkAddress].isNullOrEmpty() -> {
                isIdle = false
                currentInput = packets[networkAddress]?.poll()
                return currentInput!!.first
            }
            else -> {
                isIdle = true
                return -1
            }
        }
    }

    fun shutDown() {
        shutDown = true
    }
}

class IntCode(
    private val program: MutableList<Long>,
    var pos: Int = 0,
    var relativeBase: Int = 0
) {
    lateinit var computer: Computer

    companion object {
        private const val totalMem = 10000
        fun createProgram(intCodeList: MutableList<Long>) =
            IntCode((intCodeList + LongArray(totalMem - intCodeList.size).toMutableList()).toMutableList())
    }

    fun run() {
//        println("relative base: $relativeBase, new pos: $pos, program: ${program}")
        var output = mutableListOf<Long>()
        while (!computer.shutDown) {
            val operation = Operation.createOp(program[pos])
            if (operation.opCode == 99) break
            val currentOutput = performOperation(operation)
            currentOutput?.let {
                output += currentOutput
                if (output.size == 3) {
                    computer.sendPacket(Triple(output[0], output[1], output[2]))
                    output = mutableListOf()
                }
            }
//            println("relative base: $relativeBase, output: $output, new pos: $pos, program: ${program}")

        }
        println("ended with: $output")
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
        val input = computer.getInput()
//        if (input != -1L) println("retrieved input from computer: $input")
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
