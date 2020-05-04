package runtimeFixtures

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import junit.framework.Assert.assertEquals
import java.sql.Connection

// Example to show running the same tests against a number of databases decided at runtime
class MultiDBTests : JUnit5Minutests  {

    fun tests() = rootContext<UnderTest> {
        connectionFactories().forEach { factory ->
            context("Using ${factory.name}") {
                fixture { UnderTest(factory) }
                test("read something") {
                    assertEquals("banana", readSomething())
                }
                test("something else") {
                    assertEquals("banana", somethingElse())
                }
            }
        }
    }
}

data class ConnectionFactory(
    val name: String,
    val f: () -> Connection
): () -> Connection by f

fun connectionFactories(): List<ConnectionFactory> {
    TODO("read the environment to see what DBs we want to test against")
}

class UnderTest(
    val connectionFactory: () -> Connection
) {
    fun readSomething(): String {
        val connection = connectionFactory()
        val statement = connection.createStatement()
        statement.execute("some sql")
        TODO()
    }

    fun somethingElse(): String {
        TODO()
    }
}