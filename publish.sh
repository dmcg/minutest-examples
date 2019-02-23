#!/usr/bin/env bash
set -e

/Users/duncan/Documents/Work/book/build/install/book/bin/book src/test/kotlin/tddToSpec/part1 build/part1.md
cp build/part1.md /Users/duncan/Documents/Work/website-jekyll/site/_posts/2019-02-16-test-driven-to-specification-with-minutest-part1.md

/Users/duncan/Documents/Work/book/build/install/book/bin/book src/test/kotlin/tddToSpec/part2 build/part2.md
cp build/part2.md /Users/duncan/Documents/Work/website-jekyll/site/_posts/2019-02-17-test-driven-to-specification-with-minutest-part2.md

/Users/duncan/Documents/Work/book/build/install/book/bin/book src/test/kotlin/tddToSpec/part3 build/part3.md
cp build/part3.md /Users/duncan/Documents/Work/website-jekyll/site/_posts/2019-02-22-property-based-testing-with-minutest.md