@file:OptIn(ExperimentalStdlibApi::class)

package test.advent.edition2022

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

operator fun Regex.contains(text: CharSequence): Boolean = this.matches(text)

//

// List & Map Utils
fun <E> List<E>.subListTillEnd(fromIndex: Int): List<E> = this.subList(fromIndex, this.size)

fun List<Int>.product(): Int = this.reduce { acc, i -> acc * i }

fun List<Long>.product(): Long = this.reduce { acc, i -> acc * i }

inline fun <T> Iterable<T>.takeWhileInclusive(
    predicate: (T) -> Boolean
): List<T> {
    var shouldContinue = true
    return takeWhile {
        val result = shouldContinue
        shouldContinue = predicate(it)
        result
    }
}

fun <E> List<E>?.subList(fromIndex: Int): List<E>? = this?.subList(fromIndex, this.size)

fun <T> List<T>.toPair(): Pair<T, T> {
    if (this.size != 2) {
        throw IllegalArgumentException("List is not of length 2!")
    }
    return Pair(this[0], this[1])
}

fun <E> List<E>.splitBy(splitter: (E) -> Boolean): List<List<E>> {
    val list = mutableListOf<MutableList<E>>()
    var currentList = mutableListOf<E>()
    for (item in this) {
        if (splitter(item)) {
            list += currentList;
            currentList = mutableListOf();
        } else {
            currentList += item;
        }
    }
    if (currentList.isNotEmpty()) list += currentList
    return list;
}

fun IntRange.containsRange(o: IntRange): Boolean {
    if (this.start > this.endInclusive || o.start > o.endInclusive) throw IllegalStateException("only incrementing IntRange supported")
    return o.start >= this.start && o.endInclusive <= this.endInclusive
}

