package test

import Player
import test.Color.*
import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport
import java.io.BufferedReader
import java.io.FileReader
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalTime
import java.time.Period
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

val Double.hours: LocalTime
    get() {
        val timeAsString = toString()
        val indexOfDecimal = timeAsString.indexOf(".")
        return LocalTime.of(
            timeAsString.substring(0, indexOfDecimal).toInt(),
            timeAsString.substring(indexOfDecimal + 1).padEnd(2, '0').toInt()
        )
    }

data class Talk(val title: String, var author: String = "", var time: LocalTime = 0.00.hours)
//fun talk(title: String, init: Talk.() -> Unit): Talk = Talk(title).apply(init)

infix fun Talk.from(time: Double): Talk = apply { this.time = time.hours }
infix fun Talk.by(author: String): Talk = apply { this.author = author }

data class Event(val title: String, val talks: List<Talk>)
class EventBuilder(private val title: String) {
    private val talks = mutableListOf<Talk>()
    fun build() = Event(title, talks)
    operator fun Talk.unaryPlus(): Talk = apply { talks.add(this) }
    operator fun String.unaryPlus(): Talk = Talk(this).apply { talks.add(this) }
}

fun event(title: String, init: EventBuilder.() -> Unit): Event = EventBuilder(title).apply(init).build()

fun fib(input: Int): Long = when (input) {
    0, 1 -> 1L
    else -> fib(input - 1) + fib(input - 2)
}


fun <T, R> ((T) -> R).memoize(): ((T) -> R) {
    val original = this
    val cache = mutableMapOf<T, R>()
    return { n: T -> cache.getOrPut(n) { original(n) } }
}

class Memoize<T, R>(val func: ((T) -> R)) {
    val cache = mutableMapOf<T, R>()

    operator fun getValue(thisRef: Any?, property: KProperty<*>) = { n: T ->
        cache.getOrPut(n) { func(n) }
    }
}

val fibM: (Int) -> Long by Memoize { n: Int ->
    when (n) {
        0, 1 -> 1L
        else -> fibM(n - 1) + fibM(n - 2)
    }
}

