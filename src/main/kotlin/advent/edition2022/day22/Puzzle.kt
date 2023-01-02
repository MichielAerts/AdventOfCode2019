package test.advent.edition2022.day22

import test.advent.edition2022.*
import test.advent.edition2022.Direction.*
import java.io.File

val day = 22
val file = File("src/main/resources/edition2022/day${day}/input")

class Puzzle(private val input: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val (map, instructionsInput) = input.splitBy { it.isEmpty() }
        val board = Board.create(map)
        val reg = Regex("(?<=[RL])|(?=[RL])")
        val instructions = instructionsInput[0].split(reg).map { Move.create(it) }
        instructions.forEach { board.followInstruction(it) }
        println(board.getPassword())
    //        board.map.printV()
    }
    
    fun runPart2() {
        println("Part 2 of Day $day")
        val (map, instructionsInput) = input.splitBy { it.isEmpty() }
        val cube = Cube.create(map)
        val reg = Regex("(?<=[RL])|(?=[RL])")
        val instructions = instructionsInput[0].split(reg).map { Move.create(it) }
        instructions.forEach { cube.followInstruction(it) }
        println(cube.getPassword()) 
    }
}

data class Move(val type: Type, val amount: Int = 0) {
    enum class Type { MOVE, TURN_RIGHT, TURN_LEFT; }
    
    companion object {
        fun create(input: String) = when(input) {
            "R" -> Move(Type.TURN_RIGHT)
            "L" -> Move(Type.TURN_LEFT)
            else -> Move(Type.MOVE, input.toInt())
        }
    }
}

data class Cube (val map: List<List<Point>>, var currentPosition: Pair<Pos, Direction>, val connections: List<Pair<Pair<Pos, Direction>, Pair<Pos, Direction>>>) {

    fun followInstruction(instruction: Move) {
        when(instruction.type) {
            Move.Type.TURN_RIGHT -> currentPosition = Pair(currentPosition.first, currentPosition.second.turnRight())
            Move.Type.TURN_LEFT -> currentPosition = Pair(currentPosition.first, currentPosition.second.turnLeft())
            Move.Type.MOVE -> {
                var count = 0
                while (count < instruction.amount && currentPosition.canMoveOnCube(map, connections)) {
                    currentPosition = currentPosition.moveOnCube(map, connections)
                    count++
                    println(currentPosition)
//                    map.printV()
                }
            }
        }
        println(currentPosition)
    }

    fun getPassword(): Int = 1000 * (currentPosition.first.y + 1) + 4 * (currentPosition.first.x + 1) +
            when(currentPosition.second) {
                UP -> 3
                DOWN -> 1
                RIGHT -> 0
                LEFT -> 2
            }


    companion object {
        private val cubeSize = 50
        
        fun create(input: List<String>): Cube {
            val maxX = input.maxOf { it.length }
            val points = input.map { it.padEnd(maxX, ' ') }
            val grid = points.to2DGridOfPointsWithLetters()
            val firstPosition = grid[0].first { it.value == '.' }
            /*
             */
            val connections = listOf(
                //        AU (l>r) FL (u>d) Dir r
                horizontalSide(1, 2, 0, UP).zip(verticalSide(0, 3, 4, RIGHT)),
                //        AL (u>d) EL (d>u) Dir r
                verticalSide(50, 0, 1, LEFT).zip(verticalSide(0, 3, 2, RIGHT)),
                //        BU (l>r) FD (l>r) Dir u
                horizontalSide(2, 3, 0, UP).zip(horizontalSide(0, 1, 199, UP)),
                //        BR (u>d) DR (d>u) Dir l 
                verticalSide(149, 0, 1, RIGHT).zip(verticalSide(99, 3, 2, LEFT)),
                //        BD (l>r) CR (u>d) Dir l
                horizontalSide(2, 3, 49, DOWN).zip(verticalSide(99, 1, 2, LEFT)),
                //        CL (u>d) EU (l>r) Dir d
                verticalSide(50, 1, 2, LEFT).zip(horizontalSide(0, 1, 100, DOWN)),
                //        CR (u>d) BD (l>r) Dir u
                verticalSide(99, 1, 2, RIGHT).zip(horizontalSide(1, 2, 49, UP)),
                //        DR (u>d) BR (d>u) Dir l 
                verticalSide(99, 2, 3, RIGHT).zip(verticalSide(149, 1, 0, LEFT)),
                //        DD (l>r) FR (u>d) Dir l
                horizontalSide(1, 2, 149, DOWN).zip(verticalSide(49, 4, 3, LEFT)),
                //        EU (l>r) CL (u>d) Dir r
                horizontalSide(0, 1, 100, UP).zip(verticalSide(50, 1, 2, RIGHT)),
                //        EL (u>d) AL (d<u) Dir r
                verticalSide(0, 2, 3, LEFT).zip(verticalSide(50, 1, 0, RIGHT)),
                //        FL (u>d) AU (l>r) Dir d
                verticalSide(0, 3, 4, LEFT).zip(horizontalSide(1, 2, 0, DOWN)),
                //        FD (l>r) BU (l>r) Dir d
                horizontalSide(0, 1, 199, DOWN).zip(horizontalSide(2, 3, 0, DOWN)),
                //        FR (u>d) DD (l>r) Dir u
                verticalSide(49, 3, 49, RIGHT).zip(horizontalSide(1, 2, 149, UP)),
            )
            return Cube(grid, Pair(Pos(firstPosition.x, firstPosition.y), RIGHT), connections.flatten())
        }
        
        private fun horizontalSide(startCubeX: Int, endCubeX: Int, y: Int, direction: Direction): List<Pair<Pos, Direction>> {
            val startX = startCubeX * cubeSize
            val endX = endCubeX * cubeSize - 1
            return if (startX < endX) (startX .. endX).map { Pair(Pos(it, y), direction) }
            else (startX downTo endX).map { Pair(Pos(it, y), direction) }
        }

        private fun verticalSide(x: Int, startCubeY: Int, endCubeY: Int, direction: Direction): List<Pair<Pos, Direction>> {
            val startY = startCubeY * cubeSize
            val endY = endCubeY * cubeSize - 1
            return if (startY < endY) (startY .. endY).map { Pair(Pos(x, it), direction) }
            else (startY downTo endY).map { Pair(Pos(x, it), direction) }
        }
    }
}

