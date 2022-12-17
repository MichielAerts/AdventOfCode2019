package test.advent.edition2022.day9

import test.advent.edition2022.Direction
import test.advent.edition2022.Pos
import test.advent.edition2022.move
import java.io.File

val day = 9;
val file = File("src/main/resources/edition2022/day${day}/input")

class Puzzle(private val input: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val moves = input.map { Move.create(it) }
        val rope = ShortRope()
        moves.forEach { rope.moveHeadAndTail(it) }
        println(rope.visitedTailPositions.size)
    }
    
    fun runPart2() {
        println("Part 2 of Day $day")
        val moves = input.map { Move.create(it) }
        val rope = Rope.create(10)
        moves.forEach { rope.move(it) }
        println(rope.visitedTailPositions.size)
    }
}

data class Rope(val rope: MutableMap<Int, Pos>, val visitedTailPositions: MutableSet<Pos> = mutableSetOf()) {
    fun move(move: Move) {
        for (i in 1..move.amount) {
            for ((knot, currentPos) in rope) {
                if (knot == 1) {
                    rope[knot] = currentPos.move(move.d)
                } else {
                    rope[knot] = currentPos.moveToTouch(rope[knot - 1]!!)
                }
            }
//            println(rope)
            visitedTailPositions.add(rope[10]!!)
        }
    }

    companion object {
        fun create(length: Int): Rope = Rope((1..10).associateWith { Pos(0, 0) }.toMutableMap())
    }
}

data class ShortRope(var head: Pos = Pos(0,0), var tail: Pos = Pos(0, 0),
                     val visitedTailPositions: MutableSet<Pos> = mutableSetOf()) {
    fun moveHeadAndTail(move: Move) {
        for (i in 1..move.amount) {
            head = head.move(move.d)
            tail = tail.moveToTouch(head)
            visitedTailPositions.add(tail)
//            println("head: $head, tail: $tail")
        }
    }
}

data class Move(val d: Direction, val amount: Int) {
    companion object {
        fun create(input: String): Move {
            val (d, amount) = input.split(" ")
            return (Move(Direction.getDirectionFromFirstLetter(d), amount.toInt()))
        }
    }
}

fun Pos.moveToTouch(head: Pos) = when {
    this.isTouching(head) -> this
    head.x == this.x && head.y > this.y -> this.move(Direction.UP)
    head.x == this.x && head.y < this.y -> this.move(Direction.DOWN)
    head.y == this.y && head.x > this.x -> this.move(Direction.RIGHT)
    head.y == this.y && head.x < this.x -> this.move(Direction.LEFT)
    head.y > this.y && head.x > this.x -> this.move(Direction.UP).move(Direction.RIGHT)
    head.y > this.y && head.x < this.x -> this.move(Direction.UP).move(Direction.LEFT)
    head.y < this.y && head.x > this.x -> this.move(Direction.DOWN).move(Direction.RIGHT)
    head.y < this.y && head.x < this.x -> this.move(Direction.DOWN).move(Direction.LEFT)
    else -> throw IllegalStateException("shouldn't happen")
}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

