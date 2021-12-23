package test.advent.edition2021.day8

import test.advent.edition2021.day8.Display.Pos.*
import test.advent.edition2021.findOrThrow
import test.advent.edition2021.getAllInListOrThrow
import test.advent.edition2021.getOrThrow
import java.io.File

val day = 8;
val file = File("src/main/resources/edition2021/day${day}/input")

class Puzzle(private val rawInput: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        println(rawInput)

        /**
         *   0:      1:      2:      3:      4:
        aaaa    ....    aaaa    aaaa    ....
        b    c  .    c  .    c  .    c  b    c
        b    c  .    c  .    c  .    c  b    c
        ....    ....    dddd    dddd    dddd
        e    f  .    f  e    .  .    f  .    f
        e    f  .    f  e    .  .    f  .    f
        gggg    ....    gggg    gggg    ....

        5:      6:      7:      8:      9:
        aaaa    aaaa    aaaa    aaaa    aaaa
        b    .  b    .  .    c  b    c  b    c
        b    .  b    .  .    c  b    c  b    c
        dddd    dddd    ....    dddd    dddd
        .    f  e    f  .    f  e    f  .    f
        .    f  e    f  .    f  e    f  .    f
        gggg    gggg    ....    gggg    gggg
        */
        /**
        0: 6, 1: 2, 2: 5, 3: 5, 4: 4, 5: 5, 6: 6, 7: 3, 8: 7, 9: 6
         unique: 1: 2, 7: 3, 4: 4, 8: 7. / t, b, lo, rb, ro, lb, mid. 1, 4, 7, 8, 9, 2
         */ 
        // 7 - 1 = top. de 6 die 4 + top heeft = 9, 9 - 4 - top = bottom
        // 8 - 9 = lo. 5 die lo heeft = 2. overlap tussen 1 en 2 = rb, 1 - rb = ro
        // 2 - t - rb - lo - b = mid. 8 - t, b, lo, ro, mid, rb = lb
        // acedgfb cdfbe gcdfa fbcad dab cefabd cdfgeb eafb cagedb ab
        //   8                        7                 4          1
        // 7 - 1 = top
       
        val displays = rawInput.map { Display.createDisplay(it) }
        println(displays)
        println(displays.flatMap { it.output }.count { it.size in listOf(2, 3, 4, 7) })
    }
    
    fun runPart2() {
        println("Part 2 of Day $day")
        println(rawInput)
        val displays = rawInput.map { Display.createDisplay(it) }
//        val input = "acedgfb cdfbe gcdfa fbcad dab cefabd cdfgeb eafb cagedb ab | cdfeb fcadb cdfeb cdbaf"
//        val display = Display.createDisplay(input)
        println(displays.map { it.getOutput() }.sum())
    }
}

data class Display(val signals: List<List<Char>>, val output: List<List<Char>>) {
    
    fun getOutput(): Int {
        // "acedgfb cdfbe gcdfa fbcad dab cefabd cdfgeb eafb cagedb ab | cdfeb fcadb cdfeb cdbaf"
        val numToSignalMap = mutableMapOf(1 to signals.findOrThrow { it.size == 2 }, 7 to signals.findOrThrow { it.size == 3 }, 
            4 to signals.findOrThrow { it.size == 4 }, 8 to signals.findOrThrow { it.size == 7 })
        val posToCharMap = mutableMapOf<Pos, Char>()
        posToCharMap[T] = (numToSignalMap.getOrThrow(7) - numToSignalMap.getOrThrow(1))[0]
        numToSignalMap[9] = signals.filter { it.size == 6 }.findOrThrow { it.containsAll(numToSignalMap.getOrThrow(4) + posToCharMap.getOrThrow(T)) }
        posToCharMap[B] = (numToSignalMap.getOrThrow(9) - numToSignalMap.getOrThrow(4) - posToCharMap.getOrThrow(T))[0]
        posToCharMap[BL] = (numToSignalMap.getOrThrow(8) - numToSignalMap.getOrThrow(9))[0]
        numToSignalMap[2] = signals.filter { it.size == 5 }.findOrThrow { it.contains(posToCharMap.getOrThrow(BL)) }
        posToCharMap[TR] = numToSignalMap.getOrThrow(1).findOrThrow { numToSignalMap.getOrThrow(2).contains(it) }
        posToCharMap[BR] = (numToSignalMap.getOrThrow(1) - posToCharMap.getOrThrow(TR))[0]
        posToCharMap[M] = (numToSignalMap.getOrThrow(2) - posToCharMap.getAllInListOrThrow(T, TR, BL, B))[0]
        posToCharMap[TL] = (numToSignalMap.getOrThrow(8) - posToCharMap.getAllInListOrThrow(T, TR, M, BR, BL, B))[0]
        numToSignalMap[0] = posToCharMap.getAllInListOrThrow(T, TR, TL, BR, BL, B)
        numToSignalMap[3] = posToCharMap.getAllInListOrThrow(T, TR, M, BR, B)
        numToSignalMap[5] = posToCharMap.getAllInListOrThrow(T, TL, M, BR, B)
        numToSignalMap[6] = posToCharMap.getAllInListOrThrow(T, TL, M, BR, BL, B)
        return output.map { numToSignalMap.findByValueOrThrow(it) }.joinToString("").toInt()
    }
    
    companion object {
        fun createDisplay(input: String): Display {
            // gcafb gcf dcaebfg ecagb gf abcdeg gaef cafbge fdbac fegbdc | fgae cfgab fg bagce
            val (signals, output) = input.split(" | ")            
            return Display(signals.split(" ").map { it.toList() }, output.split(" ").map { it.toList() })
        } 
    }
    
    enum class Pos { T, TL, TR, M, BL, BR, B }
}

private fun Map<Int, List<Char>>.findByValueOrThrow(list: List<Char>) : Int {
    for ((k, v) in this.entries) {
        if (v.containsAll(list) && v.size == list.size) return k
    }
    throw IllegalStateException("Not found!")
}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

