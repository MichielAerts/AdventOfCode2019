package test.advent.day15

import java.io.File
import kotlin.random.Random

val day = 15;
val file = File("src/main/resources/edition2019/day${day}/input")

fun main() {
    val intCodeList = file.readLines().get(0).split(",")
        .map { it.toLong() }
        .toMutableList()
    //part 1
    val totalMem = 10000
    val program = IntCode((intCodeList + LongArray(totalMem - intCodeList.size).toMutableList()).toMutableList())
    val droid = Droid(program)
    val room = Room(size = 50, droid = droid)
//    room.explore() // 204 moves

    // part2
    val exploredRoom ="""
    | ##### ######### ##### ##### ### ####### 
    |#.....#.........#.....#.....#...#.......#
    | ##.#.###.###.#.#.###.###.#.#.#.#####.#.#
    |#...#...#.#...#...#.#...#.#...#.....#.#.#
    |#.## ##.#.#.#######.###.#.#########.#.#.#
    |#...#.#.#.#.#...#.....#...#.......#.#.#.#
    | ##.#.#.###.#.#.#####.#####.#######.#.#.#
    |#...#...#...#.#.....#.....#.........#.#.#
    |#.###.###.###.#####.#.#.###.###########.#
    |#.#...#...#.....#.....#.#...#.........#.#
    |#.#.###.#.#.###.#.#######.#########.#.#.#
    |#.#...#.#.#...#.#.#.....#.....#.....#...#
    |#.###.###.#####.#.#.###.#####.#.#### ##.#
    |#...#.....#.....#.#.#.......#...#...#...#
    |#.#.#######.#######.#.###########.#.#.## 
    |#.#...#.............#.#.........#.#...#.#
    |#.###.#.#############.#.#######.#.#####.#
    |#.#...#.#...........#.#.#...#...#...#...#
    | ##.#####.#########.#.#.#.#.#.#####.#.#.#
    |#...#...#...#.....#.#.....#.#.....#.#.#.#
    |#.###.#.###.#.###.#.#######.#####.#.#.## 
    |#...#.#.....#.#.....#.#...#.#...#...#...#
    |#.#.#.#######.#.#####.#.###.###.#####.#.#
    |#.#.#...#...#.#.#.....#.......#...#...#.#
    |#.#.###.###.#.###.#####.#####.#.#.#####.#
    |#.#...#.....#.....#.....#...#.#.#...#...#
    |#.#.#.#####.###########.#.#.#.#.###.#.#.#
    |#.#.#.....#...........#.#.#.#...#.#...#.#
    | ##.#.###############.###.#.#####.#####.#
    |#...#.#.............#.#...#.....#.......#
    |#.#####.###########.#.#.#######.###.#### 
    |#.#.....#.........#.#...#.....#...#.#...#
    |#.#.#####.#.###.###.#####.#.#####.#.###.#
    |#.#.#O....#.#...#...#...#.#.......#.....#
    |#.#.#######.#####.#.###.#.#############.#
    |#.#.........#.....#.....#...#...#.....#.#
    |#.#########.#.#####.#######.#.#.#.#.###.#
    |#.....#.....#.#.....#.....#.#.#...#.....#
    |#.#####.#####.#######.###.#.#.########## 
    |#...........#...........#...#...........#
    | ########### ########### ### ########### """.trimMargin()

    room.setExploredRoom(exploredRoom)
    val time = room.spreadOxygen()
    println(time)

    // 166 to low
}


