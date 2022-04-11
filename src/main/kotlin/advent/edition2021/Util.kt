package test.advent.edition2021

import test.advent.edition2021.day25.Direction
import test.advent.edition2021.day25.Direction.EAST
import test.advent.edition2021.day25.Direction.SOUTH
import kotlin.math.absoluteValue

// Regex
fun Regex.findGroupAsLong(str: String, group: String): Long =
    find(str)?.groups?.get(group)?.value?.toLong() ?: throw IllegalArgumentException("couldn't")

fun Regex.findGroupAsInt(str: String, group: String): Int =
    find(str)?.groups?.get(group)?.value?.toInt() ?: throw IllegalArgumentException("couldn't")

fun Regex.findGroupAsString(str: String, group: String): String =
    find(str)?.groups?.get(group)?.value ?: throw IllegalArgumentException("couldn't")

inline fun <reified T : Enum<T>> Regex.findGroupAsEnum(str: String, group: String): T =
    enumValueOf(find(str)?.groups?.get(group)?.value?.toUpperCase() ?: throw IllegalArgumentException("couldn't"))
//

// List & Map Utils
fun <E> List<E>.subListTillEnd(fromIndex: Int): List<E> = this.subList(fromIndex, this.size)

fun <E> List<E>?.subList(fromIndex: Int): List<E>? = this?.subList(fromIndex, this.size)

fun <T> List<T>.toPair(): Pair<T, T> {
    if (this.size != 2) {
        throw IllegalArgumentException("List is not of length 2!")
    }
    return Pair(this[0], this[1])
}

fun <E> List<List<E>>.transpose(): List<List<E>> {
    val t = MutableList(this[0].size) { MutableList(this.size) { this[0][0] } }

    for (y in 0 until this.size) {
        for (x in 0 until this[0].size) {
            t[x][y] = this[y][x]
        }
    }
    return t
}

fun List<List<Char>>.countAllOccurrences(c: Char): Int = joinToString("") { it.joinToString("") }.count { it == c }

fun List<Char>.countOccurrences(c: Char): Int = joinToString("").count { it == c }

fun <E> List<E>.findOrThrow(function: (E) -> Boolean): E =
    this.find(function) ?: throw IllegalStateException("not found")

fun <K, V> Map<K, V>.getOrThrow(k: K): V = this[k] ?: throw IllegalStateException("not found")

fun <K, V> Map<K, V>.getAllInListOrThrow(vararg keys: K): List<V> {
    val newList = mutableListOf<V>()
    for (key in keys) {
        newList.add(this.getOrThrow(key))
    }
    return newList
}

fun <T> T.log(): T { println(this); return this }
//

// Points & Neighbours
open class Point(val x: Int, val y: Int, var z: Int = 0, var charValue: Char = '.') {
    constructor(x: String, y: String) : this(x.toInt(), y.toInt())
    constructor(x: String, y: String, z: String) : this(x.toInt(), y.toInt(), z.toInt())

    private fun rotateX(): Point = Point(x, -z, y)
    private fun rotateY(): Point = Point(-z, y, x)
    private fun rotateZ(): Point = Point(-y, x, z)

    
    private fun isEastHerd() = charValue == '>'
    private fun isSouthHerd() = charValue == 'v'
    private fun isEmpty() = charValue == '.'
    
    fun rotate(xyz: Triple<Int, Int, Int>): Point {
        var newP = this
        repeat(xyz.first) { newP = newP.rotateX() }
        repeat(xyz.second) { newP = newP.rotateY() }
        repeat(xyz.third) { newP = newP.rotateZ() }
        return newP
    }

    fun getRiskLevel(): Int = z + 1

    fun getDistanceTo(other: Point): Distance = Distance(other.x - x, other.y - y, other.z - z)

    fun getDistanceToAll(others: List<Point>): List<Distance> = others.map { Distance(it.x - x, it.y - y, it.z - z) }

    operator fun minus(d: Distance) = Point(x - d.dx, y - d.dy, z - d.dz)

    override fun toString(): String = "Point(x: $x, y: $y, c: $charValue)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Point