fun IntRange.hasOverlap(o: IntRange): Boolean {
    if (this.start > this.endInclusive || o.start > o.endInclusive) throw IllegalStateException("only incrementing IntRange supported")
    return this.intersect(o).isNotEmpty()
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

fun List<Char>.hasAllDifferentCharacters(): Boolean = this.size == this.toSet().size

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

fun <T> T.log(): T {
    println(this); return this
}
//

// Points & Neighbours

fun List<String>.to2DGridOfPoints(): List<List<Point>> = this.mapIndexed { y, r ->
    r.toList().mapIndexed { x, v -> Point(x, y, v.digitToInt()) }
}

fun List<String>.to2DGridOfPointsWithLetters(): List<List<Point>> = this.mapIndexed { y, r ->
    r.toList().mapIndexed { x, v -> Point(x, y, value = v) }
}

fun initEmptyGrid(startX: Int = 0, endX: Int, startY: Int = 0, endY: Int): List<List<Point>> =
    (startY..endY).map { y -> (startX..endX).map { Point(it, y) } }

fun List<List<Point>>.findSingleValueInGrid(v: Char): Point = this.flatten().find { it.value == v }!!

fun List<List<Point>>.findAllValuesInGrid(v: Char): List<Point> = this.flatten().filter { it.value == v }

fun List<List<Point>>.changePoint(pointToBeChanged: Point, c: Char) {
    this.getPoint(pointToBeChanged.x, pointToBeChanged.y)?.value = c
} 

fun List<List<Point>>.changePoints(points: Set<Point>, c: Char) {
    for (pointToBeChanged in points) {
        this.changePoint(pointToBeChanged, c)
    }
}

enum class Direction {
    UP, DOWN, RIGHT, LEFT;

    companion object {
        fun getDirectionFromFirstLetter(input: String): Direction {
            return when (input) {
                "U" -> UP
                "D" -> DOWN
                "R" -> RIGHT
                "L" -> LEFT
                else -> throw IllegalArgumentException("No")
            }
        }
    }
}

open class Point(val x: Int, val y: Int, var z: Int = 0, var value: Char = '.') {
    constructor(x: String, y: String) : this(x.toInt(), y.toInt())
    constructor(x: String, y: String, z: String) : this(x.toInt(), y.toInt(), z.toInt())

    private fun rotateX(): Point = Point(x, -z, y)
    private fun rotateY(): Point = Point(-z, y, x)
    private fun rotateZ(): Point = Point(-y, x, z)

    fun rotate(xyz: Triple<Int, Int, Int>): Point {
        var newP = this
        repeat(xyz.first) { newP = newP.rotateX() }
        repeat(xyz.second) { newP = newP.rotateY() }
        repeat(xyz.third) { newP = newP.rotateZ() }
        return newP
    }

    fun getDistanceTo(other: Point): Distance = Distance(other.x - x, other.y - y, other.z - z)

    fun getDistanceToAll(others: List<Point>): List<Distance> = others.map { Distance(it.x - x, it.y - y, it.z - z) }

    operator fun minus(d: Distance) = Point(x - d.dx, y - d.dy, z - d.dz)

    override fun toString(): String = "Point(x: $x, y: $y, z: $z, c: $value)"

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

    operator fun plus(d: Distance): Point = Point(x + d.dx, y + d.dy, z + d.dz)
    operator fun component1(): Int = x
    operator fun component2(): Int = y
    operator fun component3(): Int = z
    fun getViews(grid: List<List<Point>>): List<List<Point>> = listOf(
        grid.getColumn(x).subList(0, y).reversed(), //up    
        grid.getColumn(x).subListTillEnd(y + 1), //down
        grid.getRow(y).subList(0, x).reversed(), //left    
        grid.getRow(y).subListTillEnd(x + 1) //right
    )

    fun getPointsInLineTo(end: Point): List<Point> = when {
        this.x == end.x && this.y < end.y -> (this.y..end.y).map { Point(this.x, it) }
        this.x == end.x && this.y > end.y -> (end.y..this.y).map { Point(this.x, it) }
        this.y == end.y && this.x < end.x -> (this.x..end.x).map { Point(it, this.y) }
        this.y == end.y && this.x > end.x -> (end.x..this.x).map { Point(it, this.y) }
        else -> throw IllegalArgumentException("shouldn't")
    }
}

data class Distance(val dx: Int, val dy: Int, val dz: Int) {
    operator fun minus(other: Distance): Distance = Distance(dx - other.dx, dy - other.dy, dz - other.dz)
    operator fun plus(other: Distance): Distance = Distance(dx + other.dx, dy + other.dy, dz + other.dz)
    fun getManhattanDistance(other: Distance): Int =
        (dx - other.dx).absoluteValue + (dy - other.dy).absoluteValue + (dz - other.dz).absoluteValue
}

data class Pos(val x: Int, val y: Int) {
    fun isTouching(other: Pos): Boolean = other in setOf(
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
}

fun Pos.move(d: Direction): Pos = when (d) {
    Direction.UP -> Pos(this.x, this.y + 1)
    Direction.DOWN -> Pos(this.x, this.y - 1)
    Direction.RIGHT -> Pos(this.x + 1, this.y)
    Direction.LEFT -> Pos(this.x - 1, this.y)
}

fun List<List<Point>>.getDirectNeighbours(p: Point): PointAndNeighbours {
    val potentialNeighbours = listOf(Pos(p.x - 1, p.y), Pos(p.x + 1, p.y), Pos(p.x, p.y - 1), Pos(p.x, p.y + 1))
    return PointAndNeighbours(p, potentialNeighbours.mapNotNull { this.getPoint(it.x, it.y) })
}
//
//fun List<List<Point>>.getAllNeighbours(p: Point): PointAndNeighbours {
//    val potentialNeighbours = listOf(
//        Pos(p.x - 1, p.y - 1),
//        Pos(p.x, p.y - 1),
//        Pos(p.x + 1, p.y - 1),
//        Pos(p.x - 1, p.y),
//        Pos(p.x + 1, p.y),
//        Pos(p.x - 1, p.y + 1),
//        Pos(p.x, p.y + 1),
//        Pos(p.x + 1, p.y + 1)
//    )
//    return PointAndNeighbours(p, potentialNeighbours.mapNotNull { this.getPoint(it.x, it.y) })
//}

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

fun List<List<Point>>.getRow(y: Int): List<Point> = this[y]

fun List<List<Point>>.getColumn(x: Int): List<Point> = this.map { it[x] }

fun List<List<Point>>.printZ() = this.forEach { println(it.map { it.z }.joinToString("")) }

fun List<List<Point>>.printV() = this.forEach { println(it.map { it.value }.joinToString("")) }

data class PointAndNeighbours(val point: Point, val neighbours: List<Point>)