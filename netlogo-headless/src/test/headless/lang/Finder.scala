// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang

import org.nlogo.headless.{ LanguageTestTag, test => headlessTest },
  headlessTest.{ Finder => TestFinder, ExtensionTests, ModelTests,
    ReporterTests, CommandTests, AbstractFixture }
import org.nlogo.util.AnyFunSuiteEx
import org.scalatest.Tag

import org.nlogo.util.SlowTest

// You might check out the object heirarchy here and think, "Wait, why not just move `AnyFunSuiteEx` over to
// org.nlogo.headless.test.Finder (aka TestFinder here)?" But the problem is Tortoise needs that other Finder, too, and
// it does not depend on the parserJVM where the `AnyFunSuiteEx` lives.  Simply adding that trait to the JS version
// doesn't help, because Tortoise uses the JVM version of headless for testing.  It's all very confusing, but this
// change is minimal and uses the thread-aware catching code here where it's needed while allowing Tortoise to continue
// running as it always has.  -Jeremy B May 2026
trait Finder extends TestFinder with AnyFunSuiteEx {
  override def withFixture[T](name: String)(body: AbstractFixture => T): T =
    Fixture.withFixture(name) { fixture =>
      body(fixture)
    }
}

trait TaggedLanguageTest extends Finder {
  override def extraTags: Seq[Tag] = Seq(LanguageTestTag)
}

trait TaggedSlowTest extends Finder {
  override def extraTags: Seq[Tag] = Seq(SlowTest.Tag)
}

class TestCommands extends CommandTests with TaggedLanguageTest
class TestReporters extends ReporterTests with TaggedLanguageTest
class TestModels extends ModelTests with TaggedSlowTest
class TestExtensions extends ExtensionTests with TaggedSlowTest {
  override def extraTags: Seq[Tag] =
    super.extraTags :+ SlowTest.ExtensionTag
}