fun main(args: Array<String>) {

    println(fibM(100))
//    lateinit var fib: (Int) -> Long
//    fib = { n: Int ->
//        when (n) {
//            0, 1 -> 1L
//            else -> fib(n - 1) + fib(n - 2)
//        }
//    }.memoize()
//    println(fib(50))
//    val ev = event("event") {
//        + "bla" by "me" from 15.00
//        + "bla2" by "me" from 16.00
//    }
//    println(ev)
//    val talk = Talk(title = "bla",
//        time = 15.00.hours,
//        author = "me")
//    val talk = talk(title = "bla") {
//        time = 15.00.hours
//        author = "me"
//    }
//    println(talk)

//    val l = listOf("bbc", "abcd")
//    println(l.associateWith { it.first() })

//    val yesterday = 1.days.ago
//    println(yesterday)
//    val s = "kotlin"
//    s should startWith("kot")

//    val dependencies = DependencyHandler()
//    dependencies.compile("bla")
//    dependencies {
//        compile("bla")
//    }
//    val s = buildString {
//        append("Hello")
//        append(" World")
//    }
//    println(s)

//    println(serialize(Person("jos", 32)))

//    val p = Person("Jos", 32)
//    val ageP = Person::age
//    println(ageP.get(p))
//    val kProperty = ::counter
//    kProperty.setter.call(15)
//    println(kProperty.get())
////    val p = Person("Kees", 20)
//    val pClass = p.javaClass.kotlin
//    println(pClass.members)

//    validators.registerValidator(DefaultStringValidator, String::class)
//    validators.registerValidator(DefaultIntValidator, Int::class)
//
//    println(validators.get(Number::class).validate(5))
//    enumerateCats(Animal::getIndex)
//    val comp = Comparator<Any> {
//        e1, e2 -> e1.hashCode() - e2.hashCode()
//    }
//    println(listOf("abc", "cab").sortedWith(comp))
//    takeCareOfCats(Herd(listOf(Cat(), Cat())))
//    val list = mutableListOf("abc", "cab")
//    val languages = listOf("java", "kotlin", 5)
//    println(languages.filterInstanceOf<String>())
//    if (languages is List<*>) println(languages)
//
//    println(max("java", "Kotlin"))

//    println(oneHalf(15))
//    val dogs = listOf("Lassie", "Boefje")
//    println(dogs.slice(0..1))
//    val people = listOf(Person("Alice", 29), Person("Bob", 31))
//    lookForAlice(people)

//    val log = listOf(
//        SiteVisit("/", 34.0, OS.WINDOWS),
//        SiteVisit("/", 22.0, OS.MAC),
//        SiteVisit("/login", 12.0, OS.WINDOWS),
//        SiteVisit("/signup", 8.0, OS.IOS),
//        SiteVisit("/", 16.3, OS.ANDROID)
//    )
//
////    val averageWindowsDuration = log.filter { it.os == OS.WINDOWS }.map { it.duration }.average();
//    println(log.averageDurationForOs(OS.WINDOWS))
//    println(log.averageDurationFor { it.os == OS.WINDOWS})

//    val order = Order(5)
//    println("shipping costs: ${getShippingCostCalculator(Delivery.EXPEDITED)(order)}")


//    twoThree({ a, b -> a * b })

//    val p = Point(1, 2)
//    val (x, y) = p
//    println("x: $x and y: $y")

//    val map = mapOf("oracle" to "Java", "kotlin" to "JetBrains")
//    for ((k, v) in map) {
//        println("$k: $v")
//    }
//    val now = LocalDate.now()
//    val vacation = now..now.plusDays(10)
//    println(now.plusWeeks(1) in vacation)
//    for (day in vacation) {
//        println(day)
//    }
//    val p1 = Point(1, 6)
//    val p2 = Point(3, 2)
//    val p_in = Point(2, 4)
//    println(p_in in Rectangle(p1, p2))
//    println(p1[0])
//    println(p1 >= Point(3, 6))
//    println(1.5 * Point(4, 8))
//    val l = IntArray(5) { i -> i * i }
//    val l = mutableListOf(1, 2).toIntArray()
//    println(l.joinToString(","))

//    yellAt(Player(null))
//    printHashCode(42)
//    println(null.strLenOrZero())
//    val str : String? = "bla"

//    println(str?.let { strLen(it) })
        // chapter 6
//    println(alphabet())
//    createAllDoneRunnable().run()
//    val naturalNumber = generateSequence(0, { it + 1 })
//    val numbersTo100 = naturalNumber.takeWhile { it <= 100 }
//    println(numbersTo100.sum())
//    val value = Executors.newFixedThreadPool(4);
//    value.submit { println("bla") }
//    val people = listOf<Person>(Person("Michiel", 33), Person("Aletta", 32))
//    people.filter { it.age > 20 }
//    println(people.maxBy(Person::age))
//    val p_con = ::Person
//    val p = people.get(0)
//    println(p::age)
//    val user = WebsiteUser.capitalize("michiel@bla.nl")
//    println(user)
//    A.bar()
//    Payroll.allEmployees.add(Person("Jos", true))
//    val c1 = Client("Jan", 15)
//    val c2 = c1.copy(name = "Kees")
//    println(c1 == c2)
//    val col = DelegatingCollection<Client>();
//    println(col.isEmpty())
        // 4.3
//    val alice = PrivateUser("alice")
//    alice.address = "Nijenbeek 4"
//    println(alice.address)
//    val b = Button("bla")
//    b.printCurrentState()
//    val button = RoundButton()
//    button.click()
//    button.close()
//    fun readNumber(reader: BufferedReader) {
//        val number = try {
//            Integer.parseInt(reader.readLine())
//        } catch (e: NumberFormatException) {
//            5
//        }
//        println(number)
//    }
//    val reader = BufferedReader(StringReader("asd"))
//    readNumber(reader)

//    fun isLetter(c: Char) = c in 'a'..'z' || c in 'A'..'Z';
//
//    fun recognize(c: Char) =
//        when(c) {
//            in 'a'..'z', in 'A'..'Z' -> "it's a char"
//            in '1'.. '9' -> "it's a number"
//            else -> "dont recognize"
//        }
//
//    println("8 ${recognize('8')}" )

//    val list = arrayListOf("10", "11", "1010");
//    for ((index, value) in list.withIndex()) {
//        println("$index: $value")
//    }
//    val binaryReps = TreeMap<Char, String>()
//    for( c in 'A'..'f') {
//        val binary = Integer.toBinaryString(c.toInt())
//        binaryReps[c] = binary
//    }
//    for((letter, binary) in binaryReps) {
//        println("$letter = $binary")
//    }

//    for (i in 1 until 20) {
//        print(fizzBuzz(i))
//    }
//    val name = "kotlin";
//    println("Hello ${name.toUpperCase()}")
//    val person = Person("Bobeline", true);
//    println("hi ${person.name}, your name is short: ${person.shortName}")
//    println("Mn for Red: ${getMnemonic(RED)}")
//    println(mix(RED, YELLOW))
//    println (eval(Sum(Sum(Num(1), Num(2)), Num (4))))
    }

    private infix fun <T> T.should(matcher: Matcher<T>) = matcher.test(this)

    interface Matcher<T> {
        fun test(value: T)
    }

    class startWith(val prefix: String) : Matcher<String> {
        override fun test(value: String) {
            if (!value.startsWith(prefix)) throw AssertionError("wrong")
        }

    }

    val Int.days: Period
    get() = Period.ofDays(this)

    val Period.ago: LocalDate
    get() = LocalDate.now() - this

    object Country : Table() {
        val id = integer("id").autoIncrement().primaryKey()
        val name = varchar("name", 50)
    }

    class Column<T>
    open class Table {
        fun integer(name: String): Column<Int> {
            return Column()
        }

        fun varchar(name: String, size: Int): Column<String> {
            return Column()
        }

        fun Column<Int>.autoIncrement(): Column<Int> {
            return this
        }

        fun <T> Column<T>.primaryKey(): Column<T> {
            return this
        }
    }


    fun buildString(builder: StringBuilder.() -> Unit): String {
        val sb = StringBuilder()
        sb.builder()
        return sb.toString()
    }

