package approvals

import com.oneeyedmen.okeydoke.Approver
import com.oneeyedmen.okeydoke.Sources
import dev.minutest.TestDescriptor
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import java.io.File


/**
 * This shows the raw steps necessary to integrate Okeydoke with Minutest.
 *
 * See [ApprovalsTest2] for a version with less boilerplate.
 */
class ApprovalsTest : JUnit5Minutests {

    class Fixture(testDescriptor: TestDescriptor) {
        val approver = Approver(
            testDescriptor.fullName().drop(1).joinToString(">"),
            Sources.`in`(File("src/test/kotlin"), this::class.java.`package`)
        )
    }

    fun tests() = rootContext<Fixture> {

        fixture { testDescriptor ->  Fixture(testDescriptor) }

        test("in root") {
            approver.assertApproved("banana")
        }

        context("context") {
            test("in context") {
                approver.assertApproved("kumquat")
            }
        }

        after {
            if (!approver.satisfactionChecked()) approver.assertSatisfied()
        }
    }
}