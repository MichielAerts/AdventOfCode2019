package test.advent.edition2021.day17

import test.advent.edition2021.Pos
import test.advent.edition2021.findGroupAsInt
import java.io.File

val day = 17;
val file = File("src/main/resources/edition2021/day${day}/input")

class Puzzle(private val rawInput: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val targetArea = TargetArea.createTargetArea(rawInput[0])
        var maxHeight = Int.MIN_VALUE
        for (xs in 1..100) {
            println(xs)
            for (ys in -1000..1000) {
                val potentialMaxHeight =
                    targetArea.checkTrajectoryAndHeight(PosAndVelocity(Pos(0, 0), Velocity(xs, ys)))
                potentialMaxHeight?.let {
                    if (potentialMaxHeight > maxHeight) {
                        maxHeight = potentialMaxHeight
                        println("new maxHeight $maxHeight for x: $xs, y: $ys")
                    }
                }
            }
        }
    }

    fun runPart2() {
        println("Part 2 of Day $day")
        val targetArea = TargetArea.createTargetArea(rawInput[0])
        val hits = mutableListOf<Velocity>()
        
        for (xs in 1..300) {
            for (ys in -4000..4000) {
                val potentialMaxHeight =
                    targetArea.checkTrajectoryAndHeight(PosAndVelocity(Pos(0, 0), Velocity(xs, ys)))
                potentialMaxHeight?.let {
                    hits.add(Velocity(xs, ys))
                }
            }
        }
        println("hits: ${hits.size}")
        //1100 too low
        //1802 too low
        //4556
    }
}

data class TargetArea(val xRange: IntRange, val yRange: IntRange) {

    fun checkTrajectoryAndHeight(initialPV: PosAndVelocity): Int? {
        var steps = 0
        var maxHeight = 0
        val maxSteps = 1000
        var pV = initialPV
        while (!inTargetArea(pV.pos) && (!hasPassed(pV.pos) || steps == maxSteps)) {
            pV = pV.move()
            if (pV.pos.y > maxHeight) maxHeight = pV.pos.y
            steps++
        }
        return if (inTargetArea(pV.pos)) maxHeight else null
    }

    private fun hasPassed(pos: Pos): Boolean = pos.x > xRange.last || pos.y < yRange.first

    private fun inTargetArea(pos: Pos): Boolean = pos.x in xRange && pos.y in yRange

    companion object {
        private val regex = "x=(?<xs>-?\\d+)..(?<xe>-?\\d+), y=(?<ys>-?\\d+)..(?<ye>-?\\d+)".toRegex()
        fun createTargetArea(input: String) = TargetArea(
            IntRange(regex.findGroupAsInt(input, "xs"), regex.findGroupAsInt(input, "xe")),
            IntRange(regex.findGroupAsInt(input, "ys"), regex.findGroupAsInt(input, "ye"))
        )
    }
}

data class PosAndVelocity(val pos: Pos, val v: Velocity) {
    fun move(): PosAndVelocity {
        val newPos = Pos(pos.x + v.xs, pos.y + v.ys)
        val newV = Velocity(
            if (v.xs > 0) v.xs - 1 else if (v.xs < 0) v.xs + 1 else v.xs, 
            v.ys - 1)
        return PosAndVelocity(newPos, newV)
    }
}

data class Velocity(val xs: Int, val ys: Int)

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