//fun serialize(obj: Any): String = buildString { serializeObject(obj) }

    private fun StringBuilder.serializeObject(x: Any) {
        val kClass = x.javaClass.kotlin
        val memberProperties = kClass.memberProperties
        this.append(
            memberProperties.joinToString(separator = ", ", prefix = "{ ", postfix = " }",
                transform = { p: KProperty1<Any, *> -> "\"${p.name}\": ${p.get(x)}" })
        )
    }

    fun foo(x: Int) = println(x)
    fun sum(x1: Int, x2: Int): Int = x1 + x2
    var counter = 0

    fun <T> copyList(source: MutableList<T>, destination: MutableList<in T>) {
        for (item in source) {
            destination.add(item)
        }
    }

    interface FieldValidator<in T> {
        fun validate(input: T): Boolean
    }

    object DefaultStringValidator : FieldValidator<String> {
        override fun validate(input: String) = input.isNotEmpty()
    }

    object DefaultIntValidator : FieldValidator<Int> {
        override fun validate(input: Int) = input >= 0
    }

    object validators {
        private val validators = mutableMapOf<KClass<*>, FieldValidator<*>>()

        fun <T : Any> registerValidator(validator: FieldValidator<T>, cl: KClass<T>) {
            validators[cl] = validator
        }

        @Suppress("UNCHECKED_CAST")
        fun <T : Any> get(cl: KClass<T>): FieldValidator<T> {
            return validators[cl] as? FieldValidator<T> ?: throw IllegalArgumentException("Did not find validator")
        }
    }

    interface Company

    class CompanyImpl : Company

    data class PersonJ(
        @JsonName("alias")
        val firstName: String,
        @JsonExclude
        val age: Int? = null,
        @DeserializeInterface(CompanyImpl::class)
        val company: Company,
        @CustomSerializer(DateSerializer::class)
        val birthDate: Date
    )

    annotation class CustomSerializer(val serializer: KClass<out ValueSerializer<*>>)

    class DateSerializer : ValueSerializer<Date> {
        override fun toJson(input: Date): String = ""
        override fun fromJson(s: String): Date = Date()
    }

    interface ValueSerializer<T> {
        fun toJson(input: T): String
        fun fromJson(s: String): T

    }

    annotation class DeserializeInterface(val clazz: KClass<out Any>)

    annotation class JsonExclude

    annotation class JsonName(val name: String)

    fun enumerateCats(f: (Cat) -> Number) {}
    fun Animal.getIndex(): Int = 0
    open class Animal {
        fun feed() = println("feeding")
    }
