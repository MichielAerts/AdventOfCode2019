package test.advent.day22

import java.io.File
import kotlin.math.max

val day = 22;
val file = File("src/main/resources/day${day}/input")

// part 2 copied from https://github.com/nibarius/aoc/blob/master/src/main/aoc2019/Day22.kt just to finish the thing, pfff..
fun main() {

    // part 1: 4284
//    val startDeck = (0..highestCard).toList()
//    println("start $startDeck")
//    var shuffledDeck = startDeck.map { it.toInt() }
//    for (op in operations) {
//        shuffledDeck = shuffledDeck.shuffle(op)
//        println("pos of 7: ${shuffledDeck.indexOf(7)}, full deck after $op: $shuffledDeck")
//    }
//    val shuffleCombinations = calculateShuffleCombinations(operations, 16)
    println(finalPositionForCard(2019, 10007L).toInt())
    
    // part 2
    val deckSize = 119315717514047L
    val repeats = 101_741_582_076_661
    val targetPosition = 2020L

    // Deck becomes sorted again after every deckSize - 1 repeats of the shuffle
    // according to Euler's Theorem as mentioned in:
    // https://www.reddit.com/r/adventofcode/comments/ee56wh/2019_day_22_part_2_so_whats_the_purpose_of_this/fbs6s6z/
    //
    // We're interested in what cards ends up at position 2020 after 'repeats' number of repeats of the shuffle
    // process. Calculate the number of times extra that the shuffle process has to be done to get to the
    // original state.
    val shufflesLeftUntilInitialState = deckSize - 1 - repeats

    // If we run the shuffle process 'shufflesLeftUntilInitialState' times and see at what position
    // the card that was at position 2020 ends up at we have the answer to the problem.

    // So first create a reduced shuffle process of two steps with the desired amount of shuffles
    val reduced = reduceShuffleProcess(deckSize, parseInput(deckSize))
    val repeated = repeatShuffleProcess(reduced, shufflesLeftUntilInitialState, deckSize)

    // Then check where the card at 2020 moves to with this shuffle process.
    println(finalPositionForCard(targetPosition, deckSize, repeated))
}

sealed class Operation {
    
    abstract fun nextPosition(card: Long, deckSize: Long): Long
    
    data class Cut(val n: Long) : Operation() {
        override fun nextPosition(card: Long, deckSize: Long): Long = Math.floorMod(card - n, deckSize)
    }

    data class Increment(val n: Long) : Operation() {
        override fun nextPosition(card: Long, deckSize: Long): Long = Math.floorMod(card * n, deckSize)
    }

    companion object {
        val regexNew = "deal into new stack".toRegex()
        val regexInc = "deal with increment (?<n>\\d+)".toRegex()
        val regexCut = "cut (?<n>-?\\d+)".toRegex()

        fun fromLine(str: String, deckSize: Long): List<Operation> {
            return when {
                regexNew.matches(str) -> listOf(Increment(deckSize - 1), Cut(1))
                regexInc.matches(str) -> listOf(Increment(regexInc.findGroupAsLong(str, "n")))
                regexCut.matches(str) -> listOf(Cut(regexCut.findGroupAsLong(str, "n")))
                else -> throw IllegalArgumentException("why?")
            }
        }
    }

    private fun mulMod(a: Long, b: Long, mod: Long): Long {
        return a.toBigInteger().multiply(b.toBigInteger()).mod(mod.toBigInteger()).longValueExact()
    }
    
    // Rules for combining techniques from:
    // https://www.reddit.com/r/adventofcode/comments/ee56wh/2019_day_22_part_2_so_whats_the_purpose_of_this/fc0xvt5/
    fun combine(other: Operation, deckSize: Long): List<Operation> {
        return when {
            this is Cut && other is Cut -> listOf(Cut(Math.floorMod(n + other.n, deckSize)))
            this is Increment && other is Increment -> listOf(Increment(mulMod(n, other.n, deckSize)))
            this is Cut && other is Increment -> listOf(Increment(other.n), Cut(mulMod(n, other.n, deckSize)))
            else -> throw IllegalStateException("Invalid combination: $this and $other")
        }
    }

    // Everything except Increment followed by Cut can be combined
    fun canBeCombinedWith(other: Operation) = !(this is Increment && other is Cut)
}

fun finalPositionForCard(card: Long,
                         deckSize: Long,
                         process: List<Operation> = reduceShuffleProcess(deckSize, parseInput(deckSize))): Long {
    return process.fold(card) { pos, operation -> operation.nextPosition(pos, deckSize) }
}

fun parseInput(deckSize: Long) = file.readLines().map { Operation.fromLine(it, deckSize) }.flatten()

private fun reduceShuffleProcess(deckSize: Long, initialProcess: List<Operation>): List<Operation> {
    var process = initialProcess
    while (process.size > 2) {
        var offset = 0
        while (offset < process.size - 1) {
            if (process[offset].canBeCombinedWith(process[offset + 1])) {
                // Combine current + next technique into one
                val combined = process[offset].combine(process[offset + 1], deckSize)
                process = process.subList(0, offset) + combined + process.subList(offset + 2, process.size)
                // Next time try to combine previous technique (if there is any) with the new one
                offset = max(0, offset - 1)
            } else {
                // Not possible to combine current + next, step ahead to check the next two
                offset++
            }
        }
    }
    return process
}

private fun repeatShuffleProcess(process: List<Operation>, times: Long, deckSize: Long): List<Operation> {
    var current = process
    val res = mutableListOf<Operation>()
    // iterate trough the bits in the binary representation of the number of times to repeat
    // from least significant to most significant
    for (bit in times.toString(2).reversed()) {
        if (bit == '1') {
            // Obviously, a number is the sum of the value of all the ones in the binary representation
            // Store the process for all bits that are set
            res.addAll(current)
        }
        // double the amount of iterations in the shuffle process and reduce it to two operations
        current = reduceShuffleProcess(deckSize, current + current)
    }
    // res now holds all the steps needed to repeat the shuffle process the given number of times,
    // do a final reduction to get a reduced process with only two steps
    return reduceShuffleProcess(deckSize, res)
}

private fun Regex.findGroupAsLong(str: String, group: String): Long =
    find(str)?.groups?.get(group)?.value?.toLong() ?: throw IllegalArgumentException("couldn't")