class Room(
    val size: Int,
    val droid: Droid,
    var grid: MutableList<MutableList<Char>> = MutableList(size) { MutableList(size) { ' ' } },
    var positionDroid: Coor = Coor(size / 2, size / 2),
    var knownSpots: MutableSet<Coor> = mutableSetOf()
) {

    init {
//        grid.set(positionDroid, 'D')
    }

    fun explore() {
        var count = 0
        val maxCount = 2000000
        while (isNotFullyExplored() && count < maxCount) {
            for (potentialDir in 1..4) {
                var dir: Int
                val newPotCoor = positionDroid.move(potentialDir)
                if (newPotCoor !in knownSpots) {
                    dir = potentialDir
                } else if (potentialDir == 4) {
                    dir = Random.nextInt(1, 5)
                } else {
                    continue
                }
                val output = droid.move(dir)
                when (output) {
                    0 -> {
                        grid.set(positionDroid.move(dir), '#')
                        knownSpots.add(positionDroid.move(dir))
                    }
                    1, 2 -> {
                        positionDroid = positionDroid.move(dir)
                        grid.set(positionDroid, if (output == 1) '.' else 'O')
                        knownSpots.add(positionDroid)
//                        grid.set(positionDroid, 'D')
                    }
                }
            }
            count++
            if (count % 100000 == 0) print()
        }
        grid.set(Coor(size / 2, size / 2), 'S')
        print()
    }

    private fun isNotFullyExplored(): Boolean {
        // for every hor + vert line, if not all empty, or starting and finishing with wall
        val notWalled: (String) -> Boolean = { l ->
            l.startsWith('D') || l.startsWith('.') || l.endsWith('D') || l.endsWith('.')
        }
        val toLine: (List<Char>) -> String = { l -> l.joinToString("").trim() }
        val toSections: (String) -> List<String> = { l -> l.split("""\s+""".toRegex()) }
        return grid.map(toLine).flatMap(toSections).any(notWalled)
                || grid.transpose().map(toLine).flatMap(toSections).any(notWalled)

    }

    fun print(): Unit = grid.forEach { println(it.joinToString(separator = "")) }

    fun setExploredRoom(exploredRoom: String) {
        val r = exploredRoom.split("\n").map { l -> l.toMutableList() }
        for (y in 0 until r.size) {
            for (x in 0 until r[1].size) {
                grid.set(Coor(x, y), r[y][x])
            }
        }
        print()
    }

    fun spreadOxygen(): Int {
        var time = 0
        while (grid.containsUnfilledSpots('.')) {
            val newGrid = grid.copy()
            for (y in 0 until grid.size) {
                for (x in 0 until grid[0].size) {
                    if (grid[y][x] == 'O') {
                        val adjCoor = listOf(
                            Coor(x + 1, y),
                            Coor(x - 1, y),
                            Coor(x, y + 1),
                            Coor(x, y - 1)
                        )
                        adjCoor.filter { grid.get(it) != '#' }.forEach { newGrid.set(it, 'O') }
                    }
                }
            }
            grid = newGrid
            time++
            println("time: $time")
            print()
        }
        return time
    }
}

private fun List<List<Char>>.containsUnfilledSpots(c: Char): Boolean {
    for (y in 0 until this.size) {
        for (x in 0 until this[0].size) {
            if (this[y][x] == c) {
                return true
            }
        }
    }
    return false
}

private fun List<List<Char>>.findCoor(c: Char): Coor {
    for (y in 0 until this.size) {
        for (x in 0 until this[0].size) {
            if (this[y][x] == c) {
                return Coor(x, y)
            }
        }
    }
    throw IllegalStateException("$c not found, but was expected")
}


private fun List<List<Char>>.transpose(): List<List<Char>> {
    val size = this.size
    val t = MutableList(size) { MutableList(size) { ' ' } }

    for (y in 0 until this.size) {
        for (x in 0 until this[0].size) {
            t[x][y] = this[y][x]
        }
    }
    return t
}


private fun MutableList<MutableList<Char>>.set(coor: Coor, c: Char): Unit {
    this[coor.y][coor.x] = c
}


private fun List<List<Char>>.copy(): MutableList<MutableList<Char>> {
    val size = this.size
    val copy = MutableList(size) { MutableList(size) { ' ' } }
    for (y in 0 until this.size) {
        for (x in 0 until this[0].size) {
            copy[y][x] = this[y][x]
        }
    }
    return copy
}


private fun List<List<Char>>.get(coor: Coor): Char = this[coor.y][coor.x]

data class Coor(val x: Int, val y: Int) {
    fun move(dir: Int): Coor {
        return when (dir) {
            1 -> Coor(x, y - 1) //N
            2 -> Coor(x, y + 1) //S
            3 -> Coor(x - 1, y) //W
            4 -> Coor(x + 1, y) //E
            else -> throw IllegalArgumentException("Unexpected direction")
        }
    }
}

class Droid(val program: IntCode) {
    fun move(input: Int): Int = program.move(input)
}

class IntCode(
    private val program: MutableList<Long>,
    var pos: Int = 0,
    var relativeBase: Int = 0
) {
    fun move(input: Int): Int {
//        println("relative base: $relativeBase, new pos: $pos, program: ${program}")
        var output: Int? = null
        while (output == null) {
            val operation = Operation.createOp(program[pos])
            if (operation.opCode == 99) throw IllegalStateException("Program finished unexpectedly")
            output = performOperation(operation, input)?.toInt()
//            println("relative base: $relativeBase, output: $output, new pos: $pos, program: ${program}")
        }
//        println("input: $input, output: $output")
        return output
    }

    private fun performOperation(
        op: Operation,
        input: Int
    ): Long? {
        val output = when (op.opCode) {
            1 -> this.processAddition(op)
            2 -> this.processMultiplication(op)
            3 -> this.saveInput(op, input.toLong())
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