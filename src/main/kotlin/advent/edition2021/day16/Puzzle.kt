package test.advent.edition2021.day16

import test.advent.edition2021.getOrThrow
import java.io.File

val day = 16;
val file = File("src/main/resources/edition2021/day${day}/input")

class Puzzle(private val rawInput: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val binary = rawInput[0].toList().joinToString("") { hexMap.getOrThrow(it) }
        val outerPacket = Packet.findPacket(binary)
        println(outerPacket)
        println(versionCount)
    }

    fun runPart2() {
        println("Part 2 of Day $day")
        val binary = rawInput[0].toList().joinToString("") { hexMap.getOrThrow(it) }
        val outerPacket = Packet.findPacket(binary)
        println(outerPacket.solve())
    }
}

var versionCount: Int = 0

val hexMap = mapOf(
    '0' to "0000",
    '1' to "0001",
    '2' to "0010",
    '3' to "0011",
    '4' to "0100",
    '5' to "0101",
    '6' to "0110",
    '7' to "0111",
    '8' to "1000",
    '9' to "1001",
    'A' to "1010",
    'B' to "1011",
    'C' to "1100",
    'D' to "1101",
    'E' to "1110",
    'F' to "1111",
)

data class Packet(
    val version: Int, val id: Int, val bitLength: Int,
    val literalValue: Long? = null, val subPackets: List<Packet>? = null
) {
    fun solve(): Long {
        if (id == 4) return literalValue!!
        val subs = subPackets!!
        return when(id) {
            0 -> subs.sumOf { it.solve() }
            1 -> subs.map { it.solve() }.reduceRight(Long::times)
            2 -> subs.minOf { it.solve() }
            3 -> subs.maxOf { it.solve() }
            5 -> if (subs[0].solve() > subs[1].solve()) 1 else 0
            6 -> if (subs[0].solve() < subs[1].solve()) 1 else 0
            7 -> if (subs[0].solve() == subs[1].solve()) 1 else 0
            else -> throw IllegalStateException("Unsupported id!")
        }
    }

    companion object {
        fun findPacket(binaryInput: String): Packet {
            val (version, id) = binaryInput.substring(0, 7).chunked(3).map { it.toInt(2) }
            versionCount += version
            when (id) {
                4 -> {
                    val firstGroups = binaryInput.substring(6)
                        .chunked(5).filter { it.length == 5 }.takeWhile { it[0] == '1' }
                    val lastGroup = binaryInput.substring(6 + firstGroups.size * 5, 11 + firstGroups.size * 5)

                    val literalValue = (firstGroups + lastGroup).joinToString("") { it.substring(1) }.toLong(2)
                    return Packet(version, id, bitLength = 6 + (firstGroups.size + 1) * 5, literalValue)
                }
                else -> {
                    val lengthTypeId = binaryInput[6]
                    when (lengthTypeId) {
                        '0' -> {
                            val totalLengthInBits = binaryInput.substring(7, 22).toInt(2)
                            var bitIndex = 0
                            val subPackets = mutableListOf<Packet>()
                            while (subPackets.sumOf { it.bitLength } < totalLengthInBits) {
                                val packet = findPacket(binaryInput.substring(22 + bitIndex))
                                bitIndex += packet.bitLength
                                subPackets.add(packet)
                            }
                            return Packet(version, id, 22 + bitIndex, subPackets = subPackets)
                        }
                        '1' -> {
                            val numberOfSubPackets = binaryInput.substring(7, 18).toInt(2)
                            var bitIndex = 0
                            var currentNumber = 1
                            val subPackets = mutableListOf<Packet>()
                            while (currentNumber <= numberOfSubPackets) {
                                val packet = findPacket(binaryInput.substring(18 + bitIndex))
                                bitIndex += packet.bitLength
                                currentNumber++
                                subPackets.add(packet)
                            }
                            return Packet(version, id, 18 + bitIndex, subPackets = subPackets)
                        }
                    }
                }
            }
            throw IllegalStateException("shouldn't reach this")
        }
    }
}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

