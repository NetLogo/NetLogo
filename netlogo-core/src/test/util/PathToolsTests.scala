// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

import java.nio.file.{ Files, Path }

import org.scalatest.{ BeforeAndAfterAll, FunSuite }

class PathToolsTests extends FunSuite with BeforeAndAfterAll {
  var tmpDir: Path = _
  var bar: Path = _
  var foo: Path = _
  var tmp2Dir: Path = _
  var baz: Path = _

  override def beforeAll(): Unit = {
    tmpDir = Files.createTempDirectory("pathToolsTests")
    bar = Files.createTempFile(tmpDir, "bar", ".txt")
    foo = Files.createTempFile(tmpDir, "foo", ".xml")
    tmp2Dir = Files.createTempDirectory(tmpDir, "rec")
    baz = Files.createTempFile(tmp2Dir, "baz", ".txt")
  }

  test("findChildrenRecursive on a regular path returns only that file") {
    assertResult(Seq(foo))(PathTools.findChildrenRecursive(foo))
  }

  test("findChildrenRecursive finds all regular-file children") {
    assertResult(Seq(baz))(PathTools.findChildrenRecursive(tmp2Dir))
    assertResult(Seq(bar, baz, foo))(PathTools.findChildrenRecursive(tmpDir).sortBy(_.getFileName.toString))
  }

  test("findChildrenRecursive accepts a directory filter predicate") {
    assertResult(Seq(bar, foo))(
      PathTools
        .findChildrenRecursive(tmpDir, filterDirectories = (path: Path) => ! path.getFileName.toString.contains("rec"))
        .sortBy(_.getFileName.toString))
  }
}
