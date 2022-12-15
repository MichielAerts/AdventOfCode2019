package test.advent.edition2022.day7

import test.advent.edition2022.getOrThrow
import java.io.File

val day = 7;
val file = File("src/main/resources/edition2022/day${day}/input")

class Puzzle(private val input: List<String>) {
    fun runPart1() {
        println("Part 1 of Day $day")
        println(input)
        val dirs = mapFileSystem()
        println(dirs.map { (_, dir) -> dir.getTotalSize() }.filter { it < 100000 }.sumOf { it })
    }

    fun runPart2() {
        println("Part 2 of Day $day")
        val totalDiskSize = 70000000
        val neededFreeSpace = 30000000
        val dirs = mapFileSystem()
        val needToClear = neededFreeSpace - (totalDiskSize - dirs.getOrThrow("/").getTotalSize())
        println(needToClear)
        println(dirs.map { (_, dir) -> dir.getTotalSize() }.filter { it > needToClear }.minByOrNull { it })

    }

    private fun mapFileSystem(): MutableMap<String, Dir> {
        val fileRegex = """(\d+) ([^\s]+)""".toRegex()
        val dirs = mutableMapOf<String, Dir>()
        val rootDir = Dir("/")
        dirs += rootDir.path to rootDir
        var currentDir = rootDir
        for (line in input) {
            when {
                line == "$ cd /" -> continue
                line == "$ cd .." -> currentDir = currentDir.parentDir!!
                line.startsWith("$ cd ") -> {
                    val dirName = "${currentDir.path.removeSuffix("/")}/${line.substringAfter("$ cd ")}"
                    val dir = Dir(dirName, currentDir)
                    dirs += dirName to dir
                    currentDir.childDirs += dir
                    currentDir = dir
                }
                line == "$ ls" -> continue
                line.startsWith("dir ") -> continue
                line.matches(fileRegex) -> {
                    val (_, fileSize, fileName) = fileRegex.find(line)?.groupValues ?: throw IllegalStateException("huh?")
                    currentDir.files += DeviceFile(fileName, fileSize.toInt())
                }
                else -> throw IllegalStateException("not caught: $line")
            }
        }
        return dirs
    }
}

data class Dir(
    val path: String, val parentDir: Dir? = null,
    var childDirs: List<Dir> = emptyList(),
    var files: List<DeviceFile> = emptyList()
) {
    override fun toString(): String = "$path,$files,${childDirs.map { it.path }}"
    fun getTotalSize(): Int = files.sumOf { it.size } + childDirs.sumOf { it.getTotalSize() }
}

data class DeviceFile(val name: String, val size: Int)

fun main() {
    val input = file.readLines()
    val puzzle = Puzzle(input)
    puzzle.runPart1()
    puzzle.runPart2()
}

