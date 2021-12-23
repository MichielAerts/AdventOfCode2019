package test.advent.day14

import java.io.File

val day = 14;
val file = File("src/main/resources/edition2019/day${day}/input")

var currentStock = mutableMapOf<String, Int>()

fun main() {
    val reactionList = file.readLines().map { Reaction.fromStr(it) }

    val reactions = reactionList.map { it.output to it.input }.toMap()
    // find reaction that produces 1 fuel
    val ore = react(reactions.react(ChemicalAmount(1, "FUEL")), reactions)
//    println(ore)
    println(ore.map { it.amount }.sum())
    println(currentStock)

    // part 2
    val trillion = 1000000000000L
    var oreSpent = 0L
    var amount = 0L
    while (oreSpent < trillion) {
        val ore = react(reactions.react(ChemicalAmount(1, "FUEL")), reactions)
        val oreForOneFuel = ore.map { it.amount }.sum()
        oreSpent += oreForOneFuel
        amount++
        if (amount % 100000 == 0L) {
            println("$amount, spent: $oreSpent, ore for one fuel: $oreForOneFuel")
        }
    }
    println(amount - 1)
//    val amountPerReaction = 13312L
//    println(trillion / amountPerReaction)
}

fun react(chemicals: List<ChemicalAmount>, reactions: Map<ChemicalAmount, List<ChemicalAmount>>): List<ChemicalAmount> {
    val result = mutableListOf<ChemicalAmount>()
    for (chemical in chemicals) {
        if (chemical.name == "ORE") {
            result.add(chemical)
        } else {
            result.addAll(react(reactions.react(chemical), reactions))
        }
    }
    return result
}

private fun Map<ChemicalAmount, List<ChemicalAmount>>.react(input: ChemicalAmount): List<ChemicalAmount> {
//    println("trying to react $input, current stock: $currentStock")
    var amountOfChemInStock = currentStock.getOrDefault(input.name, 0)
    var times = 0
    val (inReaction, outReaction) = this.entries.find { it.key.name == input.name }!! // reaction = 10A, 10 ORE
    while(amountOfChemInStock < input.amount) {
        // perform reaction & add to stock
        amountOfChemInStock += inReaction.amount
        times++
    }
    currentStock.put(input.name, amountOfChemInStock - input.amount)
    val map = outReaction.map { c -> ChemicalAmount((times * c.amount), c.name) }
//    println("reacted $times x $input to total of $map, new stock: $currentStock")
    return if (times == 0) listOf() else map
}

data class ChemicalAmount(val amount: Int, val name: String) {
    companion object {
        fun fromStr(str: String): ChemicalAmount {
            val (_, amount, name) = """\s*(\d+) (\w+)\s*""".toRegex().find(str)?.groupValues
                ?: throw IllegalStateException("no match!")
            return ChemicalAmount(amount.toInt(), name)
        }
    }
}

data class Reaction(val input: List<ChemicalAmount>, val output: ChemicalAmount) {
    companion object {
        fun fromStr(str: String): Reaction {
            val (rawInput, rawOutput) = str.split("=>")
            return Reaction(rawInput.split(",").map { ChemicalAmount.fromStr(it) }, ChemicalAmount.fromStr(rawOutput))
        }
    }
}