//
//class Herd<out T : Animal>(val members: List<T>) {
//    val size: Int get() = members.size
//    operator fun get(i: Int): T = members[i]
//}
//fun feedAll(animals: Herd<Animal>) {
//    for (i in 0 until animals.size) {
//        animals[i].feed()
//    }
//}
//class Cat : Animal() {
//    fun cleanLitter() {
//        println("cleaning")
//    }
//}
//
//fun takeCareOfCats(cats: Herd<Cat>) {
//    for (i in 0 until cats.size) {
//        cats[i].cleanLitter()
//    }
//    feedAll(cats)
//}

    fun <T : Number> oneHalf(n: T): Double = n.toDouble() / 2.0;

    //fun <T : Comparable<T>> max(first: T, second: T): T = if (first > second) first else second
    fun <T> max(first: T, second: T): T where T : Comparable<T>, T : Any = if (first > second) first else second

    inline fun <reified T> List<*>.filterInstanceOf(): List<T> {
        val dest = mutableListOf<T>();
        for (item in this) {
            if (item is T) {
                dest.add(item)
            }
        }
        return dest
    }

    fun lookForAlice(persons: List<Person>) {
        persons.forEach(fun(person) {
            if (person.name == "Alice") return
            println("not alice")
        })
    }

    data class SiteVisit(
        val path: String,
        val duration: Double,
        val os: OS
    )

    enum class OS { WINDOWS, LINUX, MAC, IOS, ANDROID }

    fun List<SiteVisit>.averageDurationForOs(osToFilter: OS) =
        this.filter { it.os == osToFilter }.map { it.duration }.average()

    inline fun List<SiteVisit>.averageDurationFor(predicate: (SiteVisit) -> Boolean) =
        this.filter(predicate).map { it.duration }.average()

    fun readFirstLineFromFile(path: String): String {
        return BufferedReader(FileReader(path)).use { it.readLine() }
    }

    enum class Delivery { STANDARD, EXPEDITED }
    class Order(val itemCount: Int)

    fun getShippingCostCalculator(delivery: Delivery): (Order) -> Double {
        if (delivery == Delivery.EXPEDITED) {
            return { order -> 6 + 2.1 * order.itemCount }
        }
        return { order -> 1.2 * order.itemCount }
    }

    fun twoThree(func: (Int, Int) -> Int, transform: ((Int) -> Unit)? = { println(it) }): Unit {
        val res = func(2, 3)
        transform?.invoke(res)
    }

    open class PropertyChangeAware {
        protected val changeSupport = PropertyChangeSupport(this)
        fun addPropertyChangeListener(listener: PropertyChangeListener) {
            changeSupport.addPropertyChangeListener(listener)
        }

        fun removePropertyChangeListener(listener: PropertyChangeListener) {
            changeSupport.removePropertyChangeListener(listener)
        }
    }

    class Person(val name: String, val age: Int, salary: Int = 10000) {

    }

//    override fun compareTo(other: Point): Int = compareValuesBy(this, other, Point::x, Point::y)
//    operator fun get(index: Int): Int {
//        return when(index) {
//            0 -> x
//            1 -> y
//            else -> throw IndexOutOfBoundsException("inv")
//        }
//    operator fun set(index: Int, value: Int) {
//        when(index) {
//            0 -> x = value
//            1 -> y = value
//            else -> throw IndexOutOfBoundsException("inv")
//        }
//    }


    operator fun ClosedRange<LocalDate>.iterator(): Iterator<LocalDate> =
        object : Iterator<LocalDate> {
            var current = start
            override fun hasNext() =
                current <= endInclusive

            override fun next() = current.apply {
                current = plusDays(1)
            }
        }

    fun strLen(input: String?) = input?.length ?: 0

    data class Rectangle(val ul: Point, val lr: Point) {
        operator fun contains(p: Point): Boolean {
            return p.x in ul.x until lr.x && p.y in lr.y until ul.y
        }
    }


    data class Point(val x: Int, val y: Int) {

//    operator fun plus(o: Point): Point = Point(x + o.x, y + o.y)

//    }
    }


    //operator fun Double.times(o: Point) = Point((this * o.x).toInt(), (this * o.y).toInt())
    fun String?.strLenOrZero(): Int = this?.length ?: 0

    fun yellAt(p: Player) = println("COME HERE, ${(p.name ?: "random").toUpperCase()}!")

    fun <T : Any> printHashCode(input: T) = println(input.hashCode())

//fun alphabet(): String =
//    StringBuilder().apply {
//        for (letter in 'A'..'Z') {
//            append(letter)
//        }
//        append(("\nNow I know the alphabet!"))
//    }.toString()

//    return ('A'..'Z').fold(StringBuilder(), StringBuilder::append).append("\nNow I know the alphabet!").toString();

