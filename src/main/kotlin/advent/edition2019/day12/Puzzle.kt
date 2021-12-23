package test.advent.day12

import java.io.File
import kotlin.math.absoluteValue

val day = 12;
val file = File("src/main/resources/edition2019/day${day}/input")

fun main() {
    val input = file.readLines().map { Coor3D.fromString(it) }
//    Io, Europa, Ganymede, and Callisto
    val moons = mutableMapOf(
        "Io" to Pair(input[0], Velocity(0, 0, 0)),
        "Europa" to Pair(input[1], Velocity(0, 0, 0)),
        "Ganymede" to Pair(input[2], Velocity(0, 0, 0)),
        "Callisto" to Pair(input[3], Velocity(0, 0, 0))
    )
    println(moons)
    val x = findRepeatingNumberOfSteps(moons.values.map { it.first.x })
    val y = findRepeatingNumberOfSteps(moons.values.map { it.first.y })
    val z = findRepeatingNumberOfSteps(moons.values.map { it.first.z })
    println("x: $x, y: $y, z: $z")
    println(lcm(longArrayOf(x, y, z)))

//    println("after step 0:")
//    moons.forEach({println(it)})

    // first part

//    val maxSteps = 1000
//    for (t in 1..maxSteps) {
//        val deltaV = mutableMapOf<String, Velocity>()
//        for ((moon, posAndV) in moons.entries) {
//            val otherPositions = moons.filter { it.key != moon }.map { it.value.first }
//            deltaV[moon] = otherPositions.map { it.applyGravity(posAndV.first) }
//                .map { it.inverse() }
//                .fold(Velocity(0, 0, 0), { acc, new -> acc + new })
//        }
//        for ((moon, posAndV) in moons.entries) {
//            val newV = posAndV.second + deltaV.getOrElse(moon, { throw IllegalStateException("not found") })
//            val newPos = posAndV.first + newV
//            moons.put(moon, Pair(newPos, newV))
//        }
////        if (t % 100 == 0) {
//            println("after step $t:")
//            moons.forEach { e -> println("${e}, pot: ${e.value.first.getPotEnergy()}, kin: ${e.value.second.getKinEnergy()} ") }
//            println("total energy, ${moons.values.map { it.first.getPotEnergy() * it.second.getKinEnergy() }.sum()}")
////        }
//    }
//    println(moons)
//    println(moons.values.map { it.first.getPotEnergy() * it.second.getKinEnergy() }.sum())

    // 8310


}

fun findRepeatingNumberOfSteps(initialPos: List<Int>, initialV: List<Int> = MutableList(4) { 0 }): Long {
    println("inital pos: $initialPos")
    val maxSteps: Long = 1000000
    var pos = initialPos.toMutableList()
    var v = initialV.toMutableList()
    for (t in 1..maxSteps) {
        val newPos = MutableList(4) { 0 }
        val newV = MutableList(4) { 0 }
        for (i in pos.indices) {
            var dv = 0
            for (j in pos.indices) {
                if (j != i) dv += if (pos[i] < pos[j]) 1 else if (pos[i] > pos[j]) -1 else 0
            }
            newV[i] = v[i] + dv
            newPos[i] = pos[i] + newV[i]
        }
        pos = newPos
        v = newV
//        println("pos: $pos, v: $v")
        if (pos == initialPos && v == initialV) {
            return t
        }
    }
    throw IllegalStateException("no repeat found")
}


private fun lcm(a: Long, b: Long): Long {
    return a * (b / gcd(a, b))
}

private fun lcm(input: LongArray): Long {
    var result = input[0]
    for (i in 1 until input.size) result = lcm(result, input[i])
    return result
}

private fun gcd(a: Long, b: Long): Long {
    var a = a
    var b = b
    while (b > 0) {
        val temp = b
        b = a % b // % is remainder
        a = temp
    }
    return a
}

data class Coor3D(val x: Int, val y: Int, val z: Int) {
    companion object {
        fun fromString(input: String): Coor3D {
            // <x=3, y=5, z=-1>
            val (_, x, y, z) = """<x=(-?\d+), y=(-?\d+), z=(-?\d+)>""".toRegex().find(input)?.groupValues
                ?: throw IllegalStateException("no match!")
            return Coor3D(x.toInt(), y.toInt(), z.toInt())
        }
    }

    fun getPotEnergy(): Int = x.absoluteValue + y.absoluteValue + z.absoluteValue

    fun applyGravity(o: Coor3D): Velocity = Velocity(
        if (x < o.x) 1 else if (x > o.x) -1 else 0,
        if (y < o.y) 1 else if (y > o.y) -1 else 0,
        if (z < o.z) 1 else if (z > o.z) -1 else 0
    )

    operator fun plus(v: Velocity) = Coor3D(x + v.vx, y + v.vy, z + v.vz)
}

data class Velocity(val vx: Int, val vy: Int, val vz: Int) {
    operator fun plus(o: Velocity) = Velocity(vx + o.vx, vy + o.vy, vz + o.vz)
    fun inverse() = Velocity(-vx, -vy, -vz)
    fun getKinEnergy(): Int = vx.absoluteValue + vy.absoluteValue + vz.absoluteValue

}
