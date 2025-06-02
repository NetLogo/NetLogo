// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang

import org.nlogo.headless.{ LanguageTestTag, test => headlessTest },
  headlessTest.{ Finder => TestFinder, ExtensionTests, ModelTests,
    ReporterTests, CommandTests, AbstractFixture }
import org.scalatest.Tag

import org.nlogo.util.SlowTest

trait Finder extends TestFinder {
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
class TestExtensions extends ExtensionTests with TaggedSlowTest
