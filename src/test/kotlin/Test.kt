import io.kotlintest.shouldBe
import io.kotlintest.specs.AbstractStringSpec
import io.kotlintest.specs.StringSpec
import org.junit.Test
import test.Cat
import test.CatRetailer
import test.Vet
import kotlin.test.assertEquals

class Test : StringSpec({
    "should sell a Loekie" {
        val seller = CatRetailer()
        seller.sell().name shouldBe "Loekie"
    }
})