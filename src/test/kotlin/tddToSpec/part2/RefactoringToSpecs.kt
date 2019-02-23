package tddToSpec.part2

import dev.minutest.TestContextBuilder
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions.assertEquals
import tddToSpec.part1.isNegative
import tddToSpec.part1.isPositive
import tddToSpec.part1.isZero

/*-
---
title: Test Driven to Specification with Minutest - Part 2
layout: post
---
-*/

/*-
This is the second part of a mini-series on [Minutest](https://github.com/dmcg/minutest).

In [Part 1](test-driven-to-specification-with-minutest-part1.html) we looked at the Test Driven Development of an
extension function, `partition`.

In this installment we'll look at refactoring the (in many ways pretty arbitrary) tests that resulted into a more
formal specification of what our code does.

## The Code Under Test

To remind ourselves - here is the code:
-*/

//`
fun <T> Iterable<T>.partition(predicates: List<(T) -> Boolean>): List<List<T>> {
    val grouped: Map<((T) -> Boolean)?, List<T>> = this.groupBy { item ->
        predicates.firstMatch(item)
    }
    return predicates.map { grouped.getOrDefault(it, emptyList()) }
}

private fun <T> Iterable<(T) -> Boolean>.firstMatch(item: T): ((T) -> Boolean)? =
    this.find { predicate -> predicate(item) }
//`

/*-

## The TDD Tests

Here are the tests from the end of our TDD session.
-*/

object Context1 {
    //`
    class PartitionTests : JUnit5Minutests {
        fun tests() = rootContext<Unit>() {
            test("every item matches a predicate and every predicate matches an item") {
                val items = listOf(-1, 0, 1, 2, 3)
                val predicates = listOf(::isNegative, ::isZero, ::isPositive)
                val result = items.partition(predicates)
                assertEquals(listOf(listOf(-1), listOf(0), listOf(1, 2, 3)), result)
            }
            test("an item matches no predicate") {
                val items = listOf(-1, 0, 1, 2, 3)
                val predicates = listOf(::isZero, ::isPositive)
                val result = items.partition(predicates)
                assertEquals(listOf(listOf(0), listOf(1, 2, 3)), result)
            }
            test("a predicate matches no item") {
                val items = listOf(0, 1, 2, 3)
                val predicates = listOf(::isNegative, ::isZero, ::isPositive)
                val result = items.partition(predicates)
                assertEquals(listOf(listOf(), listOf(0), listOf(1, 2, 3)), result)
            }
            test("some items no predicates") {
                val items = listOf(-1, 0, 1, 2, 3)
                val predicates = emptyList<(Int) -> Boolean>()
                val result = items.partition(predicates)
                assertEquals(emptyList<List<Int>>(), result)
            }
            test("some predicates no items") {
                val items = emptyList<Int>()
                val predicates = listOf(::isNegative, ::isZero, ::isPositive)
                val result = items.partition(predicates)
                assertEquals(listOf(emptyList<Int>(), emptyList(), emptyList()), result)
            }
            test("no predicates no items") {
                val items = emptyList<Int>()
                val predicates = emptyList<(Int) -> Boolean>()
                val result = items.partition(predicates)
                assertEquals(emptyList<List<Int>>(), result)
            }
        }
    }
    //`
}

/*-
Personally, even though we've tried to be formal in the names of our tests, I find that it's hard to see the structure
in this flat list. Another problem is that the test names tell us the conditions - the 'whens', but don't actually tell
us what the assertions mean. We can fix that with more words, but then we have a sea of text to deal with.

Finally there is all that duplication.

## Removing Duplication

I guess that's the easiest thing to address. Let's extract a check function.
-*/

object Context2 {
    class PartitionTests : JUnit5Minutests {
        //`
        fun tests() = rootContext<Unit>() {
            test("every item matches a predicate and every predicate matches an item") {
                val items = listOf(-1, 0, 1, 2, 3)
                val predicates = listOf(::isNegative, ::isZero, ::isPositive)
                check(items, predicates, listOf(listOf(-1), listOf(0), listOf(1, 2, 3)))
            }
            test("an item matches no predicate") {
                val items = listOf(-1, 0, 1, 2, 3)
                val predicates = listOf(::isZero, ::isPositive)
                check(items, predicates, listOf(listOf(0), listOf(1, 2, 3)))
            }
            test("a predicate matches no item") {
                val items = listOf(0, 1, 2, 3)
                val predicates = listOf(::isNegative, ::isZero, ::isPositive)
                check(items, predicates, listOf(listOf(), listOf(0), listOf(1, 2, 3)))
            }
            test("some items no predicates") {
                val items = listOf(-1, 0, 1, 2, 3)
                val predicates = emptyList<(Int) -> Boolean>()
                check(items, predicates, emptyList())
            }
            test("some predicates no items") {
                val items = emptyList<Int>()
                val predicates = listOf(::isNegative, ::isZero, ::isPositive)
                check(items, predicates, listOf(emptyList(), emptyList(), emptyList()))
            }
            test("no predicates no items") {
                val items = emptyList<Int>()
                val predicates = emptyList<(Int) -> Boolean>()
                check(items, predicates, emptyList())
            }
        }

