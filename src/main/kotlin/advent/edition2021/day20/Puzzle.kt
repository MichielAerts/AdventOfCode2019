package test.advent.edition2021.day20

import test.advent.edition2021.countAllOccurrences
import test.advent.edition2021.getThreeByThreeSquare
import test.advent.edition2021.subListTillEnd
import java.io.File

val day = 20;
val file = File("src/main/resources/edition2021/day${day}/input")

class Puzzle(private val rawInput: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        val algorithm = rawInput[0]
        val inputImage = rawInput.subListTillEnd(2).map { it.toList() }
        val image = Image.createImage(algorithm, inputImage, 50)
        image.print()
        val steps = 2
        for (i in 1 .. steps) {
            image.runEnhancementStep()
            println("after step: $i")
            image.print()
        }
        println(image.countAllOccurrences('#'))
    }
    
    fun runPart2() {
        println("Part 2 of Day $day")
        val algorithm = rawInput[0]
        val inputImage = rawInput.subListTillEnd(2).map { it.toList() }
        val image = Image.createImage(algorithm, inputImage, 500)
        image.print()
        val steps = 50
        for (i in 1 .. steps) {
            image.runEnhancementStep()
            println("after step: $i")
            image.print()
        }
        println(image.countAllOccurrences('#',100))   
    }
}

data class Image(val algorithm: String, var image: List<List<Char>>) {
    fun print() = image.map { it.joinToString("") }.forEach(::println)
    fun runEnhancementStep() {
        val size = image.size
        val newImage: MutableList<MutableList<Char>> = MutableList(size) { MutableList(size) { '.' } }
        for (y in 1 until size - 1) for (x in 1 until size - 1) {
            newImage[y][x] = algorithm[mapToDecimalNumber(image.getThreeByThreeSquare(x, y))]
        }
        image = newImage
    }

    private fun mapToDecimalNumber(input: List<Char>): Int =
        input.joinToString("") { if (it == '#') "1" else "0" }.toInt(2)

    fun countAllOccurrences(c: Char, buffer: Int = 5): Int =
        image.subList(buffer, image.size - buffer)
            .map { it.subList(buffer, image[0].size - buffer) }
            .countAllOccurrences(c)

    companion object {
        fun createImage(algorithm: String, inputImage: List<List<Char>>, sizeEmptyBuffer: Int): Image {
            val size = 2 * sizeEmptyBuffer + inputImage.size
            val image: MutableList<MutableList<Char>> = MutableList(size) { MutableList(size) { '.' } }
            for (y in image.indices) for (x in image.indices) {
                if (y in sizeEmptyBuffer until sizeEmptyBuffer + inputImage.size &&
                        x in sizeEmptyBuffer until sizeEmptyBuffer + inputImage.size) {
                            image[y][x] = inputImage[y - sizeEmptyBuffer][x - sizeEmptyBuffer]
                        }
                }
        return Image(algorithm, image)
        }
    }
}

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

