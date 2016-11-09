// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang

import java.io.File
import org.nlogo.headless.{ LanguageTestTag, test => headlessTest },
  headlessTest.{ Finder => TestFinder, ExtensionTests, ModelTests, ReporterTests, CommandTests,
                 AbstractFixture, TestMode, Reporter, Command, Declaration, Open, LanguageTest}
import org.scalactic.source.Position
import org.scalatest.{ FunSuite, Tag }

import
  org.nlogo.{ api, core, util },
    api.FileIO.file2String,
    core.{ Model, Resource },
    util.SlowTestTag

trait Finder extends TestFinder {
  override def withFixture[T](name: String)(body: AbstractFixture => T): T =
    Fixture.withFixture(name) { fixture =>
      body(fixture)
    }
}

trait TaggedLanguageTest extends Finder {
  override def test(name: String, otherTags: Tag*)(testFun: => Any)(implicit pos: Position) =
    super.test(name, (LanguageTestTag +: otherTags):_*)(testFun)
}

trait TaggedSlowTest extends Finder {
  override def test(name: String, otherTags: Tag*)(testFun: => Any)(implicit pos: Position) = {
    super.test(name, (SlowTestTag +: otherTags):_*)(testFun)
  }
}

class TestCommands extends CommandTests with TaggedLanguageTest
class TestReporters extends ReporterTests with TaggedLanguageTest
class TestModels extends ModelTests with TaggedSlowTest
class TestExtensions extends ExtensionTests with TaggedSlowTest
