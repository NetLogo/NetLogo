// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang

import org.nlogo.headless.{ LanguageTestTag, test => headlessTest },
  headlessTest.{ Finder => TestFinder, ExtensionTests, ModelTests,
    ReporterTests, CommandTests, AbstractFixture }
import org.scalactic.source.Position
import org.scalatest.Tag

import org.nlogo.util.SlowTest

trait Finder extends TestFinder {
  override def withFixture[T](name: String)(body: AbstractFixture => T): T =
    Fixture.withFixture(name) { fixture =>
      body(fixture)
    }
}

trait TaggedLanguageTest extends Finder {
  override def test(name: String, otherTags: Tag*)(testFun: => Any)(implicit pos: Position) =
    super.test(name, (LanguageTestTag +: otherTags)*)(testFun)
}

trait TaggedSlowTest extends Finder {
  override def test(name: String, otherTags: Tag*)(testFun: => Any)(implicit pos: Position) = {
    super.test(name, (SlowTest.Tag +: otherTags)*)(testFun)
  }
}

class TestCommands extends CommandTests with TaggedLanguageTest
class TestReporters extends ReporterTests with TaggedLanguageTest
class TestModels extends ModelTests with TaggedSlowTest
class TestExtensions extends ExtensionTests with TaggedSlowTest
