// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang

import java.io.File
import org.scalatest.FunSuite
import org.nlogo.util.SlowTest

/// top level entry points

class TestCommands extends Finder {
  override def files =
    TxtsInDir("test/commands")
}
class TestReporters extends Finder {
  override def files =
    TxtsInDir("test/reporters")
}
class TestModels extends Finder {
  override def files =
    TxtsInDir("models/test")
      .filterNot(_.getName.startsWith("checksums"))
}
class TestExtensions extends Finder {
  override def files = new Iterable[File] {
    override def iterator = {
    def filesInDir(parent: File): Iterable[File] =
      parent.listFiles.flatMap{f =>
        if(f.isDirectory) filesInDir(f)
        else List(f)}
    filesInDir(new File("extensions"))
      .filter(_.getName == "tests.txt")
      .iterator
    }
  }
}

/// common infrastructure

trait Finder extends FunSuite with SlowTest with Reader {
  def files: Iterable[File]
}

case class TxtsInDir(dir: String) extends Iterable[File] {
  override def iterator =
    new File(dir).listFiles
      .filter(_.getName.endsWith(".txt"))
      .filterNot(_.getName.containsSlice("SDM"))
      .filterNot(_.getName.containsSlice("HubNet"))
      .iterator
}