//    return with(result) {
//
//        for (letter in 'A'..'Z') {
//            this.append(letter)
//        }
//        this.append("\nNow I know the alphabet!")
//        this.toString()
//    }

//    buildString {
//        for (letter in 'A'..'Z') {
//            append(letter)
//        }
//        append(("\nNow I know the alphabet!"))
//    }

    fun createAllDoneRunnable(): Runnable {
        return Runnable { println("all done") }
    }

    fun Person.isAdult() = age >= 18

    fun salute() = "salute!"

    class WebsiteUser private constructor(val nickname: String) {
        companion object {
            fun fromEmail(email: String) =
                WebsiteUser(email.substringBefore('@'))

            fun fromFaceBookId(id: Int) =
                WebsiteUser(id.toString())
        }

        override fun toString() = nickname
    }

    fun WebsiteUser.Companion.capitalize(nickname: String): WebsiteUser = fromEmail(nickname.toUpperCase())

    class A {
        companion object {
            fun bar() {
                println("bar called")
            }
        }
    }

    object Payroll {
        val allEmployees = arrayListOf<Person>()

        fun calcSalary() {
            for (employee in allEmployees) {
                //
            }
        }
    }


    class DelegatingCollection<T>(val innerList: Collection<T> = ArrayList<T>()) : Collection<T> by innerList {
        override fun isEmpty(): Boolean {
            println("I'm empty")
            return innerList.isEmpty()
        }
    }

    data class Client(val name: String, val age: Int)

    interface User {
        val nickname: String
        val yellName: String get() = nickname.toUpperCase()
    }

    class PrivateUser(override val nickname: String) : User {
        var address: String = "unspecified"
            set(value) {
                println(
                    """
                    Address was changed for $nickname:
                    "$field" -> "$value".""".trimIndent()
                )
                field = value
            }
    }

    class SubscribingUser(val email: String) : User {
        override val nickname: String
            get() = email.substringBefore("@")
    }

    class FaceBookUser(val accountId: Int) : User {
        override val nickname = getFaceBookName(accountId);

        private fun getFaceBookName(accountId: Int): String {
            return "user"
        }
    }

    interface State : Serializable

    interface View {
        fun getCurrentState(): State
        fun restoreState(state: State) {}
    }

    class Button(val state: String) : View {

        inner class ButtonState : State {
            fun printState() = println(this@Button.state);
        }

        fun printCurrentState(): Unit = ButtonState().printState();
        override fun getCurrentState(): State {
            TODO("Not yet implemented")
        }
    }


    interface Clickable {
        fun click()
        fun move() {
            println("clicker was moved")
        }
    }


    interface Focusable {
        fun move() {
            println("focusser was moved")
        }
    }

    open class RoundButton {
        internal fun click() {
            println("clicking round button")
        }

        fun close() {
            println("closing time")
        }
    }

    fun RoundButton.go() {
        click()
        close()
    }


    fun fizzBuzz(i: Int) = when {
        i % 15 == 0 -> "FizzBuzz "
        i % 3 == 0 -> "Fizz "
        i % 5 == 0 -> "Buzz "
        else -> "$i "
    }

    fun eval(input: Expr): Int {
        when (input) {
            is Num -> return input.value
            is Sum -> return eval(input.left) + eval(input.right)
        }
    }

    sealed class Expr
    class Num(val value: Int) : Expr()
    class Sum(val left: Expr, val right: Expr) : Expr()

    fun max(a: Int, b: Int) = if (a > b) a else b

    enum class Color(val r: Int, val g: Int, val b: Int) {
        RED(255, 0, 0), ORANGE(255, 165, 0),
        YELLOW(255, 255, 0), GREEN(0, 255, 0), BLUE(0, 0, 255),
        INDIGO(75, 0, 130), VIOLET(238, 130, 238);

    }


    fun getMnemonic(color: Color) =
        when (color) {
            RED -> "Richard"
            ORANGE -> "of"
            YELLOW -> "York"
            GREEN -> "Gave"
            BLUE -> "Battle"
            INDIGO -> "In"
            VIOLET -> "Vain"
        }

    fun mix(c1: Color, c2: Color) =
        when (setOf(c1, c2)) {
            setOf(RED, YELLOW) -> ORANGE
            setOf(YELLOW, BLUE) -> GREEN
            setOf(RED, VIOLET) -> INDIGO
            else -> throw Exception("Dirty color")
        }