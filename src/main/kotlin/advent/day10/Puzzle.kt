package test.advent.day10

import java.io.File
import java.math.BigDecimal
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sqrt

val day = 10;
val file = File("src/main/resources/day${day}/input")

data class Coor(val x: Int, val y: Int) {
    fun getAngleTo(c_cand: Coor): BigDecimal {
        val dy = (c_cand.y - y).toDouble()
        val dx = (c_cand.x - x).toDouble()
        val th = atan2(dy, dx) + PI / 2
        val thetaInRad = if (th < 0) th + 2 * PI else th
        return (thetaInRad * 180 / PI).toBigDecimal()
    }

    fun getDistance(c_cand: Coor): BigDecimal {
        val dy = (c_cand.y - y).toDouble()
        val dx = (c_cand.x - x).toDouble()
        return sqrt(dx * dx + dy * dy).toBigDecimal()
    }
}

fun main() {
    val map = file.readLines().map { it.toList() }
    val (c_station, asteroids) = getStation(map)
    println("answer $c_station ${asteroids.size}, planets in view: $asteroids")
    val station = Coor(19, 11)
    println(map.get(station))
    var remainingAsteroids = map.toList()
    while(remainingAsteroids.flatten().contains('#')) {
        val asteroidsInview = remainingAsteroids.asteroidsInViewOf(station)
        remainingAsteroids = remainingAsteroids.vaporize(asteroidsInview)
    }
    println(remainingAsteroids)

}
var countVaporization = 1

fun List<List<Char>>.vaporize(asteroidsInview: Map<BigDecimal, Coor>): List<List<Char>> {
    val newMap = ArrayList<ArrayList<Char>>()
    for (i in this.indices) newMap.add(arrayListOf(*(this[i].toTypedArray())))
    for (asteroid in asteroidsInview.values) {
        newMap[asteroid.y][asteroid.x] = '.'
        println("vaporizing ${asteroid} as number ${countVaporization++}")
    }
    return newMap.toList()
}

private fun getStation(map: List<List<Char>>): Map.Entry<Coor, Map<BigDecimal, Coor>> {
    val asteroidsInView = hashMapOf<Coor, Map<BigDecimal, Coor>>()
    for (y in map.indices) {
        for (x in map[0].indices) {
            val c = map[y][x]
            if (c == '#') {
                asteroidsInView.put(Coor(x, y), map.asteroidsInViewOf(Coor(x, y)))
            }
        }
    }
    return asteroidsInView.maxByOrNull { it.value.size } ?: throw IllegalStateException("no max")
}

private fun List<List<Char>>.get(c: Coor) = this[c.y][c.x]

private fun List<List<Char>>.asteroidsInViewOf(c_station: Coor): Map<BigDecimal, Coor> {
    val asteroidsInView = mutableMapOf<BigDecimal, Coor>()
    for (y in this.indices) {
        for (x in this[0].indices) {
            val c_candidate = Coor(x, y)
            if (get(c_candidate) == '#' && c_candidate != c_station) {
                asteroidsInView.merge(c_station.getAngleTo(c_candidate), c_candidate)
                { c_old, c_new
                    -> if (c_station.getDistance(c_new) < c_station.getDistance(c_old)) c_new else c_old
                }
            }
        }
    }
    return asteroidsInView.toList().sortedBy { it.first }.toMap()
}