private fun Pair<Pos, Direction>.canMoveOnCube(map: List<List<Point>>, connections: List<Pair<Pair<Pos, Direction>, Pair<Pos, Direction>>>): Boolean {
    val newPos = this.first.getNextPos(this.second)
    var newPoint = map.getPoint(newPos)
    if (newPoint == null || newPoint.value == ' ') {
        // wraparound
        newPoint = map.getPoint(connections.findOrThrow { it.first == this }.second.first)
    }
    if (newPoint?.value == '#') return false
    return true
}

private fun Pair<Pos, Direction>.moveOnCube(map: List<List<Point>>, connections: List<Pair<Pair<Pos, Direction>, Pair<Pos, Direction>>>): Pair<Pos, Direction> {
    val newPos = this.first.getNextPos(this.second)
    var newPoint = map.getPoint(newPos)
    var newDirection = this.second
    if (newPoint == null || newPoint.value == ' ') {
        // wraparound
        val newPos = connections.findOrThrow { it.first == this }.second
        newPoint = map.getPoint(newPos.first)!!
        newDirection = newPos.second
    }
    if (newPoint.value == '#') return this
    return Pair(Pos(newPoint.x, newPoint.y), newDirection)
}

data class Board (val map: List<List<Point>>, var currentPosition: Pair<Pos, Direction>) {

    fun followInstruction(instruction: Move) {
        when(instruction.type) {
            Move.Type.TURN_RIGHT -> currentPosition = Pair(currentPosition.first, currentPosition.second.turnRight())
            Move.Type.TURN_LEFT -> currentPosition = Pair(currentPosition.first, currentPosition.second.turnLeft())
            Move.Type.MOVE -> {
                var count = 0
                while (count < instruction.amount && currentPosition.canMove(map)) {
                    currentPosition = currentPosition.move(map)
                    count++
                    println(currentPosition)
                }
            }
        }
        println(currentPosition)
    }

    fun getPassword(): Int = 1000 * (currentPosition.first.y + 1) + 4 * (currentPosition.first.x + 1) + 
            when(currentPosition.second) {
                UP -> 3
                DOWN -> 1
                RIGHT -> 0
                LEFT -> 2
            }


    companion object {
        fun create(input: List<String>): Board {
            val maxX = input.maxOf { it.length }
            val points = input.map { it.padEnd(maxX, ' ') }
            val grid = points.to2DGridOfPointsWithLetters()
            val firstPosition = grid[0].first { it.value == '.' }
            return Board(grid, Pair(Pos(firstPosition.x, firstPosition.y), RIGHT))
        } 
    }
}

private fun Pair<Pos, Direction>.canMove(map: List<List<Point>>): Boolean {
    val newPos = this.first.getNextPos(this.second)
    var newPoint = map.getPoint(newPos)
    if (newPoint == null || newPoint.value == ' ') {
        // wraparound
        newPoint = map.getPoint(this.first)!!.getView(this.second.opposite(), map).last { it.value != ' ' }
    }
    if (newPoint.value == '#') return false
    return true
}

private fun Pair<Pos, Direction>.move(map: List<List<Point>>): Pair<Pos, Direction> {
    val newPos = this.first.getNextPos(this.second)
    var newPoint = map.getPoint(newPos)
    if (newPoint == null || newPoint.value == ' ') {
        // wraparound
        newPoint = map.getPoint(this.first)!!.getView(this.second.opposite(), map).last { it.value != ' ' }
    }
    if (newPoint.value == '#') return this
    return Pair(Pos(newPoint.x, newPoint.y), this.second)
}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
//    puzzle.runPart1()
    puzzle.runPart2()
}

