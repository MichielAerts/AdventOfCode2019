package test.advent.edition2022.day17

import test.advent.edition2022.*
import test.advent.edition2022.day17.Chamber.Dir.*
import java.io.File

val day = 17
val file = File("src/main/resources/edition2022/day${day}/input")

class Puzzle(private val input: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val noOfRocks = 2022
        val chamber = Chamber.create(input[0], noOfRocks * 5)
        for (rock in 0 until noOfRocks) {
            chamber.addRock(rocks[rock % 5])
//            chamber.print()
        }
        println(chamber.highestRock + 1)
    }
    
    fun runPart2() {
        println("Part 2 of Day $day")
        val noOfRocks = 10000
        val chamber = Chamber.create(input[0], noOfRocks * 5)
        val rocksAndHeight = mutableListOf<Pair<Int, Int>>()
        for (rock in 0 until noOfRocks) {
            chamber.addRock(rocks[rock % 5])
//            chamber.print()
            val highestRow = chamber.grid.getHighestRowContaining('#')
            if (rock == 8319) println(highestRow)
            if (chamber.grid.getRow(highestRow).all { it.value == '#' }) {
                rocksAndHeight.add(Pair(rock, highestRow))
                println("rocks: $rock, height: $highestRow, ${chamber.jetPattern.size}")
            }
        }
        // pattern repeats every 1720 rocks (height 2626) after rock 566 (height 867), leaving 874 rocks at the end (height 1327)
        // 867 + 2626 * (floor(1000000000000 / 1720) ) + 1327
        println("1526744186042")
        println(chamber.highestRock + 1)
    }
}

val rocks = listOf(
    // shape from top line
    /*
    ####
     */
    listOf(Pos(2, 4), Pos(3,4), Pos(4, 4), Pos(5, 4)),
    /*
    .#.
    ###
    .#. 
     */
    listOf(Pos(3, 4), Pos(2,5), Pos(3, 5), Pos(4, 5), Pos(3, 6)),
    /*
    ..#
    ..#
    ###    
     */
    listOf(Pos(2, 4), Pos(3,4), Pos(4, 4), Pos(4, 5), Pos(4, 6)),
    /*
    #
    #
    #
    #
     */
    listOf(Pos(2, 4), Pos(2,5), Pos(2, 6), Pos(2, 7)),
    /*
    ##
    ##
     */
    listOf(Pos(2, 4), Pos(3,4), Pos(2, 5), Pos(3, 5))
)

data class Chamber(val grid: List<List<Point>>, val jetPattern: ArrayDeque<Char>, var highestRock: Int = -1) {
    fun addRock(relativePos: List<Pos>) {
        var rock = relativePos.map { Pos(it.x, it.y + highestRock) }
        var rockStopped = false
        var dir = DOWN
        while(!rockStopped) {
//            println(rock)
            dir = if (dir == DOWN) Dir.get(jetPattern.removeFirst()) else DOWN
            if (canMove(rock, dir)) {
                rock = move(rock, dir)
            } else when(dir) {
                DOWN -> {
                    rockStopped = stop(rock)
                    highestRock = grid.getHighestRowContaining('#')
                }
                RIGHT, LEFT -> continue
            }
        }
//        println(rock)
    }

    private fun stop(rock: List<Pos>): Boolean {
        grid.changePoints(rock.map { Point(it.x, it.y) }.toSet(), '#')
        return true
    }
    
    fun move(rock: List<Pos>, direction: Dir): List<Pos> = when(direction) {
        DOWN -> rock.map { Pos(it.x, it.y - 1) }
        RIGHT -> rock.map { Pos(it.x + 1, it.y) }
        LEFT -> rock.map { Pos(it.x - 1, it.y) }
    }
    
    private fun canMove(rock: List<Pos>, direction: Dir): Boolean = when(direction) {
        DOWN -> rock.none { it.y == 0 || grid.getPoint(it.x, it.y - 1)!!.value == '#'}
        RIGHT -> rock.none { it.x == 6 || grid.getPoint(it.x + 1, it.y)!!.value == '#'} 
        LEFT -> rock.none { it.x == 0 || grid.getPoint(it.x - 1, it.y)!!.value == '#'}
    }
    
    fun print() {
        grid.take(30).reversed().printV()
    }

    enum class Dir(val c: Char) {

        DOWN('V'), RIGHT('>'), LEFT('<');

        companion object {
            fun get(c: Char) = values().find { it.c == c } ?: throw IllegalStateException()
        }
    }
    
    companion object {
        fun create(input: String, maxSize: Int): Chamber {
            val jetPattern = ArrayDeque<Char>()
            while (jetPattern.size < 5 * maxSize) {
                jetPattern.addAll(input.toList())
            }
            return Chamber(initEmptyGrid(0, 6, 0, maxSize), jetPattern)  
        } 
    }
}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