        private fun <T> check(items: List<T>, predicates: List<(T) -> Boolean>, expected: List<List<T>>) {
            assertEquals(expected, items.partition(predicates))
        }
        //`
    }
}

/*-
That's a bit better, or at least a bit shorter. Actually our cognitive load looking at the tests is reduced, because
we can be reasonably sure that each is doing the same thing.

## Grouping into Contexts

We're still left with a flat list of 6 tests though. Minutest allows the grouping of tests into contexts - let's try
just grouping what we have.
-*/

private fun <T> check(items: List<T>, predicates: List<(T) -> Boolean>, expected: List<List<T>>) {
    assertEquals(expected, items.partition(predicates))
}

object Context3 {
    class PartitionTests : JUnit5Minutests {
        //`
        fun tests() = rootContext<Unit>() {
            context("some items") {
                test("every item matches a predicate and every predicate matches an item") {
                    // ...
                }
                test("an item matches no predicate") {
                    // ...
                }
                test("a predicate matches no item") {
                    // ...
                }
                test("no predicates") {
                    // ...
                }
            }
            context("no items") {
                test("some predicates") {
                    // ...
                }
                test("no predicates") {
                    // ...
                }
            }
        }
        //`
    }
}

/*-
This helps a bit, but there is no relationship in code between the context and its name. That would be a nice feature,
and Minutest supports this with fixtures. I think of the fixture as the world of the tests - all the state we need to
concern ourselves with.

## Adding a Fixture

At the moment the fixture type is `Unit` - the one in `rootContext<Unit>`. Let's change that to be a class containing
the items that we want to test with. This seems overkill for now, but bear with me.
-*/

object Context4 {
    //`
    class PartitionTests : JUnit5Minutests {

        class Fixture(val items: List<Int>)

        fun tests() = rootContext<Fixture>() {
            context("some items") {
                // ...
                test("ok") {}
            }
        }
    }
    //`
}

/*-
If you run this you get a ~~nasty `ClassCastException` which is a [bad way](https://github.com/dmcg/minutest/issues/24) of~~
`IllegalStateException: Fixture has not been set in context "some items"`
telling you that you need to tell Minutest how to provide a fixture to the tests.

We can do that with a `fixture` block, which returns the fixture that should be used in tests in the context. So in `some items`
we use `Fixture(listOf(-1, 0, 1, 2, 3))`, and in `no items`, `Fixture(emptyList())`.
-*/

fun isBiggerThan10(x: Int) = x > 100
fun isBiggerThan1(x: Int) = x > 1

object Context5 {
    //`
    class PartitionTests : JUnit5Minutests {

        class Fixture(val items: List<Int>)

        fun tests() = rootContext<Fixture>() {
            context("some items") {
                fixture {
                    Fixture(listOf(-1, 0, 1, 2, 3))
                }
                test("every item matches a predicate and every predicate matches an item") {
                    val predicates = listOf(::isNegative, ::isZero, ::isPositive)
                    check(items, predicates, listOf(listOf(-1), listOf(0), listOf(1, 2, 3)))
                }
                test("an item matches no predicate") {
                    val predicates = listOf(::isZero, ::isPositive)
                    check(items, predicates, listOf(listOf(0), listOf(1, 2, 3)))
                }
                test("a predicate matches no item") {
                    val items = listOf(0, 1, 2, 3)
                    val predicates = listOf(::isNegative, ::isZero, ::isPositive)
                    check(items, predicates, listOf(listOf(), listOf(0), listOf(1, 2, 3)))
                }
                test("no predicates") {
                    val predicates = emptyList<(Int) -> Boolean>()
                    check(items, predicates, emptyList())
                }
            }
            context("no items") {
                fixture {
                    Fixture(emptyList())
                }
                test("some predicates") {
                    val items = emptyList<Int>()
                    val predicates = listOf(::isNegative, ::isZero, ::isPositive)
                    check(items, predicates, listOf(emptyList(), emptyList(), emptyList()))
                }
                test("no predicates") {
                    val items = emptyList<Int>()
                    val predicates = emptyList<(Int) -> Boolean>()
                    check(items, predicates, emptyList())
                }
            }
        }
    }
    //`
}

