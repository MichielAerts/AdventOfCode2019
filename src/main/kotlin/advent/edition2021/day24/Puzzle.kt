package test.advent.edition2021.day24

import test.advent.edition2021.getOrThrow
import java.io.File

val day = 24;
val file = File("src/main/resources/edition2021/day${day}/input")

class Puzzle(private val rawInput: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val instructions = rawInput.filter { !it.startsWith("#") }.map { Instruction.createInstruction(it) }
        val alu = ALU(instructionBlocks = instructions.chunked(18))
        
        // run last block, aim for z = 0, add z input to set
        // run next to last block, aim for z in set
        val outputForBlocks = mutableMapOf<Int, Set<Int>>()
        outputForBlocks[14] = setOf(0)
        var zSet = mutableSetOf(0)
        for (block in 14 downTo 1) {
            val validInputs = mutableSetOf<Int>()
            for (inp in 1..9) for (inputZ in 0..10000000) {
                alu.reset(inp, inputZ)
                alu.runBlock(block)
                if (alu.z in zSet) {
                    //println("z: ${alu.z} for input $inp and z input $inputZ")
                    validInputs.add(inputZ)
                }
            }
            println("calculating input for $block, output for ${block - 1}")
            outputForBlocks[block - 1] = validInputs
            zSet = validInputs
        }
        var zInputForBlock = 0
        blocks@ for (block in 1 .. 14) {
            println("block: $block")
//            for (input in 9 downTo 1) {
                for (input in 1 .. 9) {
                alu.reset(input, zInputForBlock)
                alu.runBlock(block)
                if (alu.z in outputForBlocks.getOrThrow(block)) {
                    println(input)
                    zInputForBlock = alu.z
                    continue@blocks
                }
            }
        }
        //97919997299495
    }
    
    fun runPart2() {
        println("Part 2 of Day $day")
//        println(rawInput)      
    }
}

private fun Long.toInts(): List<Int> = toString().toList().map { Character.getNumericValue(it) }

data class ALU(var w: Int = 0, var x: Int = 0, var y: Int = 0, var z: Int = 0,
               val instructionBlocks: List<List<Instruction>>, var inputQ: ArrayDeque<Int> = ArrayDeque(), var input: Int = 0, var inputZ: Int = 0) {
    fun runProgram() {
        instructionBlocks.flatten().forEach { runInstruction(it) }
    }

    fun runBlock(no: Int) {
        instructionBlocks[no - 1].forEach { runInstruction(it) }
    }

    fun print() = println("final state: z $z, input $input, inputZ $inputZ")
    
    fun reset(newInput: Int, newInputZ: Int) {
        w = 0
        x = 0
        y = 0
        z = newInputZ
        input = newInput
        inputZ = newInputZ
        inputQ = ArrayDeque(listOf(newInput))
    }
    
    private fun runInstruction(instruction: Instruction) {
        val (type, first, second) = instruction
        when(type) {
            Instruction.Operation.INP -> setVar(first, inputQ.removeFirst())
            Instruction.Operation.ADD -> setVar(first, getVar(first) + getVar(second!!))
            Instruction.Operation.DIV -> setVar(first, getVar(first) / getVar(second!!))
            Instruction.Operation.MOD -> setVar(first, getVar(first) % getVar(second!!))
            Instruction.Operation.MUL -> setVar(first, getVar(first) * getVar(second!!))
            Instruction.Operation.EQL -> setVar(first, if (getVar(first) == getVar(second!!)) 1 else 0)
        }
    }

    private fun setVar(variable: String, value: Int) {
        when (variable) {
            "w" -> w = value
            "x" -> x = value
            "y" -> y = value
            "z" -> z = value
            else -> throw IllegalArgumentException("couldnt")
        }
    }

    private fun getVar(variable: String) : Int {
        return when (variable) {
            "x" -> x
            "y" -> y
            "w" -> w
            "z" -> z
            else -> variable.toInt()
        }
    }
}

data class Instruction(val type: Operation, val first: String, val second: String? = null) {
    enum class Operation { INP, ADD, MUL, DIV, MOD, EQL }
    
    companion object {
        fun createInstruction(input: String): Instruction {
            val fields = input.split(" ")
            return when (fields[0]) {
                "inp" -> Instruction(Operation.INP, fields[1])
                "add", "mul", "div", "mod", "eql" -> Instruction(Operation.valueOf(fields[0].toUpperCase()), fields[1], fields[2])
                else -> throw IllegalArgumentException("couldnt")
            }
        }
    }
}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