        if (x != other.x) return false
        if (y != other.y) return false
        if (z != other.z) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        result = 31 * result + z
        return result
    }

    fun toReferenceZero(distanceToScanner0: Distance) = this + distanceToScanner0
    operator fun plus(d: Distance): Point = Point(x + d.dx, y + d.dy, z + d.dz)
    operator fun component1(): Int = x
    operator fun component2(): Int = y
    operator fun component3(): Int = z
    private fun willMoveEast(floor: List<List<Point>>): Boolean = floor.getEastPointWithWrapAround(this).isEmpty()
    private fun willMoveSouth(floor: List<List<Point>>): Boolean = floor.getSouthPointWithWrapAround(this).isEmpty()
    private fun willMoveEastTo(floor: List<List<Point>>): Point = floor.getEastPointWithWrapAround(this)
    private fun willMoveSouthTo(floor: List<List<Point>>): Point = floor.getSouthPointWithWrapAround(this)
    fun isHerd(direction: Direction): Boolean {
        return when(direction) {
            EAST -> isEastHerd()
            SOUTH -> isSouthHerd()
        }
    }

    fun willMove(direction: Direction, floor: List<List<Point>>): Boolean {
        return when(direction) {
            EAST -> willMoveEast(floor)
            SOUTH -> willMoveSouth(floor)
        }
    }

    fun willMoveTo(direction: Direction, floor: List<List<Point>>): Point {
        return when(direction) {
            EAST -> willMoveEastTo(floor)
            SOUTH -> willMoveSouthTo(floor)
        }
    }

}

data class Distance(val dx: Int, val dy: Int, val dz: Int) {
    operator fun minus(other: Distance): Distance = Distance(dx - other.dx, dy - other.dy, dz - other.dz)
    operator fun plus(other: Distance): Distance = Distance(dx + other.dx, dy + other.dy, dz + other.dz)
    fun getManhattanDistance(other: Distance): Int =
        (dx - other.dx).absoluteValue + (dy - other.dy).absoluteValue + (dz - other.dz).absoluteValue
}

data class Pos(val x: Int, val y: Int)

fun List<List<Point>>.getDirectNeighbours(p: Point): PointAndNeighbours {
    val potentialNeighbours = listOf(Pos(p.x - 1, p.y), Pos(p.x + 1, p.y), Pos(p.x, p.y - 1), Pos(p.x, p.y + 1))
    return PointAndNeighbours(p, potentialNeighbours.mapNotNull { this.getPoint(it.x, it.y) })
}

fun List<List<Point>>.getEastPointWithWrapAround(p: Point): Point {
    val xSize = this[0].size
    val eastX = if (p.x + 1 < xSize) p.x + 1 else 0
    return this[p.y][eastX]
}


fun List<List<Point>>.getSouthPointWithWrapAround(p: Point): Point {
    val ySize = this.size
    val southY = if (p.y + 1 < ySize) p.y + 1 else 0
    return this[southY][p.x]
}

fun List<List<Point>>.getAllNeighbours(p: Point): PointAndNeighbours {
    val potentialNeighbours = listOf(
        Pos(p.x - 1, p.y - 1),
        Pos(p.x, p.y - 1),
        Pos(p.x + 1, p.y - 1),
        Pos(p.x - 1, p.y),
        Pos(p.x + 1, p.y),
        Pos(p.x - 1, p.y + 1),
        Pos(p.x, p.y + 1),
        Pos(p.x + 1, p.y + 1)
    )
    return PointAndNeighbours(p, potentialNeighbours.mapNotNull { this.getPoint(it.x, it.y) })
}

fun List<List<Char>>.getThreeByThreeSquare(x: Int, y: Int): List<Char> {
    val points = listOf(
        Pos(x - 1, y - 1),
        Pos(x, y - 1),
        Pos(x + 1, y - 1),
        Pos(x - 1, y),
        Pos(x, y),
        Pos(x + 1, y),
        Pos(x - 1, y + 1),
        Pos(x, y + 1),
        Pos(x + 1, y + 1)
    )
    return points.map { this[it.y][it.x] }
}

fun List<List<Point>>.getPoint(x: Int, y: Int): Point? {
    if (x < 0 || x > (this[0].size - 1) || y < 0 || y > (this.size - 1)) return null
    return this[y][x]
}

data class PointAndNeighbours(val point: Point, val neighbours: List<Point>)
//