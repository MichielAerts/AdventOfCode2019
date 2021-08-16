package test.advent.day25

import org.paukov.combinatorics3.Generator
import java.io.File
import kotlin.streams.toList

val day = 25;
val file = File("src/main/resources/day${day}/input")

fun main() {
    val intCodeList = file.readLines().get(0).split(",")
        .map { it.toLong() }
        .toMutableList()
    //part 1
    val gatherAndGoToFloor = arrayOf(
        "north\n",
        "north\n",
        "north\n",
        "take mutex\n",
        "south\n",
        "south\n",
        "east\n",
//        "take escape pod\n",
        "north\n",
        "take loom\n",
        "south\n",
        "west\n",
        "south\n",
        "east\n",
        "take semiconductor\n",
        "east\n",
        "take ornament\n",
//        "north\n",
//        "take photons\n",
//        "west\n",
//        "take infinite loop\n",
//        "west\n",
//        "take giant electromagnet\n",
//        "east\n",
//        "east\n",
//        "south\n",
        "west\n",
        "west\n",
        "west\n",
//        "south\n"
        "west\n",
        "south\n",
//        "take molten lava\n",
        "east\n",
        "take asterisk\n",
        "north\n",
        "take wreath\n",
        "south\n",
        "west\n",
        "north\n",
        "take sand\n",
        "north\n",
        "take dark matter\n",
        "east\n",
        "inv\n"
    )
//    "east\n"
    val items = listOf(
        "mutex", "loom", "semiconductor", "ornament", "sand",
        "dark matter", "asterisk", "wreath"
    )
    val subSets = Generator.subset(items).simple().stream().toList()
    println(subSets)
    for (set in subSets) {
        try {
            val robot = Robot(IntCode.createProgram(intCodeList))
            robot.addInput(*gatherAndGoToFloor)
            for (el in set) robot.addInput("drop $el\n")
            robot.addInput("east\n")
            robot.run()
        } catch (e: Exception) {
            continue
        }
        println("yeah, dropping $set worked!")
        break
    }

    /*
    				Passages (mutex)				
				Observatory	Holodeck (loom)			
Science Lab (dark matter)	-	Security Checkpoint	PSF!	Gift Wrapping Center	Corridor (escape pod)	Hot Chocolate Fountain (giant electromagnet)	Storage (infinite loop)	Navigation (photons)
Hallway (sand)	-	Engineering	-	Start	Kitchen (semiconductor)	-	-	Arcade (ornament)
|	Stables (wreath)	|						
Sick Bay (molten lava)	Warp Drive Maintenance (asterisk)	Crew Quarters						

     */
}

class Robot(val program: IntCode, var input: MutableList<Int> = mutableListOf(), var inputCount: Int = 0) {
    init {
        program.robot = this
    }

    fun run() = program.run()

    fun addInput(vararg inputs: String) {
        for (inputStr in inputs) {
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

    companion object {
        private const val totalMem = 10000
        fun createProgram(intCodeList: MutableList<Long>) =
            IntCode((intCodeList + LongArray(totalMem - intCodeList.size).toMutableList()).toMutableList())
    }

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