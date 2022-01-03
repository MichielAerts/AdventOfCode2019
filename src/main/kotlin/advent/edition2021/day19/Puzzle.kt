package test.advent.edition2021.day19

import test.advent.edition2021.*
import java.io.File

val day = 19;
val file = File("src/main/resources/edition2021/day${day}/input")

class Puzzle(private val rawInput: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val input = rawInput.joinToString("\n").split("\n\n")
        val scanners = input.map { Scanner.createScanner(it.split("\n")) }
        scanners[0].distanceToScanner0 = Distance(0, 0, 0)
        scanners[0].orientation = 0
        while (scanners.any { it.distanceToScanner0 == null }) {
            val locatedScanners = scanners.filter { it.distanceToScanner0 != null }
            val unlocatedScanners = scanners.filter { it.distanceToScanner0 == null }
            for (locatedScanner in locatedScanners) for (unlocatedScanner in unlocatedScanners) {
                println("trying ${unlocatedScanner.id} from ${locatedScanner.id}")
                val sharedBeacons = locatedScanner.findOverlap(unlocatedScanner)
//                sharedBeacons.forEach(::println)
            }
        }
        
        val beacons = scanners.flatMap { scanner -> scanner.pointsInAllOrientations[scanner.orientation!!].map { it.toReferenceZero(scanner.distanceToScanner0!!) } }.toSet()
//        println(beacons.sortedWith(compareBy({ it.x },{ it.y },{ it.z })).forEach(::println))
        println(beacons.size)
    }

    fun runPart2() {
        println("Part 2 of Day $day")
        val input = rawInput.joinToString("\n").split("\n\n")
        val scanners = input.map { Scanner.createScanner(it.split("\n")) }
        scanners[0].distanceToScanner0 = Distance(0, 0, 0)
        scanners[0].orientation = 0
        while (scanners.any { it.distanceToScanner0 == null }) {
            val locatedScanners = scanners.filter { it.distanceToScanner0 != null }
            val unlocatedScanners = scanners.filter { it.distanceToScanner0 == null }
            println("number of scanners calibrated: ${locatedScanners.size}, to do: ${unlocatedScanners.size}")
            for (locatedScanner in locatedScanners) for (unlocatedScanner in unlocatedScanners) {
//                println("trying ${unlocatedScanner.id} from ${locatedScanner.id}")
                val sharedBeacons = locatedScanner.findOverlap(unlocatedScanner)
//                sharedBeacons.forEach(::println)
            }
        }
        val scannersAndDistances = scanners.map { Pair(it.id, it.distanceToScanner0!!) }
        println(scannersAndDistances.flatMap { first -> scannersAndDistances.toList()
            .map { second -> Triple(first, second, first.second.getManhattanDistance(second.second)) } }
            .maxByOrNull { it.third })
        
        // 9741 too low
    }
}

data class Scanner(
    val id: Int, val points: List<Point>,
    val pointsInAllOrientations: List<List<Point>> = Orientations.allOrientations(points),
    val pointsInAllOrientationsAndDistances: List<Map<Point, List<Distance>>> =
        pointsInAllOrientations.map { pointsInOneOrientation ->
            pointsInOneOrientation.associateWith { it.getDistanceToAll(pointsInOneOrientation.toList()) }
        },
    var distanceToScanner0: Distance? = null,
    var orientation: Int? = null
) {
    fun findOverlap(other: Scanner): List<Point> {
        if (distanceToScanner0 == null || orientation == null) throw IllegalStateException("shouldn't!")
        // compare with other scanner (all its orientations), if >=12 distances match, it's a shared beacon 
        //
        val sharedBeacons = mutableListOf<Pair<Point, Point>>()
        val pointsInOneOrientation = pointsInAllOrientationsAndDistances[orientation!!]
        for ((idx, pointsInOneOrientationOtherScanner) in other.pointsInAllOrientationsAndDistances.withIndex()) {
            for ((point, distances) in pointsInOneOrientation) {
                for ((otherPoint, otherDistances) in pointsInOneOrientationOtherScanner) {
                    if (distances.count { it in otherDistances } >= 12) {
                        sharedBeacons.add(Pair(point, otherPoint))
//                        println("found shared beacon for $point and $otherPoint, scanner ${other.id } at ${otherPoint.getDistanceTo(point)} from scanner $id")
                        other.distanceToScanner0 = otherPoint.getDistanceTo(point) + distanceToScanner0!!
                        other.orientation = idx
                    }
                }
            }
            if (sharedBeacons.size >= 12) {
//                println("other scanner position with reference to 0: ${other.distanceToScanner0}")
//                println("shared points with reference to scanner 0 according to scanner $id:")
                val pointsWithReferenceToScanner0 = sharedBeacons.map { it.first }.map { it + distanceToScanner0!! }
//                pointsWithReferenceToScanner0.forEach(::println)
            
                val pointsFromOtherWithReferenceToScanner0 = other.pointsInAllOrientations[other.orientation!!].map { it + other.distanceToScanner0!! }
//                println("all points with reference to scanner 0 according to scanner ${other.id}:")
//                pointsFromOtherWithReferenceToScanner0.forEach(::println)
                return pointsWithReferenceToScanner0
            }
        }
        return listOf<Point>()
    }

    companion object {
        private val regex = "--- scanner (?<id>\\d+) ---".toRegex()

        fun createScanner(input: List<String>): Scanner {
            val id = regex.findGroupAsInt(input[0], "id")
            val points = input.subListTillEnd(1)
                .map { it.split(",") }
                .map { Point(it[0], it[1], it[2]) }
            return Scanner(id, points)
        }
    }
}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
//    puzzle.runPart1()
    puzzle.runPart2()
}

class Orientations {
    companion object {

        fun allOrientations(point: Point): List<Point> {
            val single = mutableSetOf<Point>()
            for (i in 0..3) for (j in 0..3) for (k in 0..3) {
                val rotatedPoint = point.rotate(Triple(i, j, k))
                single.add(rotatedPoint)
            }
            return single.toList()
        }

        fun allOrientations(points: List<Point>): List<List<Point>> =
            // outer list: all orientations // inner list, all points within one orientation
            points.map { allOrientations(it) }.transpose()
    }
}