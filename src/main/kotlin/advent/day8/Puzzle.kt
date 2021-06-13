package test.advent.day8

import java.io.File

val day = 8
val file = File("src/main/resources/day${day}/input")

fun main() {
    val input = file.readLines()[0]
//    println(input)

// first part
//    val layerWithFewestZeroes = image.getLayerWithFewestZeroes()
//    val oneTimesTwoes = layerWithFewestZeroes.flatten().count { it == "1" } * layerWithFewestZeroes.flatten().count { it == "2" }

//    val width = 2
//    val height = 2
    val width = 25
    val height = 6
    val image = Image.createMatrix(input, width, height)
    image.getDecodedImage().second.forEach { println(it) }

}


class Image(val matrix: List<List<List<String>>>, val width: Int, val height: Int) {
    companion object {
        fun createMatrix(input: String, width: Int, height: Int): Image =
            Image(input.chunked(width * height) { it.chunked(width) { it.chunked(1) } }, width, height)
    }

    fun getDecodedImage(): Pair<Array<IntArray>, List<String>> {
        val decodedImage = Array(height) { IntArray(width) }
        for (y in 0 until height) {
            for (x in 0 until width) {
                var pixel = "2"
                var z = 0
                while(pixel == "2") {
                    pixel = matrix[z++][y][x]
                }
                decodedImage[y][x] = pixel.toInt()
            }
        }
        val prettyPrint = decodedImage.map { it.joinToString() }
        return Pair(decodedImage, prettyPrint)
    }

    fun getLayerWithFewestZeroes(): List<List<String>> = matrix.minByOrNull { it.flatten().count { it == "0" } }!!
}

