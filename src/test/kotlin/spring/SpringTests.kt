package spring

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.reset
import com.nhaarman.mockito_kotlin.whenever
import dev.minutest.TestContextBuilder
import dev.minutest.TestDescriptor
import org.junit.Assert.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.http.MediaType.TEXT_PLAIN_VALUE
import org.springframework.test.context.TestContextManager
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod.GET
import org.springframework.web.bind.annotation.RestController

interface RandomWordService {
    fun words(n: Int = 1): Iterable<String>
}

@RestController
open class RandomWordController(
    @Autowired private val service: RandomWordService
) {
    @RequestMapping("/words", method = [GET], produces = [TEXT_PLAIN_VALUE])
    fun words(): String = service.words(3).joinToString()
}

class SpringTests : JUnit5Minutests {

    // We annotate the Fixture as we would have annotated the JUnit test class
    @SpringBootTest(classes = [RandomWordController::class])
    @AutoConfigureMockMvc
    class Fixture {
        val words = listOf("catflap", "rubberplant", "marzipan")

        @Autowired
        lateinit var mvc: MockMvc

        @MockBean
        lateinit var service: RandomWordService
    }


    fun tests() = rootContext<Fixture> {

        context("a spring integration test environment") {

            // springFixture does the job of wiring the Fixture
            springFixture {
                Fixture()
            }

            before {
                // this is setting up the mock
                whenever(service.words(any<Int>())) doAnswer {
                    val n = it.arguments.first() as Int
                    words.slice(0 until n)
                }
            }

            test("we can access an autowired bean") {
                assertEquals(words, service.words(3))
            }

            test("we can hit a spring web endpoint") {
                mvc
                    .perform(get("/words").accept(TEXT_PLAIN))
                    .andExpect(status().isOk)
                    .andExpect(content().string(words.joinToString()))
            }
        }
    }
}

private inline fun <PF, reified F> TestContextBuilder<PF, F>.springFixture(
    crossinline factory: (Unit).(testDescriptor: TestDescriptor) -> F
) {
    fixture { testDescriptor ->
        factory(testDescriptor).also {
            TestContextManager(F::class.java).prepareTestInstance(it)
        }
    }
}