/*-
The reason that this compiles is that inside a `test` block, `this` is the fixture that was created, so that `items`
now refers to `Fixture.items`.

Now we can do the same with predicates, introducing a context for the different values.
-*/

object Context6 {
    //`
    class PartitionTests : JUnit5Minutests {

        data class Fixture(val items: List<Int>, val predicates: List<(Int) -> Boolean>)

        fun tests() = rootContext<Fixture>() {
            context("some items") {
                fixture {
                    Fixture(listOf(-1, 0, 1, 2, 3), emptyList())
                }
                context("every item matches a predicate and every predicate matches an item") {
                    deriveFixture {
                        // derive fixture allows us to take the parent fixture and return a derivation
                        parentFixture.copy(predicates = listOf(::isNegative, ::isZero, ::isPositive))
                    }
                    test("returns a list for each predicate") {
                        check(items, predicates, listOf(listOf(-1), listOf(0), listOf(1, 2, 3)))
                    }
                }
                context("an item matches no predicate") {
                    deriveFixture {
                        parentFixture.copy(predicates = listOf(::isZero, ::isPositive))
                    }
                    test("item is not in the returned lists") {
                        check(items, predicates, listOf(listOf(0), listOf(1, 2, 3)))
                    }
                }
                context("a predicate matches no item") {
                    deriveFixture {
                        // Note that we have added a predicate to allow the same items as the other tests
                        parentFixture.copy(
                            predicates = listOf(::isBiggerThan10, ::isNegative, ::isZero, ::isPositive)
                        )
                    }
                    test("returns an empty list for that predicate") {
                        check(items, predicates, listOf(emptyList(), listOf(-1), listOf(0), listOf(1, 2, 3)))
                    }
                }
                context("no predicates") {
                    test("returns empty list") {
                        check(items, predicates, emptyList())
                    }
                }
            }
            context("no items") {
                //...
            }
        }
    }
    //`
}

/*-
You can see now that there is a direct relationship between the (nested) name of the context and the state of the fixture.
Not only that, but because the conditions are moved out into the contexts, the tests are free to describe just the
result - eg `item is not in the returned lists`.

## Removing More Duplication

A little bit of refactoring gives the following.
-*/

object Context7 {
    //`
    class PartitionTests : JUnit5Minutests {

        data class Fixture(
            val items: List<Int> = emptyList(),
            val predicates: List<(Int) -> Boolean> = emptyList()
        ) {
            fun assertResultIs(vararg expected: List<Int>) {
                assertEquals(expected.asList(), items.partition(predicates))
            }

            fun withPredicates(vararg predicates: (Int) -> Boolean) =
                copy(predicates = predicates.asList())
        }

        fun tests() = rootContext<Fixture> {
            context("some items") {
                fixture {
                    Fixture(items = listOf(-1, 0, 1, 2, 3))
                }
                context("everything matches something") {
                    deriveFixture {
                        withPredicates(::isNegative, ::isZero, ::isPositive)
                    }
                    test("returns a list for each predicate") {
                        assertResultIs(listOf(-1), listOf(0), listOf(1, 2, 3))
                    }
                }
                context("a predicate doesn't match any item") {
                    deriveFixture {
                        withPredicates(::isBiggerThan10, ::isNegative, ::isZero, ::isPositive)
                    }
                    test("returns an empty list for that predicate") {
                        assertResultIs(emptyList(), listOf(-1), listOf(0), listOf(1, 2, 3))
                    }
                }
                context("an item doesn't match any predicate") {
                    deriveFixture {
                        withPredicates(::isNegative, ::isPositive)
                    }
                    test("item is not in the returned lists") {
                        assertResultIs(listOf(-1), listOf(1, 2, 3))
                    }
                }
                context("no predicates") {
                    test("returns empty list") {
                        assertResultIs()
                    }
                }
            }
            context("no items") {
                //...
            }
        }
    }
    //`
}

