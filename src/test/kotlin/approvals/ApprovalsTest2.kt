package approvals

import com.oneeyedmen.okeydoke.Approver
import com.oneeyedmen.okeydoke.ApproverFactories.fileSystemApproverFactory
import com.oneeyedmen.okeydoke.ApproverFactory
import dev.minutest.TestContextBuilder
import dev.minutest.TestDescriptor
import dev.minutest.dependentFixture
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import java.io.File


/**
 * A less-boilerplatey way of integrating Okeydoke and Minutest for the common case.
 */
class ApprovalsTest2 : JUnit5Minutests {

    private val approverFactory = fileSystemApproverFactory(File("src/test/kotlin"))

    class Fixture(val approver: Approver) {
        // other fixture things here
    }

    fun tests() = rootContext<Fixture> {

        approvalsFixture(approverFactory) { approver ->
            Fixture(approver)
        }

        test("in root") {
            approver.assertApproved("banana")
        }

        context("context") {
            test("in context") {
                approver.assertApproved("kumquat")
            }
        }
    }
}

inline fun <PF, reified F> TestContextBuilder<PF, F>.approvalsFixture(
    approverFactory: ApproverFactory<Approver>,
    crossinline namer: (TestDescriptor) -> String = { it.fullName().drop(1).joinToString(">") },
    crossinline factory: (Unit).(approver: Approver) -> F
) {
    dependentFixture(
        dependencyBuilder = { testDescriptor ->  approverFactory.createApprover(namer(testDescriptor), F::class.java) },
        dependencyDisposer = { approver, _ -> if (!approver.satisfactionChecked()) approver.assertSatisfied() },
        factory = { approver, _ -> factory(approver)}
    )
}