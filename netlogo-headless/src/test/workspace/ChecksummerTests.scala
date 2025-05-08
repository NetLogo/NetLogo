// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.scalatest.funsuite.AnyFunSuite
import java.io.PrintWriter

// At the moment these tests don't make much sense, but the next step is to make
// the checksummer calculate separate checksums for the separate sections. - ST 7/15/10

class ChecksummerTests extends AnyFunSuite {
  def foo(fn: PrintWriter): Unit = {
    fn.println("FOO")
    fn.println("hi there")
  }
  def bar(fn: PrintWriter): Unit = {
    fn.println("BAR")
    fn.println("how are you")
    fn.println("fine thank you")
  }
  def emptySection(fn: PrintWriter): Unit = {
    fn.println("EMPTY")
  }
  test("one section") {
    def tester(fn: PrintWriter): Unit = {
      foo(fn)
    }
    assertResult("41ECD0D21169ED248C8499E22CF3CF636F5DADC1")(
      Checksummer.calculateChecksum(tester))
  }
  test("two sections") {
    def tester(fn: PrintWriter): Unit = {
      foo(fn)
      fn.println()
      bar(fn)
    }
    assertResult("046F28A95D201F85B8C5664C36FDECF24D23213D")(
      Checksummer.calculateChecksum(tester))
  }
  test("three sections") {
    def tester(fn: PrintWriter): Unit = {
      foo(fn)
      fn.println()
      bar(fn)
      fn.println()
      emptySection(fn)
    }
    assertResult("EA4F5E16376F2FC7242F9BF28328CC00B5A8190D")(
      Checksummer.calculateChecksum(tester))
  }
  test("two empty sections") {
    def tester(fn: PrintWriter): Unit = {
      emptySection(fn)
      fn.println()
      emptySection(fn)
    }
    assertResult("376368FF0F759FD60F473E56EA58D5E7D8305E0F")(
      Checksummer.calculateChecksum(tester))
  }
}