/*-
This is longer than the original, but, for me at least, the contexts give my brain room to consider other cases, and
somewhere to put the tests that doesn't overwhelm the reader.

## Hiding Minutest

Nitpicking - I'm not happy with the deriveFixture calls there - they leak Minutest minutiae into our tests. Luckily this is
_All Just Kotlin™_ and so we can make better named operations to do the job for us. I'm going with `withItems` and
`withPredicates`.

Now our spec reads in a way that we might even show to our customers (except, perhaps, for the term `predicates`). We
even have the cognitive space to add tests that show what happens in some special cases.
-*/

object Context8 {
    //`
    class PartitionTests : JUnit5Minutests {

        data class Fixture(
            val items: List<Int> = emptyList(),
            val predicates: List<(Int) -> Boolean> = emptyList()
        ) {
            fun assertResultIs(vararg expected: List<Int>) {
                assertEquals(expected.asList(), items.partition(predicates))
            }
        }

        fun tests() = rootContext<Fixture> {
            fixture { Fixture() }

            context("some items") {
                withItems(-1, 0, 1, 2, 3)
                context("no predicates") {
                    test("returns empty list") {
                        assertResultIs()
                    }
                }
                context("everything matches something") {
                    withPredicates(::isNegative, ::isZero, ::isPositive)
                    test("returns a list for each predicate") {
                        assertResultIs(listOf(-1), listOf(0), listOf(1, 2, 3))
                    }
                }
                context("a predicate doesn't match any item") {
                    withPredicates(::isBiggerThan10, ::isNegative, ::isZero, ::isPositive)
                    test("returns an empty list for that predicate") {
                        assertResultIs(emptyList(), listOf(-1), listOf(0), listOf(1, 2, 3))
                    }
                }
                context("an item doesn't match any predicate") {
                    withPredicates(::isNegative, ::isPositive)
                    test("item is not in the returned lists") {
                        assertResultIs(listOf(-1), listOf(1, 2, 3))
                    }
                }
                context("an item matches more than one predicate") {
                    withPredicates(::isBiggerThan10, ::isNegative, ::isZero, ::isPositive, ::isBiggerThan1)
                    test("item is assigned to the first matching predicate") {
                        assertResultIs(emptyList(), listOf(-1), listOf(0), listOf(1, 2, 3), emptyList())
                    }
                }
                context("repeated predicates") {
                    withPredicates(::isNegative, ::isNegative, ::isZero, ::isPositive)
                    test("item is assigned to each predicate") {
                        assertResultIs(listOf(-1), listOf(-1), listOf(0), listOf(1, 2, 3))
                    }
                }
                context("lambda predicates") {
                    withPredicates({ x -> isNegative(x) }, { x -> isPositive(x) })
                    test("item is assigned to each predicate") {
                        assertResultIs(listOf(-1), listOf(1, 2, 3))
                    }
                }
                context("items in a different order") {
                    withItems(3, 2, 1, 0, -1)
                    withPredicates(::isNegative, ::isZero, ::isPositive)
                    test("input order is preserved") {
                        assertResultIs(listOf(-1), listOf(0), listOf(3, 2, 1))
                    }
                }
            }
            context("no items") {
                context("no predicates") {
                    test("returns empty list") {
                        assertResultIs()
                    }
                }
                context("some predicates") {
                    withPredicates(::isNegative, ::isZero, ::isPositive)
                    test("returns an empty list for each predicate") {
                        assertResultIs(emptyList(), emptyList(), emptyList())
                    }
                }
            }
        }

        private fun TestContextBuilder<Fixture, Fixture>.withItems(vararg items: Int) =
            // before_ allows us to replace an existing fixture, and to have multiple calls
            before_ { copy(items = items.asList()) }

        private fun TestContextBuilder<Fixture, Fixture>.withPredicates(vararg predicates: (Int) -> Boolean) =
            // so that we can combine withs in the same context
            before_ { copy(predicates = predicates.asList()) }
    }
    //`
}

/*-
## Conclusions

I won't claim that this is a particularly good specification for our partition - I'm just finding my way with this style
and find it hard to strike a balance between formality and comprehension. But I do know that had I tried to write a spec
before writing any code I would have floundered - this way we TDD'd our way to working code quickly, then produced a
more formal and comprehensive set of tests that capture more of the behaviour, with a good degree of rigour between the
contexts and the fixtures.

If you liked this, please try the [bonus episode](property-based-testing-with-minutest) where I look at Property
Based Testing the same code using Minutest.

[Post updated 2019-02-22]
-*/



