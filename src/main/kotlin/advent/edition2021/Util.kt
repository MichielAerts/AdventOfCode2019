package test.advent.edition2021

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

fun <E> List<List<E>>.transpose(): List<List<E>> {
    val t = MutableList(this[0].size) { MutableList(this.size) { this[0][0] } }

    for (y in 0 until this.size) {
        for (x in 0 until this[0].size) {
            t[x][y] = this[y][x]
        }
    }
    return t
}

fun <E> List<E>.findOrThrow(function: (E) -> Boolean): E = this.find(function) ?: throw IllegalStateException("not found")

fun <K, V> Map<K, V>.getOrThrow(k: K): V = this[k] ?: throw IllegalStateException("not found")

fun <K, V> Map<K, V>.getAllInListOrThrow(vararg keys: K): List<V> {
    val newList = mutableListOf<V>()
    for (key in keys) {
        newList.add(this.getOrThrow(key))
    }
    return newList
}
//

// Points & Neighbours
open class Point(val x: Int, val y: Int, var z: Int = 0) {
    constructor(x: String, y: String) : this(x.toInt(), y.toInt())
    
    fun getRiskLevel() : Int = z + 1

    override fun toString(): String = "Point(x: $x, y: $y, z: $z)"
}

data class Pos(val x: Int, val y: Int)

fun List<List<Point>>.getDirectNeighbours(p: Point) : PointAndNeighbours {
    val potentialNeighbours = listOf(Pos(p.x - 1, p.y), Pos(p.x + 1, p.y), Pos(p.x, p.y - 1), Pos(p.x, p.y + 1))
    return PointAndNeighbours(p, potentialNeighbours.map { this.getPoint(it.x, it.y) }.filterNotNull())
}

fun List<List<Point>>.getAllNeighbours(p: Point) : PointAndNeighbours {
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

fun List<List<Point>>.getPoint(x: Int, y: Int) : Point? {
    if (x < 0 || x > (this[0].size - 1) || y < 0 || y > (this.size - 1)) return null
    return this[y][x]
}

data class PointAndNeighbours(val point: Point, val neighbours: List<Point>)
//